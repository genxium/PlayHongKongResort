package controllers;
import model.BasicUser;
import model.Activity;
import model.Image;
import model.UserActivityRelation;
import model.UserActivityRelationTable;

import org.json.simple.JSONObject;

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
          		    user=BasicUser.create(userId, email, password, name);
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
 	          		    user=BasicUser.create(userId, email, password, name);
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
		columnNames.add(BasicUser.emailKey);
		columnNames.add(BasicUser.passwordKey);
		columnNames.add(BasicUser.nameKey);
		
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
				ret=sqlHelper.updateTableByColumnsAndWhereClauses(tableName, columnNames, columnValues, whereClauses, SQLHelper.logicAND);
			
			} catch(Exception e){
				System.out.println("SQLCommander.updateActivity:"+e.getMessage());
			}
		}while(false);
		return ret;
	}

	public static boolean deleteActivity(int userId, int activityId){
		boolean ret=false;
		do{
			String activityTableName="Activity";
			try{
				SQLHelper sqlHelper=new SQLHelper();
				String relationTableName="UserActivityRelationTable";
				List<String> relationWhereClauses=new LinkedList<String>();
				relationWhereClauses.add(BasicUser.idKey+"="+SQLHelper.convertToQueryValue(userId));
				relationWhereClauses.add(Activity.idKey+"="+SQLHelper.convertToQueryValue(activityId));
				boolean resultRelationDeletion=sqlHelper.deleteFromTableByWhereClauses(relationTableName, relationWhereClauses, SQLHelper.logicAND);
				if(resultRelationDeletion==true){
					List<String> activityWhereClauses=new LinkedList<String>();
					activityWhereClauses.add(Activity.idKey+"="+SQLHelper.convertToQueryValue(activityId));
					ret=sqlHelper.deleteFromTableByWhereClauses(activityTableName, activityWhereClauses, SQLHelper.logicAND);
				}
			} catch(Exception e){
				System.out.println("SQLCommander.deleteActivity:"+e.getMessage());
			}
		}while(false);

		return ret;
	}
	
	public static boolean submitActivity(int userId, Activity activity){
		boolean ret=false;
		do{
			String activityTableName="Activity";
			try{
				SQLHelper sqlHelper=new SQLHelper();
				List<String> columnNames=new LinkedList<String>();
				columnNames.add(Activity.statusKey);
				
				List<Object> columnValues=new LinkedList<Object>();
				columnValues.add(Activity.StatusType.pending.ordinal());
				
				List<String> whereClauses=new LinkedList<String>();
				whereClauses.add(Activity.idKey+"="+activity.getId());
		
				ret=sqlHelper.updateTableByColumnsAndWhereClauses(activityTableName, columnNames, columnValues, whereClauses, SQLHelper.logicAND);

			} catch(Exception e){
				System.out.println("SQLCommander.submitActivity:"+e.getMessage());
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
		      		Activity.StatusType status=Activity.StatusType.getTypeForValue((Integer)jsonObject.get(Activity.statusKey));
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
			activityColumnNames.add(Activity.statusKey);
			
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

	public static List<JSONObject> queryAcceptedActivitiesByStatusAndChronologicalOrder(){
		List<JSONObject> records=null;

		try{
			String tableName="Activity";
			SQLHelper sqlHelper=new SQLHelper();

			// query table Activity
			List<String> columnNames=new LinkedList<String>();
			columnNames.add(Activity.idKey);
			columnNames.add(Activity.titleKey);
			columnNames.add(Activity.contentKey);
			columnNames.add(Activity.createdTimeKey);
			columnNames.add(Activity.beginDateKey);
			columnNames.add(Activity.endDateKey);
			columnNames.add(Activity.capacityKey);
				
			List<String> whereClauses=new LinkedList<String>();
			whereClauses.add(Activity.statusKey+"="+Activity.StatusType.accepted.ordinal());

			List<String> orderClauses=new LinkedList<String>();
			orderClauses.add(Activity.createdTimeKey);
			records=sqlHelper.queryTableByColumnsAndWhereClausesAndOrderClauses(tableName, columnNames, whereClauses, SQLHelper.logicAND, orderClauses, SQLHelper.directionDescend);
		

		} catch(Exception e){
			System.out.println("SQLCommander.queryActivitiesByStatusAndChronologicalOrder:"+e.getMessage());
		}
		return records;
	}

	public static List<JSONObject> queryAcceptedActivitiesByStatusAndChronologicalOrderByUser(int userId){
		List<JSONObject> records=null;

		try{
			String tableName="Activity";
			SQLHelper sqlHelper=new SQLHelper();

			// query table Activity
			List<String> columnNames=new LinkedList<String>();
			columnNames.add(Activity.idKey);
			columnNames.add(Activity.titleKey);
			columnNames.add(Activity.contentKey);
			columnNames.add(Activity.createdTimeKey);
			columnNames.add(Activity.beginDateKey);
			columnNames.add(Activity.endDateKey);
			columnNames.add(Activity.capacityKey);
				
			List<String> whereClauses=new LinkedList<String>();
			whereClauses.add(Activity.statusKey+"="+Activity.StatusType.accepted.ordinal());

			List<String> orderClauses=new LinkedList<String>();
			orderClauses.add(Activity.createdTimeKey);
			List<JSONObject> activityRecords=sqlHelper.queryTableByColumnsAndWhereClausesAndOrderClauses(tableName, columnNames, whereClauses, SQLHelper.logicAND, orderClauses, SQLHelper.directionDescend);
		
			records=new LinkedList<JSONObject>();
			Iterator<JSONObject> itActivityRecord=activityRecords.iterator();
			while(itActivityRecord.hasNext()){
				JSONObject activityJson=itActivityRecord.next();
				int activityId=(Integer)activityJson.get(Activity.idKey);
				UserActivityRelation.RelationType relation=SQLCommander.queryRelationOfUserAndActivity(userId, activityId);
				JSONObject recordJson=(JSONObject) activityJson.clone();
				if(relation!=null){
					recordJson.put(UserActivityRelationTable.relationIdKey, relation.ordinal());
				}
				records.add(recordJson);
			}
		} catch(Exception e){
			System.out.println("SQLCommander.queryActivitiesByStatusAndChronologicalOrderByUser:"+e.getMessage());
		}
		return records;
	}
	
	public static UserActivityRelation.RelationType queryRelationOfUserAndActivity(int userId, int activityId){
		String tableName="UserActivityRelationTable";
		UserActivityRelation.RelationType ret=null; 
		try{
			SQLHelper sqlHelper=new SQLHelper();
			// query table UserActivityRelationTable 
			List<String> relationColumnNames=new LinkedList<String>();
			relationColumnNames.add(UserActivityRelationTable.relationIdKey);

			List<String> relationWhereClauses=new LinkedList<String>();
			relationWhereClauses.add(UserActivityRelationTable.userIdKey+"="+userId);
			relationWhereClauses.add(UserActivityRelationTable.activityIdKey+"="+activityId);
		
			List<JSONObject> relationTableRecords=sqlHelper.queryTableByColumnsAndWhereClauses(tableName, relationColumnNames, relationWhereClauses, SQLHelper.logicAND);
		
			Iterator<JSONObject> itRecord=relationTableRecords.iterator();
			if(itRecord.hasNext()){
				JSONObject record=itRecord.next();
				Integer relationId=(Integer)record.get(UserActivityRelationTable.relationIdKey);
				ret=UserActivityRelation.RelationType.getTypeForValue(relationId);
			}
		} catch(Exception e){
			System.out.println("SQLCommander.queryRelationOfUserAndActivity:"+e.getMessage());
		}
		return ret;
	}
	
	public static boolean validateOwnershipOfActivity(int userId, int activityId){
		boolean ret=false;
		do{
			// validate host relation
			UserActivityRelation.RelationType type=SQLCommander.queryRelationOfUserAndActivity(userId, activityId);
			if(type==null || type!=UserActivityRelation.RelationType.host) break;
			ret=true;
		}while(false);
		return ret;
	}
	
	public static boolean validateOwnershipOfActivity(int userId, Activity activity){
		boolean ret=false;
		do{
			if(activity==null) break;
			int activityId=activity.getId();
			ret=validateOwnershipOfActivity(userId, activityId);
		}while(false);
		return ret;
	}
	
	public static boolean isActivityEditable(int userId, int activityId){
		boolean ret=false;
		do{
			if(userId==DataUtils.invalidId) break;
			Activity activity=SQLCommander.queryActivityByActivityId(activityId);
  	  		ret=isActivityEditable(userId, activity);
		} while(false);
		return ret;
	}
	
	public static boolean isActivityEditable(int userId, Activity activity){
		boolean ret=false;
		do{
			if(userId==DataUtils.invalidId) break;
			if(activity==null) break;
	     	if(validateOwnershipOfActivity(userId, activity)==false)	break;
	  	  	if(activity.getStatus()!=Activity.StatusType.created) break;
	  	  	ret=true;
		}while(false);
		return ret;
	}

	public static boolean isActivityJoinable(BasicUser user, Activity activity){
		boolean ret=false;
		do{
			if(user==null) break;
			int userId=user.getUserId();
			ret=isActivityJoinable(userId, activity);
		}while(false);
		return ret;
	}
	
	public static boolean isActivityJoinable(int userId, Activity activity){
		boolean ret=false;
		do{
			if(userId==DataUtils.invalidId) break;
			if(activity==null) break;
			if(activity.getStatus()!=Activity.StatusType.accepted) break;
			int activityId=activity.getId();
			UserActivityRelation.RelationType relation=queryRelationOfUserAndActivity(userId, activityId);
			if(relation!=null) break;
			ret=true;
		}while(false);
		return ret;
	}
	
	public static boolean isActivityJoinable(int userId, int activityId){
		boolean ret=false;
		do{
			if(userId==DataUtils.invalidId) break;
			Activity activity=queryActivityByActivityId(activityId);
			ret=isActivityJoinable(userId, activity);
		}while(false);
		return ret;
	}
	
	public static boolean joinActivity(int userId, int activityId){
		boolean ret=false;
		do{
			try{
				SQLHelper sqlHelper=new SQLHelper();
				java.util.Date date= new java.util.Date();
				Timestamp currentTime=new Timestamp(date.getTime());
				
				List<String> columnNames=new LinkedList<String>();
				columnNames.add(UserActivityRelationTable.activityIdKey);
				columnNames.add(UserActivityRelationTable.userIdKey);
				columnNames.add(UserActivityRelationTable.relationIdKey);
				columnNames.add(UserActivityRelationTable.generatedTimeKey);
				
				List<Object> columnValues=new LinkedList<Object>();
				columnValues.add(activityId);
				columnValues.add(userId);
				columnValues.add(UserActivityRelation.RelationType.applied.ordinal());
				columnValues.add(currentTime.toString());
				
				int lastRelationTableId=sqlHelper.insertToTableByColumns("UserActivityRelationTable", columnNames, columnValues);
				if(lastRelationTableId==SQLHelper.invalidId) break;
				
				ret=true;
			} catch(Exception e){
				System.out.println("SQLCommander.joinActivity:"+e.getMessage());
			}
		}while(false);
		return ret;
	}

	public static boolean uploadUserAvatar(BasicUser user, String imageAbsolutePath, String imageURL){
		boolean ret=false;
		do{
			SQLHelper sqlHelper=new SQLHelper();
			String imageTableName="Image";
			String userTableName="User";

			List<String> imageColumnNames=new LinkedList<String>();
			imageColumnNames.add(Image.absolutePathKey);
			imageColumnNames.add(Image.urlKey);

			List<Object> imageColumnValues=new LinkedList<Object>();
			imageColumnValues.add(imageAbsolutePath);
			imageColumnValues.add(imageURL);

			int lastImageId=sqlHelper.insertToTableByColumns(imageTableName, imageColumnNames, imageColumnValues);
			if(lastImageId==sqlHelper.invalidId) break;

			List<String> userColumnNames=new LinkedList<String>();
			userColumnNames.add(BasicUser.avatarKey);

			List<Object> userColumnValues=new LinkedList<Object>();
			userColumnValues.add(lastImageId);

			List<String> userWhereClauses=new LinkedList<String>();
			userWhereClauses.add(BasicUser.idKey+"="+user.getUserId());

			boolean result=sqlHelper.updateTableByColumnsAndWhereClauses(userTableName, userColumnNames, userColumnValues, userWhereClauses, SQLHelper.logicAND);
			if(result==false) break;

			ret=true;
		}while(false);
		return ret;
	}

	public static Image queryImageByImageId(int imageId){
		Image image=null;
		do{
			SQLHelper sqlHelper=new SQLHelper();
			List<String> columnNames=new LinkedList<String>();
			columnNames.add(Image.urlKey);
			columnNames.add(Image.absolutePathKey);

			List<String> whereClauses=new LinkedList<String>();
			whereClauses.add(Image.idKey+"="+SQLHelper.convertToQueryValue(imageId));

			List<JSONObject> images=sqlHelper.queryTableByColumnsAndWhereClauses("Image", columnNames, whereClauses, SQLHelper.logicAND);
		}while(false);
		return image;
	}
};