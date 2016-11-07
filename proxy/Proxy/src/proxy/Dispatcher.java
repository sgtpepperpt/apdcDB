package proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import attestation.AttestationModule;
import attestation.TpmAttestationException;
import cbir.ServerCBIR;
import cryptDB.Proxy;
import cryptDB.ProxyConnector;
import util.ProxyConfigs;

public class Dispatcher {
	private ProxyConfigs config;
	private Proxy proxy;
	private ServerCBIR cbir;
	private AttestationModule tpm;

	//private List<Session> connected;

	public Dispatcher(ProxyConfigs config) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {
		this.config = config;
		
		String host;
		if(config.isCloud)
			host = config.CLOUD_HOST;
		else
			host = config.LOCAL_NETWORK_HOST;//localhostAddress();

		proxy		= new ProxyConnector(config, host);

		//ServerDataLoader.initializeCryptDB(proxy); //must be done at first run

		cbir		= new ServerCBIR(config, host, Main.DEFAULT_SERVER_CBIR_PORT);
		tpm			= new AttestationModule(config, host, Main.DEFAULT_SERVER_ATTESTATION_PORT);

		//connected	= new ArrayList<Session>();
	}

	public void listenForRequests() throws IOException {
		try(ServerSocket sock = new ServerSocket(Main.DEFAULT_PROXY_DISPATCHER_PORT)){
			System.out.println("Waiting for requests...");

			while(true){
				try{
					Socket s = sock.accept();
					s.setSoTimeout(60000);

					InputStream is	= s.getInputStream();
					OutputStream os	= s.getOutputStream();

					//first get the size of the message to come
					byte [] bSize = new byte[10];
					is.read(bSize);				
					int size = Integer.valueOf(new String(bSize));

					//start getting the message, with specified "size"
					int read = 0;
					String msg = "";

					byte [] buffer; //tmp buffer, max size is total size
					while(read < size){
						buffer = new byte[size-read];

						read += is.read(buffer);
						msg += new String(buffer).trim();
					}

					String query = msg.trim();
					System.out.println("Received query \"" + query.substring(0, Math.min(query.length(), 100)) + "\"");
					System.out.println(query.length() + " " + size);

					String response = processQuery(proxy, query, s.getLocalAddress().toString().substring(1));

					// write response to client
					os.write(String.format("%010d", response.length()).getBytes());
					os.write(response.getBytes());
					System.out.println("answered " + String.format("%010d", response.length()));
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private String processQuery(Proxy proxy, String query, String clientIP) {
		//verify if user is logged in
		if(!isConnected(clientIP) && !query.startsWith("META LOGIN")){
			System.out.println("Client not logged in yet.");

			JSONObject json = new JSONObject();
			json.put("success", false);
			return json.toJSONString();
		}

		if(query.startsWith("META"))
			return handleMetaQuery(proxy, query, clientIP).toJSONString();
		else{
			Pattern imgQuery = Pattern.compile("SELECT \\* FROM images WHERE tag='([a-zA-Z\\d]*)' OR IMAGE ~<(.*)>");
			Matcher m = imgQuery.matcher(query);

			if (m.matches())
				return handleImageQuery(proxy, m.group(1), m.group(2));
			else
				return proxy.queryCryptDBProxy(query).toJSONString();
		}
	}

	@SuppressWarnings("unchecked")
	private JSONObject handleMetaQuery(Proxy proxy, String query, String clientIP) {
		JSONObject json = new JSONObject();
		json.put("success", true);

		String[] args = query.split("\\s+");
		switch(args[1]){
		case "STATUS":
			json.put("isEncrypted", proxy.isEncrypted());
			json.put("isCloud", proxy.isCloud());
			break;
		case "LOGIN":
			String[] data = query.split("\\s+");

			if(data.length != 4 || !data[2].equals(config.PROXY_LOGIN_USERNAME) || !data[3].equals(config.PROXY_LOGIN_PASSWORD))
				json.put("success", false);
			else {
				json.put("success", checkServers());
			}

			//connected.add(new Session(clientIP));

			break;
		}
		return json;
	}

	@SuppressWarnings("unchecked")
	private String handleImageQuery(Proxy proxy, String tag, String image) {
		//process image data and hash
		byte[] img = DatatypeConverter.parseBase64Binary(image);
		byte[] hash = ImageEncryptor.generateHash(img);
		System.out.printf("Receiving image %s (size %d)...\n", tag, img.length);

		JSONObject json = new JSONObject();

		try {
			//get image's id from database
			JSONObject idJSON = proxy.queryCryptDBProxy("SELECT id FROM images WHERE hash='" + DatatypeConverter.printHexBinary(hash) + "'");
			String id = (String) ((JSONArray) idJSON.get("row 1")).get(0); //we only expect 1 result from db

			//cbir return list of images, with id, score and img bytes
			List<String[]> images = cbir.search(Integer.parseInt(id));

			int imgsFromCBIR = Math.min(images.size(), 4);

			//put all data on json to return
			for(int i = 0; i < imgsFromCBIR; i++){
				String [] current = images.get(i);
				json.put("row_id_" + i, current[0]);
				json.put("row_score_" + i, current[1]);
				json.put("row_image_" + i, current[2]);
			}

			//now get images with tag
			/*	JSONObject tags = proxy.queryCryptDBProxy("SELECT id FROM images WHERE tag='" + tag+"'");
			int count = 0;
			int imgsFromTag = (int) tags.get("rowcount");
			for(int i = imgsFromCBIR; i <= imgsFromTag + imgsFromCBIR; i++){
				int currentRow = (int) tags.get("row " + count++);

				json.put("row_id_" + i, currentRow);
				json.put("row_score_" + i, -1);
				json.put("row_image_" + i, cbir.getImage(currentRow));
			}
			 */
			json.put("rowcount", imgsFromCBIR/* + imgsFromTag*/);
			json.put("success", true);
		} catch (Exception e) {
			json.put("success", false);
			e.printStackTrace();
		}

		return json.toJSONString();
	}

	private boolean isConnected(String clientIP) {
		/*for(Session s : connected)
			if(s.equals(clientIP))
				return true;

		return false;*/
		return true;
	}

	private boolean checkServers() {
		System.out.println("Checking server integrity...");

		try {
			return tpm.verifyServers();
		} catch(TpmAttestationException e){
			System.err.println("Server is not trustable - exiting...");
			return false;
		}
	}
}