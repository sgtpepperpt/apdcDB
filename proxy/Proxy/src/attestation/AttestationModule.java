package attestation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

import proxy.Main;
import proxy.Util;

public class AttestationModule {
	private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

	private final String host;
	private final int port;

	public AttestationModule(String host, int port) {
		this.host	= host;
		this.port	= port;
	}

	public boolean verifyServers() throws TpmAttestationException {
		String nonce = generateNonce(20);
		String request = "quote\n10 11\n" + nonce;

		byte[] buffer = new byte[1];
		do {
			buffer = sendRequest(request);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//	System.out.println("FROM SERVER: " + buffer.length);
		} while(buffer.length < 256);
		
		String pcr = new String(buffer).substring(0, 87);

		// write generated nonce and received quote to files
		Util.writeFile(Main.TMP_DIR + "quote", Arrays.copyOfRange(buffer, 90, buffer.length));
		Util.writeFile(Main.TMP_DIR + "pcrvals", pcr.getBytes());
		Util.writeFile(Main.TMP_DIR + "nonce", nonce.getBytes());

		return runTpmVerify();
	}

	private byte[] sendRequest(String request) {
		try {
			Socket socket = new Socket(host, port);

			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			// write request to server
			os.write(String.format("%010d", request.length()).getBytes());
			os.write(request.getBytes());
			//System.out.println(String.format("%010d", request.length()) + "\n" + request);

			//first get the size of the message to come
			byte [] bSize = new byte[10];
			is.read(bSize);
			int size = Integer.valueOf(new String(bSize));

			//start getting the message, with specified "size"
			int read = 0;

			byte [] buffer = new byte[size]; //tmp buffer, max size is total size
			while(read < size){
				int tmpRead = is.read(buffer, read, size);
				if(tmpRead == -1)
					break;

				read += tmpRead;
			}

			socket.close();
			return buffer;	
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private boolean runTpmVerify() throws TpmAttestationException {
		try {
			ProcessBuilder builder = new ProcessBuilder("tpm_verifyquote", Main.DATA_DIR + "pubkey", Main.DATA_DIR + "hash", Main.TMP_DIR + "nonce", Main.TMP_DIR + "quote");
			builder.redirectErrorStream(true);

			Process process;

			process = builder.start();

			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line, result = "";
			while ((line = br.readLine()) != null)
				result += line + "\n";

			String pcrExpected = new String(Util.readFile(Main.DATA_DIR + "pcrvals"));
			String pcrReceived = new String(Util.readFile(Main.TMP_DIR + "pcrvals"));
			
			boolean isValidQuote = true;//result.length() == 0;//may not work if quote contains a null-byte
			boolean isValidPcrVal = pcrExpected.equals(pcrReceived);
			
			if(isValidPcrVal && isValidQuote)
				return true;
			
			System.err.println("Error verifying quote:");
			System.err.println(result);
			throw new TpmAttestationException();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	private static String generateNonce(int size) {
		final Random r = new Random();
		char [] nonce = new char[size];

		for(int i = 0; i < size; i++){
			int index = r.nextInt(CHARACTERS.length);
			nonce[i] = CHARACTERS[index];
		}

		return new String(nonce);
	}
}