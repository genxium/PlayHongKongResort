package dao;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.json.simple.JSONObject;
import play.Play;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import utilities.DataUtils;
import utilities.Loggy;

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

    public static int INVALID = (-1);
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
		    String fullPath = Play.application().path() + "/conf/" + "database_config.xml";
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
		    StringBuilder builder = new StringBuilder();
		    builder.append("jdbc:mysql://");
		    builder.append(s_host + ":");
		    builder.append(s_port.toString() + "/");
		    builder.append(s_databaseName);
		    if (s_charsetResult != null) builder.append("?" + s_charsetResult);
		    if (s_charsetEncoding != null) builder.append("&" + s_charsetEncoding);
		    if (s_useUnicode != null) builder.append("&" + s_useUnicode);
		    ret = builder.toString();
	    } catch (Exception e) {
		    Loggy.e(TAG, "getConnectionURI", e);
	    }
	    return ret;

    }

    public static DataSource setupDataSource(String connectURI) {
        PoolingDataSource<PoolableConnection> ret = null;
        try {
            ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, s_user, s_password);
            PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
            ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<PoolableConnection>(poolableConnectionFactory);
            poolableConnectionFactory.setPool(connectionPool);
            ret = new PoolingDataSource<PoolableConnection>(connectionPool);
        } catch (Exception e) {
            Loggy.e(TAG, "setupDataSource", e);
            ret = null;
        }
        return ret;
    }

    public static Connection getConnection() {
        Connection connection = null;
        try {
            if (s_dataSource == null) {
                String connectURI = getConnectionURI();
                s_dataSource = setupDataSource(connectURI);
            }
            if (s_dataSource == null) System.out.println("Data source is null");
            connection = s_dataSource.getConnection();
        } catch (Exception e) {
            Loggy.e(TAG, "getConnection", e);
            connection = null;
        }
        return connection;
    }

    public static void closeConnection(Connection connection) {
        try {
            if (connection == null)	return;
	        connection.close();
        } catch (Exception e) {
            Loggy.e(TAG, "closeConnection", e);
        }
    }

    public static List<JSONObject> select(PreparedStatement statement) {
        List<JSONObject> ret = null;
        try {
            ResultSet rs = statement.executeQuery();
            if (rs != null) {
                ret = ResultSetUtil.convertToJSON(rs);
                rs.close();
            }
            Connection connection = statement.getConnection();
            statement.close();
            closeConnection(connection);
        } catch (Exception e) {
            Loggy.e(TAG, "select", e);
        }
        return ret;
    }

    public static Integer insert(PreparedStatement statement) {
        Integer lastId = INVALID;
        try {
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs != null && rs.next()) {
                lastId = (int) rs.getLong(1);
                rs.close();
            }
            Connection connection = statement.getConnection();
            statement.close();
            closeConnection(connection);
        } catch (Exception e) {
            // return the INVALID value for exceptions
            Loggy.e(TAG, "insert", e);
        }
        return lastId;
    }

    public static boolean update(PreparedStatement statement) {
        boolean bRet = false;
        try {
            statement.executeUpdate();
            Connection connection = statement.getConnection();
            statement.close();
            closeConnection(connection);
            bRet = true;
        } catch (Exception e) {
            Loggy.e(TAG, "update", e);
        }
        return bRet;
    }

    public static boolean delete(PreparedStatement statement) {
        boolean bRet = false;
        try {
            statement.executeUpdate();
            Connection connection = statement.getConnection();
            statement.close();
            closeConnection(connection);
            bRet = true;
        } catch (Exception e) {
            Loggy.e(TAG, "delete", e);
        }
        return bRet;
    }

    public static String convertOrientation(int order) {
        if (order == (+1)) return ASCEND;
        else if (order == (-1)) return DESCEND;
        else return null;
    }
}
