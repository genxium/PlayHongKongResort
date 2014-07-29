package controllers;

import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import model.*;
import org.json.simple.JSONObject;
import scala.util.parsing.json.JSON;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SQLCommander {

	public static Integer INVALID = (-1);
	public static String INITIAL_REF_INDEX = "0";
	public static Integer DIRECTION_FORWARD = (+1);
	public static Integer DIRECTION_BACKWARD = (-1);

 	public static User queryUser(Integer userId){

 		User user=null;
 		do{
			try{
				String[] names={User.ID, User.EMAIL, User.PASSWORD, User.NAME, User.GROUP_ID, User.AUTHENTICATION_STATUS, User.GENDER, User.LAST_LOGGED_IN_TIME, User.AVATAR};
                EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
                builder.select(names).from(User.TABLE).where(User.ID, "=", userId);
				List<JSONObject> results=SQLHelper.select(builder);
				if(results==null || results.size()<=0) break;
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

 	public static User queryUserByEmail(String email){
 		User user=null;
 		do{
			try{
				String[] names={User.ID, User.EMAIL, User.PASSWORD, User.NAME, User.GROUP_ID, User.AUTHENTICATION_STATUS, User.GENDER, User.LAST_LOGGED_IN_TIME, User.AVATAR};
				EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
				builder.select(names).from(User.TABLE).where(User.EMAIL, "=", email);
				List<JSONObject> results=SQLHelper.select(builder);
				if(results==null || results.size()<=0) break;
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
		try{
			String[] cols={User.EMAIL, User.PASSWORD, User.NAME, User.GROUP_ID};
			Object[] values={user.getEmail(), user.getPassword(), user.getName(), user.getGroupId()};

			EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
			builder.insert(cols, values).into(User.TABLE);
			ret=SQLHelper.insert(builder);
		} catch (Exception e){
			System.out.println(SQLCommander.class.getName()+".registerUser, "+e.getMessage());
		}
		return ret;
	}

	public static Integer createActivity(String title, String content, Integer userId){
		int lastActivityId= SQLHelper.INVALID;
		do{
			List<String> names=new LinkedList<String>();
			names.add(Activity.TITLE);
			names.add(Activity.CONTENT);
			names.add(Activity.HOST_ID);

			List<Object> values=new LinkedList<Object>();
			values.add(title);
			values.add(content);
			values.add(userId);

			EasyPreparedStatementBuilder builderActivity=new EasyPreparedStatementBuilder();
			builderActivity.insert(names, values).into(Activity.TABLE);
			lastActivityId=SQLHelper.insert(builderActivity);
			if(lastActivityId==SQLHelper.INVALID) break;
			names.clear();
			values.clear();

			names.add(UserActivityRelation.ACTIVITY_ID);
			names.add(UserActivityRelation.USER_ID);
			names.add(UserActivityRelation.RELATION);

			values.add(lastActivityId);
			values.add(userId);
			values.add(UserActivityRelation.hosted);

			EasyPreparedStatementBuilder builderRelation=new EasyPreparedStatementBuilder();
			builderRelation.insert(names, values).into(UserActivityRelation.TABLE);
			int lastRelationId=SQLHelper.insert(builderRelation);
			if(lastRelationId==SQLHelper.INVALID) {
				EasyPreparedStatementBuilder builderDelete=new EasyPreparedStatementBuilder();
				builderDelete.from(Activity.TABLE).where(Activity.ID, "=", lastActivityId);
				boolean isDeleted=SQLHelper.delete(builderDelete);
				if(isDeleted==true){
				    System.out.println(SQLCommander.class.getName()+".createActivity, successfully reverted");
				}
				lastActivityId=SQLHelper.INVALID;
				break;
			}
		}while (false);
		return lastActivityId;
	}

	public static boolean updateActivity(Activity activity){
		boolean ret=false;
		do{
			int activityId=activity.getId();

			try{
				String[] cols={Activity.TITLE, Activity.CONTENT, Activity.CREATED_TIME, Activity.BEGIN_TIME, Activity.DEADLINE, Activity.CAPACITY};
				Object[] values={activity.getTitle(), activity.getContent(), activity.getCreatedTime().toString(), activity.getBeginTime().toString(), activity.getDeadline().toString(), activity.getCapacity()};
				EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
                builder.update(Activity.TABLE).set(cols, values).where(Activity.ID, "=", activityId);
			    ret=SQLHelper.update(builder);

			} catch(Exception e){
				System.out.println(SQLCommander.class.getName()+".updateActivity: "+e.getMessage());
			}
		}while(false);
		return ret;
	}

	/* querying activities */
	public static Activity queryActivity(int activityId){

		Activity activity=null;
		do{
			try{
				String[] names= {Activity.ID, Activity.TITLE, Activity.CONTENT, Activity.CREATED_TIME, Activity.BEGIN_TIME, Activity.DEADLINE, Activity.CAPACITY, Activity.STATUS, Activity.HOST_ID};
				EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
				builder.select(names).from(Activity.TABLE).where(Activity.ID, "=", activityId);
				List<JSONObject> results=SQLHelper.select(builder);
				if(results==null || results.size()<=0) break;
				Iterator<JSONObject> it=results.iterator();
				if(it.hasNext()){
					JSONObject activityJson=it.next();
					activity=new Activity(activityJson);
				}
			} catch (Exception e) {
				System.out.println(SQLCommander.class.getName()+".queryActivity, "+e.getMessage());
			}
		} while(false);
		return activity;
	}

	public static ActivityDetail queryActivityDetail(int activityId){
		ActivityDetail activityDetail=null;
		try{
			Activity activity= queryActivity(activityId);
			List<Image> images=queryImages(activityId);
			List<BasicUser> appliedParticipants=SQLCommander.queryUsers(activityId, UserActivityRelation.applied);
			List<BasicUser> selectedParticipants=SQLCommander.queryUsers(activityId, UserActivityRelation.selected);
			List<BasicUser> presentParticipants=SQLCommander.queryUsers(activityId, UserActivityRelation.present);

			activityDetail=new ActivityDetail(activity, images, appliedParticipants, selectedParticipants, presentParticipants);
		} catch (Exception e){
			System.out.println(SQLCommander.class.getName()+".queryActivityDetail, "+e.getMessage());
		}
		return activityDetail;
	}
	
	public static List<Activity> queryActivities(Integer userId, int relation){
		List<Activity> ret=new ArrayList<Activity>();
		try{
			String query="SELECT ";

			String[] names= {Activity.ID, Activity.TITLE, Activity.CONTENT, Activity.CREATED_TIME, Activity.BEGIN_TIME, Activity.DEADLINE, Activity.CAPACITY, Activity.STATUS, Activity.HOST_ID};
			for(int i=0;i<names.length;i++){
			    query+=names[i];
			    if(i<names.length-1) query+=", ";
			}

			query+=" FROM "+Activity.TABLE+" WHERE EXISTS (SELECT NULL FROM "+UserActivityRelation.TABLE+" WHERE "
	       +UserActivityRelation.USER_ID+"=? AND "
	       +UserActivityRelation.RELATION+"=? AND "
	       +UserActivityRelation.TABLE+"."+ UserActivityRelation.ACTIVITY_ID+"="+Activity.TABLE+"."+Activity.ID+")";
			Connection connection=SQLHelper.getConnection();
			PreparedStatement statement=connection.prepareStatement(query);
			statement.setInt(1, userId);
			statement.setInt(2, relation);
			List<JSONObject> activityJsons=SQLHelper.select(statement);
			if(activityJsons!=null){
				for(JSONObject activityJson : activityJsons){
					ret.add(new Activity(activityJson));
				}
			}
		} catch (Exception e){
			System.out.println(SQLCommander.class.getName()+".queryActivities, "+e.getMessage());
		}
		return ret;
	}

	public static List<Activity> queryActivities(String refIndex, String orderKey, String orientation, Integer numItems, Integer direction, int status){
		List<Activity> ret=new ArrayList<Activity>();
		try{
			EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
			String[] names= {Activity.ID, Activity.TITLE, Activity.CONTENT, Activity.CREATED_TIME, Activity.BEGIN_TIME, Activity.DEADLINE, Activity.CAPACITY, Activity.STATUS, Activity.HOST_ID};
			builder.select(names).from(Activity.TABLE).where(Activity.STATUS, "=", status).order(orderKey, orientation);

			if(refIndex.equals(INITIAL_REF_INDEX)){
				builder.where(orderKey, ">=", Integer.valueOf(INITIAL_REF_INDEX));
			} else if(direction.equals(DIRECTION_FORWARD)){
				builder.where(orderKey, ">", refIndex);
			} else{
				builder.where(orderKey, "<", refIndex);
			}
			if (numItems!=null) {
				builder.limit(numItems);	
			}

			List<JSONObject> activityJsons=SQLHelper.select(builder);
			if(activityJsons!=null) {
				for(JSONObject activityJson : activityJsons){
				    ret.add(new Activity(activityJson));
				}
			}
		} catch(Exception e){
			System.out.println(SQLCommander.class.getName()+".queryActivities: "+e.getMessage());
		}
		return ret;
	}

	public static int queryUserActivityRelation(Integer userId, Integer activityId){
		int ret= UserActivityRelation.invalid;
		do{
			if(userId==null) break;
			if(activityId==null) break;
			try{
				EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();

                builder.select(UserActivityRelation.RELATION).from(UserActivityRelation.TABLE);
				builder.where(UserActivityRelation.USER_ID, "=", userId);
				builder.where(UserActivityRelation.ACTIVITY_ID, "=", activityId);

				List<JSONObject> records=SQLHelper.select(builder);
				if(records==null) break;

				for(JSONObject record : records){
					Integer relation=(Integer)record.get(UserActivityRelation.RELATION);
					ret=relation;
					break;
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
				EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
				String[] names={Comment.ID, Comment.CONTENT, Comment.COMMENTER_ID, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.GENERATED_TIME};
				builder.select(names).from(Comment.TABLE).where(Comment.ID, "=", commentId);
				List<JSONObject> commentsJson=SQLHelper.select(builder);
				if(commentsJson==null || commentsJson.size()<=0) break;
				ret=new Comment(commentsJson.get(0));
			} catch(Exception e){

			}
		}while(false);
		return ret;
	}

	public static List<Comment> queryTopLevelComments(Integer activityId, String refIndex, String orderKey, String orderDirection, Integer numItems, Integer direction){
		List<Comment> ret=new ArrayList<Comment>();
		do{
			try{
				EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
						
				// query table Comment
				String[] names={Comment.ID, Comment.CONTENT, Comment.COMMENTER_ID, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.GENERATED_TIME};
				String[] whereCols={Comment.ACTIVITY_ID, Comment.PARENT_ID};
				String[] whereOps={"=", "="};
				Object[] whereVals={activityId, INVALID};

				builder.select(names).from(Comment.TABLE).where(whereCols, whereOps, whereVals).order(orderKey, orderDirection);

				if(refIndex.equals(INITIAL_REF_INDEX)){
					builder.where(orderKey, ">=", INITIAL_REF_INDEX);
				} else if(direction.equals(DIRECTION_FORWARD)){
					builder.where(orderKey, ">", refIndex);
				} else{
					builder.where(orderKey, "<", refIndex);
				}

				if (numItems!=null) {
					builder.limit(numItems);	
				}

				List<JSONObject> commentsJson=SQLHelper.select(builder);
				if(commentsJson==null) break;
				for(JSONObject commentJson : commentsJson){
				    ret.add(new Comment(commentJson));
				}
			} catch(Exception e){
				System.out.println(SQLCommander.class.getName()+".queryTopLevelComments, "+e.getMessage());
			}
		}while(false);
		return ret;
	}

	public static List<Comment> querySubComments(Integer parentId, String refIndex, String orderKey, String orderDirection, Integer numItems, Integer direction){
		List<Comment> ret=new ArrayList<Comment>();
		do{
			try{
				EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();

				String[] names={Comment.ID, Comment.CONTENT, Comment.COMMENTER_ID, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.GENERATED_TIME};     
				builder.select(names).from(Comment.TABLE).where(Comment.PARENT_ID, "=", parentId).order(orderKey, orderDirection);

				if(refIndex.equals(INITIAL_REF_INDEX)){
					builder.where(orderKey, ">=", INITIAL_REF_INDEX);
				} else if(direction== DIRECTION_FORWARD){
					builder.where(orderKey, ">", refIndex);
				} else{
					builder.where(orderKey, "<", refIndex);
				}
				if (numItems!=null) {
					builder.limit(numItems);	
				}

				List<JSONObject> commentsJson=SQLHelper.select(builder);
				if(commentsJson==null) break;
				for(JSONObject commentJson : commentsJson){
					ret.add(new Comment(commentJson));
				}
			} catch(Exception e){
				System.out.println(SQLCommander.class.getName()+".querySubComments, "+e.getMessage());
			}
		}while(false);
		return ret;
	}

    public static Assessment queryAssessment(Integer activityId, Integer assessorId, Integer assesseeId){
        Assessment ret=null;
        try{
            EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
            String[] names={Assessment.ID, Assessment.ACTIVITY_ID, Assessment.FROM, Assessment.TO, Assessment.CONTENT, Assessment.GENERATED_TIME};
            String[] whereCols={Assessment.ACTIVITY_ID, Assessment.FROM, Assessment.TO};
            String[] whereOps={"=", "=", "="};
            Object[] whereVals={activityId, assessorId, assesseeId};
            builder.select(names).where(whereCols, whereOps, whereVals).from(Assessment.TABLE);
            List<JSONObject> assessmentJsons=SQLHelper.select(builder);
            if(assessmentJsons!=null && assessmentJsons.size()==1){
                ret = new Assessment(assessmentJsons.get(0));
            }
        } catch (Exception e){

        }
        return ret;
    }

	public static List<Assessment> queryAssessments(String refIndex, String orderKey, String orientation, Integer numItems, Integer direction, Integer activityId){
		List<Assessment> ret=new ArrayList<Assessment>();
		try{
			EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
			String[] names={Assessment.ID, Assessment.CONTENT, Assessment.CONTENT, Assessment.FROM, Assessment.ACTIVITY_ID, Assessment.TO, Assessment.GENERATED_TIME};		
			builder.select(names).from(Assessment.TABLE).where(Assessment.ACTIVITY_ID, "=", activityId).order(orderKey, orientation);

			if(refIndex.equals(INITIAL_REF_INDEX)){
				builder.where(orderKey, ">=", Integer.valueOf(INITIAL_REF_INDEX));
			} else if(direction.equals(DIRECTION_FORWARD)){
				builder.where(orderKey, ">", refIndex);
			} else{
				builder.where(orderKey, "<", refIndex);
			}
			if (numItems!=null) {
				builder.limit(numItems);	
			}

			List<JSONObject> assessmentJsons=SQLHelper.select(builder);
			if(assessmentJsons!=null) {
				for(JSONObject assessmentJson : assessmentJsons){
				    ret.add(new Assessment(assessmentJson));
				}
			}
		} catch (Exception e){
			System.out.println(SQLCommander.class.getName()+".queryAssessments, "+e.getMessage());
		}
		return ret;
	}

    public static boolean updateAssessment(Integer activityId, Integer assessorId, Integer assesseeId, String content){
        boolean ret=false;
        try{
            EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
            String[] whereCols={Assessment.ACTIVITY_ID, Assessment.FROM, Assessment.TO};
            String[] whereOps={"=", "=", "="};
            Object[] whereVals={activityId, assessorId, assesseeId};
            builder.update(Assessment.TABLE).set(Assessment.CONTENT, content).where(whereCols, whereOps, whereVals);
            ret=SQLHelper.update(builder);
        } catch (Exception e){

        }
        return ret;
    }

    public static int createAssessment(Integer activityId, Integer assessorId, Integer assesseeId, String content){
        int ret=SQLHelper.INVALID;
        try{
            EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
            String[] cols={Assessment.ACTIVITY_ID, Assessment.FROM, Assessment.TO, Assessment.CONTENT};
            Object[] vals={activityId, assessorId, assesseeId, content};
            builder.insert(cols, vals).into(Assessment.TABLE);
            ret=SQLHelper.insert(builder);
        } catch (Exception e){

        }
        return ret;
    }

	public static boolean validateOwnershipOfActivity(int userId, int activityId){
		boolean ret=false;
		do{
			// validate host relation
			int relation=SQLCommander.queryUserActivityRelation(userId, activityId);
			if(relation==UserActivityRelation.invalid || relation!= UserActivityRelation.hosted) break;
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
	  	  	if(activity.getStatus()!=Activity.CREATED) break;
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
			if(activity.getStatus()!=Activity.ACCEPTED) break;
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
				EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
				builder.update(Activity.TABLE).set(Activity.STATUS, Activity.ACCEPTED).where(Activity.ID, "=", activity.getId());
				ret=SQLHelper.update(builder);
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
				EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
                builder.update(Activity.TABLE).set(Activity.STATUS, Activity.REJECTED).where(Activity.ID, "=", activity.getId());
				ret=SQLHelper.update(builder);
			} catch(Exception e){

			}
		}while(false);
		return ret;
	}

	public static int uploadUserAvatar(User user, String imageURL){
		int lastImageId=INVALID;
		do{
			EasyPreparedStatementBuilder builderImage=new EasyPreparedStatementBuilder();
			builderImage.insert(Image.URL, imageURL).into(Image.TABLE);
			lastImageId=SQLHelper.insert(builderImage);
			if(lastImageId==SQLHelper.INVALID) break;

			EasyPreparedStatementBuilder builderUser=new EasyPreparedStatementBuilder();
			builderUser.update(User.TABLE).set(User.AVATAR, lastImageId).where(User.ID, "=", user.getId());
			boolean updateResult=SQLHelper.update(builderUser);
			if(updateResult==false){
				boolean isRecovered= deleteImageRecord(lastImageId);
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
				EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
				String[] names={Image.ID, Image.URL};
				builder.select(names).from(Image.TABLE).where(Image.ID, "=", imageId);
				List<JSONObject> images=SQLHelper.select(builder);
				if(images==null) break;
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

	public static boolean deleteImageRecord(int imageId){
		boolean ret=false;
		do{
			try{
				EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
				builder.from(Image.TABLE).where(Image.ID, "=", imageId);
				ret=SQLHelper.delete(builder);
			} catch (Exception e){

			}
		}while(false);
		return ret;
	}

	public static boolean deleteImageRecord(int imageId, int activityId){
		boolean ret=false;
		do{
			try{
				String[] whereCols={ActivityImageRelation.ACTIVITY_ID, ActivityImageRelation.IMAGE_ID};
				String[] whereOps={"=", "="};
				Object[] whereVals={activityId, imageId};

				EasyPreparedStatementBuilder builderRelation=new EasyPreparedStatementBuilder();
				builderRelation.from(ActivityImageRelation.TABLE).where(whereCols, whereOps, whereVals);
				boolean resultRelationDeletion=SQLHelper.delete(builderRelation);

				if(resultRelationDeletion==false) break;

				EasyPreparedStatementBuilder builderImage=new EasyPreparedStatementBuilder();
				builderImage.from(Image.TABLE).where(Image.ID, "=", imageId);
				ret=SQLHelper.delete(builderImage);
			} catch(Exception e){

			}
		}while(false);

		return ret;
	}

	public static List<Image> queryImages(int activityId){
		List<Image> images=new LinkedList<Image>();;
		try{
			String query="SELECT "+Image.ID+", "+Image.URL+" FROM "+Image.TABLE+" WHERE EXISTS (SELECT NULL FROM "+ActivityImageRelation.TABLE+" WHERE "
			    +ActivityImageRelation.ACTIVITY_ID+"=? AND "+ActivityImageRelation.TABLE+"."+ ActivityImageRelation.IMAGE_ID+"="+Image.TABLE+"."+Image.ID+
			    ")";
			PreparedStatement statement = SQLHelper.getConnection().prepareStatement(query);
			    statement.setInt(1, activityId);
			List<JSONObject> imageRecords=SQLHelper.select(statement);
			for(JSONObject imageRecord : imageRecords){
				Image image=new Image(imageRecord);
				images.add(image);
			}

		} catch (Exception e){
			System.out.println("SQLCommander.queryImages, "+e.getMessage());
		}
		return images;
	}

	public static int uploadImage(User user, final Activity activity, final String imageURL){
		int lastImageId= INVALID;
		do{
			if(user==null) break;
			if(activity==null) break;
			EasyPreparedStatementBuilder builderImage=new EasyPreparedStatementBuilder();
			builderImage.insert(Image.URL, imageURL).into(Image.TABLE);
			lastImageId=SQLHelper.insert(builderImage);
			if(lastImageId==INVALID) break;

			String[] cols={ActivityImageRelation.ACTIVITY_ID, ActivityImageRelation.IMAGE_ID};
			Object[] vals={activity.getId(), lastImageId};
			EasyPreparedStatementBuilder builderRelation=new EasyPreparedStatementBuilder();
			builderRelation.insert(cols, vals).into(ActivityImageRelation.TABLE);

			int lastRecordId=SQLHelper.insert(builderRelation);
			if(lastRecordId==SQLHelper.INVALID){
				boolean isRecovered= deleteImageRecord(lastImageId);
				if(isRecovered==true){
					lastImageId=INVALID;
					System.out.println(SQLCommander.class.getName()+".uploadImage: image "+lastImageId+ " reverted");
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
				String[] names={User.ID, User.EMAIL, User.PASSWORD, User.NAME, User.GROUP_ID, User.AUTHENTICATION_STATUS, User.GENDER, User.LAST_LOGGED_IN_TIME, User.AVATAR};
				String query="SELECT ";
				for(int i=0;i<names.length;i++){
				    query+=names[i];
				    if(i<names.length-1) query+=", ";
				}
				query+=" FROM "+User.TABLE+" WHERE EXISTS (SELECT NULL FROM "+UserActivityRelation.TABLE+" WHERE "
					+UserActivityRelation.ACTIVITY_ID+"=? AND "
					+UserActivityRelation.RELATION+"=? AND "
					+UserActivityRelation.TABLE+"."+UserActivityRelation.USER_ID+"="+User.TABLE+"."+User.ID+")";
				Connection connection=SQLHelper.getConnection();
				PreparedStatement statement=connection.prepareStatement(query);
				statement.setInt(1, activityId);
				statement.setInt(2, relation);
				List<JSONObject> records=SQLHelper.select(statement);
				if(records==null) break;

				for(JSONObject userJson : records){
					BasicUser user=new BasicUser(userJson);
					users.add(user);
				}

			} catch(Exception e){
				System.out.println(SQLCommander.class.getName()+".queryUsers: "+e.getMessage());
			}
		}while(false);
		return users;
	}

    public static boolean updateUserActivityRelation(Integer ownerId, Integer userId, Integer activityId, int relation){
        boolean ret=false;
        do{
            try{
                java.util.Date date= new java.util.Date();
                Timestamp currentTime=new Timestamp(date.getTime());
                String timeStr=currentTime.toString();

                String[] cols={UserActivityRelation.RELATION, UserActivityRelation.GENERATED_TIME};
                Object[] vals={relation, timeStr};

                String[] whereCols={UserActivityRelation.ACTIVITY_ID, UserActivityRelation.USER_ID};
                String[] whereOps={"=", "="};
                Object[] whereVals={activityId, userId};

                EasyPreparedStatementBuilder builder=new EasyPreparedStatementBuilder();
                builder.update(UserActivityRelation.TABLE).set(cols, vals).where(whereCols, whereOps, whereVals);
                boolean result=SQLHelper.update(builder);
                if(result==false) break;
                ret=true;
            } catch(Exception e){

            }
        }while(false);
        return ret;
    }
};
