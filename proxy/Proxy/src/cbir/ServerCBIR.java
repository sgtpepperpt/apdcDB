package cbir;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.NoSuchPaddingException;

import cbir.Connector.SearchResult;
import mie.utils.CBIRCipherText;
import mie.utils.UnrecognizedFormatException;
import proxy.Util;

public class ServerCBIR {
	private Connector server;
	private Crypto crypto;

	public ServerCBIR(String address, int port) throws NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		this.server = new Connector(address, port);
		this.crypto = new Crypto();
	}
	
	public void uploadOne(int id) throws UnknownHostException, IOException, UnrecognizedFormatException {
		byte[] img = Util.readImg(id);
		byte[] cimg = crypto.encryptImg(img);
		CBIRCipherText c = new CBIRCipherText(cimg);
		server.addImg(id, c);
	}
	
	public void indexAll() throws UnknownHostException, IOException, UnrecognizedFormatException {
		for(int id = 0; id < 1000; id++){
			byte[] img = Util.readImg(id);
			byte[] cimg = crypto.encryptImg(img);
			CBIRCipherText c = new CBIRCipherText(cimg);
			server.addImg(id, c);
		}

		server.index();
	}
	
	public void indexRepository() throws UnknownHostException, IOException {
		server.index();
	}

	public List<String[]> search(int k) throws IOException {
		System.out.println("searching for: "+k);

		try{
			byte[] img = Util.readImg(k);
			byte[] simg = crypto.getCBIRImgFeatures(img);
			CBIRCipherText c = new CBIRCipherText(simg);

			List<SearchResult> res = server.search(c);
			List<String[]> ret = new ArrayList<>(res.size());

			for (SearchResult i : res) {
				String id		= Integer.toString(i.getId());
				String score	= String.format("%.6f", i.getScore());
			//	System.out.println(id + " " + score);
				
				byte[] image	= crypto.decryptImg(server.getImg(i.getId()));
				ret.add(new String[]{id, score, Util.toBase64(image)});
			}

			return ret;
		}catch(UnrecognizedFormatException e){

		}
		return null;
	}
	
	public String getImage(int id) throws IOException{
		return Util.toBase64(crypto.decryptImg(server.getImg(id)));
	}
}