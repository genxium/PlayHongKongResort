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
	 		List<String> names=new LinkedList<String>();
	 		List<String> where=new LinkedList<String>();

	 		names.add(User.EMAIL);
	 		names.add(User.PASSWORD);
	 		names.add(User.NAME);
	 		names.add(User.GROUP_ID);
			names.add(User.AUTHENTICATION_STATUS);
			names.add(User.GENDER);
			names.add(User.LAST_LOGGED_IN_TIME);
			names.add(User.AVATAR);

			where.add(User.ID +"="+SQLHelper.convertToQueryValue(userId));
			String logicLink=SQLHelper.AND;

			SQLHelper sqlHelper=new SQLHelper();
	 		List<JSONObject> results=sqlHelper.query(tableName, names, where, logicLink);
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

	 		List<String> names=new LinkedList<String>();
	 		List<String> where=new LinkedList<String>();

	 		names.add(User.ID);
	 		names.add(User.PASSWORD);
	 		names.add(User.NAME);
	 		names.add(User.GROUP_ID);
			names.add(User.AUTHENTICATION_STATUS);
			names.add(User.GENDER);
			names.add(User.LAST_LOGGED_IN_TIME);
			names.add(User.AVATAR);

			where.add(User.EMAIL +"="+SQLHelper.convertToQueryValue(email));
			String logicLink=SQLHelper.AND;

			SQLHelper sqlHelper=new SQLHelper();
	 		List<JSONObject> results=sqlHelper.query(tableName, names, where, logicLink);
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

		List<String> names=new LinkedList<String>();
		names.add(User.EMAIL);
		names.add(User.PASSWORD);
		names.add(User.NAME);
		names.add(User.GROUP_ID);

		List<Object> values=new LinkedList<Object>();
		values.add(user.getEmail());
		values.add(user.getPassword());
		values.add(user.getName());
		values.add(user.getUserGroup().ordinal());

		try{
			lastInsertedId=sqlHelper.insert("User", names, values);
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
            List<String> names=new LinkedList<String>();

            names.add(Activity.TITLE);
            names.add(Activity.CONTENT);
	    names.add(Activity.HOST_ID);

            List<Object> values=new LinkedList<Object>();

            values.add(title);
            values.add(content);
	    values.add(userId);

            int tmpLastActivityId=sqlHelper.insert("Activity", names, values);
            if(tmpLastActivityId!=SQLHelper.INVALID_ID){
                names.clear();
                values.clear();

                names.add(UserActivityRelationTable.ACTIVITY_ID);
                names.add(UserActivityRelationTable.USER_ID);
                names.add(UserActivityRelationTable.RELATION);

                values.add(tmpLastActivityId);
                values.add(userId);
                values.add(UserActivityRelationTable.hosted);

                int lastRelationTableId=sqlHelper.insert("UserActivityRelationTable", names, values);
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
				List<String> names=new LinkedList<String>();

				names.add(Activity.TITLE);
				names.add(Activity.CONTENT);
				names.add(Activity.CREATED_TIME);
				names.add(Activity.BEGIN_TIME);
				names.add(Activity.DEADLINE);
				names.add(Activity.CAPACITY);

				List<Object> values=new LinkedList<>();
				values.add(activity.getTitle());
				values.add(activity.getContent());
				values.add(activity.getCreatedTime().toString());
				values.add(activity.getBeginTime().toString());
				values.add(activity.getDeadline().toString());
				values.add(activity.getCapacity());

				List<String> where=new LinkedList<>();
				where.add(Activity.ID +"="+SQLHelper.convertToQueryValue(activityId));
				ret=sqlHelper.update(tableName, names, values, where, SQLHelper.AND);

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

		List<String> names=new LinkedList<String>();

		names.add(UserActivityRelationTable.USER_ID);
		names.add(UserActivityRelationTable.ACTIVITY_ID);
		names.add(UserActivityRelationTable.RELATION);

		List<Object> values=new LinkedList<Object>();

		values.add(userId);
		values.add(activityId);
		values.add(relation);

		try{
			int lastId=sqlHelper.insert("UserActivityRelationTable", names, values);
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

	 		List<String> names=new LinkedList<String>();
            names.add(Activity.ID);
			names.add(Activity.TITLE);
			names.add(Activity.CONTENT);
			names.add(Activity.CREATED_TIME);
			names.add(Activity.BEGIN_TIME);
			names.add(Activity.DEADLINE);
			names.add(Activity.CAPACITY);
			names.add(Activity.STATUS);

			List<String> where=new LinkedList<String>();
			where.add(Activity.ID +"="+activityId);

			SQLHelper sqlHelper=new SQLHelper();
			List<JSONObject> results=sqlHelper.query(tableName, names, where, SQLHelper.AND);
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
			List<Image> images=queryImages(activityId);
			List<BasicUser> appliedParticipants=SQLCommander.queryUsersByActivityIdAndRelation(activityId, UserActivityRelationTable.applied);
			List<BasicUser> selectedParticipants=SQLCommander.queryUsersByActivityIdAndRelation(activityId, UserActivityRelationTable.selected);

			activityDetail=new ActivityDetail(activity, images, appliedParticipants, selectedParticipants);

		}while(false);
		return activityDetail;
	}

	public static List<Activity> queryActivities(Integer userId, int relation){
		List<Activity> ret=new ArrayList<>();
		do{
			SQLHelper sqlHelper=new SQLHelper();

			// query table Activity
			List<String> names=new LinkedList<String>();
			names.add(Activity.ID);
			names.add(Activity.TITLE);
			names.add(Activity.CONTENT);
			names.add(Activity.CREATED_TIME);
			names.add(Activity.BEGIN_TIME);
			names.add(Activity.DEADLINE);
			names.add(Activity.CAPACITY);
			names.add(Activity.STATUS);
			names.add(Activity.HOST_ID);

			List<String> where=new LinkedList<>();
			where.add("EXISTS (SELECT NULL FROM UserActivityRelationTable WHERE "+
										UserActivityRelationTable.USER_ID+"="+userId+" AND "+
										UserActivityRelationTable.RELATION+"="+relation+" AND "+
										UserActivityRelationTable.TABLE+"."+UserActivityRelationTable.ACTIVITY_ID+"="+Activity.TABLE+"."+Activity.ID+
				")");

			List<JSONObject> activityJsons=sqlHelper.query(Activity.TABLE, names, where, SQLHelper.AND);
			if(activityJsons==null) break;
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
			List<String> names=new LinkedList<String>();
			names.add(Activity.ID);
			names.add(Activity.TITLE);
			names.add(Activity.CONTENT);
			names.add(Activity.CREATED_TIME);
			names.add(Activity.BEGIN_TIME);
			names.add(Activity.DEADLINE);
			names.add(Activity.CAPACITY);
			names.add(Activity.STATUS);
			names.add(Activity.HOST_ID);

			List<String> where=new LinkedList<String>();
			where.add(Activity.STATUS +"="+status.ordinal());

			List<String> orderClauses=new LinkedList<String>();
			orderClauses.add(orderKey);

			List<String> orderDirections=new LinkedList<String>();
			orderDirections.add(orderDirection);

			if(refIndex.equals(INITIAL_REF_INDEX)){
			    where.add(orderKey+">="+SQLHelper.convertToQueryValue(Integer.valueOf(INITIAL_REF_INDEX)));
			} else if(direction== DIRECTION_FORWARD){
			    where.add(orderKey+">"+SQLHelper.convertToQueryValue(refIndex));
			} else{
			    where.add(orderKey+"<"+SQLHelper.convertToQueryValue(refIndex));
			}

			List<Integer> limits=new ArrayList<Integer>();
			limits.add(numItems);

			List<JSONObject> activitiesJson=sqlHelper.query(tableName, names, where, SQLHelper.AND, orderClauses, orderDirections, limits);
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

	public static int queryUserActivityRelation(Integer userId, Integer activityId){
		int ret=UserActivityRelationTable.invalid;
		do{
			if(userId==null) break;
			if(activityId==null) break;
			try{
				SQLHelper sqlHelper=new SQLHelper();
				// query table UserActivityRelationTable
				List<String> names=new LinkedList<String>();
				names.add(UserActivityRelationTable.RELATION);

				List<String> where=new LinkedList<String>();
				where.add(UserActivityRelationTable.USER_ID +"="+SQLHelper.convertToQueryValue(userId));
				where.add(UserActivityRelationTable.ACTIVITY_ID +"="+SQLHelper.convertToQueryValue(activityId));

				List<JSONObject> records=sqlHelper.query(UserActivityRelationTable.TABLE, names, where, SQLHelper.AND);
				if(records==null || records.size()<=0 || records.size()>1) break;

				for(JSONObject record : records){
					Integer relation=(Integer)record.get(UserActivityRelationTable.RELATION);
					ret=relation;
				}
			} catch(Exception e){

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
                List<String> names=new LinkedList<String>();
                names.add(CommentOnActivity.ID);
                names.add(CommentOnActivity.CONTENT);
                names.add(CommentOnActivity.COMMENTER_ID);
                names.add(CommentOnActivity.PARENT_ID);
                names.add(CommentOnActivity.PREDECESSOR_ID);
                names.add(CommentOnActivity.ACTIVITY_ID);
                names.add(CommentOnActivity.COMMENT_TYPE);
                names.add(CommentOnActivity.GENERATED_TIME);

                List<String> where=new LinkedList<String>();
                where.add(CommentOnActivity.ID+"="+commentId);
			
                List<JSONObject> commentsJson=sqlHelper.query(tableName, names, where, SQLHelper.AND);
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
                List<String> names=new LinkedList<String>();
                names.add(CommentOnActivity.ID);
                names.add(CommentOnActivity.CONTENT);
                names.add(CommentOnActivity.COMMENTER_ID);
                names.add(CommentOnActivity.PARENT_ID);
                names.add(CommentOnActivity.PREDECESSOR_ID);
                names.add(CommentOnActivity.ACTIVITY_ID);
                names.add(CommentOnActivity.COMMENT_TYPE);
                names.add(CommentOnActivity.GENERATED_TIME);

                List<String> where=new LinkedList<String>();
                where.add(CommentOnActivity.ACTIVITY_ID+"="+activityId);
                where.add(CommentOnActivity.COMMENT_TYPE+"="+commentType);
                where.add(CommentOnActivity.PARENT_ID+"="+INVALID.toString());

                List<String> orderClauses=new LinkedList<String>();
                orderClauses.add(orderKey);

                List<String> orderDirections=new LinkedList<String>();
                orderDirections.add(orderDirection);

                if(refIndex.equals(INITIAL_REF_INDEX)){
                    where.add(orderKey+">="+SQLHelper.convertToQueryValue(INITIAL_REF_INDEX));
                } else if(direction== DIRECTION_FORWARD){
                    where.add(orderKey+">"+SQLHelper.convertToQueryValue(refIndex));
                } else{
                    where.add(orderKey+"<"+SQLHelper.convertToQueryValue(refIndex));
                }

                List<Integer> limits=new ArrayList<Integer>();
                limits.add(numItems);

                List<JSONObject> commentsJson=sqlHelper.query(tableName, names, where, SQLHelper.AND, orderClauses, orderDirections, limits);
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
                List<String> names=new LinkedList<String>();
                names.add(CommentOnActivity.ID);
                names.add(CommentOnActivity.CONTENT);
                names.add(CommentOnActivity.COMMENTER_ID);
                names.add(CommentOnActivity.PARENT_ID);
                names.add(CommentOnActivity.PREDECESSOR_ID);
                names.add(CommentOnActivity.ACTIVITY_ID);
                names.add(CommentOnActivity.COMMENT_TYPE);
                names.add(CommentOnActivity.GENERATED_TIME);

                List<String> where=new LinkedList<String>();
                where.add(CommentOnActivity.PARENT_ID+"="+parentId);
                where.add(CommentOnActivity.COMMENT_TYPE+"="+commentType);

                List<String> orderClauses=new LinkedList<String>();
                orderClauses.add(orderKey);

                List<String> orderDirections=new LinkedList<String>();
                orderDirections.add(orderDirection);

                if(refIndex.equals(INITIAL_REF_INDEX)){
                    where.add(orderKey+">="+SQLHelper.convertToQueryValue(INITIAL_REF_INDEX));

                } else if(direction== DIRECTION_FORWARD){
                    where.add(orderKey+">"+SQLHelper.convertToQueryValue(refIndex));
                } else{
                    where.add(orderKey+"<"+SQLHelper.convertToQueryValue(refIndex));
                }

                List<Integer> limits=null;
                if(numItems!=null){
                    limits=new ArrayList<Integer>();
                    limits.add(numItems);
                }
                List<JSONObject> commentsJson=sqlHelper.query(tableName, names, where, SQLHelper.AND, orderClauses, orderDirections, limits);
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
			int relation=SQLCommander.queryUserActivityRelation(userId, activityId);
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

	public static boolean isActivityEditable(Integer userId, Integer activityId){
		boolean ret=false;
		do{
			if(userId==null) break;
			if(activityId==null) break;
			Activity activity=SQLCommander.queryActivity(activityId);
  	  		ret=isActivityEditable(userId, activity);
		} while(false);
		return ret;
	}

	public static boolean isActivityEditable(Integer userId, Activity activity){
		boolean ret=false;
		do{
			if(userId==null) break;
			if(activity==null) break;
			if(validateOwnershipOfActivity(userId, activity)==false) break;
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

	public static boolean isActivityJoinable(Integer userId, Activity activity){
		boolean ret=false;
		do{
			if(userId==null) break;
			if(activity==null) break;
			if(activity.getStatus()!=Activity.StatusType.accepted) break;
			int activityId=activity.getId();
			int relation=queryUserActivityRelation(userId, activityId);
			if(relation==UserActivityRelationTable.invalid) break;
			ret=true;
		}while(false);
		return ret;
	}

	public static boolean isActivityJoinable(Integer userId, int activityId){
		boolean ret=false;
		do{
			if(userId==null) break;
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

				List<String> names=new LinkedList<String>();
				names.add(Activity.STATUS);
				List<Object> values=new LinkedList<Object>();
				values.add(Activity.StatusType.accepted.ordinal());
				List<String> where=new LinkedList<String>();
				where.add(Activity.ID +"="+SQLHelper.convertToQueryValue(activity.getId()));

				ret=sqlHelper.update(activityTableName, names, values, where, SQLHelper.AND);
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

				List<String> names=new LinkedList<String>();
				names.add(Activity.STATUS);
				List<Object> values=new LinkedList<Object>();
				values.add(Activity.StatusType.rejected.ordinal());
				List<String> where=new LinkedList<String>();
				where.add(Activity.ID +"="+SQLHelper.convertToQueryValue(activity.getId()));

				ret=sqlHelper.update(activityTableName, names, values, where, SQLHelper.AND);
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

			List<String> names=new LinkedList<String>();
			names.add(Image.URL);

			List<Object> imageColumnValues=new LinkedList<Object>();
			imageColumnValues.add(imageURL);

			lastImageId=sqlHelper.insert(imageTableName, names, imageColumnValues);
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
				List<String> names=new LinkedList<String>();
				names.add(Image.URL);

				List<String> where=new LinkedList<String>();
				where.add(Image.ID +"="+SQLHelper.convertToQueryValue(imageId));

				List<JSONObject> images=sqlHelper.query("Image", names, where, SQLHelper.AND);
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
				List<String> where=new LinkedList<String>();
				where.add(Image.ID +"="+SQLHelper.convertToQueryValue(imageId));
				ret=sqlHelper.delete(imageTableName, where, SQLHelper.AND);
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

	public static List<Image> queryImages(int activityId){
		List<Image> images=new LinkedList<Image>();;
		do{
			SQLHelper sqlHelper=new SQLHelper();

			List<String> names=new LinkedList<String>();
			names.add(Image.ID);
			names.add(Image.URL);

			List<String> where=new LinkedList<String>();
			where.add("EXISTS (SELECT NULL FROM ActivityImageRelationTable WHERE "
										+Activity.ID+"="+SQLHelper.convertToQueryValue(activityId)
										+ActivityImageRelationTable.TABLE+"."+ActivityImageRelationTable.IMAGE_ID+"="+Image.TABLE+"."+Image.ID+
				")");
			List<JSONObject> imageRecords=sqlHelper.query(Image.TABLE, names, where, SQLHelper.AND);
			for(JSONObject imageRecord : imageRecords){
				Image image=new Image(imageRecord);
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
			String imageTableName=Image.TABLE;
			String relationTableName=ActivityImageRelationTable.TABLE;

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
				relationWhereClauses.add(UserActivityRelationTable.RELATION +"="+SQLHelper.convertToQueryValue(relation));
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

                List<String> names=new LinkedList<String>();
                names.add(UserActivityRelationTable.RELATION);
                names.add(UserActivityRelationTable.GENERATED_TIME);

                List<Object> values=new LinkedList<Object>();
                values.add(relation);
                values.add(currentTime.toString());

                List<String> where=new LinkedList<String>();
                where.add(UserActivityRelationTable.ACTIVITY_ID +"="+SQLHelper.convertToQueryValue(activityId));
                where.add(UserActivityRelationTable.USER_ID +"="+SQLHelper.convertToQueryValue(userId));

                String relationTableName="UserActivityRelationTable";
                boolean result=sqlHelper.update(relationTableName, names, values, where, SQLHelper.AND);
                if(result==false) break;
                ret=true;
            } catch(Exception e){

            }
        }while(false);
        return ret;
    }
};
