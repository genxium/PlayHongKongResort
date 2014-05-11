package dao;
import org.json.simple.JSONObject;
import play.Play;

import java.sql.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SQLHelper {
	
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

	private static String databaseName=null;
	private static String host=null;
	private static Integer port=null;
	private static String user=null;
	private static String password=null;
	private static String charsetResult=null;
	private static String charsetEncoding=null;
	private static String useUnicode=null;

	public static boolean readMySQLConfig(){
		boolean ret=false;
		try{
			String fullPath=Play.application().path()+"/conf/"+"database_config.xml";
			Map<String, String> attributes=XMLHelper.readDatabaseConfig(fullPath);
			databaseName=attributes.get(DATABASE_NAME);
			host=attributes.get(HOST);
			port=Integer.parseInt(attributes.get(PORT));
			user=attributes.get(USER);
			password=attributes.get(PASSWORD);
			charsetResult=attributes.get(CHARSET_RESULT);
			charsetEncoding=attributes.get(CHARSET_ENCODING);
			useUnicode=attributes.get(USE_UNICODE);
			ret=true;
		} catch(Exception e){
			System.out.println("SQLHelper.readMySQLConfig:"+e.getMessage());
		}
		return ret;
	}
	
	public static Connection getConnection(){
		Connection connection=null;
		do{
			try{
				boolean configResult=readMySQLConfig();
				if(configResult==false) break;
				
				Class.forName("com.mysql.jdbc.Driver");
				StringBuilder connectionBuilder=new StringBuilder();
				connectionBuilder.append("jdbc:mysql://");
				connectionBuilder.append(host+":");
				connectionBuilder.append(port.toString()+"/");
				connectionBuilder.append(databaseName);
				if(true){
					connectionBuilder.append("?"+charsetResult);
					connectionBuilder.append("&"+charsetEncoding);
					connectionBuilder.append("&"+useUnicode);
				}
				String connectionStr=connectionBuilder.toString();
				connection = DriverManager.getConnection(connectionStr,user,password);
				if(connection==null) break;
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
		}while(false);
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
			Statement statement= connection.createStatement(); 
			ResultSet rs=statement.executeQuery(query);
			if(rs!=null){
				ret=ResultSetUtil.convertToJSON(rs);
				rs.close();
			}
			statement.close();
			query=null;
			closeConnection(connection);
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
			query=null;
			closeConnection(connection);
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
			query=null;
			closeConnection(connection);
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

	public List<JSONObject> query(String tableName, List<String> columnNames, List<String> whereClauses, String whereLink, List<String> orderClauses, List<String> orderDirections, List<Integer> limits){
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
					String clause=itClause.next();
					queryBuilder.append(clause);
					if(itClause.hasNext()){
						if(whereLink==null) queryBuilder.append(" AND ");
						else queryBuilder.append(" "+whereLink+" ");
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

	public List<JSONObject> query(String tableName, List<String> columnNames, List<String> whereClauses, String whereLink, List<String> orderClauses, String orderDirection){
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
		return query(tableName, columnNames, whereClauses, whereLink, orderClauses, orderDirections, null);
	}
	
	public List<JSONObject> query(String tableName, List<String> columnNames, List<String> whereClauses, String whereLink){
		return query(tableName, columnNames, whereClauses, whereLink, null, null);
	}

	public boolean update(String tableName, List<String> columnNames, List<Object> columnValues, List<String> whereClauses, String logicLink){
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
					if(itClause.hasNext()) queryBuilder.append(" "+logicLink+" ");
				}
			}
			String query=queryBuilder.toString();
			ret=executeUpdate(query);
		}while(false);
		
		return ret;
	}

	public boolean delete(String tableName, List<String> whereClauses, String logicLink){
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
					if(itClause.hasNext()) queryBuilder.append(" "+logicLink+" ");
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
		}
		else if (item instanceof String){
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
