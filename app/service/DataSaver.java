package service;
import dao.SQLHelper;
import dao.ResultSetUtil;
import model.BasicUser;

public class DataSaver {
	public static boolean saveObject(BasicUser user){
		// DAO
		SQLHelper sqlHelper=new SQLHelper();
		
		StringBuilder queryBuilder=new StringBuilder();
		queryBuilder.append("INSERT INTO User(UserEmail, UserPassword, UserName) VALUES(");
		queryBuilder.append("'"+user.getEmail()+"'");
		queryBuilder.append(",");
		queryBuilder.append("'"+user.getPassword()+"'");
		queryBuilder.append(",");
		queryBuilder.append("'"+user.getName()+"'");
		queryBuilder.append(")");
		String query=queryBuilder.toString();
		sqlHelper.executeInsert(query);
	
		return true;
	}
}
