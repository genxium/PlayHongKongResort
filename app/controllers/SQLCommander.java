package controllers;

import dao.SQLHelper;
import model.*;
import org.json.simple.JSONObject;

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
	 		List<String> names=new LinkedList<String>();
	 		List<String> where=new LinkedList<String>();

            names.add(User.ID);
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
	 		List<JSONObject> results=sqlHelper.query(User.TABLE, names, where, logicLink);
			if(results==null || results.size()<=0) break;
			try{
				Iterator<JSONObject> it=results.iterator();
				if(it.hasNext()){
					JSONObject userJson=(JSONObject)it.next();
					user=new User(userJson);
				}
			} catch (Exception e) {

			}
		} while(false);
		return user;
	}

 	public static User queryUserByEmail(String email){

 		User user=null;
 		do{
	 		List<String> names=new LinkedList<String>();
	 		List<String> where=new LinkedList<String>();

	 		names.add(User.ID);
	 		names.add(User.PASSWORD);
	 		names.add(User.NAME);
            names.add(User.EMAIL);
	 		names.add(User.GROUP_ID);
			names.add(User.AUTHENTICATION_STATUS);
			names.add(User.GENDER);
			names.add(User.LAST_LOGGED_IN_TIME);
			names.add(User.AVATAR);

			where.add(User.EMAIL +"="+SQLHelper.convertToQueryValue(email));
			String logicLink=SQLHelper.AND;

			SQLHelper sqlHelper=new SQLHelper();
	 		List<JSONObject> results=sqlHelper.query(User.TABLE, names, where, logicLink);
			if(results==null || results.size()<=0) break;
			try{
				Iterator<JSONObject> it=results.iterator();
				if(it.hasNext()){
				JSONObject userJson=it.next();
				user=new User(userJson);
			}
			} catch (Exception e) {

			}
		} while(false);
 		return user;
 	}

	public static int registerUser(User user){
		int ret=INVALID;

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
		values.add(user.getGroupId());

		try{
			ret=sqlHelper.insert(User.TABLE, names, values);
		} catch (Exception e){
			System.out.println("SQLCommander.registerUser, "+e.getMessage());
		}
		return ret;
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

			int tmpLastActivityId=sqlHelper.insert(Activity.TABLE, names, values);
			if(tmpLastActivityId!=SQLHelper.INVALID_ID){
				names.clear();
				values.clear();

				names.add(UserActivityRelation.ACTIVITY_ID);
				names.add(UserActivityRelation.USER_ID);
				names.add(UserActivityRelation.RELATION);

				values.add(tmpLastActivityId);
				values.add(userId);
				values.add(UserActivityRelation.hosted);

				int lastRelationTableId=sqlHelper.insert(UserActivityRelation.TABLE, names, values);
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
				ret=sqlHelper.update(Activity.TABLE, names, values, where, SQLHelper.AND);

			} catch(Exception e){
				System.out.println("SQLCommander.updateActivity: "+e.getMessage());
			}
		}while(false);
		return ret;
	}

	public static boolean createUserActivityRelation(int activityId, User user, int relation){

		boolean bRet=false;

		int userId=user.getId();

		SQLHelper sqlHelper=new SQLHelper();

		List<String> names=new LinkedList<String>();

		names.add(UserActivityRelation.USER_ID);
		names.add(UserActivityRelation.ACTIVITY_ID);
		names.add(UserActivityRelation.RELATION);

		List<Object> values=new LinkedList<Object>();

		values.add(userId);
		values.add(activityId);
		values.add(relation);

		try{
			int lastId=sqlHelper.insert(UserActivityRelation.TABLE, names, values);
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
			where.add(Activity.ID +"="+SQLHelper.convertToQueryValue(activityId));

			SQLHelper sqlHelper=new SQLHelper();
			List<JSONObject> results=sqlHelper.query(Activity.TABLE, names, where, SQLHelper.AND);
			if(results==null || results.size()<=0) break;
			try{
				Iterator<JSONObject> it=results.iterator();
				if(it.hasNext()){
					JSONObject activityJson=(JSONObject)it.next();
					activity=new Activity(activityJson);
				}
			} catch (Exception e) {
				System.out.println("SQLCommander.queryActivity, "+e.getMessage());
			}
		} while(false);
		return activity;
	}

	public static ActivityDetail queryActivityDetail(int activityId){
		ActivityDetail activityDetail=null;
		do{
			Activity activity= queryActivity(activityId);
			List<Image> images=queryImages(activityId);
			List<BasicUser> appliedParticipants=SQLCommander.queryUsers(activityId, UserActivityRelation.applied);
			List<BasicUser> selectedParticipants=SQLCommander.queryUsers(activityId, UserActivityRelation.selected);

			activityDetail=new ActivityDetail(activity, images, appliedParticipants, selectedParticipants);

		}while(false);
		return activityDetail;
	}

	public static List<Activity> queryActivities(Integer userId, int relation){
		List<Activity> ret=new ArrayList<>();
		do{
			try{
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
				where.add("EXISTS (SELECT NULL FROM "+UserActivityRelation.TABLE+" WHERE "+
											UserActivityRelation.USER_ID+"="+userId+" AND "+
											UserActivityRelation.RELATION+"="+relation+" AND "+
											UserActivityRelation.TABLE+"."+ UserActivityRelation.ACTIVITY_ID+"="+Activity.TABLE+"."+Activity.ID+
					")");

				List<JSONObject> activityJsons=sqlHelper.query(Activity.TABLE, names, where, SQLHelper.AND);
				if(activityJsons==null) break;
				for(JSONObject activityJson : activityJsons){
					ret.add(new Activity(activityJson));
				}
			} catch (Exception e){
				System.out.println("SQLCommander.queryActivities, "+e.getMessage());
			}
		}while(false);
		return ret;
	}

	public static List<Activity> queryActivities(String refIndex, String orderKey, String orderDirection, Integer numItems, Integer direction, Activity.StatusType status){
		List<Activity> ret=null;
		do{
		    try{
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

			List<JSONObject> activitiesJson=sqlHelper.query(Activity.TABLE, names, where, SQLHelper.AND, orderClauses, orderDirections, limits);
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
		int ret= UserActivityRelation.invalid;
		do{
			if(userId==null) break;
			if(activityId==null) break;
			try{
				SQLHelper sqlHelper=new SQLHelper();
				// query table UserActivityRelation
				List<String> names=new LinkedList<String>();
				names.add(UserActivityRelation.RELATION);

				List<String> where=new LinkedList<String>();
				where.add(UserActivityRelation.USER_ID +"="+SQLHelper.convertToQueryValue(userId));
				where.add(UserActivityRelation.ACTIVITY_ID +"="+SQLHelper.convertToQueryValue(activityId));

				List<JSONObject> records=sqlHelper.query(UserActivityRelation.TABLE, names, where, SQLHelper.AND);
				if(records==null || records.size()<=0 || records.size()>1) break;

				for(JSONObject record : records){
					Integer relation=(Integer)record.get(UserActivityRelation.RELATION);
					ret=relation;
				}
			} catch(Exception e){

			}
		}while(false);
		return ret;
	}

	public static Comment queryComment(Integer commentId){
        Comment ret=null;
        do{
            try{
                SQLHelper sqlHelper=new SQLHelper();
                
                // query table Comment
                List<String> names=new LinkedList<String>();
                names.add(Comment.ID);
                names.add(Comment.CONTENT);
                names.add(Comment.COMMENTER_ID);
                names.add(Comment.PARENT_ID);
                names.add(Comment.PREDECESSOR_ID);
                names.add(Comment.ACTIVITY_ID);
                names.add(Comment.TYPE);
                names.add(Comment.GENERATED_TIME);

                List<String> where=new LinkedList<String>();
                where.add(Comment.ID+"="+commentId);
			
                List<JSONObject> commentsJson=sqlHelper.query(Comment.TABLE, names, where, SQLHelper.AND);
                if(commentsJson==null || commentsJson.size()<=0) break;

				ret=new Comment(commentsJson.get(0));
			} catch(Exception e){
				
			}
		}while(false);
		return ret;
	}

    public static List<Comment> queryTopLevelComments(Integer activityId, String refIndex, String orderKey, String orderDirection, Integer numItems, Integer direction, Integer commentType){
         
        List<Comment> ret=null;
        do{
            try{
                SQLHelper sqlHelper=new SQLHelper();
				
                // query table Comment
                List<String> names=new LinkedList<String>();
                names.add(Comment.ID);
                names.add(Comment.CONTENT);
                names.add(Comment.COMMENTER_ID);
                names.add(Comment.PARENT_ID);
                names.add(Comment.PREDECESSOR_ID);
                names.add(Comment.ACTIVITY_ID);
                names.add(Comment.TYPE);
                names.add(Comment.GENERATED_TIME);

                List<String> where=new LinkedList<String>();
                where.add(Comment.ACTIVITY_ID+"="+activityId);
                where.add(Comment.TYPE +"="+commentType);
                where.add(Comment.PARENT_ID+"="+INVALID.toString());

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

                List<JSONObject> commentsJson=sqlHelper.query(Comment.TABLE, names, where, SQLHelper.AND, orderClauses, orderDirections, limits);
                if(commentsJson==null) break;

                ret=new ArrayList<Comment>();
                for(JSONObject commentJson : commentsJson){
                    ret.add(new Comment(commentJson));
                }

            } catch(Exception e){

            }
        }while(false);
        return ret;
    }

    public static List<Comment> querySubComments(Integer parentId, String refIndex, String orderKey, String orderDirection, Integer numItems, Integer direction, Integer commentType){
        List<Comment> ret=null;
        do{
            try{
                SQLHelper sqlHelper=new SQLHelper();
                //
                // query table Comment
                List<String> names=new LinkedList<String>();
                names.add(Comment.ID);
                names.add(Comment.CONTENT);
                names.add(Comment.COMMENTER_ID);
                names.add(Comment.PARENT_ID);
                names.add(Comment.PREDECESSOR_ID);
                names.add(Comment.ACTIVITY_ID);
                names.add(Comment.TYPE);
                names.add(Comment.GENERATED_TIME);

                List<String> where=new LinkedList<String>();
                where.add(Comment.PARENT_ID+"="+parentId);
                where.add(Comment.TYPE +"="+commentType);

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
                List<JSONObject> commentsJson=sqlHelper.query(Comment.TABLE, names, where, SQLHelper.AND, orderClauses, orderDirections, limits);
                if(commentsJson==null) break;

                ret=new ArrayList<Comment>();
                for(JSONObject commentJson : commentsJson){
                    ret.add(new Comment(commentJson));
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
			if(relation== UserActivityRelation.invalid || relation!= UserActivityRelation.hosted) break;
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
			int userId=user.getId();
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
			if(relation!= UserActivityRelation.invalid) break;
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
			try{
				SQLHelper sqlHelper=new SQLHelper();

				List<String> names=new LinkedList<String>();
				names.add(Activity.STATUS);
				List<Object> values=new LinkedList<Object>();
				values.add(Activity.StatusType.accepted.ordinal());
				List<String> where=new LinkedList<String>();
				where.add(Activity.ID +"="+SQLHelper.convertToQueryValue(activity.getId()));

				ret=sqlHelper.update(Activity.TABLE, names, values, where, SQLHelper.AND);
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
			try{
				SQLHelper sqlHelper=new SQLHelper();

				List<String> names=new LinkedList<String>();
				names.add(Activity.STATUS);
				List<Object> values=new LinkedList<Object>();
				values.add(Activity.StatusType.rejected.ordinal());
				List<String> where=new LinkedList<String>();
				where.add(Activity.ID +"="+SQLHelper.convertToQueryValue(activity.getId()));

				ret=sqlHelper.update(Activity.TABLE, names, values, where, SQLHelper.AND);
			} catch(Exception e){

			}
		}while(false);
		return ret;
	}

	public static int uploadUserAvatar(User user, String imageURL){
		int lastImageId= INVALID;
		do{
			SQLHelper sqlHelper=new SQLHelper();

			List<String> names=new LinkedList<String>();
			names.add(Image.URL);

			List<Object> imageColumnValues=new LinkedList<Object>();
			imageColumnValues.add(imageURL);

			lastImageId=sqlHelper.insert(Image.TABLE, names, imageColumnValues);
			if(lastImageId==SQLHelper.INVALID_ID) break;

			List<String> userColumnNames=new LinkedList<String>();
			userColumnNames.add(User.AVATAR);

			List<Object> userColumnValues=new LinkedList<Object>();
			userColumnValues.add(lastImageId);

			List<String> userWhereClauses=new LinkedList<String>();
			userWhereClauses.add(User.ID +"="+user.getId());

			boolean updateResult=sqlHelper.update(User.TABLE, userColumnNames, userColumnValues, userWhereClauses, SQLHelper.AND);
			if(updateResult==false){
				boolean isRecovered=deleteImageRecordById(lastImageId);
				if(isRecovered==true){

                }
				break;
			}

		}while(false);
		return lastImageId;
	}

	public static Image queryImage(int imageId){
		Image image=null;
		do{
			try{
				SQLHelper sqlHelper=new SQLHelper();
				List<String> names=new LinkedList<String>();
				names.add(Image.URL);

				List<String> where=new LinkedList<String>();
				where.add(Image.ID +"="+SQLHelper.convertToQueryValue(imageId));

				List<JSONObject> images=sqlHelper.query(Image.TABLE, names, where, SQLHelper.AND);
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
			try{
				SQLHelper sqlHelper=new SQLHelper();
				List<String> where=new LinkedList<String>();
				where.add(Image.ID +"="+SQLHelper.convertToQueryValue(imageId));
				ret=sqlHelper.delete(Image.TABLE, where, SQLHelper.AND);
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
				List<String> relationWhereClauses=new LinkedList<String>();
				relationWhereClauses.add(Activity.ID +"="+SQLHelper.convertToQueryValue(activityId));
				relationWhereClauses.add(Image.ID +"="+SQLHelper.convertToQueryValue(imageId));
				boolean resultRelationDeletion=sqlHelper.delete(ActivityImageRelation.TABLE, relationWhereClauses, SQLHelper.AND);

				if(resultRelationDeletion==false) break;

				List<String> imageWhereClauses=new LinkedList<String>();
				imageWhereClauses.add(Image.ID +"="+SQLHelper.convertToQueryValue(imageId));
				ret=sqlHelper.delete(Image.TABLE, imageWhereClauses, SQLHelper.AND);

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
			where.add("EXISTS (SELECT NULL FROM "+ActivityImageRelation.TABLE+" WHERE "
										+Activity.ID+"="+SQLHelper.convertToQueryValue(activityId)+" AND "
										+ ActivityImageRelation.TABLE+"."+ ActivityImageRelation.IMAGE_ID+"="+Image.TABLE+"."+Image.ID+
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

			List<String> imageColumnNames=new LinkedList<String>();
			imageColumnNames.add(Image.URL);

			List<Object> imageColumnValues=new LinkedList<Object>();
			imageColumnValues.add(imageURL);

			lastImageId=sqlHelper.insert(Image.TABLE, imageColumnNames, imageColumnValues);
			if(lastImageId==SQLHelper.INVALID_ID) break;

			List<String> relationTableColumnNames=new LinkedList<String>();
			relationTableColumnNames.add(Activity.ID);
			relationTableColumnNames.add(Image.ID);

			List<Object> relationTableColumnValues=new LinkedList<Object>();
			relationTableColumnValues.add(activity.getId());
			relationTableColumnValues.add(lastImageId);

			int lastRecordId=sqlHelper.insert(ActivityImageRelation.TABLE, relationTableColumnNames, relationTableColumnValues);
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

	public static List<BasicUser> queryUsers(int activityId, int relation){
		List<BasicUser> users=new ArrayList<BasicUser>();
		do{
			try{
				SQLHelper sqlHelper=new SQLHelper();
				List<String> relationColumnNames=new LinkedList<String>();
				relationColumnNames.add(User.ID);
				List<String> relationWhereClauses=new LinkedList<String>();
				relationWhereClauses.add(Activity.ID +"="+SQLHelper.convertToQueryValue(activityId));
				relationWhereClauses.add(UserActivityRelation.RELATION +"="+SQLHelper.convertToQueryValue(relation));
				List<String> relationOrderClauses=new LinkedList<String>();
				relationOrderClauses.add(UserActivityRelation.GENERATED_TIME);
				List<JSONObject> relationRecords=sqlHelper.query(UserActivityRelation.TABLE, relationColumnNames, relationWhereClauses, SQLHelper.AND, relationOrderClauses, null, null);
				if(relationRecords==null || relationRecords.size()<=0) break;

				Iterator<JSONObject> it=relationRecords.iterator();
				while(it.hasNext()){
					JSONObject relationRecord=it.next();
					Integer userId=(Integer)relationRecord.get(BasicUser.ID);
					BasicUser user= queryUser(userId);
					users.add(user);
				}

			} catch(Exception e){
				System.out.println("SQLCommander.queryUsers: "+e.getMessage());
			}
		}while(false);
		return users;
	}

    public static boolean updateUserActivityRelation(Integer ownerId, Integer userId, Integer activityId, int relation){
        boolean ret=false;
        do{
            try{
                SQLHelper sqlHelper=new SQLHelper();
                java.util.Date date= new java.util.Date();
                Timestamp currentTime=new Timestamp(date.getTime());

                List<String> names=new LinkedList<String>();
                names.add(UserActivityRelation.RELATION);
                names.add(UserActivityRelation.GENERATED_TIME);

                List<Object> values=new LinkedList<Object>();
                values.add(relation);
                values.add(currentTime.toString());

                List<String> where=new LinkedList<String>();
                where.add(UserActivityRelation.ACTIVITY_ID +"="+SQLHelper.convertToQueryValue(activityId));
                where.add(UserActivityRelation.USER_ID +"="+SQLHelper.convertToQueryValue(userId));

                String relationTableName="UserActivityRelation";
                boolean result=sqlHelper.update(relationTableName, names, values, where, SQLHelper.AND);
                if(result==false) break;
                ret=true;
            } catch(Exception e){

            }
        }while(false);
        return ret;
    }
};
