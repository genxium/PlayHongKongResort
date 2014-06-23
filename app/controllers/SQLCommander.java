package controllers;

import com.google.common.collect.ImmutableMap;
import dao.SQLHelper;
import model.*;
import org.json.simple.JSONObject;
import utilities.DataUtils;

import java.sql.Timestamp;
import java.util.*;

public class SQLCommander {

	public static Integer INVALID =(-1);
    public static String INITIAL_REF_INDEX = "0";
    public static Integer DIRECTION_FORWARD = (+1);
    public static Integer DIRECTION_BACKWARD = (-1);

 	public static User queryUser(Integer userId){

 		User user=null;
 		do{
	 		String tableName="User";
	 		List<String> columnNames=new LinkedList<String>();
	 		List<String> whereClauses=new LinkedList<String>();

	 		columnNames.add(User.EMAIL);
	 		columnNames.add(User.PASSWORD);
	 		columnNames.add(User.NAME);
	 		columnNames.add(User.GROUP_ID);
			columnNames.add(User.AUTHENTICATION_STATUS);
			columnNames.add(User.GENDER);
			columnNames.add(User.LAST_LOGGED_IN_TIME);
			columnNames.add(User.AVATAR);

			whereClauses.add(User.ID +"="+SQLHelper.convertToQueryValue(userId));
			String logicLink=SQLHelper.AND;

			SQLHelper sqlHelper=new SQLHelper();
	 		List<JSONObject> results=sqlHelper.query(tableName, columnNames, whereClauses, logicLink);
			if(results==null || results.size()<=0) break;
			try{
	            Iterator<JSONObject> it=results.iterator();
		        if(it.hasNext()){
			        JSONObject userJson=(JSONObject)it.next();
	        		String email=(String)userJson.get(User.EMAIL);
		      		String password=(String)userJson.get(User.PASSWORD);
		      		String name=(String)userJson.get(User.NAME);
		      		Integer userGroupId=(Integer)userJson.get(User.GROUP_ID);
		      		UserGroup.GroupType userGroup=UserGroup.GroupType.getTypeForValue(userGroupId);
		      		Integer avatar=(Integer)userJson.get(User.AVATAR);

          		    user=User.create(userId, email, password, name, userGroup, avatar);
				}
			} catch (Exception e) {

		    }
		} while(false);
		return user;
	}

 	public static User queryUserByEmail(String email){

 		User user=null;
 		do{
	 		String tableName="User";

	 		List<String> columnNames=new LinkedList<String>();
	 		List<String> whereClauses=new LinkedList<String>();

	 		columnNames.add(User.ID);
	 		columnNames.add(User.PASSWORD);
	 		columnNames.add(User.NAME);
	 		columnNames.add(User.GROUP_ID);
			columnNames.add(User.AUTHENTICATION_STATUS);
			columnNames.add(User.GENDER);
			columnNames.add(User.LAST_LOGGED_IN_TIME);
			columnNames.add(User.AVATAR);

			whereClauses.add(User.EMAIL +"="+SQLHelper.convertToQueryValue(email));
			String logicLink=SQLHelper.AND;

			SQLHelper sqlHelper=new SQLHelper();
	 		List<JSONObject> results=sqlHelper.query(tableName, columnNames, whereClauses, logicLink);
	 	    if(results==null || results.size()<=0) break;
            try{
	            Iterator<JSONObject> it=results.iterator();
		        if(it.hasNext()){
			        JSONObject userJson=(JSONObject)it.next();
			       	int userId=(Integer)userJson.get(User.ID);
		      		String password=(String)userJson.get(User.PASSWORD);
		      		String name=(String)userJson.get(User.NAME);
		      		Integer userGroupId=(Integer)userJson.get(User.GROUP_ID);
		      		UserGroup.GroupType userGroup=UserGroup.GroupType.getTypeForValue(userGroupId);
		      		Integer avatar=(Integer)userJson.get(User.AVATAR);

          		    user=User.create(userId, email, password, name, userGroup, avatar);
				}
			} catch (Exception e) {

	        }
		} while(false);
 		return user;
 	}

	public static int registerUser(User user){
		int lastInsertedId= INVALID;

		// DAO
		SQLHelper sqlHelper=new SQLHelper();

		List<String> columnNames=new LinkedList<String>();
		columnNames.add(User.EMAIL);
		columnNames.add(User.PASSWORD);
		columnNames.add(User.NAME);
		columnNames.add(User.GROUP_ID);

		List<Object> columnValues=new LinkedList<Object>();
		columnValues.add(user.getEmail());
		columnValues.add(user.getPassword());
		columnValues.add(user.getName());
		columnValues.add(user.getUserGroup().ordinal());

		try{
			lastInsertedId=sqlHelper.insert("User", columnNames, columnValues);
			sqlHelper=null;
		} catch (Exception e){

		}
		return lastInsertedId;
	}

    public static Integer createActivity(String title, String content, Integer userId){
        Integer ret=null;
        do{
            int lastActivityId= SQLHelper.INVALID_ID;

            SQLHelper sqlHelper=new SQLHelper();
            List<String> columnNames=new LinkedList<String>();

            columnNames.add(Activity.TITLE);
            columnNames.add(Activity.CONTENT);

            List<Object> columnValues=new LinkedList<Object>();

            columnValues.add(title);
            columnValues.add(content);

            int tmpLastActivityId=sqlHelper.insert("Activity", columnNames, columnValues);
            if(tmpLastActivityId!=SQLHelper.INVALID_ID){
                columnNames.clear();
                columnValues.clear();

                columnNames.add(UserActivityRelationTable.ACTIVITY_ID);
                columnNames.add(UserActivityRelationTable.USER_ID);
                columnNames.add(UserActivityRelationTable.RELATION_ID);

                columnValues.add(tmpLastActivityId);
                columnValues.add(userId);
                columnValues.add(UserActivityRelationTable.hosted);

                int lastRelationTableId=sqlHelper.insert("UserActivityRelationTable", columnNames, columnValues);
                if(lastRelationTableId==SQLHelper.INVALID_ID) break;

                lastActivityId=tmpLastActivityId;
            }

            if(lastActivityId==SQLHelper.INVALID_ID) break;
            ret=lastActivityId;

        }while (false);
        return ret;
    }

	public static boolean updateActivity(Activity activity){
		boolean ret=false;
		do{
			String tableName="Activity";
			int activityId=activity.getId();

			try{
				SQLHelper sqlHelper=new SQLHelper();
				List<String> columnNames=new LinkedList<String>();

				columnNames.add(Activity.TITLE);
				columnNames.add(Activity.CONTENT);
				columnNames.add(Activity.CREATED_TIME);
				columnNames.add(Activity.BEGIN_TIME);
				columnNames.add(Activity.DEADLINE);
				columnNames.add(Activity.CAPACITY);

				List<Object> columnValues=new LinkedList<>();
				columnValues.add(activity.getTitle());
				columnValues.add(activity.getContent());
				columnValues.add(activity.getCreatedTime().toString());
				columnValues.add(activity.getBeginTime().toString());
				columnValues.add(activity.getDeadline().toString());
				columnValues.add(activity.getCapacity());

				List<String> whereClauses=new LinkedList<>();
				whereClauses.add(Activity.ID +"="+SQLHelper.convertToQueryValue(activityId));
				ret=sqlHelper.update(tableName, columnNames, columnValues, whereClauses, SQLHelper.AND);

			} catch(Exception e){
				System.out.println("SQLCommander.updateActivity: "+e.getMessage());
			}
		}while(false);
		return ret;
	}

	public static boolean createUserActivityRelation(int activityId, User user, int relation){

		boolean bRet=false;

		int userId=user.getUserId();

		SQLHelper sqlHelper=new SQLHelper();

		List<String> columnNames=new LinkedList<String>();

		columnNames.add(UserActivityRelationTable.USER_ID);
		columnNames.add(UserActivityRelationTable.ACTIVITY_ID);
		columnNames.add(UserActivityRelationTable.RELATION_ID);

		List<Object> columnValues=new LinkedList<Object>();

		columnValues.add(userId);
		columnValues.add(activityId);
		columnValues.add(relation);

		try{
			int lastId=sqlHelper.insert("UserActivityRelationTable", columnNames, columnValues);
			if(lastId!= INVALID){
				bRet=true;
			}
		} catch(Exception e){

		}
		return bRet;
	}

	/* querying activities */
	public static Activity queryActivity(int activityId){

		Activity activity=null;
		do{
	 		String tableName="Activity";

	 		List<String> columnNames=new LinkedList<String>();
            columnNames.add(Activity.ID);
			columnNames.add(Activity.TITLE);
			columnNames.add(Activity.CONTENT);
			columnNames.add(Activity.CREATED_TIME);
			columnNames.add(Activity.BEGIN_TIME);
			columnNames.add(Activity.DEADLINE);
			columnNames.add(Activity.CAPACITY);
			columnNames.add(Activity.STATUS);

			List<String> whereClauses=new LinkedList<String>();
			whereClauses.add(Activity.ID +"="+activityId);

			SQLHelper sqlHelper=new SQLHelper();
			List<JSONObject> results=sqlHelper.query(tableName, columnNames, whereClauses, SQLHelper.AND);
			if(results==null || results.size()<=0) break;
			try{
	            Iterator<JSONObject> it=results.iterator();
		        if(it.hasNext()){
			        JSONObject activityJson=(JSONObject)it.next();
		      		activity=new Activity(activityJson);
				}
			} catch (Exception e) {
				System.out.println("SQLCommander.queryActivity:"+e.getMessage());
	        }
		} while(false);
		return activity;
	}

	public static ActivityDetail queryActivityDetail(int activityId){
		ActivityDetail activityDetail=null;
		do{
			Activity activity= queryActivity(activityId);
			List<Image> images=queryImagesByActivityId(activityId);
			List<BasicUser> appliedParticipants=SQLCommander.queryUsersByActivityIdAndRelation(activityId, UserActivityRelationTable.applied);
			List<BasicUser> selectedParticipants=SQLCommander.queryUsersByActivityIdAndRelation(activityId, UserActivityRelationTable.selected);

			activityDetail=new ActivityDetail(activity, images, appliedParticipants, selectedParticipants);

		}while(false);
		return activityDetail;
	}

	public static List<Activity> queryActivities(Integer userId, int relation){
		List<Activity> ret=null;
		do{
			SQLHelper sqlHelper=new SQLHelper();
			
			// query table Activity
			List<String> activityColumnNames=new LinkedList<String>();
			activityColumnNames.add(Activity.ID);
			activityColumnNames.add(Activity.TITLE);
			activityColumnNames.add(Activity.CONTENT);
			activityColumnNames.add(Activity.CREATED_TIME);
			activityColumnNames.add(Activity.BEGIN_TIME);
			activityColumnNames.add(Activity.DEADLINE);
			activityColumnNames.add(Activity.CAPACITY);
			activityColumnNames.add(Activity.STATUS);

			List<String> activityWhereClauses=new LinkedList<>();
			activityWhereClauses.add("EXISTS (SELECT NULL FROM UserActivityRelationTable WHERE "+
										UserActivityRelationTable.USER_ID+"="+userId+" AND "+
										UserActivityRelationTable.RELATION_ID+"="+relation+" AND "+
										UserActivityRelationTable.TABLE+"."+UserActivityRelationTable.ACTIVITY_ID+"="+Activity.TABLE+"."+Activity.ID+
									")");

            List<JSONObject> activityJsons=sqlHelper.query("Activity", activityColumnNames, activityWhereClauses, SQLHelper.AND);
			if(activityJsons==null) break;
            ret=new ArrayList<>();
            for(JSONObject activityJson : activityJsons){
                ret.add(new Activity(activityJson));
            }

		}while(false);
		return ret;
	}

    public static List<Activity> queryActivities(String refIndex, String orderKey, String orderDirection, Integer numItems, Integer direction, Activity.StatusType status){
        List<Activity> ret=null;
        do{
            try{
                String tableName="Activity";
                SQLHelper sqlHelper=new SQLHelper();

                // query table Activity
                List<String> columnNames=new LinkedList<String>();
                columnNames.add(Activity.ID);
                columnNames.add(Activity.TITLE);
                columnNames.add(Activity.CONTENT);
                columnNames.add(Activity.CREATED_TIME);
                columnNames.add(Activity.BEGIN_TIME);
                columnNames.add(Activity.DEADLINE);
                columnNames.add(Activity.CAPACITY);
                columnNames.add(Activity.STATUS);
                List<String> whereClauses=new LinkedList<String>();
                whereClauses.add(Activity.STATUS +"="+status.ordinal());

                List<String> orderClauses=new LinkedList<String>();
                orderClauses.add(orderKey);

                List<String> orderDirections=new LinkedList<String>();
                orderDirections.add(orderDirection);

                if(refIndex.equals(INITIAL_REF_INDEX)){
                    whereClauses.add(orderKey+">="+SQLHelper.convertToQueryValue(Integer.valueOf(INITIAL_REF_INDEX)));
                } else if(direction== DIRECTION_FORWARD){
                    whereClauses.add(orderKey+">"+SQLHelper.convertToQueryValue(refIndex));
                } else{
                    whereClauses.add(orderKey+"<"+SQLHelper.convertToQueryValue(refIndex));
                }

                List<Integer> limits=new ArrayList<Integer>();
                limits.add(numItems);

                List<JSONObject> activitiesJson=sqlHelper.query(tableName, columnNames, whereClauses, SQLHelper.AND, orderClauses, orderDirections, limits);
                if(activitiesJson==null) break;

                ret=new ArrayList<Activity>();
                for(JSONObject activityJson : activitiesJson){
                    ret.add(new Activity(activityJson));
                }

            } catch(Exception e){
                System.out.println("SQLCommander.queryActivities: "+e.getMessage());
            }
        }while(false);
        return ret;
    }
	public static int queryRelationOfUserIdAndActivity(int userId, int activityId){
		String tableName="UserActivityRelationTable";
		int ret=UserActivityRelationTable.invalid;
		do{
			try{
				SQLHelper sqlHelper=new SQLHelper();
				// query table UserActivityRelationTable
				List<String> relationColumnNames=new LinkedList<String>();
				relationColumnNames.add(UserActivityRelationTable.RELATION_ID);

				List<String> relationWhereClauses=new LinkedList<String>();
				relationWhereClauses.add(UserActivityRelationTable.USER_ID +"="+userId);
				relationWhereClauses.add(UserActivityRelationTable.ACTIVITY_ID +"="+activityId);

				List<JSONObject> relationTableRecords=sqlHelper.query(tableName, relationColumnNames, relationWhereClauses, SQLHelper.AND);
				if(relationTableRecords==null || relationTableRecords.size()<=0) break;

				Iterator<JSONObject> itRecord=relationTableRecords.iterator();
				if(itRecord.hasNext()){
					JSONObject record=itRecord.next();
					Integer relation=(Integer)record.get(UserActivityRelationTable.RELATION_ID);
					ret=relation;
				}
			} catch(Exception e){
				System.out.println("SQLCommander.queryRelationOfUserIdAndActivity:"+e.getMessage());
			}
		}while(false);
		return ret;
	}

	public static CommentOnActivity queryComment(Integer commentId){
        CommentOnActivity ret=null;
        do{
            try{
                String tableName="CommentOnActivity";
                SQLHelper sqlHelper=new SQLHelper();
                
                // query table CommentOnActivity
                List<String> columnNames=new LinkedList<String>();
                columnNames.add(CommentOnActivity.ID);
                columnNames.add(CommentOnActivity.CONTENT);
                columnNames.add(CommentOnActivity.COMMENTER_ID);
                columnNames.add(CommentOnActivity.PARENT_ID);
                columnNames.add(CommentOnActivity.PREDECESSOR_ID);
                columnNames.add(CommentOnActivity.ACTIVITY_ID);
                columnNames.add(CommentOnActivity.COMMENT_TYPE);
                columnNames.add(CommentOnActivity.GENERATED_TIME);

                List<String> whereClauses=new LinkedList<String>();
                whereClauses.add(CommentOnActivity.ID+"="+commentId);
			
                List<JSONObject> commentsJson=sqlHelper.query(tableName, columnNames, whereClauses, SQLHelper.AND);
                if(commentsJson==null || commentsJson.size()<=0) break;

				ret=new CommentOnActivity(commentsJson.get(0));
			} catch(Exception e){
				
			}
		}while(false);
		return ret;
	}

    public static List<CommentOnActivity> queryTopLevelComments(Integer activityId, String refIndex, String orderKey, String orderDirection, Integer numItems, Integer direction, Integer commentType){
         
        List<CommentOnActivity> ret=null;
        do{
            try{
                String tableName="CommentOnActivity";
                SQLHelper sqlHelper=new SQLHelper();
				
                // query table CommentOnActivity
                List<String> columnNames=new LinkedList<String>();
                columnNames.add(CommentOnActivity.ID);
                columnNames.add(CommentOnActivity.CONTENT);
                columnNames.add(CommentOnActivity.COMMENTER_ID);
                columnNames.add(CommentOnActivity.PARENT_ID);
                columnNames.add(CommentOnActivity.PREDECESSOR_ID);
                columnNames.add(CommentOnActivity.ACTIVITY_ID);
                columnNames.add(CommentOnActivity.COMMENT_TYPE);
                columnNames.add(CommentOnActivity.GENERATED_TIME);

                List<String> whereClauses=new LinkedList<String>();
                whereClauses.add(CommentOnActivity.ACTIVITY_ID+"="+activityId);
                whereClauses.add(CommentOnActivity.COMMENT_TYPE+"="+commentType);
                whereClauses.add(CommentOnActivity.PARENT_ID+"="+INVALID.toString());

                List<String> orderClauses=new LinkedList<String>();
                orderClauses.add(orderKey);

                List<String> orderDirections=new LinkedList<String>();
                orderDirections.add(orderDirection);

                if(refIndex.equals(INITIAL_REF_INDEX)){
                    whereClauses.add(orderKey+">="+SQLHelper.convertToQueryValue(INITIAL_REF_INDEX));
                } else if(direction== DIRECTION_FORWARD){
                    whereClauses.add(orderKey+">"+SQLHelper.convertToQueryValue(refIndex));
                } else{
                    whereClauses.add(orderKey+"<"+SQLHelper.convertToQueryValue(refIndex));
                }

                List<Integer> limits=new ArrayList<Integer>();
                limits.add(numItems);

                List<JSONObject> commentsJson=sqlHelper.query(tableName, columnNames, whereClauses, SQLHelper.AND, orderClauses, orderDirections, limits);
                if(commentsJson==null) break;

                ret=new ArrayList<CommentOnActivity>();
                for(JSONObject commentJson : commentsJson){
                    ret.add(new CommentOnActivity(commentJson));
                }

            } catch(Exception e){

            }
        }while(false);
        return ret;
    }

    public static List<CommentOnActivity> querySubComments(Integer parentId, String refIndex, String orderKey, String orderDirection, Integer numItems, Integer direction, Integer commentType){
        List<CommentOnActivity> ret=null;
        do{
            try{
                String tableName="CommentOnActivity";
                SQLHelper sqlHelper=new SQLHelper();
                //
                // query table CommentOnActivity
                List<String> columnNames=new LinkedList<String>();
                columnNames.add(CommentOnActivity.ID);
                columnNames.add(CommentOnActivity.CONTENT);
                columnNames.add(CommentOnActivity.COMMENTER_ID);
                columnNames.add(CommentOnActivity.PARENT_ID);
                columnNames.add(CommentOnActivity.PREDECESSOR_ID);
                columnNames.add(CommentOnActivity.ACTIVITY_ID);
                columnNames.add(CommentOnActivity.COMMENT_TYPE);
                columnNames.add(CommentOnActivity.GENERATED_TIME);

                List<String> whereClauses=new LinkedList<String>();
                whereClauses.add(CommentOnActivity.PARENT_ID+"="+parentId);
                whereClauses.add(CommentOnActivity.COMMENT_TYPE+"="+commentType);

                List<String> orderClauses=new LinkedList<String>();
                orderClauses.add(orderKey);

                List<String> orderDirections=new LinkedList<String>();
                orderDirections.add(orderDirection);

                if(refIndex.equals(INITIAL_REF_INDEX)){
                    whereClauses.add(orderKey+">="+SQLHelper.convertToQueryValue(INITIAL_REF_INDEX));

                } else if(direction== DIRECTION_FORWARD){
                    whereClauses.add(orderKey+">"+SQLHelper.convertToQueryValue(refIndex));
                } else{
                    whereClauses.add(orderKey+"<"+SQLHelper.convertToQueryValue(refIndex));
                }

                List<Integer> limits=null;
                if(numItems!=null){
                    limits=new ArrayList<Integer>();
                    limits.add(numItems);
                }
                List<JSONObject> commentsJson=sqlHelper.query(tableName, columnNames, whereClauses, SQLHelper.AND, orderClauses, orderDirections, limits);
                if(commentsJson==null) break;

                ret=new ArrayList<CommentOnActivity>();
                for(JSONObject commentJson : commentsJson){
                    ret.add(new CommentOnActivity(commentJson));
                }

            } catch(Exception e){

            }
        }while(false);
        return ret;
    }

	public static boolean validateOwnershipOfActivity(int userId, int activityId){
		boolean ret=false;
		do{
			// validate host relation
			int relation=SQLCommander.queryRelationOfUserIdAndActivity(userId, activityId);
			if(relation==UserActivityRelationTable.invalid || relation!=UserActivityRelationTable.hosted) break;
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

	public static boolean validateAdminAccess(User user){
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
			Activity activity=SQLCommander.queryActivity(activityId);
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

	public static boolean isActivityJoinable(User user, Activity activity){
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
			int relation=queryRelationOfUserIdAndActivity(userId, activityId);
			if(relation==UserActivityRelationTable.invalid) break;
			ret=true;
		}while(false);
		return ret;
	}

	public static boolean isActivityJoinable(int userId, int activityId){
		boolean ret=false;
		do{
			if(userId==DataUtils.invalidId) break;
			Activity activity= queryActivity(activityId);
			ret=isActivityJoinable(userId, activity);
		}while(false);
		return ret;
	}

	public static boolean acceptActivity(User user, Activity activity){
		boolean ret=false;
		do{
			if(user==null) break;
			if(activity==null) break;
			if(validateAdminAccess(user)==false) break;
			try{
				SQLHelper sqlHelper=new SQLHelper();
				String activityTableName="Activity";

				List<String> columnNames=new LinkedList<String>();
				columnNames.add(Activity.STATUS);
				List<Object> columnValues=new LinkedList<Object>();
				columnValues.add(Activity.StatusType.accepted.ordinal());
				List<String> whereClauses=new LinkedList<String>();
				whereClauses.add(Activity.ID +"="+SQLHelper.convertToQueryValue(activity.getId()));

				ret=sqlHelper.update(activityTableName, columnNames, columnValues, whereClauses, SQLHelper.AND);
			} catch(Exception e){

			}
		}while(false);
		return ret;
	}

	public static boolean rejectActivity(User user, Activity activity){
		boolean ret=false;
		do{
			if(user==null) break;
			if(activity==null) break;
			if(validateAdminAccess(user)==false) break;
			try{
				SQLHelper sqlHelper=new SQLHelper();
				String activityTableName="Activity";

				List<String> columnNames=new LinkedList<String>();
				columnNames.add(Activity.STATUS);
				List<Object> columnValues=new LinkedList<Object>();
				columnValues.add(Activity.StatusType.rejected.ordinal());
				List<String> whereClauses=new LinkedList<String>();
				whereClauses.add(Activity.ID +"="+SQLHelper.convertToQueryValue(activity.getId()));

				ret=sqlHelper.update(activityTableName, columnNames, columnValues, whereClauses, SQLHelper.AND);
			} catch(Exception e){

			}
		}while(false);
		return ret;
	}

	public static int uploadUserAvatar(User user, String imageURL){
		int lastImageId= INVALID;
		do{
			SQLHelper sqlHelper=new SQLHelper();
			String imageTableName="Image";
			String userTableName="User";

			List<String> imageColumnNames=new LinkedList<String>();
			imageColumnNames.add(Image.URL);

			List<Object> imageColumnValues=new LinkedList<Object>();
			imageColumnValues.add(imageURL);

			lastImageId=sqlHelper.insert(imageTableName, imageColumnNames, imageColumnValues);
			if(lastImageId==SQLHelper.INVALID_ID) break;

			List<String> userColumnNames=new LinkedList<String>();
			userColumnNames.add(User.AVATAR);

			List<Object> userColumnValues=new LinkedList<Object>();
			userColumnValues.add(lastImageId);

			List<String> userWhereClauses=new LinkedList<String>();
			userWhereClauses.add(User.ID +"="+user.getUserId());

			boolean updateResult=sqlHelper.update(userTableName, userColumnNames, userColumnValues, userWhereClauses, SQLHelper.AND);
			if(updateResult==false){
				boolean isRecovered=deleteImageRecordById(lastImageId);
				if(isRecovered==true){

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
				columnNames.add(Image.URL);

				List<String> whereClauses=new LinkedList<String>();
				whereClauses.add(Image.ID +"="+SQLHelper.convertToQueryValue(imageId));

				List<JSONObject> images=sqlHelper.query("Image", columnNames, whereClauses, SQLHelper.AND);
				if(images==null || images.size()<=0) break;
				Iterator<JSONObject> itImage=images.iterator();
				if(itImage.hasNext()){
					JSONObject imageJson=itImage.next();
					image=new Image(imageJson);
				}
			} catch (Exception e){

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
				whereClauses.add(Image.ID +"="+SQLHelper.convertToQueryValue(imageId));
				ret=sqlHelper.delete(imageTableName, whereClauses, SQLHelper.AND);
			} catch (Exception e){

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
				relationWhereClauses.add(Activity.ID +"="+SQLHelper.convertToQueryValue(activityId));
				relationWhereClauses.add(Image.ID +"="+SQLHelper.convertToQueryValue(imageId));
				boolean resultRelationDeletion=sqlHelper.delete(relationTableName, relationWhereClauses, SQLHelper.AND);

				if(resultRelationDeletion==false) break;

				String imageTableName="Image";
				List<String> imageWhereClauses=new LinkedList<String>();
				imageWhereClauses.add(Image.ID +"="+SQLHelper.convertToQueryValue(imageId));
				ret=sqlHelper.delete(imageTableName, imageWhereClauses, SQLHelper.AND);

			} catch(Exception e){

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
			relationColumnNames.add(Image.ID);
			List<String> relationWhereClauses=new LinkedList<String>();
			relationWhereClauses.add(Activity.ID +"="+SQLHelper.convertToQueryValue(activityId));
			List<JSONObject> relationRecords=sqlHelper.query(relationTableName, relationColumnNames, relationWhereClauses, SQLHelper.AND);
			if(relationRecords==null || relationRecords.size()<=0) break;

			imageIds=new LinkedList<Integer>();
			Iterator<JSONObject> itRelationRecord=relationRecords.iterator();
			while(itRelationRecord.hasNext()){
				JSONObject relationRecord=itRelationRecord.next();
				Integer imageId=(Integer)relationRecord.get(Image.ID);
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
			relationColumnNames.add(Image.ID);
			List<String> relationWhereClauses=new LinkedList<String>();
			relationWhereClauses.add(Activity.ID +"="+SQLHelper.convertToQueryValue(activityId));
			List<JSONObject> relationRecords=sqlHelper.query(relationTableName, relationColumnNames, relationWhereClauses, SQLHelper.AND);
			if(relationRecords==null || relationRecords.size()<=0) break;

			String imageTableName="Image";
			List<String> imageColumnNames=new LinkedList<String>();
			imageColumnNames.add(Image.ID);
			imageColumnNames.add(Image.URL);

			List<String> imageWhereClauses=new LinkedList<String>();

			Iterator<JSONObject> itRelationRecord=relationRecords.iterator();
			while(itRelationRecord.hasNext()){
				JSONObject relationRecord=itRelationRecord.next();
				Integer imageId=(Integer)relationRecord.get(Image.ID);
				imageWhereClauses.add(Image.ID +"="+SQLHelper.convertToQueryValue(imageId));
			}

			List<JSONObject> imageRecords=sqlHelper.query(imageTableName, imageColumnNames, imageWhereClauses, SQLHelper.OR);
			if(imageRecords==null || imageRecords.size()<=0) break;

			images=new LinkedList<Image>();
			Iterator<JSONObject> itImageRecord=imageRecords.iterator();
			while(itImageRecord.hasNext()){
				JSONObject imageJson=itImageRecord.next();
				Image image=new Image(imageJson);
				images.add(image);
			}

		}while(false);
		return images;
	}

	public static int uploadImageOfActivity(User user, Activity activity, String imageURL){
		int lastImageId= INVALID;
		do{
			if(user==null) break;
			if(activity==null) break;
			SQLHelper sqlHelper=new SQLHelper();
			String imageTableName="Image";
			String relationTableName="ActivityImageRelationTable";

			List<String> imageColumnNames=new LinkedList<String>();
			imageColumnNames.add(Image.URL);

			List<Object> imageColumnValues=new LinkedList<Object>();
			imageColumnValues.add(imageURL);

			lastImageId=sqlHelper.insert(imageTableName, imageColumnNames, imageColumnValues);
			if(lastImageId==SQLHelper.INVALID_ID) break;

			List<String> relationTableColumnNames=new LinkedList<String>();
			relationTableColumnNames.add(Activity.ID);
			relationTableColumnNames.add(Image.ID);

			List<Object> relationTableColumnValues=new LinkedList<Object>();
			relationTableColumnValues.add(activity.getId());
			relationTableColumnValues.add(lastImageId);

			int lastRecordId=sqlHelper.insert(relationTableName, relationTableColumnNames, relationTableColumnValues);
			if(lastRecordId==SQLHelper.INVALID_ID){
				boolean isRecovered=deleteImageRecordById(lastImageId);
				if(isRecovered==true){
					System.out.println("SQLCommander.uploadImageOfActivity: image "+lastImageId+ " reverted");
				}
				break;
			}

		}while(false);
		return lastImageId;
	}

	public static List<BasicUser> queryUsersByActivityIdAndRelation(int activityId, int relation){
		List<BasicUser> users=new ArrayList<BasicUser>();
		do{
			try{
				SQLHelper sqlHelper=new SQLHelper();
				String relationTableName="UserActivityRelationTable";
				List<String> relationColumnNames=new LinkedList<String>();
				relationColumnNames.add(User.ID);
				List<String> relationWhereClauses=new LinkedList<String>();
				relationWhereClauses.add(Activity.ID +"="+SQLHelper.convertToQueryValue(activityId));
				relationWhereClauses.add(UserActivityRelationTable.RELATION_ID +"="+SQLHelper.convertToQueryValue(relation));
				List<String> relationOrderClauses=new LinkedList<String>();
				relationOrderClauses.add(UserActivityRelationTable.GENERATED_TIME);
				List<JSONObject> relationRecords=sqlHelper.query(relationTableName, relationColumnNames, relationWhereClauses, SQLHelper.AND, relationOrderClauses, null, null);
				if(relationRecords==null || relationRecords.size()<=0) break;

				Iterator<JSONObject> it=relationRecords.iterator();
				while(it.hasNext()){
					JSONObject relationRecord=it.next();
					Integer userId=(Integer)relationRecord.get(BasicUser.ID);
					BasicUser user= queryUser(userId);
					users.add(user);
				}

			} catch(Exception e){
				System.out.println("SQLCommander.queryUsersByActivityIdAndRelation: "+e.getMessage());
			}
		}while(false);
		return users;
	}

    public static boolean updateRelationOfUserIdAndActivity(Integer ownerId, Integer userId, Integer activityId, int relation){
        boolean ret=false;
        do{
            try{
                SQLHelper sqlHelper=new SQLHelper();
                java.util.Date date= new java.util.Date();
                Timestamp currentTime=new Timestamp(date.getTime());

                List<String> columnNames=new LinkedList<String>();
                columnNames.add(UserActivityRelationTable.RELATION_ID);
                columnNames.add(UserActivityRelationTable.GENERATED_TIME);

                List<Object> columnValues=new LinkedList<Object>();
                columnValues.add(relation);
                columnValues.add(currentTime.toString());

                List<String> whereClauses=new LinkedList<String>();
                whereClauses.add(UserActivityRelationTable.ACTIVITY_ID +"="+SQLHelper.convertToQueryValue(activityId));
                whereClauses.add(UserActivityRelationTable.USER_ID +"="+SQLHelper.convertToQueryValue(userId));

                String relationTableName="UserActivityRelationTable";
                boolean result=sqlHelper.update(relationTableName, columnNames, columnValues, whereClauses, SQLHelper.AND);
                if(result==false) break;
                ret=true;
            } catch(Exception e){

            }
        }while(false);
        return ret;
    }
};
