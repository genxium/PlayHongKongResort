package dao;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import play.Play;
import utilities.Loggy;
import utilities.XMLHelper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Savepoint;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class SQLHelper {

	public static final String TAG = SQLHelper.class.getName();

	private static DataSource dataSource = null;

	public static String DATABASE_NAME = "DatabaseName";
	public static String HOST = "Host";
	public static String PORT = "Port";
	public static String USER = "User";
	public static String PASSWORD = "Password";
	public static String CHARSET_RESULT = "CharsetResult";
	public static String CHARSET_ENCODING = "CharsetEncoding";
	public static String USE_UNICODE = "UseUnicode";

	public static long INVALID = (-1);
	public static String AND = "AND";
	public static String OR = "OR";

	public static String ASCEND = "ASC";
	public static String DESCEND = "DESC";

	private static String databaseName = null;
	private static String host = null;
	private static Integer port = null;
	private static String user = null;
	private static String password = null;
	private static String charsetResult = null;
	private static String charsetEncoding = null;
	private static String useUnicode = null;

	private static boolean readMySQLConfig() {
		boolean ret = false;
		try {
			String fullPath = Play.application().path() + "/conf/";
			if (Play.application().isProd()) fullPath += "database_config.xml";
			else if (Play.application().isDev()) fullPath += "devel_database_config.xml";
			else fullPath += "test_database_config.xml";
			Map<String, String> attributes = XMLHelper.readDatabaseConfig(fullPath);
			databaseName = attributes.get(DATABASE_NAME);
			host = attributes.get(HOST);
			port = Integer.parseInt(attributes.get(PORT));
			user = attributes.get(USER);
			password = attributes.get(PASSWORD);
			charsetResult = attributes.get(CHARSET_RESULT);
			charsetEncoding = attributes.get(CHARSET_ENCODING);
			useUnicode = attributes.get(USE_UNICODE);
			ret = true;
		} catch (Exception e) {
			Loggy.e(TAG, "readMySQLConfig", e);
		}
		return ret;
	}

	private static String getConnectionURI() {
		String ret = null;
		try {
			if (!readMySQLConfig()) return null;
			Class.forName("com.mysql.jdbc.Driver");
			ret = "jdbc:mysql://" + host + ":" + port.toString() + "/" + databaseName;
		} catch (Exception e) {
			Loggy.e(TAG, "getConnectionURI", e);
		}
		return ret;
	}

	public static DataSource setupDataSource(String connectURI) {
		try {
			/**
			 * MySQL server connection options reference: http://pages.citebite.com/p4x3a0r8pmhm
			 * */
			Properties prop = new Properties();
			prop.setProperty("user", user);
			prop.setProperty("password", password);

			if (useUnicode != null) prop.setProperty("useUnicode", useUnicode);
			if (charsetEncoding != null) prop.setProperty("characterEncoding", charsetEncoding);
			if (charsetResult != null) prop.setProperty("characterSetResults", charsetResult);

			/**
			 * avoid auto-disconnection from MySQL server after 8 hours' idle time
			 * TODO: reconnection properties set for connection factory don't seem working!
			 * */
			prop.setProperty("autoReconnect", "true");
			prop.setProperty("autoReconnectForPools", "true");

			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, prop);
			PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);

			/**
			 * TODO: optimize these numbers!
			 * */
			GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
			poolConfig.setMaxIdle(10);
			poolConfig.setMinIdle(5);
			poolConfig.setTimeBetweenEvictionRunsMillis(60000); // validate every minute
			poolConfig.setNumTestsPerEvictionRun(1);
			poolConfig.setTestWhileIdle(true);
			poolConfig.setTestOnBorrow(false);
			poolConfig.setTestOnReturn(false);

			ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory, poolConfig);
			poolableConnectionFactory.setPool(connectionPool);
			return new PoolingDataSource<>(connectionPool);
		} catch (Exception e) {
			Loggy.e(TAG, "setupDataSource", e);
		}
		return null;
	}

	public static Connection getConnection() {
		try {
			if (dataSource == null) {
				String connectURI = getConnectionURI();
				dataSource = setupDataSource(connectURI);
			}
			if (dataSource == null) return null;
			else return dataSource.getConnection();
		} catch (Exception e) {
			Loggy.e(TAG, "getConnection", e);
		}
		return null;
	}

	public static boolean closeConnection(final Connection connection) {
		try {
			if (connection == null) return false;
			connection.close();
			return true;
		} catch (Exception e) {
			Loggy.e(TAG, "closeConnection", e);
			return false;
		}
	}

	public static boolean disableAutoCommit(final Connection connection) {
		return setAutoCommit(connection, false);
	}

	public static boolean enableAutoCommit(final Connection connection) {
		return setAutoCommit(connection, true);
	}

	public static boolean enableAutoCommitAndClose(final Connection connection) {
		return setAutoCommit(connection, true) && closeConnection(connection);
	}

	protected static boolean setAutoCommit(final Connection connection, final boolean val) {
		if (connection == null) return false;
		try {
			connection.setAutoCommit(val);
			return true;
		} catch (SQLException e) {
			Loggy.e(TAG, "setAutoCommit", e);
			return false;
		}
	}

	public static boolean executeAndCloseStatement(final PreparedStatement stat) throws SQLException {
		if (stat == null) return false;
		boolean res = stat.execute();
		stat.close();
		return res;
	}

	public static Long executeInsertAndCloseStatement(final PreparedStatement stat) throws SQLException {
		if (stat == null) return null;
		Long ret = null;
		stat.executeUpdate();
		ResultSet rs = stat.getGeneratedKeys();
		if (rs != null && rs.next()) {
			ret = rs.getLong(1);
			rs.close();
		}
		stat.close();
		return ret;
	}
		
	public static Savepoint setSavepoint(final Connection connection) throws SQLException {
		// reference http://docs.oracle.com/javase/7/docs/api/java/sql/Connection.html#setSavepoint()
		if (connection == null) return null;
		return connection.setSavepoint();
	}

	public static boolean commit(final Connection connection) throws SQLException {
		// reference http://docs.oracle.com/javase/7/docs/api/java/sql/Connection.html#commit()
		if (connection == null) return false;
		connection.commit();
		return true;
	}

	public static boolean rollback(final Connection connection) throws SQLException {
		if (connection == null) return false;
		connection.rollback();
		return true;
	}

	public static boolean rollback(final Connection connection, final Savepoint sp) throws SQLException {
		// reference http://docs.oracle.com/javase/7/docs/api/java/sql/Connection.html#rollback(java.sql.Savepoint)
		if (connection == null) return false;
		connection.rollback(sp);
		return true;
	}

	public static String convertOrientation(int order) {
		if (order == (+1)) return ASCEND;
		else if (order == (-1)) return DESCEND;
		else return null;
	}
}
