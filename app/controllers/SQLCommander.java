package controllers;
import model.BasicUser;
import model.Activity;
import model.UserActivityRelation;

import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import dao.SQLHelper;

public class SQLCommander {
	
	public static Integer invalidId=-1;
	
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

	public static int registerUser(BasicUser user){
		int invalidId=-1;
		int lastInsertedId=invalidId;
		
		// DAO
		SQLHelper sqlHelper=new SQLHelper();
		
		List<String> columnNames=new LinkedList<String>();
		columnNames.add("UserEmail");
		columnNames.add("UserPassword");
		columnNames.add("UserName");
		
		List<Object> columnValues=new LinkedList<Object>();
		columnValues.add(user.getEmail());
		columnValues.add(user.getPassword());
		columnValues.add(user.getName());
		
		try{
			lastInsertedId=sqlHelper.insertToTableByColumns("User", columnNames, columnValues);
			sqlHelper=null;
		} catch (Exception e){
			
		}
		return lastInsertedId;
	}

	public static boolean createActivity(Activity activity, BasicUser user){
		int invalidId=-1;
		int lastInsertedActivityId=invalidId;
		boolean bRet=false;
		// DAO
		SQLHelper sqlHelper=new SQLHelper();
		List<String> columnNames=new LinkedList<String>();
		
		columnNames.add("ActivityName");
		columnNames.add("ActivityContent");
		columnNames.add("ActivityCreatedTime");
		columnNames.add("ActivityBeginDate");
		columnNames.add("ActivityEndDate");
		columnNames.add("ActivityCapacity");
		
		List<Object> columnValues=new LinkedList<Object>();
		
		columnValues.add(activity.getName());
		columnValues.add(activity.getContent());
		columnValues.add(activity.getCreatedTime().toString());
		columnValues.add(activity.getBeginDate().toString());
		columnValues.add(activity.getEndDate().toString());
		columnValues.add(activity.getCapacity());
		
		try{
			lastInsertedActivityId=sqlHelper.insertToTableByColumns("User", columnNames, columnValues);
			bRet=createUserActivityRelation(lastInsertedActivityId, user, UserActivityRelation.RelationShipType.host);
			sqlHelper=null;
		} catch (Exception e){
			
		}
		return bRet;
	}

	public static boolean createUserActivityRelation(int activityId, BasicUser user, UserActivityRelation.RelationShipType relationShip){
		
		return true;
	}
};