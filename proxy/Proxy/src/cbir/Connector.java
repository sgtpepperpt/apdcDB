package cbir;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import mie.utils.CBIRCipherText;

public class Connector {
	
	private static final int DEFAULT_SERVER_PORT = 9978;
	private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
	
	private String server_host;
	private int server_port;
	
	Connector() {
		this.server_host = DEFAULT_SERVER_HOST;
		this.server_port = DEFAULT_SERVER_PORT;
	}
	
	Connector(String host, int port) {
		this.server_host = host;
		this.server_port = port;
	}
	
	/**
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void index() throws UnknownHostException, IOException{
		Socket sock = new Socket(server_host, server_port);
		send(sock, 'i', null);
		sock.close();
	}
	
	/**
	 * Adds an image to the repository. The image will be identified by id and every operation will use this id
	 * to reference a stored image.
	 * @param id an int that will serve as identifier in the server
	 * @param features_size obtained from the m value of the cipher
	 * @param img_cipher_text the cipher text obtained from the cipher
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public void addImg(int id, CBIRCipherText cipher_text) throws UnknownHostException, IOException{
		int cbir_length = cipher_text.getCBIRLength();
		byte[] img_cipher = cipher_text.getCipherText();
		float[][] imgFeatures = cipher_text.getImgFeatures();
		
		int n_features = imgFeatures != null ? imgFeatures.length: 0;
		int features_size = imgFeatures.length > 0 ? 
							imgFeatures[0].length > 0 ? imgFeatures[0].length : 0 
							: 0; ///any array will do, they are all the same size
		
		int keyword_size = 0;
		int n_keywords = 0;
		
		///setup buffer to send
		ByteBuffer buffer = ByteBuffer.allocate(7*4+cbir_length+img_cipher.length);
		///cbir metadata
		buffer.putInt(id);
		buffer.putInt(n_features); ///number of features
		buffer.putInt(features_size);
		buffer.putInt(n_keywords);
		buffer.putInt(keyword_size);
		
		///cipher text lengths
		buffer.putInt(img_cipher.length);
		buffer.putInt(0); ///text cipher length, not being used
		
		///add img cipher text to buffer;
		buffer.put(img_cipher);
		
		///txt cipher text would go here
		
		///add img features to buffer
		for(int i = 0; i < n_features; i++){
			for(int j = 0; j < features_size; j++){
				buffer.putInt((int)imgFeatures[i][j]);
			}
		}
		///txt features would go here
		
		///send data
		Socket sock = new Socket(server_host, server_port);
		send(sock, 'a', buffer.array());
		sock.close();
	}
	
	/**
	 * Searches for an image with a specific set of features. This features can be obtained directly by using a MAC
	 * instance with the same cbir key as used in the cipher to encrypt it, or it can be obtained from the cipher text
	 * produced by the cipher. The cipher text first 4 bytes contain an X length of the features. The next X bytes will
	 * be the features required for this method
	 * @param img_features
	 * @param features_size
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public List<SearchResult> search(CBIRCipherText img_features) throws UnknownHostException, IOException{
		float[][] imgFeatures = img_features.getImgFeatures();
		int n_features = imgFeatures.length;
		int features_size = imgFeatures.length > 0 ? 
				imgFeatures[0].length > 0 ? imgFeatures[0].length : 0 
				: 0;
		///ignore text
		int keyword_size = 0;
		int n_keywords = 0;
		///setup buffer to send
		int id = 0;
		ByteBuffer buffer = ByteBuffer.allocate(5*4+img_features.getCBIRLength());
		buffer.putInt(id);
		buffer.putInt(n_features); ///number of features
		buffer.putInt(features_size);
		buffer.putInt(n_keywords);
		buffer.putInt(keyword_size);
		///add img features to buffer
		for(int i = 0; i < n_features; i++){
			for(int j = 0; j < features_size; j++){
				buffer.putInt((int)imgFeatures[i][j]);
			}
		}
		
		Socket sock = new Socket(server_host, server_port);
		send(sock, 's', buffer.array());
		List<SearchResult> ret = receiveQueryResults(sock);
		sock.close();
		return ret;
	}
	
	@SuppressWarnings("resource")
	public byte[] getImg(int id) throws UnknownHostException, IOException{
		///send request
		Socket sock = new Socket(server_host, server_port);
		byte[] buffer = new byte[5];
		ByteBuffer b = ByteBuffer.wrap(buffer, 1, buffer.length-1);
		buffer[0] = (byte)'g';
		b.putInt(id);
		OutputStream out = sock.getOutputStream();
		out.write(buffer);
		///get first part of the response
		buffer = new byte[16];
		int n = 0;
		InputStream in = sock.getInputStream();
		while(((n += in.read(buffer, n, buffer.length-n)) > 0) && n < buffer.length);
		b = ByteBuffer.wrap(buffer);
		long zipSize = b.getLong();
		long dataSize = b.getLong();
		///receive compressed data
		buffer = new byte[(int)zipSize];
		n = 0;
		while(((n += in.read(buffer, n, buffer.length-n)) > 0) && n < buffer.length);

		///uncompress data
		byte[] data = unzip(buffer, (int)dataSize);
		b = ByteBuffer.wrap(data, 0, 8);
		int img_size = b.getInt();
		///next b.getInt() would get text size
		byte[] ret = new byte[img_size];
		System.arraycopy(data, 8, ret, 0, img_size);
		sock.close();
		return ret;
	}

	@SuppressWarnings("resource")
	private void send(Socket sock, char op, byte[] buffer) throws UnknownHostException, IOException{
		OutputStream out = sock.getOutputStream();
		byte[] b = new byte[1];
		b[0] = (byte)op;
		out.write(b);
		if(buffer != null && buffer.length > 0){
			byte[] toSend = zip(buffer);
			out.write(toSend);
		}
	}
	
	private byte[] zip(byte[] data){
		Deflater compressor = new Deflater();
		compressor.setInput(data);
		compressor.finish();
		byte[] tmp = new byte[500];
		Queue<Byte> buffer = new LinkedList<Byte>();
		int compressed;
		while((compressed = compressor.deflate(tmp)) > 0){
			for(int i = 0; i < compressed; i++){
				buffer.add(tmp[i]);
			}
		}
		byte[] ret = new byte[buffer.size()+16];
		ByteBuffer bb = ByteBuffer.wrap(ret, 0, 16);
		bb.putLong(buffer.size());
		bb.putLong(data.length);
		int done = buffer.size();
		for(int i = 16; i < done+16; i++){
			ret[i] = buffer.remove();
		}
		return ret;
	}
	
	private byte[] unzip(byte[] zip, int dataSize){
		Inflater decompressor = new Inflater();
		decompressor.setInput(zip);
		try {
			byte[] data = new byte[dataSize];
			decompressor.inflate(data);
			decompressor.end();
			return data;
		} catch (DataFormatException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("resource")
	private List<SearchResult> receiveQueryResults(Socket sock){
		List<SearchResult> results = new ArrayList<SearchResult>();
		try {
			byte[] results_size = new byte[4];
			InputStream in = sock.getInputStream();
			int n = 0;
			while(((n += in.read(results_size, n, results_size.length-n)) > 0) && n < results_size.length);
			ByteBuffer buffer = ByteBuffer.wrap(results_size);
			int nResults = buffer.getInt();
			//System.out.println("nResults: "+nResults);
			results = new ArrayList<SearchResult>(nResults);
			byte[] tmp = new byte[12];
			buffer = ByteBuffer.wrap(tmp);
			for(int i = 0; i < nResults; i++){
				n = 0;
				while(((n += in.read(tmp, n, tmp.length-n)) > 0) && n < tmp.length);
				int id = buffer.getInt();
				long tmp_float;
				ByteOrder b;
				if((b = buffer.order()) != ByteOrder.LITTLE_ENDIAN){
					buffer.order(ByteOrder.LITTLE_ENDIAN);
					tmp_float = buffer.getLong();
					buffer.order(b);
				}
				else{
					tmp_float = buffer.getInt();
				}
				double f = Double.longBitsToDouble(tmp_float);
				SearchResult r = new SearchResult(id, f);
				results.add(i, r);
				buffer.rewind();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return results;
	}
	
	public class SearchResult{
		
		private int id;
		private double score;
		
		public SearchResult(int id, double score){
			this.id = id;
			this.score = score;
		}
		
		public int getId(){
			return this.id;
		}
		
		public double getScore(){
			return this.score;
		}
	}
}
