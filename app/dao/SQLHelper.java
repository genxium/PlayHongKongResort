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
		if(checkConnection()==true){
			try{
				Statement statement= connection.createStatement(); 
				ResultSet resultSet=statement.executeQuery(query);
				if(resultSet!=null){
					return ResultSetUtil.convertToJSON(resultSet);
				} else{
					return null;
				}
			} catch (Exception e){
				return null;
			}
		}else{
			return null;	
		}
	}

	public int executeInsert(String query){
		int invalidRet=-1;
		int ret=0;
		if(checkConnection()==true){
			try{
				Statement statement= connection.createStatement(); 
				// the following command returns the last inserted row id for the auto incremented key
				ret=statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			} catch (Exception e){
				// return the invalid value for exceptions
				ret=invalidRet;
			}
		}
		return ret;
	}
};