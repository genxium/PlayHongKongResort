package controllers;
import model.BasicUser;
import model.Activity;
import model.UserActivityRelation;
import model.UserActivityRelationTable;

import org.json.simple.JSONObject;

import ch.qos.logback.classic.db.names.ColumnName;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import dao.SQLHelper;

public class SQLCommander {
	
	public static Integer invalidId=(-1);
	
 	public static BasicUser queryUserByUserId(Integer userId){
 		
 		BasicUser user=null;
 		String tableName="User";
 		
 		List<String> columnNames=new LinkedList<String>();
 		List<String> whereClauses=new LinkedList<String>();
 		
 		columnNames.add(BasicUser.emailKey);
 		columnNames.add(BasicUser.passwordKey);
 		columnNames.add(BasicUser.nameKey);
 		columnNames.add(BasicUser.groupIdKey);
		columnNames.add(BasicUser.authenticationStatusKey);
		columnNames.add(BasicUser.genderKey);
		columnNames.add(BasicUser.lastLoggedInTimeKey);

		whereClauses.add(BasicUser.idKey+"="+SQLHelper.convertToQueryValue(userId));
		String logicLink=SQLHelper.logicAND;
		
		SQLHelper sqlHelper=new SQLHelper();
 		List<JSONObject> results=sqlHelper.queryTableByColumnsAndWhereClauses(tableName, columnNames, whereClauses, logicLink);
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
 		
 		List<String> columnNames=new LinkedList<String>();
 		List<String> whereClauses=new LinkedList<String>();
 		
 		columnNames.add(BasicUser.idKey);
 		columnNames.add(BasicUser.passwordKey);
 		columnNames.add(BasicUser.nameKey);
 		columnNames.add(BasicUser.groupIdKey);
		columnNames.add(BasicUser.authenticationStatusKey);
		columnNames.add(BasicUser.genderKey);
		columnNames.add(BasicUser.lastLoggedInTimeKey);

		whereClauses.add(BasicUser.emailKey+"="+SQLHelper.convertToQueryValue(email));
		String logicLink=SQLHelper.logicAND;
		
		SQLHelper sqlHelper=new SQLHelper();
 		List<JSONObject> results=sqlHelper.queryTableByColumnsAndWhereClauses(tableName, columnNames, whereClauses, logicLink); 			if(results!=null && results.size()>0){
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

	public static int createActivity(Activity activity, Integer userId){
	
		int lastActivityId=invalidId;
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
			int tmpLastActivityId=sqlHelper.insertToTableByColumns("Activity", columnNames, columnValues);
			if(tmpLastActivityId!=SQLHelper.invalidId){
				columnNames.clear();
				columnValues.clear();
				
				columnNames.add(UserActivityRelationTable.activityIdKey);
				columnNames.add(UserActivityRelationTable.userIdKey);
				columnNames.add(UserActivityRelationTable.relationIdKey);
				columnNames.add(UserActivityRelationTable.generatedTimeKey);
				
				columnValues.add(tmpLastActivityId);
				columnValues.add(userId);
				columnValues.add(UserActivityRelation.RelationType.host.ordinal());
				columnValues.add(activity.getCreatedTime().toString());
				
				int lastRelationTableId=sqlHelper.insertToTableByColumns("UserActivityRelationTable", columnNames, columnValues);
				if(lastRelationTableId!=SQLHelper.invalidId){
					lastActivityId=tmpLastActivityId;
				}
			}
		} catch (Exception e){
			System.out.println("SQLCommander.createActivity:"+e.getMessage());
		}
		return lastActivityId;
	}
	
	public static boolean updateActivity(Activity activity){
		boolean ret=false;
		do{
			String tableName="Activity";
			int activityId=activity.getId();
			
			try{
				
				SQLHelper sqlHelper=new SQLHelper();
				List<String> columnNames=new LinkedList<String>();
				List<Object> columnValues=new LinkedList<Object>();
				List<String> whereClauses=new LinkedList<String>();
				
				columnNames.add(Activity.titleKey);
				columnValues.add(activity.getTitle());
				columnNames.add(Activity.contentKey);
				columnValues.add(activity.getContent());
				columnNames.add(Activity.createdTimeKey);
				columnValues.add(activity.getCreatedTime().toString());
				columnNames.add(Activity.beginDateKey);
				columnValues.add(activity.getBeginDate().toString());
				columnNames.add(Activity.endDateKey);
				columnValues.add(activity.getEndDate());
				columnNames.add(Activity.capacityKey);
				columnValues.add(activity.getCapacity());
				
				whereClauses.add(Activity.idKey+"="+SQLHelper.convertToQueryValue(activity.getId()));
				String logicLink=SQLHelper.logicAND;
				ret=sqlHelper.updateTableByColumnsAndWhereClauses(tableName, columnNames, columnValues, whereClauses, logicLink);
			} catch(Exception e){
				System.out.println("SQLCommander.updateActivity:"+e.getMessage());
			}
		}while(false);
		return ret;
	}

	public static boolean createUserActivityRelation(int activityId, BasicUser user, UserActivityRelation.RelationType relation){
		
		boolean bRet=false;
		
		int userId=user.getUserId();
		int relationId=relation.ordinal();
		
		SQLHelper sqlHelper=new SQLHelper();
		
		List<String> columnNames=new LinkedList<String>();
		
		columnNames.add(UserActivityRelationTable.userIdKey);
		columnNames.add(UserActivityRelationTable.activityIdKey);
		columnNames.add(UserActivityRelationTable.relationIdKey);
		
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

 		List<String> columnNames=new LinkedList<String>();
		columnNames.add(Activity.titleKey);
		columnNames.add(Activity.contentKey);
		columnNames.add(Activity.createdTimeKey);
		columnNames.add(Activity.beginDateKey);
		columnNames.add(Activity.endDateKey);
		columnNames.add(Activity.capacityKey);
		columnNames.add(Activity.statusKey);

		List<String> whereClauses=new LinkedList<String>();
		whereClauses.add(Activity.idKey+"="+activityId);

		SQLHelper sqlHelper=new SQLHelper();
		List<JSONObject> results=sqlHelper.queryTableByColumnsAndWhereClauses(tableName, columnNames, whereClauses, SQLHelper.logicAND);
		if(results!=null && results.size()>0){
            Iterator<JSONObject> it=results.iterator();
	        if(it.hasNext()){
		        JSONObject jsonObject=(JSONObject)it.next();
		        try {
		      		String title=(String)jsonObject.get(Activity.titleKey);
		      		String content=(String)jsonObject.get(Activity.contentKey);
		      		Timestamp createdTime=(Timestamp)jsonObject.get(Activity.createdTimeKey);
		      		Timestamp beginDate=(Timestamp)jsonObject.get(Activity.beginDateKey);
		      		Timestamp endDate=(Timestamp)jsonObject.get(Activity.endDateKey);
		      		int capacity=(Integer)jsonObject.get(Activity.capacityKey);
		      		int status=(Integer)jsonObject.get(Activity.statusKey);
		      		activity=new Activity(activityId, title, content, createdTime, beginDate, endDate, capacity, status);
			    } catch (Exception e) {
			    	System.out.println("SQLCommander.queryActivityByActivityId:"+e.getMessage());
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
			relationColumnNames.add(UserActivityRelationTable.activityIdKey);
			
			List<String> relationWhereClauses=new LinkedList<String>();
			relationWhereClauses.add(UserActivityRelationTable.userIdKey+"="+user.getUserId());
			relationWhereClauses.add(UserActivityRelationTable.relationIdKey+"="+relation.ordinal());
			
			List<JSONObject> relationTableRecords=sqlHelper.queryTableByColumnsAndWhereClauses("UserActivityRelationTable", relationColumnNames, relationWhereClauses, SQLHelper.logicAND);
			
			List<Integer> activityIds=new LinkedList<Integer>();
			Iterator<JSONObject> itRecord=relationTableRecords.iterator();
			while(itRecord.hasNext()){
				JSONObject record=itRecord.next();
				Integer activityId=(Integer)record.get(UserActivityRelationTable.activityIdKey);
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