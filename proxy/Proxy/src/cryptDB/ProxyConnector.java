package cryptDB;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import util.ProxyConfigs;

public class ProxyConnector implements Proxy {
	ProxyConfigs config;
	private Connection conn;
	@SuppressWarnings("unused")
	private Shell s;
	private final String mysqlIp, port, dbName;

	public ProxyConnector(ProxyConfigs config, String localIP)
			throws IOException {
		this.config = config;
		this.s = null;

		if (config.isEncrypted) {
			if (config.isCloud) // a proxy is needed anyway
				s = new Shell(config.LOCAL_NETWORK_HOST, config.CLOUD_MYSQL_IP, config);
			else
				s = new Shell(config.LOCAL_NETWORK_HOST, localIP, config);

			this.mysqlIp = config.LOCAL_NETWORK_HOST;// localIP;
			this.port = config.PROXY_PORT;
			this.dbName = config.ENC_DB_NAME;
		}

		else {
			// no proxy needed, but may need to connect to cloud MySQL
			if (config.isCloud)
				this.mysqlIp = config.CLOUD_MYSQL_IP;
			else
				this.mysqlIp = config.LOCAL_NETWORK_HOST;// localIP;

			this.port = config.MYSQL_PORT;
			this.dbName = config.UNENC_DB_NAME;
		}

		try {
			Thread.sleep(500L);
		} catch (InterruptedException e) {
		}

		openConnection();
	}

	@Override
	public boolean openConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s", mysqlIp, port,
					dbName, config.CRYPTDB_PROXY_USER, config.CRYPTDB_PROXY_PASSWORD));
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public void closeConnection() {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject queryCryptDBProxy(String statement) {
		try {
			if (conn.isClosed())
				openConnection();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		int i = 0;
		while (i < 2) {
			try {
				if (statement.startsWith("select") || statement.startsWith("show") || statement.startsWith("SELECT"))
					return selectQuery(statement);
				else
					return executeQuery(statement);
			} catch (Exception e) {
				System.out.println("reopening");
				openConnection();
				i++;
			}
		}

		JSONObject fail = new JSONObject();
		fail.put("success", false);
		return fail;
	}

	@Override
	public boolean isEncrypted() {
		return config.isEncrypted;
	}

	@Override
	public boolean isCloud() {
		return config.isCloud;
	}

	@SuppressWarnings("unchecked")
	private JSONObject selectQuery(String statement) {
		// create json to return
		JSONObject json = new JSONObject();
		json.put("query", statement);

		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(statement);

			ResultSetMetaData rsmd = rs.getMetaData();
			int columns = rsmd.getColumnCount();

			// add table column names to json
			JSONArray cols = new JSONArray();
			for (int i = 1; i <= columns; i++)
				cols.add(rsmd.getColumnLabel(i));

			json.put("columns", cols);

			// add each row to the json object
			int rowcount = 0;
			while (rs.next()) {
				JSONArray row = new JSONArray();
				for (int j = 1; j <= columns; j++)
					row.add(rs.getString(j));

				json.put("row " + rs.getRow(), row);
				rowcount++;
			}

			// add row count to json
			json.put("rowcount", rowcount);

			rs.close();
			st.close();
			json.put("success", true);
		} catch (SQLException e) {
			e.printStackTrace();
			json.put("success", false);
		}

		return json;
	}

	@SuppressWarnings("unchecked")
	private JSONObject executeQuery(String statement) {
		JSONObject json = new JSONObject();

		try {
			Statement st = conn.createStatement();
			int rows = st.executeUpdate(statement);

			json.put("rowcount", rows);
			json.put("success", true);
		} catch (SQLException e) {
			e.printStackTrace();
			json.put("success", false);
		}

		return json;
	}
}