package proxy;

import java.io.PrintStream;
import java.security.Security;
import java.util.Random;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import attestation.AttestationModule;
import cbir.ServerCBIR;

public class Main {
	static final int DEFAULT_PROXY_DISPATCHER_PORT = 5482;
	static final int DEFAULT_SERVER_ATTESTATION_PORT = 7868;
	public static final int DEFAULT_SERVER_CBIR_PORT = 9978;

	// proxy credentials - what the client needs to send to log in
	public static final String PROXY_LOGIN_PASSWORD = "apdc_2016";
	public static final String PROXY_LOGIN_USERNAME = "guilherme";

	// cryptdb proxy sql-like credentials
	public static final String CRYPTDB_PROXY_USER = "root";
	public static final String CRYPTDB_PROXY_PASSWORD = "FQ971bXn";

	public static final String LOCAL_NETWORK_HOST = "127.0.0.1";

	// mysql server ip and server's dispatcher (cbir and attestation) ip - may
	// be the same
	public static final String CLOUD_MYSQL_IP = "104.155.123.25";
	public static final String CLOUD_HOST = "104.155.125.250";

	public static final String PROXY_DIR = "/home/pepper/apdc/proxy/";
	static final String DATASET_DIRECTORY = "/home/pepper/apdc/datasets/";

	// attestation folder, containing trusted hashes and pubkey from tpm
	public static final String DATA_DIR = PROXY_DIR + "attestation_tpm/data/";
	public static final String TMP_DIR = DATA_DIR + "tmp/";

	// location of iv files for cbir algorithm
	public static final String IMG_KEY_FILE = PROXY_DIR + "cbir/cbird.key";
	public static final String IV_FILE = PROXY_DIR + "cbir/iv";

	public static final String PROXY_PORT = "3307";
	public static final String MYSQL_PORT = "3306";
	public static final String ENC_DB_NAME = "music_enc";
	public static final String UNENC_DB_NAME = "music_unenc";

	public static void main(String[] args) throws Exception {
		if (args.length > 2) {
			System.out.println("usage: ProxyConnector [-u] [-l]");
			System.exit(0);
		}

		boolean isEncrypted = true;
		boolean isCloud = true;

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-u":
				isEncrypted = false;
				break;
			case "-l":
				isCloud = false;
				break;
			}
		}

		Security.addProvider(new BouncyCastleProvider());
		AttestationModule atm = new AttestationModule("localhost", 7868);
		atm.verifyServers();
		/*
		 * Dispatcher d = new Dispatcher(isEncrypted, isCloud);
		 * d.listenForRequests();
		 */
/*
		PrintStream ps = new PrintStream("test");
		for (int k = 0; k < 10; k++) {
			ps.println("TEST " + k);
			int[] vals = { 1, 5, 10, 50, 100, 500, 1000 };
			for (int v : vals) {
				System.out.println("test: " + k + "; repository size: " + v);
				ServerCBIR s = new ServerCBIR("172.17.0.1", DEFAULT_SERVER_CBIR_PORT);
				long start = System.currentTimeMillis();
				for (int i = 0; i < v; i++)
					s.uploadOne(i);
				long total = System.currentTimeMillis() - start;
				ps.printf("Uploading; repository size %d; took %dms.\n", v, total);
				
				start = System.currentTimeMillis();
				s.indexRepository();
				total = System.currentTimeMillis() - start;
				ps.printf("Indexing; repository size %d; took %dms.\n", v, total);
				
				int imgSearch = new Random().nextInt(v);
				start = System.currentTimeMillis();
				s.search(imgSearch);
				total = System.currentTimeMillis() - start;
				ps.printf("Search for img %d; repository size %d; took %dms.\n", imgSearch, v, total);
			}
			ps.println("-------------------------------------------------");
		}
		ps.close();*/
	}
}