package controllers;
import model.*;

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
 		do{
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
			columnNames.add(BasicUser.avatarKey);

			whereClauses.add(BasicUser.idKey+"="+SQLHelper.convertToQueryValue(userId));
			String logicLink=SQLHelper.logicAND;
			
			SQLHelper sqlHelper=new SQLHelper();
	 		List<JSONObject> results=sqlHelper.queryTableByColumnsAndWhereClauses(tableName, columnNames, whereClauses, logicLink);
			if(results==null || results.size()<=0) break;
			try{
	            Iterator<JSONObject> it=results.iterator();
		        if(it.hasNext()){
			        JSONObject userJson=(JSONObject)it.next();
	        		String email=(String)userJson.get(BasicUser.emailKey);
		      		String password=(String)userJson.get(BasicUser.passwordKey);
		      		String name=(String)userJson.get(BasicUser.nameKey);
		      		Integer userGroupId=(Integer)userJson.get(BasicUser.groupIdKey);
		      		UserGroup.GroupType userGroup=UserGroup.GroupType.getTypeForValue(userGroupId);
		      		Integer avatar=(Integer)userJson.get(BasicUser.avatarKey);

          		    user=BasicUser.create(userId, email, password, name, userGroup, avatar);
				} 
			} catch (Exception e) {
				    	
		    } 
		} while(false);
		return user;
	}
 	
 	public static BasicUser queryUserByEmail(String email){
 
 		BasicUser user=null;
 		do{
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
			columnNames.add(BasicUser.avatarKey);

			whereClauses.add(BasicUser.emailKey+"="+SQLHelper.convertToQueryValue(email));
			String logicLink=SQLHelper.logicAND;
			
			SQLHelper sqlHelper=new SQLHelper();
	 		List<JSONObject> results=sqlHelper.queryTableByColumnsAndWhereClauses(tableName, columnNames, whereClauses, logicLink); 			
	 	    if(results==null || results.size()<=0) break;
            try{		 
	            Iterator<JSONObject> it=results.iterator();
		        if(it.hasNext()){
			        JSONObject userJson=(JSONObject)it.next();
			       	int userId=(Integer)userJson.get(BasicUser.idKey);
		      		String password=(String)userJson.get(BasicUser.passwordKey);
		      		String name=(String)userJson.get(BasicUser.nameKey);
		      		Integer userGroupId=(Integer)userJson.get(BasicUser.groupIdKey);
		      		UserGroup.GroupType userGroup=UserGroup.GroupType.getTypeForValue(userGroupId);
		      		Integer avatar=(Integer)userJson.get(BasicUser.avatarKey);

          		    user=BasicUser.create(userId, email, password, name, userGroup, avatar);
				}
			} catch (Exception e) {
				    	
	        }
		} while(false);
 		return user;
 	}

	public static int registerUser(BasicUser user){
		int lastInsertedId=invalidId;
		
		// DAO
		SQLHelper sqlHelper=new SQLHelper();
		
		List<String> columnNames=new LinkedList<String>();
		columnNames.add(BasicUser.emailKey);
		columnNames.add(BasicUser.passwordKey);
		columnNames.add(BasicUser.nameKey);
		columnNames.add(BasicUser.groupIdKey);
		
		List<Object> columnValues=new LinkedList<Object>();
		columnValues.add(user.getEmail());
		columnValues.add(user.getPassword());
		columnValues.add(user.getName());
		columnValues.add(user.getUserGroup().ordinal());
		
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
				columnValues.add(activity.getEndDate());
				columnValues.add(activity.getCapacity());
				
				List<String> whereClauses=new LinkedList<String>();
				whereClauses.add(Activity.idKey+"="+SQLHelper.convertToQueryValue(activityId));
				ret=sqlHelper.updateTableByColumnsAndWhereClauses(tableName, columnNames, columnValues, whereClauses, SQLHelper.logicAND);
			
			} catch(Exception e){
				System.out.println("SQLCommander.updateActivity:"+e.getMessage());
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
				columnNames.add(Activity.titleKey);
				columnNames.add(Activity.contentKey);
				columnNames.add(Activity.createdTimeKey);
				columnNames.add(Activity.beginDateKey);
				columnNames.add(Activity.endDateKey);
				columnNames.add(Activity.capacityKey);

				List<Object> columnValues=new LinkedList<Object>();
				columnValues.add(Activity.StatusType.pending.ordinal());
				columnValues.add(activity.getTitle());
				columnValues.add(activity.getContent());
				columnValues.add(activity.getCreatedTime().toString());
				columnValues.add(activity.getBeginDate().toString());
				columnValues.add(activity.getEndDate());
				columnValues.add(activity.getCapacity());
				
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
		do{
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
			if(results==null || results.size()<=0) break;
			try{
	            Iterator<JSONObject> it=results.iterator();
		        if(it.hasNext()){
			        JSONObject jsonObject=(JSONObject)it.next();
		      		String title=(String)jsonObject.get(Activity.titleKey);
		      		String content=(String)jsonObject.get(Activity.contentKey);
		      		Timestamp createdTime=(Timestamp)jsonObject.get(Activity.createdTimeKey);
		      		Timestamp beginDate=(Timestamp)jsonObject.get(Activity.beginDateKey);
		      		Timestamp endDate=(Timestamp)jsonObject.get(Activity.endDateKey);
		      		int capacity=(Integer)jsonObject.get(Activity.capacityKey);
		      		Activity.StatusType status=Activity.StatusType.getTypeForValue((Integer)jsonObject.get(Activity.statusKey));
		      		activity=new Activity(activityId, title, content, createdTime, beginDate, endDate, capacity, status);
				}  	
			} catch (Exception e) {
				System.out.println("SQLCommander.queryActivityByActivityId:"+e.getMessage());
	        }
		} while(false);
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
			if(relationTableRecords==null || relationTableRecords.size()<=0) break;

			List<Integer> activityIds=new LinkedList<Integer>();
			Iterator<JSONObject> itRecord=relationTableRecords.iterator();
			while(itRecord.hasNext()){
				JSONObject record=itRecord.next();
				Integer activityId=(Integer)record.get(UserActivityRelationTable.activityIdKey);
				activityIds.add(activityId);
			}
				
			if(activityIds==null || activityIds.size()<=0) break;
			
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

	public static List<JSONObject> queryAcceptedActivitiesInChronologicalOrder(){
		List<JSONObject> records=null;

		try{
			records=SQLCommander.queryActivitiesByStatusAndChronologicalOrder(Activity.StatusType.accepted);
		} catch(Exception e){
			System.out.println("SQLCommander.queryAcceptedActivitiesInChronologicalOrder: "+e.getMessage());
		}
		return records;
	}
	
	public static List<JSONObject> queryPendingActivitiesInChronologicalOrder(){
		List<JSONObject> records=null;
		do{
			try{
				records=SQLCommander.queryActivitiesByStatusAndChronologicalOrder(Activity.StatusType.pending);
			} catch(Exception e){
				System.out.println("SQLCommander.queryPendingActivitiesInChronologicalOrder: "+e.getMessage());
			}
		}while(false);
		return records;
	}
	
	public static List<JSONObject> queryAcceptedActivitiesInChronologicalOrderByUser(int userId){
		List<JSONObject> records=null;
		do{
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
				if(activityRecords==null || activityRecords.size()<=0) break;

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
				System.out.println("SQLCommander.queryAcceptedActivitiesInChronologicalOrderByUser: "+e.getMessage());
			}
		}while(false);
		return records;
	}

	
	public static List<JSONObject> queryActivitiesByStatusAndChronologicalOrder(Activity.StatusType status){
		List<JSONObject> records=null;
		do{
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
			whereClauses.add(Activity.statusKey+"="+status.ordinal());

			List<String> orderClauses=new LinkedList<String>();
			orderClauses.add(Activity.createdTimeKey);
			records=sqlHelper.queryTableByColumnsAndWhereClausesAndOrderClauses(tableName, columnNames, whereClauses, SQLHelper.logicAND, orderClauses, SQLHelper.directionDescend);

		} catch(Exception e){
			System.out.println("SQLCommander.queryActivitiesByStatusAndChronologicalOrder: "+e.getMessage());
		}
		}while(false);
		return records;
	}
	
	public static UserActivityRelation.RelationType queryRelationOfUserAndActivity(int userId, int activityId){
		String tableName="UserActivityRelationTable";
		UserActivityRelation.RelationType ret=null; 
		do{
			try{
				SQLHelper sqlHelper=new SQLHelper();
				// query table UserActivityRelationTable 
				List<String> relationColumnNames=new LinkedList<String>();
				relationColumnNames.add(UserActivityRelationTable.relationIdKey);

				List<String> relationWhereClauses=new LinkedList<String>();
				relationWhereClauses.add(UserActivityRelationTable.userIdKey+"="+userId);
				relationWhereClauses.add(UserActivityRelationTable.activityIdKey+"="+activityId);
			
				List<JSONObject> relationTableRecords=sqlHelper.queryTableByColumnsAndWhereClauses(tableName, relationColumnNames, relationWhereClauses, SQLHelper.logicAND);
				if(relationTableRecords==null || relationTableRecords.size()<=0) break;

				Iterator<JSONObject> itRecord=relationTableRecords.iterator();
				if(itRecord.hasNext()){
					JSONObject record=itRecord.next();
					Integer relationId=(Integer)record.get(UserActivityRelationTable.relationIdKey);
					ret=UserActivityRelation.RelationType.getTypeForValue(relationId);
				}
			} catch(Exception e){
				System.out.println("SQLCommander.queryRelationOfUserAndActivity:"+e.getMessage());
			}
		}while(false);
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
	
	public static boolean validateAdminAccess(BasicUser user){
		boolean ret=false;
		do{
			if(user==null) break;
			if(user.getUserGroup()!=UserGroup.GroupType.admin) break;
			ret=true;
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
	
	public static boolean acceptActivity(BasicUser user, Activity activity){
		boolean ret=false;
		do{
			if(user==null) break;
			if(activity==null) break;
			if(validateAdminAccess(user)==false) break;
			try{
				SQLHelper sqlHelper=new SQLHelper();
				String activityTableName="Activity";
			
				List<String> columnNames=new LinkedList<String>();
				columnNames.add(Activity.statusKey);
				List<Object> columnValues=new LinkedList<Object>();
				columnValues.add(Activity.StatusType.accepted.ordinal());
				List<String> whereClauses=new LinkedList<String>();
				whereClauses.add(Activity.idKey+"="+SQLHelper.convertToQueryValue(activity.getId()));
			
				ret=sqlHelper.updateTableByColumnsAndWhereClauses(activityTableName, columnNames, columnValues, whereClauses, SQLHelper.logicAND); 
			} catch(Exception e){
				System.out.println("SQLCommander.acceptActivity: "+e.getMessage());
			}
		}while(false);
		return ret;
	}

	public static int uploadUserAvatar(BasicUser user, String imageAbsolutePath, String imageURL){
		int lastImageId=invalidId;
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

			lastImageId=sqlHelper.insertToTableByColumns(imageTableName, imageColumnNames, imageColumnValues);
			if(lastImageId==SQLHelper.invalidId) break;

			List<String> userColumnNames=new LinkedList<String>();
			userColumnNames.add(BasicUser.avatarKey);

			List<Object> userColumnValues=new LinkedList<Object>();
			userColumnValues.add(lastImageId);

			List<String> userWhereClauses=new LinkedList<String>();
			userWhereClauses.add(BasicUser.idKey+"="+user.getUserId());

			boolean updateResult=sqlHelper.updateTableByColumnsAndWhereClauses(userTableName, userColumnNames, userColumnValues, userWhereClauses, SQLHelper.logicAND);
			if(updateResult==false){
				boolean isRecovered=deleteImageRecordById(lastImageId);
				if(isRecovered==true){
					System.out.println("SQLCommander.uploadUserAvatar: image "+lastImageId+ " reverted");
				}
				break;
			}

		}while(false);
		return lastImageId;
	}

	public static Image queryImageByImageId(int imageId){
		Image image=null;
		do{
			try{
				SQLHelper sqlHelper=new SQLHelper();
				List<String> columnNames=new LinkedList<String>();
				columnNames.add(Image.urlKey);
				columnNames.add(Image.absolutePathKey);

				List<String> whereClauses=new LinkedList<String>();
				whereClauses.add(Image.idKey+"="+SQLHelper.convertToQueryValue(imageId));

				List<JSONObject> images=sqlHelper.queryTableByColumnsAndWhereClauses("Image", columnNames, whereClauses, SQLHelper.logicAND);
				if(images==null || images.size()<=0) break;
				Iterator<JSONObject> itImage=images.iterator();
				if(itImage.hasNext()){
					JSONObject imageJson=itImage.next();
					String imageAbsolutePath=(String)imageJson.get(Image.absolutePathKey);
					String imageURL=(String)imageJson.get(Image.urlKey);
					image=Image.create(imageId, imageAbsolutePath, imageURL);
				}

			} catch (Exception e){
				System.out.println("SQLCommander.queryImageByImageId:"+e.getMessage());
			}
		}while(false);
		return image;
	}

	public static boolean deleteImageRecordById(int imageId){
		boolean ret=false;
		do{
			String imageTableName="Image";
			try{
				SQLHelper sqlHelper=new SQLHelper();
				List<String> whereClauses=new LinkedList<String>();
				whereClauses.add(Image.idKey+"="+SQLHelper.convertToQueryValue(imageId));
				ret=sqlHelper.deleteFromTableByWhereClauses(imageTableName, whereClauses, SQLHelper.logicAND);
			} catch (Exception e){
				System.out.println("SQLCommander.deleteImageRecordById"+e.getMessage());
			}
		}while(false);
		return ret;
	}

	public static boolean deleteImageRecordOfActivityById(int imageId, int activityId){
		boolean ret=false;
		do{
			try{
				SQLHelper sqlHelper=new SQLHelper();
				String relationTableName="ActivityImageRelationTable";
				List<String> relationWhereClauses=new LinkedList<String>();
				relationWhereClauses.add(Activity.idKey+"="+SQLHelper.convertToQueryValue(activityId));
				relationWhereClauses.add(Image.idKey+"="+SQLHelper.convertToQueryValue(imageId));
				boolean resultRelationDeletion=sqlHelper.deleteFromTableByWhereClauses(relationTableName, relationWhereClauses, SQLHelper.logicAND);
				
				if(resultRelationDeletion==false) break;
				
				String imageTableName="Image";
				List<String> imageWhereClauses=new LinkedList<String>();
				imageWhereClauses.add(Image.idKey+"="+SQLHelper.convertToQueryValue(imageId));
				ret=sqlHelper.deleteFromTableByWhereClauses(imageTableName, imageWhereClauses, SQLHelper.logicAND);
				
			} catch(Exception e){
				System.out.println("SQLCommander.deleteImageRecordOfActivityById:"+e.getMessage());
			}
		}while(false);

		return ret;
	}

	public static List<Integer> queryImageIdsByActivityId(int activityId){
		List<Integer> imageIds=null;
		do{
			String relationTableName="ActivityImageRelationTable";
			SQLHelper sqlHelper=new SQLHelper();
			List<String> relationColumnNames=new LinkedList<String>();
			relationColumnNames.add(Image.idKey);
			List<String> relationWhereClauses=new LinkedList<String>();
			relationWhereClauses.add(Activity.idKey+"="+SQLHelper.convertToQueryValue(activityId));
			List<JSONObject> relationRecords=sqlHelper.queryTableByColumnsAndWhereClauses(relationTableName, relationColumnNames, relationWhereClauses, SQLHelper.logicAND);
			if(relationRecords==null || relationRecords.size()<=0) break;

			imageIds=new LinkedList<Integer>();
			Iterator<JSONObject> itRelationRecord=relationRecords.iterator();
			while(itRelationRecord.hasNext()){
				JSONObject relationRecord=itRelationRecord.next();
				Integer imageId=(Integer)relationRecord.get(Image.idKey);
				imageIds.add(imageId);
			}
			
		}while(false);
		return imageIds;
	}
	
	public static List<Image> queryImagesByActivityId(int activityId){
		List<Image> images=null;
		
		do{
			String relationTableName="ActivityImageRelationTable";
			SQLHelper sqlHelper=new SQLHelper();
			List<String> relationColumnNames=new LinkedList<String>();
			relationColumnNames.add(Image.idKey);
			List<String> relationWhereClauses=new LinkedList<String>();
			relationWhereClauses.add(Activity.idKey+"="+SQLHelper.convertToQueryValue(activityId));
			List<JSONObject> relationRecords=sqlHelper.queryTableByColumnsAndWhereClauses(relationTableName, relationColumnNames, relationWhereClauses, SQLHelper.logicAND);
			if(relationRecords==null || relationRecords.size()<=0) break;

			String imageTableName="Image";
			List<String> imageColumnNames=new LinkedList<String>();
			imageColumnNames.add(Image.idKey);
			imageColumnNames.add(Image.absolutePathKey);
			imageColumnNames.add(Image.urlKey);

			List<String> imageWhereClauses=new LinkedList<String>();
			
			Iterator<JSONObject> itRelationRecord=relationRecords.iterator();
			while(itRelationRecord.hasNext()){
				JSONObject relationRecord=itRelationRecord.next();
				Integer imageId=(Integer)relationRecord.get(Image.idKey);
				imageWhereClauses.add(Image.idKey+"="+SQLHelper.convertToQueryValue(imageId));
			}

			List<JSONObject> imageRecords=sqlHelper.queryTableByColumnsAndWhereClauses(imageTableName, imageColumnNames, imageWhereClauses, SQLHelper.logicOR);
			if(imageRecords==null || imageRecords.size()<=0) break;

			images=new LinkedList<Image>();
			Iterator<JSONObject> itImageRecord=imageRecords.iterator();
			while(itImageRecord.hasNext()){
				JSONObject imageRecord=itImageRecord.next();
				Integer imageId=(Integer)imageRecord.get(Image.idKey);
				String imageAbsolutePath=(String)imageRecord.get(Image.absolutePathKey);
				String imageURL=(String)imageRecord.get(Image.urlKey);
				Image image=Image.create(imageId, imageAbsolutePath, imageURL);
				images.add(image);
			}

		}while(false);
		return images;
	}
	
	public static int uploadImageOfActivity(BasicUser user, Activity activity, String imageAbsolutePath, String imageURL){
		int lastImageId=invalidId;
		do{
			if(user==null) break;
			if(activity==null) break;
			SQLHelper sqlHelper=new SQLHelper();
			String imageTableName="Image";
			String relationTableName="ActivityImageRelationTable";

			List<String> imageColumnNames=new LinkedList<String>();
			imageColumnNames.add(Image.absolutePathKey);
			imageColumnNames.add(Image.urlKey);

			List<Object> imageColumnValues=new LinkedList<Object>();
			imageColumnValues.add(imageAbsolutePath);
			imageColumnValues.add(imageURL);

			lastImageId=sqlHelper.insertToTableByColumns(imageTableName, imageColumnNames, imageColumnValues);
			if(lastImageId==SQLHelper.invalidId) break;

			List<String> relationTableColumnNames=new LinkedList<String>();
			relationTableColumnNames.add(Activity.idKey);
			relationTableColumnNames.add(Image.idKey);

			List<Object> relationTableColumnValues=new LinkedList<Object>();
			relationTableColumnValues.add(activity.getId());
			relationTableColumnValues.add(lastImageId);

			int lastRecordId=sqlHelper.insertToTableByColumns(relationTableName, relationTableColumnNames, relationTableColumnValues);
			if(lastRecordId==SQLHelper.invalidId){
				boolean isRecovered=deleteImageRecordById(lastImageId);
				if(isRecovered==true){
					System.out.println("SQLCommander.uploadImageOfActivity: image "+lastImageId+ " reverted");
				}
				break;
			}

		}while(false);
		return lastImageId;

	}
};