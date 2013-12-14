package controllers;
import model.Guest;
import model.BasicUser;
import model.Activity;
import org.json.simple.JSONObject;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import dao.SQLHelper;
import dao.ResultSetUtil;

public class SQLCommander {
 	public static BasicUser queryUserByEmail(String email){

 		String tableName="User";
 		String idKey="UserId";
 		String emailKey="UserEmail";
 		String nameKey="UserName";
 		String passwordKey="UserPassword";
 		String groupIdKey="UserGroupId";
 		String authenticationStatusKey="UserAuthenticationStatus";
 		String genderKey="UserGender";
 		String lastLoggedInTimeKey="UserLastLoggedInTime";

 		StringBuilder queryBuilder=new StringBuilder();
		queryBuilder.append("SELECT ");
		queryBuilder.append(idKey+",");
		queryBuilder.append(nameKey+",");
		queryBuilder.append(passwordKey+",");
		queryBuilder.append(groupIdKey+",");
		queryBuilder.append(authenticationStatusKey+",");
		queryBuilder.append(genderKey+",");
		queryBuilder.append(lastLoggedInTimeKey);

		queryBuilder.append(" FROM "+tableName+" WHERE ");

		queryBuilder.append(emailKey+"="+"'"+email+"'");

		String query=queryBuilder.toString();
		SQLHelper sqlHelper=new SQLHelper();
		List<JSONObject> results=sqlHelper.executeSelect(query);
		if(results!=null && results.size()>0){
            Iterator it=results.iterator();
	        if(it.hasNext()){
		        JSONObject jsonObject=(JSONObject)it.next();
		        try {

		      		String password=(String)jsonObject.get(passwordKey);
		      		String name=(String)jsonObject.get(nameKey);
		      		int id=(Integer)jsonObject.get(idKey);
	        		
          		    BasicUser user=new BasicUser(id, email, password, name, false, false, false);
          		    return user;
			    } catch (Exception e) {
			    		System.out.print(e.getMessage());
		        }
	    		} 	
		}
		
		return null;
	}

	public static boolean registerUser(BasicUser user){
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
		try{
			sqlHelper.executeInsert(query);
			sqlHelper=null;
		} catch (Exception e){
			return false;
		}
		return true;
	}

	public static boolean createActivity(Activity activity){
		// DAO
		SQLHelper sqlHelper=new SQLHelper();
		
		StringBuilder queryBuilder=new StringBuilder();
		queryBuilder.append("INSERT INTO User(ActivityName, ActivityContent, ActivityCreatedTime, ActivityBeginDate, ActivityEndDate, ActivityCapacity) VALUES(");
		queryBuilder.append("'"+activity.getName()+"'");
		queryBuilder.append(",");
		queryBuilder.append("'"+activity.getContent()+"'");
		queryBuilder.append(",");
		queryBuilder.append("'"+activity.getCreatedTime()+"'");
		queryBuilder.append(",");
		queryBuilder.append("'"+activity.getBeginDate+"'");
		queryBuilder.append(",");
		queryBuilder.append("'"+activity.getEndDate+"'");
		queryBuilder.append(",");
		queryBuilder.append(activity.getCapacity);
		queryBuilder.append(")");
		String query=queryBuilder.toString();
		try{
			sqlHelper.executeInsert(query);
			sqlHelper=null;
		} catch (Exception e){
			return false;
		}
		return true;
	}

	public static boolean createUserActivityRelation(Activity activity, BasicUser user, int relationShip){
		return true;
	}
};