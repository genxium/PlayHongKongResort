package controllers;
import model.BasicUser;
import model.Activity;
import model.UserActivityRelation;

import org.json.simple.JSONObject;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import dao.SQLHelper;

public class SQLCommander {
	
	public static Integer invalidId=-1;
	
 	public static BasicUser queryUserByUserId(Integer userId){
 		
 		BasicUser user=null;
 		String tableName="User";
 		
 		StringBuilder queryBuilder=new StringBuilder();
		queryBuilder.append("SELECT ");
		queryBuilder.append(BasicUser.emailKey+",");
		queryBuilder.append(BasicUser.nameKey+",");
		queryBuilder.append(BasicUser.passwordKey+",");
		queryBuilder.append(BasicUser.groupIdKey+",");
		queryBuilder.append(BasicUser.authenticationStatusKey+",");
		queryBuilder.append(BasicUser.genderKey+",");
		queryBuilder.append(BasicUser.lastLoggedInTimeKey);

		queryBuilder.append(" FROM "+tableName+" WHERE ");

		queryBuilder.append(BasicUser.idKey+SQLHelper.convertToQueryValue(userId));

		String query=queryBuilder.toString();
		SQLHelper sqlHelper=new SQLHelper();
		List<JSONObject> results=sqlHelper.executeSelect(query);
		if(results!=null && results.size()>0){
            Iterator<JSONObject> it=results.iterator();
	        if(it.hasNext()){
		        JSONObject jsonObject=(JSONObject)it.next();
		        try {
		        		String email=(String)jsonObject.get(BasicUser.emailKey);
		      		String password=(String)jsonObject.get(BasicUser.passwordKey);
		      		String name=(String)jsonObject.get(BasicUser.nameKey);
          		    user=new BasicUser(userId, email, password, name, false, false, false);
			    } catch (Exception e) {
			    	
		        }
	    		} 	
		}
		
		return user;
	}
 	
 	public static BasicUser queryUserByEmail(String email){
 
 			BasicUser user=null;
 			String tableName="User";
 			  		
 			StringBuilder queryBuilder=new StringBuilder();
 			queryBuilder.append("SELECT ");
 			queryBuilder.append(BasicUser.idKey+",");
 			queryBuilder.append(BasicUser.nameKey+",");
 			queryBuilder.append(BasicUser.passwordKey+",");
 			queryBuilder.append(BasicUser.groupIdKey+",");
 			queryBuilder.append(BasicUser.authenticationStatusKey+",");
 			queryBuilder.append(BasicUser.genderKey+",");
 			queryBuilder.append(BasicUser.lastLoggedInTimeKey);

 			queryBuilder.append(" FROM "+tableName+" WHERE ");

 			queryBuilder.append(BasicUser.emailKey+"="+SQLHelper.convertToQueryValue(email));

 			String query=queryBuilder.toString();
 			SQLHelper sqlHelper=new SQLHelper();
 			List<JSONObject> results=sqlHelper.executeSelect(query);
 			if(results!=null && results.size()>0){
 	            Iterator<JSONObject> it=results.iterator();
 		        if(it.hasNext()){
 			        JSONObject jsonObject=(JSONObject)it.next();
 			        try {
 			        		Integer userId=(Integer)jsonObject.get(BasicUser.idKey);
 			      		String password=(String)jsonObject.get(BasicUser.passwordKey);
 			      		String name=(String)jsonObject.get(BasicUser.nameKey);
 	          		    user=new BasicUser(userId, email, password, name, false, false, false);
 				    } catch (Exception e) {
 				    	
 			        }
 		    		} 	
 			}
 			
 			return user;
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
	
		boolean bRet=false;
		// DAO
		SQLHelper sqlHelper=new SQLHelper();
		List<String> columnNames=new LinkedList<String>();
		
		columnNames.add(Activity.titleKey);
		columnNames.add(Activity.contentKey);
		columnNames.add(Activity.createdTimeKey);
		columnNames.add(Activity.beginDateKey);
		columnNames.add(Activity.endDateKey);
		columnNames.add(Activity.capacityKey);
		
		List<Object> columnValues=new LinkedList<Object>();
		
		columnValues.add(activity.getTitle());
		columnValues.add(activity.getContent());
		columnValues.add(activity.getCreatedTime().toString());
		columnValues.add(activity.getBeginDate().toString());
		columnValues.add(activity.getEndDate().toString());
		columnValues.add(activity.getCapacity());
		
		try{
			int lastId=sqlHelper.insertToTableByColumns("Activity", columnNames, columnValues);
			if(lastId!=invalidId){
				bRet=true;
			}
		} catch (Exception e){
			
		}
		return bRet;
	}
	
	public static void updateActivity(Activity activity){
		
		String tableName="Activity";
		int activityId=activity.getId();
		Activity res=queryActivityByActivityId(activityId);
		if(res!=null){
			
			StringBuilder queryBuilder=new StringBuilder();
			queryBuilder.append("UPDATE "+tableName+" SET ");
			queryBuilder.append(Activity.titleKey+"="+"'"+activity.getTitle()+"'");
			queryBuilder.append(",");
			queryBuilder.append(Activity.contentKey+"="+"'"+activity.getContent()+"'");
			queryBuilder.append(",");
			queryBuilder.append(Activity.createdTimeKey+"="+"'"+activity.getCreatedTime().toString()+"'");
			queryBuilder.append(",");
			queryBuilder.append(Activity.beginDateKey+"="+"'"+activity.getBeginDate().toString()+"'");
			queryBuilder.append(",");
			queryBuilder.append(Activity.endDateKey+"="+"'"+activity.getEndDate().toString()+"'");
			queryBuilder.append(",");
			queryBuilder.append(Activity.capacityKey+"="+activity.getCapacity());
			queryBuilder.append(" WHERE "+Activity.idKey+"="+activity.getId());
			
			String query=queryBuilder.toString();
			
			SQLHelper sqlHelper=new SQLHelper();
			sqlHelper.executeUpdate(query);
		}

	}

	public static boolean createUserActivityRelation(int activityId, BasicUser user, UserActivityRelation.RelationType relation){
		
		boolean bRet=false;
		
		int userId=user.getUserId();
		int relationId=relation.ordinal();
		
		SQLHelper sqlHelper=new SQLHelper();
		
		List<String> columnNames=new LinkedList<String>();
		
		columnNames.add(UserActivityRelation.userIdKey);
		columnNames.add(UserActivityRelation.activityIdKey);
		columnNames.add(UserActivityRelation.relationIdKey);
		
		List<Object> columnValues=new LinkedList<Object>();
		
		columnValues.add(userId);
		columnValues.add(activityId);
		columnValues.add(relationId);
		
		try{
			int lastId=sqlHelper.insertToTableByColumns("UserActivityRelationTable", columnNames, columnValues);
			if(lastId!=invalidId){
				bRet=true;
			}
		} catch(Exception e){
			
		}
		return bRet;
	}
	
	public static Activity queryActivityByActivityId(int activityId){
			
		Activity activity=null;
 		String tableName="Activity";
		
 		StringBuilder queryBuilder=new StringBuilder();
		queryBuilder.append("SELECT ");
		queryBuilder.append(Activity.titleKey+",");
		queryBuilder.append(Activity.contentKey+",");
		queryBuilder.append(Activity.createdTimeKey+",");
		queryBuilder.append(Activity.beginDateKey+",");
		queryBuilder.append(Activity.endDateKey+",");
		queryBuilder.append(Activity.capacityKey);

		queryBuilder.append(" FROM "+tableName+" WHERE ");

		queryBuilder.append(Activity.idKey+"="+activityId);

		String query=queryBuilder.toString();
		SQLHelper sqlHelper=new SQLHelper();
		List<JSONObject> results=sqlHelper.executeSelect(query);
		if(results!=null && results.size()>0){
            Iterator<JSONObject> it=results.iterator();
	        if(it.hasNext()){
		        JSONObject jsonObject=(JSONObject)it.next();
		        try {
		        		int id=(Integer)jsonObject.get(Activity.idKey);
		      		String title=(String)jsonObject.get(Activity.titleKey);
		      		String content=(String)jsonObject.get(Activity.contentKey);
		      		Timestamp createdTime=Timestamp.valueOf((String)jsonObject.get(Activity.createdTimeKey));
		      		Timestamp beginDate=Timestamp.valueOf((String)jsonObject.get(Activity.beginDateKey));
		      		Timestamp endDate=Timestamp.valueOf((String)jsonObject.get(Activity.endDateKey));
		      		int capacity=(Integer)jsonObject.get(Activity.capacityKey);
		      		activity=new Activity(id, title, content, createdTime, beginDate, endDate, capacity);
			    } catch (Exception e) {
			    		
		        }
	    		} 	
		}
		
		return activity;
	}
	
	public static List<JSONObject> queryActivitiesByUserAndRelation(BasicUser user, UserActivityRelation.RelationType relation){
		List<JSONObject> activityRecords=null;
		do{
			SQLHelper sqlHelper=new SQLHelper();
			// query table UserActivityRelationTable 
			List<String> relationColumnNames=new LinkedList<String>();
			relationColumnNames.add(UserActivityRelation.activityIdKey);
			
			List<String> relationWhereClauses=new LinkedList<String>();
			relationWhereClauses.add(UserActivityRelation.userIdKey+"="+user.getUserId());
			relationWhereClauses.add(UserActivityRelation.relationIdKey+"="+relation.ordinal());
			
			List<JSONObject> relationTableRecords=sqlHelper.queryTableByColumnsAndWhereClauses("UserActivityRelationTable", relationColumnNames, relationWhereClauses, SQLHelper.logicAND);
			
			List<Integer> activityIds=new LinkedList<Integer>();
			Iterator<JSONObject> itRecord=relationTableRecords.iterator();
			while(itRecord.hasNext()){
				JSONObject record=itRecord.next();
				Integer activityId=(Integer)record.get(UserActivityRelation.activityIdKey);
				activityIds.add(activityId);
			}
				
			if(activityIds.size()<=0) break;
			
			// query table Activity
			List<String> activityColumnNames=new LinkedList<String>();
			activityColumnNames.add(Activity.idKey);
			activityColumnNames.add(Activity.titleKey);
			activityColumnNames.add(Activity.contentKey);
			activityColumnNames.add(Activity.createdTimeKey);
			activityColumnNames.add(Activity.beginDateKey);
			activityColumnNames.add(Activity.endDateKey);
			activityColumnNames.add(Activity.capacityKey);
			
			List<String> activityWhereClauses=new LinkedList<String>();
			Iterator<Integer> itActivityId=activityIds.iterator();
			while(itActivityId.hasNext()){
				Integer targetActivityId=itActivityId.next();
				activityWhereClauses.add(Activity.idKey+"="+SQLHelper.convertToQueryValue(targetActivityId));
			}
			activityRecords=sqlHelper.queryTableByColumnsAndWhereClauses("Activity", activityColumnNames, activityWhereClauses, SQLHelper.logicOR);
			
		}while(false);
		return activityRecords;
	}
};