package util;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class ProxyConfigs {
	// proxy credentials - what the client needs to send to log in
	public final String PROXY_LOGIN_PASSWORD, PROXY_LOGIN_USERNAME;
	
	// cryptdb proxy sql-like credentials
	public final String CRYPTDB_PROXY_USER, CRYPTDB_PROXY_PASSWORD;
	
	public final String PROXY_DIR; //used by config to get other paths
	
	// attestation folder, containing trusted hashes and pubkey from tpm
	public final String DATA_DIR, TMP_DIR;
	
	// location of iv files for cbir algorithm
	public final String IV_FILE, IMG_KEY_FILE;
	
	public final String PATH_CRYPTDB;
	
	//dataset dir for db population
	public final String DATASET_DIRECTORY;
	
	// mysql server ip and server's dispatcher (cbir and attestation) ip - may
	// be the same
	public final String CLOUD_MYSQL_IP;
	public final String CLOUD_HOST, LOCAL_NETWORK_HOST;

	public final String UNENC_DB_NAME;
	public final String ENC_DB_NAME;
	public final String MYSQL_PORT;
	public final String PROXY_PORT;

	//from cmd arguments
	public final boolean isEncrypted, isCloud;
	
	public ProxyConfigs(String file, boolean isEncrypted, boolean isCloud) throws ConfigurationException {
		this.isEncrypted = isEncrypted;
		this.isCloud = isCloud;
		
		final Configuration config = new Configurations().properties(file);
		
		PROXY_DIR = config.getString("PROXY_DIR");
		
		PROXY_LOGIN_PASSWORD = config.getString("PROXY_LOGIN_PASSWORD");
		PROXY_LOGIN_USERNAME = config.getString("PROXY_LOGIN_USERNAME");
		
		IV_FILE			= config.getString("IV_FILE");
		IMG_KEY_FILE	= config.getString("IMG_KEY_FILE");
		
		DATA_DIR = config.getString("DATA_DIR");
		TMP_DIR = config.getString("TMP_DIR");
		
		PATH_CRYPTDB = config.getString("PATH_CRYPTDB");
		
		DATASET_DIRECTORY = config.getString("DATASET_DIRECTORY");
		CLOUD_HOST = config.getString("CLOUD_HOST");
		
		CLOUD_MYSQL_IP = config.getString("CLOUD_MYSQL_IP");
		LOCAL_NETWORK_HOST = config.getString("LOCAL_NETWORK_HOST");
		
		UNENC_DB_NAME = config.getString("UNENC_DB_NAME");
		ENC_DB_NAME = config.getString("ENC_DB_NAME");
		MYSQL_PORT = config.getString("MYSQL_PORT");
		PROXY_PORT = config.getString("PROXY_PORT");

		CRYPTDB_PROXY_USER		= config.getString("CRYPTDB_PROXY_USER");
		CRYPTDB_PROXY_PASSWORD	= config.getString("CRYPTDB_PROXY_PASSWORD");
	}
}