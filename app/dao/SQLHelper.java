package dao;
import org.json.simple.JSONObject;
import play.Play;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SQLHelper {

	protected static DataSource s_dataSource=null;
		
	public static String DATABASE_NAME ="DatabaseName";
	public static String HOST ="Host";
	public static String PORT ="Port";
	public static String USER ="User";
	public static String PASSWORD ="Password";
	public static String CHARSET_RESULT ="CharsetResult";
	public static String CHARSET_ENCODING ="CharsetEncoding";
	public static String USE_UNICODE ="UseUnicode";

	public static Integer INVALID_ID =(-1);
	public static String AND ="AND";
	public static String OR ="OR";

	public static String ASCEND ="ASC";
	public static String DESCEND ="DESC";

	private static String s_databaseName=null;
	private static String s_host=null;
	private static Integer s_port=null;
	private static String s_user=null;
	private static String s_password=null;
	private static String s_charsetResult=null;
	private static String s_charsetEncoding=null;
	private static String s_useUnicode=null;

	public static boolean readMySQLConfig(){
		boolean ret=false;
		try{
			String fullPath=Play.application().path()+"/conf/"+"database_config.xml";
			Map<String, String> attributes=XMLHelper.readDatabaseConfig(fullPath);
			s_databaseName=attributes.get(DATABASE_NAME);
			s_host=attributes.get(HOST);
			s_port=Integer.parseInt(attributes.get(PORT));
			s_user=attributes.get(USER);
			s_password=attributes.get(PASSWORD);
			s_charsetResult=attributes.get(CHARSET_RESULT);
			s_charsetEncoding=attributes.get(CHARSET_ENCODING);
			s_useUnicode=attributes.get(USE_UNICODE);
			ret=true;
		} catch(Exception e){
			System.out.println("SQLHelper.readMySQLConfig:"+e.getMessage());
		}
		return ret;
	}

	public static String getConnectionURI(){
		String ret=null;
		do{
			try{
				boolean configResult=readMySQLConfig();
				if(configResult==false) break;
				
				Class.forName("com.mysql.jdbc.Driver");
				StringBuilder builder=new StringBuilder();
				builder.append("jdbc:mysql://");
				builder.append(s_host+":");
				builder.append(s_port.toString()+"/");
				builder.append(s_databaseName);
				if(s_charsetResult!=null) builder.append("?"+s_charsetResult);
				if(s_charsetEncoding!=null) builder.append("&"+s_charsetEncoding);
				if(s_useUnicode!=null) builder.append("&"+s_useUnicode);
				ret=builder.toString();
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
		}while(false);
		return ret;
		
	}
	
	public static DataSource setupDataSource(String connectURI) {
		PoolingDataSource<PoolableConnection> ret=null;
		try{
			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, s_user, s_password);
			PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
			ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
			poolableConnectionFactory.setPool(connectionPool);
			ret = new PoolingDataSource<>(connectionPool);
		} catch(Exception e){
			System.out.println("SQLHelper.setupDataSource: "+e.getMessage());
			ret=null;	
		}
		return ret;
	}
		
	public static Connection getConnection(){
		Connection connection=null;
		try{
			if(s_dataSource==null){
				String connectURI=getConnectionURI();
				System.out.println("connectionURI="+connectURI);
				s_dataSource=setupDataSource(connectURI);
			}
			if(s_dataSource==null) System.out.println("s_dataSource is null");
			connection=s_dataSource.getConnection();
		} catch (Exception e) {
			System.out.println("SQLHelper.getConnection: "+e.getMessage());
			connection=null;	
		}
		return connection;
	}

	public static void closeConnection(Connection connection){
		try{
			if(connection!=null){
				connection.close();
			}
		}catch(Exception e){
			System.out.println("SQLHelper.closeConnection:"+e.getMessage());
		}
	}

	public List<JSONObject> executeSelect(String query){
		List<JSONObject> ret=null;
		try{
			Connection connection=getConnection();
			PreparedStatement statement= connection.prepareStatement(query); 
			ResultSet rs=statement.executeQuery();
			if(rs!=null){
				ret=ResultSetUtil.convertToJSON(rs);
				rs.close();
			}
			statement.close();
			closeConnection(connection);
			query=null;
		} catch (Exception e){
			System.out.println("SQLHelper.executeSelect: "+e.getMessage());
		}
		return ret;
	}

	public Integer executeInsert(String query){
		Integer lastId= INVALID_ID;
		try{
			Connection connection=getConnection();
			PreparedStatement statement= connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS); 
			// the following command returns the last inserted row id for the auto incremented key
			statement.executeUpdate();
			ResultSet rs = statement.getGeneratedKeys();
			if (rs != null && rs.next()) {
				lastId = (int) rs.getLong(1);
				rs.close();
			}
			statement.close();
			closeConnection(connection);
			query=null;
		} catch (Exception e){
			// return the invalid value for exceptions
			System.out.println("SQLHelper.executeInsert: "+e.getMessage());
		}
		return lastId;
	}
	
	public boolean executeUpdate(String query){
		boolean bRet=false;
		try{
			Connection connection=getConnection();
			PreparedStatement statement= connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS); 
			// the following command returns the last inserted row id for the auto incremented key
			statement.executeUpdate();
			statement.close();
			closeConnection(connection);
			query=null;
			bRet=true;
		} catch (Exception e){
			System.out.println("SQLHelper.executeUpdate: "+e.getMessage()+", while query is "+query);
		}
		return bRet;
	}
	
	public Integer insert(String tableName, List<String> columnNames, List<Object> columnValues){
		Integer lastId= INVALID_ID;
		do{
			if(columnNames.size()!=columnValues.size()) break;
			
			StringBuilder queryBuilder=new StringBuilder();
			queryBuilder.append("INSERT INTO "+tableName+"(");
			
			Iterator<String> itName=columnNames.iterator();
			while(itName.hasNext()){	
				String name=(String)itName.next();
				queryBuilder.append(name);
				if(itName.hasNext()) queryBuilder.append(",");
				else queryBuilder.append(")");
			}
		
			queryBuilder.append(" VALUES(");
			
			Iterator<Object> itValue=columnValues.iterator();
			while(itValue.hasNext()){
				Object valueObj=itValue.next();
				if (valueObj instanceof Integer){
					Integer value=(Integer)valueObj;
				    queryBuilder.append(value);
					if(itValue.hasNext()) queryBuilder.append(",");
					else queryBuilder.append(")");
				}
				else if (valueObj instanceof String){
				    String value=(String)valueObj;
				    queryBuilder.append("'"+value+"'");
					if(itValue.hasNext()) queryBuilder.append(",");
					else queryBuilder.append(")");
				} else{
					// left blank deliberately 
				}
			}
			String query=queryBuilder.toString();
			lastId=executeInsert(query);
		}while(false);
		return lastId;
	}

	public List<JSONObject> query(String tableName, List<String> columnNames, List<String> whereClauses, String link, List<String> orderClauses, List<String> orderDirections, List<Integer> limits){
		List<JSONObject> ret=null;
		do{
			StringBuilder queryBuilder=new StringBuilder();
			queryBuilder.append("SELECT ");
			Iterator<String> itName=columnNames.iterator();
			while(itName.hasNext()){
				String name=itName.next();
				queryBuilder.append(name);
				if(itName.hasNext()) queryBuilder.append(", ");
			}
			queryBuilder.append(" FROM "+tableName);
			
			if(whereClauses!=null && whereClauses.size()>0){
				queryBuilder.append(" WHERE ");
				Iterator<String> itClause=whereClauses.iterator();
				while(itClause.hasNext()){
					String whereClause=itClause.next();
					queryBuilder.append(whereClause);
					if(itClause.hasNext()){
						if(link==null) queryBuilder.append(" AND ");
						else queryBuilder.append(" "+link+" ");
					}
				}
			}

			if(orderClauses!=null && orderClauses.size()>0){
				queryBuilder.append(" ORDER BY ");
				Iterator<String> itClause=orderClauses.iterator();
				Iterator<String> itDirection=null;
				if(orderDirections!=null && orderDirections.size()>0){
					itDirection=orderDirections.iterator();
				}
				while(itClause.hasNext()){
					String clause=itClause.next();
					queryBuilder.append(clause);
					if(itDirection!=null && itDirection.hasNext()){
						String direction=itDirection.next();
						queryBuilder.append(" "+direction);
					}
					if(itClause.hasNext()) queryBuilder.append(", ");
				}
			}

			if(limits!=null && limits.size()>0 && limits.size()<=2){
				queryBuilder.append(" LIMIT ");
				Iterator<Integer> itLimit=limits.iterator();
				while(itLimit.hasNext()){
					Integer limit=itLimit.next();
					queryBuilder.append(limit.toString());
					if(itLimit.hasNext()) queryBuilder.append(", ");
				}
			}

			String query=queryBuilder.toString();
			ret=executeSelect(query);
		}while(false);
		return ret;
	}

	public List<JSONObject> query(String tableName, List<String> columnNames, List<String> whereClauses, String link, List<String> orderClauses, String orderDirection){
		List<String> orderDirections=null;
		do{
			if(orderDirection==null) break;
			orderDirections=new LinkedList<String>();
			Iterator<String> itClause=orderClauses.iterator();
			while(itClause.hasNext()){
				String clause=itClause.next();
				orderDirections.add(orderDirection);
			}
		}while(false);
		return query(tableName, columnNames, whereClauses, link, orderClauses, orderDirections, null);
	}
	
	public List<JSONObject> query(String tableName, List<String> columnNames, List<String> whereClauses, String link){
		return query(tableName, columnNames, whereClauses, link, null, null);
	}

	public boolean update(String tableName, List<String> columnNames, List<Object> columnValues, List<String> whereClauses, String link){
		boolean ret=false;
		do{			
			if(columnNames.size()!=columnValues.size()) break;
			
			StringBuilder queryBuilder=new StringBuilder();
			queryBuilder.append("UPDATE "+tableName+" SET ");
			Iterator<String> itName=columnNames.iterator();
			Iterator<Object> itValue=columnValues.iterator();
			while(itName.hasNext() && itValue.hasNext()){
				String name=itName.next();
				Object value=itValue.next();
				queryBuilder.append(name+"="+SQLHelper.convertToQueryValue(value));
				if(itName.hasNext()) queryBuilder.append(",");
			}
			
			if(whereClauses.size()>0){
				queryBuilder.append(" WHERE ");
				Iterator<String> itClause=whereClauses.iterator();
				while(itClause.hasNext()){
					String clause=itClause.next();
					queryBuilder.append(clause);
					if(itClause.hasNext()) queryBuilder.append(" "+link+" ");
				}
			}
			String query=queryBuilder.toString();
			ret=executeUpdate(query);
		}while(false);
		
		return ret;
	}

	public boolean delete(String tableName, List<String> whereClauses, String link){
		boolean ret=false;
		do{
			if(whereClauses.size()<=0) break;
			StringBuilder queryBuilder=new StringBuilder();
			queryBuilder.append("DELETE FROM "+tableName);

			if(whereClauses.size()>0){
				queryBuilder.append(" WHERE ");
				Iterator<String> itClause=whereClauses.iterator();
				while(itClause.hasNext()){
					String clause=itClause.next();
					queryBuilder.append(clause);
					if(itClause.hasNext()) queryBuilder.append(" "+link+" ");
				}
			}
			String query=queryBuilder.toString();
			ret=executeUpdate(query);
		}while(false);

		return ret;
	}
	
	public static String convertToQueryValue(Object item){
		String res=null;
		try{
		if (item instanceof Integer){
			Integer value=(Integer)item;
			res=value.toString();
		} else if (item instanceof String){
		    String value=(String)item;
		    StringBuilder valueBuilder=new StringBuilder();
		    for(int i=0;i<value.length();i++){
		    	char ch=value.charAt(i);
		    	switch (ch) {
		    		case '\'': // single column
		    		{
		    			valueBuilder.append("\'");
		    		}
		    		break;
		    		default:
		    		{
		    			valueBuilder.append(ch);
		    		}
		    		break;
		    	}
		    }
		
		    String securedValue=valueBuilder.toString();
		    res = "\""+securedValue+"\"";
		} else{
			// left blank deliberately 
		}
		} catch(Exception e){
			System.out.println("SQLHelper.convertToQueryValue: "+e.getMessage());
		}
		return res;
	}
};
