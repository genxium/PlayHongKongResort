package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.io.*;

import com.mysql.jdbc.Driver;

import model.Activity;

import org.json.simple.JSONObject;

import play.Play;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class SQLHelper {
	
	public static String databaseNameKey="DatabaseName";
	public static String hostKey="Host";
	public static String portKey="Port";
	public static String userKey="User";
	public static String passwordKey="Password";
	
	public static Integer invalidId=-1;
	public static String logicAND="AND";
	public static String logicOR="OR";
	
	public static String directionAscend="ASC";
	public static String directionDescend="DESC";

	private static Connection connection=null;
	private String databaseName=null;
	private String host=null;
	private Integer port=null;
	private String user=null;
	private String password=null;
	
	public boolean readMySQLConfig(){
		boolean ret=false;
		try{
			String fullPath=Play.application().path()+"/conf/"+"database_config.xml";
			Map<String, String> attributes=XMLHelper.readDatabaseConfig(fullPath);
			databaseName=attributes.get(databaseNameKey);
			host=attributes.get(hostKey);
			port=Integer.parseInt(attributes.get(portKey));
			user=attributes.get(userKey);
			password=attributes.get(passwordKey);
			ret=true;
		} catch(Exception e){
			System.out.println("SQLHelper.readMySQLConfig:"+e.getMessage());
		}
		return ret;
	}

	public static Connection getConnection(){
		return connection;
	}
	
	public boolean checkConnection(){
		try{
			// lazy init
			if(connection!=null) return true;
			boolean configResult=readMySQLConfig();
			if(configResult==false) return false;
			
			Class.forName("com.mysql.jdbc.Driver");
			StringBuilder connectionBuilder=new StringBuilder();
			connectionBuilder.append("jdbc:mysql://");
			connectionBuilder.append(host+":");
			connectionBuilder.append(port.toString()+"/");
			connectionBuilder.append(databaseName);
			String connectionStr=connectionBuilder.toString();
			connection = DriverManager.getConnection(connectionStr,user,password);
			if(connection!=null){
				return true;
			} else{
				return false;
			}
		}catch(Exception e){
			System.out.println("SQLHelper.checkConnection:"+e.getMessage());
			return false;
		}
	}

	public void closeConnection(){
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
		if(checkConnection()==true){
			try{
				Statement statement= connection.createStatement(); 
				ResultSet resultSet=statement.executeQuery(query);
				if(resultSet!=null){
					ret=ResultSetUtil.convertToJSON(resultSet);
				}
			} catch (Exception e){
				System.out.println("SQLHelper.executeSelect:"+e.getMessage());
			}
		}
		return ret;
	}

	public Integer executeInsert(String query){
		Integer lastId=invalidId;
		if(checkConnection()==true){
			try{
				PreparedStatement statement= connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS); 
				// the following command returns the last inserted row id for the auto incremented key
				statement.executeUpdate();
				ResultSet rs = statement.getGeneratedKeys();
				if (rs != null && rs.next()) {
					lastId = (int) rs.getLong(1);
				}
			} catch (Exception e){
				// return the invalid value for exceptions
				System.out.println("SQLHelper.executeInsert:"+e.getMessage());
			}
		}
		return lastId;
	}
	
	public boolean executeUpdate(String query){
		boolean bRet=false;
		if(checkConnection()==true){
			try{
				PreparedStatement statement= connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS); 
				// the following command returns the last inserted row id for the auto incremented key
				statement.executeUpdate();
				bRet=true;
			} catch (Exception e){
				System.out.println("SQLHelper.executeUpdate:"+e.getMessage());
			}
		}
		return bRet;
	}
	
	public Integer insertToTableByColumns(String tableName, List<String> columnNames, List<Object> columnValues){
		Integer lastId=invalidId;
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
	
	public List<JSONObject> queryTableByColumnsAndWhereClauses(String tableName, List<String> columnNames, List<String> whereClauses, String logicLink){
		List<JSONObject> ret=null;
		do{
			StringBuilder queryBuilder=new StringBuilder();
			queryBuilder.append("SELECT ");
			Iterator<String> itName=columnNames.iterator();
			while(itName.hasNext()){
				String name=itName.next();
				queryBuilder.append(name);
				if(itName.hasNext()) queryBuilder.append(",");
			}
			queryBuilder.append(" FROM "+tableName);
			
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
			ret=executeSelect(query);
		}while(false);
		return ret;
	}

	public List<JSONObject> queryTableByColumnsAndWhereClausesAndOrderClauses(String tableName, List<String> columnNames, List<String> whereClauses, String whereLogicLink, List<String> orderClauses, String orderDirection){
		List<JSONObject> ret=null;
		do{
			StringBuilder queryBuilder=new StringBuilder();
			queryBuilder.append("SELECT ");
			Iterator<String> itName=columnNames.iterator();
			while(itName.hasNext()){
				String name=itName.next();
				queryBuilder.append(name);
				if(itName.hasNext()) queryBuilder.append(",");
			}
			queryBuilder.append(" FROM "+tableName);
			
			if(whereClauses.size()>0){
				queryBuilder.append(" WHERE ");
				Iterator<String> itClause=whereClauses.iterator();
				while(itClause.hasNext()){
					String clause=itClause.next();
					queryBuilder.append(clause);
					if(itClause.hasNext()) queryBuilder.append(" "+whereLogicLink+" ");
				}
			}

			if(orderClauses.size()>0){
				queryBuilder.append(" ORDER BY ");
				Iterator<String> itClause=orderClauses.iterator();
				while(itClause.hasNext()){
					String clause=itClause.next();
					queryBuilder.append(clause);
					if(itClause.hasNext()) queryBuilder.append(",");
				}
				if(orderDirection!=null && orderDirection.length()>0){
					queryBuilder.append(" "+orderDirection);
				}
			}

			String query=queryBuilder.toString();
			ret=executeSelect(query);
		}while(false);
		return ret;
	}
	
	public boolean updateTableByColumnsAndWhereClauses(String tableName, List<String> columnNames, List<Object> columnValues, List<String> whereClauses, String logicLink){
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
	
	public static String convertToQueryValue(Object item){
		String res=null;
		if (item instanceof Integer){
			Integer value=(Integer)item;
			res=value.toString();
		}
		else if (item instanceof String){
		    String value=(String)item;
		    res="'"+value+"'";
		} else{
			// left blank deliberately 
		}
		return res;
	}
};