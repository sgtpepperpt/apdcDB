package cryptDB;

import java.io.IOException;

import util.ProxyConfigs;

public class Shell {
	private Process p;

	public Shell(String ipProxy, String ipMySQL, ProxyConfigs config) throws IOException {
		String[] args = new String[] { "xterm", "-e", config.PROXY_DIR + "cryptdb-proxy.sh " + ipProxy + " " + ipMySQL
				+ " " + config.PATH_CRYPTDB + " " + config.CRYPTDB_PROXY_PASSWORD };
		p = Runtime.getRuntime().exec(args);
	}

	public void kill() {
		p.destroy();
	}
}