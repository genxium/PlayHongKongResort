package dao;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import play.Play;
import utilities.Loggy;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

public class SQLHelper {

    public static final String TAG = SQLHelper.class.getName();

    protected static DataSource s_dataSource = null;

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

    private static String s_databaseName = null;
    private static String s_host = null;
    private static Integer s_port = null;
    private static String s_user = null;
    private static String s_password = null;
    private static String s_charsetResult = null;
    private static String s_charsetEncoding = null;
    private static String s_useUnicode = null;

    public static boolean readMySQLConfig() {
	    boolean ret = false;
	    try {
		    String fullPath = Play.application().path() + "/conf/";
			if (Play.application().isProd()) fullPath += "database_config.xml";
			else fullPath += "devel_database_config.xml";
		    Map<String, String> attributes = XMLHelper.readDatabaseConfig(fullPath);
		    s_databaseName = attributes.get(DATABASE_NAME);
		    s_host = attributes.get(HOST);
		    s_port = Integer.parseInt(attributes.get(PORT));
		    s_user = attributes.get(USER);
		    s_password = attributes.get(PASSWORD);
		    s_charsetResult = attributes.get(CHARSET_RESULT);
		    s_charsetEncoding = attributes.get(CHARSET_ENCODING);
		    s_useUnicode = attributes.get(USE_UNICODE);
		    ret = true;
	    } catch (Exception e) {
		    Loggy.e(TAG, "readMySQLConfig", e);
	    }
	    return ret;
    }

    public static String getConnectionURI() {
	    String ret = null;
	    try {
		    if(!readMySQLConfig()) return null;
		    Class.forName("com.mysql.jdbc.Driver");
            ret = "jdbc:mysql://" + s_host + ":" + s_port.toString() + "/" + s_databaseName;
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
            prop.setProperty("user", s_user);
            prop.setProperty("password", s_password);

            if (s_useUnicode != null) prop.setProperty("useUnicode", s_useUnicode);
            if (s_charsetEncoding != null) prop.setProperty("characterEncoding", s_charsetEncoding);
            if (s_charsetResult != null) prop.setProperty("characterSetResults", s_charsetResult);

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
            if (s_dataSource == null) {
                String connectURI = getConnectionURI();
                s_dataSource = setupDataSource(connectURI);
            }
            return s_dataSource.getConnection();
        } catch (Exception e) {
            Loggy.e(TAG, "getConnection", e);
        }
        return null;
    }

    public static void closeConnection(Connection connection) {
        try {
            if (connection == null)	return;
	        connection.close();
        } catch (Exception e) {
            Loggy.e(TAG, "closeConnection", e);
        }
    }

    public static String convertOrientation(int order) {
        if (order == (+1)) return ASCEND;
        else if (order == (-1)) return DESCEND;
        else return null;
    }
}
