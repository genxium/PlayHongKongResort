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

import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class SQLHelper {
	
	private static Integer invalidId=-1;
	
	private static Connection connection=null;
	private String hostName="localhost";
	private Integer port=3306;
	private String databaseName="HongKongResort";
	private String user="root";
	private String password="";

	public static Connection getConnection(){
		return connection;
	}
	
	public boolean checkConnection(){
		try{
			if(connection!=null) return true;
			Class.forName("com.mysql.jdbc.Driver");
			StringBuilder connectionBuilder=new StringBuilder();
			connectionBuilder.append("jdbc:mysql://");
			connectionBuilder.append(hostName+":");
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
			return false;
		}
	}

	public void closeConnection(){
		try{
			if(connection!=null){
				connection.close();
			}
		}catch(Exception e){

		}
	}

	public String checkConnectionWithStringResult(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/HongKongResort",user,password);
			if(connection!=null){
				return "Success";
			} else{
				return "Normal Failed";
			}
		}catch(Exception e){
			return e.getMessage();
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
				System.out.println(e.getMessage());
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
	
	public List<JSONObject> queryTableByColumns(String tableName, List<String> columnNames){
		List<JSONObject> ret=null;
		do{
			StringBuilder queryBuilder=new StringBuilder();
			queryBuilder.append("SELECT ");
			Iterator<String> itName=columnNames.iterator();
			while(itName.hasNext()){
				String name=(String)itName.next();
				queryBuilder.append(name);
				if(itName.hasNext()) queryBuilder.append(",");
			}
			queryBuilder.append(" FROM "+tableName);
			String query=queryBuilder.toString();
			ret=executeSelect(query);
		}while(false);
		return ret;
	}
};