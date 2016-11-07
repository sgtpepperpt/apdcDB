package proxy;

import java.io.PrintStream;
import java.security.Security;
import java.util.Random;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import attestation.AttestationModule;
import cbir.ServerCBIR;
import util.ProxyConfigs;

public class Main {
	static final int DEFAULT_PROXY_DISPATCHER_PORT = 5482;
	static final int DEFAULT_SERVER_ATTESTATION_PORT = 7868;
	static final int DEFAULT_SERVER_CBIR_PORT = 9978;

	public static void main(String[] args) throws Exception {
		if (args.length > 2) {
			System.out.println("usage: ProxyConnector [-u] [-l]");
			System.out.println("Test modes:");
			System.out.println("\t--attest <server-host>");
			System.out.println("\t--cbir <server-host> NOT FULLY IMPLEMENTED");
			System.exit(1);
		}

		Security.addProvider(new BouncyCastleProvider());

		boolean isEncrypted = true;
		boolean isCloud = true;

		boolean attestMode = false;
		boolean cbirMode = false;

		String tmpHost = null;

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-u":
				isEncrypted = false;
				break;
			case "-l":
				isCloud = false;
				break;
			case "--attest":
				attestMode = true;
				tmpHost = args[i + 1];
				break;
			case "--cbirtests":
				cbirMode = true;
				break;
			}
		}

		ProxyConfigs config = new ProxyConfigs("config.properties", isEncrypted, isCloud);

		if (attestMode) {
			AttestationModule atm = new AttestationModule(config, tmpHost, DEFAULT_SERVER_ATTESTATION_PORT);
			if (atm.verifyServers())
				System.out.println("Server is trustable!");
			else
				System.err.println("Server may have been compromised!");

			System.exit(0);
		}

		if (cbirMode) {
			cbirTest(config);
			System.exit(0);
		}

		Dispatcher d = new Dispatcher(config);
		d.listenForRequests();
	}

	private static void cbirTest(ProxyConfigs config) throws Exception {
		PrintStream ps = new PrintStream("test");
		for (int k = 0; k < 10; k++) {
			ps.println("TEST " + k);
			int[] vals = { 1, 5, 10, 50, 100, 500, 1000 };
			for (int v : vals) {
				System.out.println("test: " + k + "; repository size: " + v);
				ServerCBIR s = new ServerCBIR(config, "localhost", DEFAULT_SERVER_CBIR_PORT);
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
		ps.close();
	}
}