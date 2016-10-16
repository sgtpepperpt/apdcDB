package cryptDB;
import org.json.simple.JSONObject;

public interface Proxy {
	boolean openConnection();
	void closeConnection();
	
	boolean isEncrypted();
	boolean isCloud();
	
	JSONObject queryCryptDBProxy(String statement);
}