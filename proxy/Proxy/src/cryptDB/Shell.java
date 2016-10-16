package cryptDB;
import java.io.IOException;

public class Shell {
	private Process p;
	
	public Shell(String ipProxy, String ipMySQL) throws IOException {
		String[] args = new String[]{"xterm", "-e", "/home/pepper/apdc/proxy.sh " + ipProxy + " " + ipMySQL};
		p = Runtime.getRuntime().exec(args);
	}

	public void kill(){
		p.destroy();
	}
}