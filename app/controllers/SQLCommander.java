package controllers;

import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.*;
import models.*;
import org.json.simple.JSONObject;
import utilities.DataUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.*;

/*
 * Note that the relation (a.k.a UserActivityRelation.RELATION) in this class is always referring to masked relation
 * */

public class SQLCommander {

    public static final String TAG = SQLCommander.class.getName();

    public static final int INVALID = (-1);
    public static final String INITIAL_REF_INDEX = "0";
    public static final int DIRECTION_FORWARD = (+1);
    public static final int DIRECTION_BACKWARD = (-1);

    public static User queryUser(Integer userId) {

	    User user = null;
	    try {
		    String[] names = {User.ID, User.EMAIL, User.PASSWORD, User.NAME, User.GROUP_ID, User.AUTHENTICATION_STATUS, User.GENDER, User.LAST_LOGGED_IN_TIME, User.AVATAR};
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    builder.select(names).from(User.TABLE).where(User.ID, "=", userId);
		    List<JSONObject> results = SQLHelper.select(builder);
		    if (results == null || results.size() <= 0) return null;
		    Iterator<JSONObject> it = results.iterator();
		    if (!it.hasNext()) return null;
		    JSONObject userJson = it.next();
		    user = new User(userJson);
	    } catch (Exception e) {

	    }
	    return user;
    }

    public static User queryUserByEmail(String email) {
	    User user = null;
	    try {
		    String[] names = {User.ID, User.EMAIL, User.PASSWORD, User.NAME, User.GROUP_ID, User.AUTHENTICATION_STATUS, User.GENDER, User.LAST_LOGGED_IN_TIME, User.AVATAR};
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    builder.select(names).from(User.TABLE).where(User.EMAIL, "=", email);
		    List<JSONObject> results = SQLHelper.select(builder);
		    if (results == null || results.size() <= 0) return null;
		    Iterator<JSONObject> it = results.iterator();
		    if (!it.hasNext()) return null;
		    JSONObject userJson = it.next();
		    user = new User(userJson);
	    } catch (Exception e) {
		    System.out.println(SQLCommander.class.getName() + ".queryUserByEmail, " + e.getMessage());

	    }
	    return user;
    }

    public static int registerUser(User user) {
	    int ret = INVALID;
	    try {
		    String[] cols = {User.EMAIL, User.PASSWORD, User.NAME, User.GROUP_ID};
		    Object[] values = {user.getEmail(), user.getPassword(), user.getName(), user.getGroupId()};

		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    builder.insert(cols, values).into(User.TABLE);
		    ret = SQLHelper.insert(builder);
	    } catch (Exception e) {
		    System.out.println(SQLCommander.class.getName() + ".registerUser, " + e.getMessage());
	    }
	    return ret;
    }

    public static Integer createActivity(String title, String content, Integer userId) {
	    Integer lastActivityId = null;

	    List<String> names = new LinkedList<String>();
	    names.add(Activity.TITLE);
	    names.add(Activity.CONTENT);
	    names.add(Activity.HOST_ID);

	    List<Object> values = new LinkedList<Object>();
	    values.add(title);
	    values.add(content);
	    values.add(userId);

	    EasyPreparedStatementBuilder builderActivity = new EasyPreparedStatementBuilder();
	    builderActivity.insert(names, values).into(Activity.TABLE);
	    lastActivityId = SQLHelper.insert(builderActivity);
	    if (lastActivityId.equals(SQLHelper.INVALID)) return SQLHelper.INVALID;
	    names.clear();
	    values.clear();

	    names.add(UserActivityRelation.ACTIVITY_ID);
	    names.add(UserActivityRelation.USER_ID);
	    names.add(UserActivityRelation.RELATION);

	    values.add(lastActivityId);
	    values.add(userId);
	    values.add(UserActivityRelation.maskRelation(UserActivityRelation.selected, null));

	    EasyPreparedStatementBuilder builderRelation = new EasyPreparedStatementBuilder();
	    builderRelation.insert(names, values).into(UserActivityRelation.TABLE);
	    int lastRelationId = SQLHelper.insert(builderRelation);
	    if (lastRelationId != SQLHelper.INVALID) return lastActivityId;

	    EasyPreparedStatementBuilder builderDelete = new EasyPreparedStatementBuilder();
	    builderDelete.from(Activity.TABLE).where(Activity.ID, "=", lastActivityId);
	    if (SQLHelper.delete(builderDelete))	System.out.println(SQLCommander.class.getName() + ".createActivity, successfully reverted");
	    return SQLHelper.INVALID;
    }

    public static boolean updateActivity(Activity activity) {
	    int activityId = activity.getId();
	    try {
		    String[] cols = {Activity.TITLE, Activity.CONTENT, Activity.CREATED_TIME, Activity.BEGIN_TIME, Activity.DEADLINE, Activity.CAPACITY};
		    Object[] values = {activity.getTitle(), activity.getContent(), activity.getCreatedTime().toString(), activity.getBeginTime().toString(), activity.getDeadline().toString(), activity.getCapacity()};
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    builder.update(Activity.TABLE).set(cols, values).where(Activity.ID, "=", activityId);
		    return SQLHelper.update(builder);
	    } catch (Exception e) {
		    DataUtils.log(TAG, "updateActivity", e);
	    }
	    return false;
    }

    /* querying activities */
    public static Activity queryActivity(int activityId) {

	    Activity activity = null;
	    try {
		    String[] names = {Activity.ID, Activity.TITLE, Activity.CONTENT, Activity.CREATED_TIME, Activity.BEGIN_TIME, Activity.DEADLINE, Activity.CAPACITY, Activity.STATUS, Activity.HOST_ID};
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    builder.select(names).from(Activity.TABLE).where(Activity.ID, "=", activityId);
		    List<JSONObject> results = SQLHelper.select(builder);
		    if (results == null || results.size() <= 0) return null;
		    Iterator<JSONObject> it = results.iterator();
		    if (it.hasNext()) {
			    JSONObject activityJson = it.next();
			    User host = queryUser((Integer) (activityJson.get(Activity.HOST_ID)));
			    activity = new Activity(activityJson, host);
		    }
	    } catch (Exception e) {
		    System.out.println(SQLCommander.class.getName() + ".queryActivity, " + e.getMessage());
	    }
	    return activity;

    }

    public static ActivityDetail queryActivityDetail(int activityId) {
	    ActivityDetail activityDetail = null;
	    try {
		    Activity activity = queryActivity(activityId);
		    List<Image> images = queryImages(activityId);
		    List<BasicUser> appliedParticipants = queryUsers(activityId, UserActivityRelation.maskRelation(UserActivityRelation.applied, null));
		    List<BasicUser> selectedParticipants = querySelectedParticipants(activityId);
		    List<BasicUser> presentParticipants = queryPresentParticipants(activityId);
		    activityDetail = new ActivityDetail(activity, images, appliedParticipants, selectedParticipants, presentParticipants);
	    } catch (Exception e) {
		    System.out.println(SQLCommander.class.getName() + ".queryActivityDetail, " + e.getMessage());
	    }
	    return activityDetail;
    }

    public static List<Activity> queryActivities(Integer vieweeId, int relation) {
	    List<Activity> ret = new ArrayList<Activity>();
	    try {
		    String query = "SELECT ";

		    String[] names = {Activity.ID, Activity.TITLE, Activity.CONTENT, Activity.CREATED_TIME, Activity.BEGIN_TIME, Activity.DEADLINE, Activity.CAPACITY, Activity.STATUS, Activity.HOST_ID};
		    for (int i = 0; i < names.length; i++) {
			    query += names[i];
			    if (i < names.length - 1) query += ", ";
		    }

		    query += " FROM " + Activity.TABLE + " WHERE EXISTS (SELECT NULL FROM " + UserActivityRelation.TABLE + " WHERE "
			    + UserActivityRelation.USER_ID + "=? AND "
			    + UserActivityRelation.RELATION + "=? AND "
			    + UserActivityRelation.TABLE + "." + UserActivityRelation.ACTIVITY_ID + "=" + Activity.TABLE + "." + Activity.ID + ")";
		    Connection connection = SQLHelper.getConnection();
		    PreparedStatement statement = connection.prepareStatement(query);
		    statement.setInt(1, vieweeId);
		    statement.setInt(2, relation);
		    List<JSONObject> activityJsons = SQLHelper.select(statement);
		    if (activityJsons == null) return null;
		    for (JSONObject activityJson : activityJsons) {
			    User host = queryUser((Integer) (activityJson.get(Activity.HOST_ID)));
			    ret.add(new Activity(activityJson, host));
		    }
	    } catch (Exception e) {
		    DataUtils.log(TAG, "queryActivities", e);
	    }
	    return ret;
    }

    public static List<Activity> queryActivities(String refIndex, String orderKey, String orientation, Integer numItems, Integer direction, int status) {
	    List<Activity> ret = new ArrayList<Activity>();
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    String[] names = {Activity.ID, Activity.TITLE, Activity.CONTENT, Activity.CREATED_TIME, Activity.BEGIN_TIME, Activity.DEADLINE, Activity.CAPACITY, Activity.STATUS, Activity.HOST_ID};
		    builder.select(names).from(Activity.TABLE).where(Activity.STATUS, "=", status);
            List<JSONObject> activityJsons = processOrientationAndDirection(builder, refIndex, orderKey, orientation, direction, numItems);

		    if (activityJsons == null)	return null;
		    for (JSONObject activityJson : activityJsons) {
			    User host = queryUser((Integer) (activityJson.get(Activity.HOST_ID)));
			    ret.add(new Activity(activityJson, host));
		    }

	    } catch (Exception e) {
		    DataUtils.log(TAG, "queryActivities", e);
	    }
	    return ret;
    }

    public static List<Activity> queryHostedActivities(Integer hostId, Integer viewerId, String refIndex, String orderKey, String orientation, Integer numItems, Integer direction){
	    List<Activity> ret = new ArrayList<Activity>();
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    String[] names = {Activity.ID, Activity.TITLE, Activity.CONTENT, Activity.CREATED_TIME, Activity.BEGIN_TIME, Activity.DEADLINE, Activity.CAPACITY, Activity.STATUS, Activity.HOST_ID};
		    builder.select(names).from(Activity.TABLE);
            // extra where criterion
            builder.where(Activity.HOST_ID, "=", hostId);
            if(!hostId.equals(viewerId)) builder.where(Activity.STATUS, "=", Activity.ACCEPTED);

            List<JSONObject> activityJsons = processOrientationAndDirection(builder, refIndex, orderKey, orientation, direction, numItems);

		    if (activityJsons == null) return null;
		    for (JSONObject activityJson : activityJsons) {
			    User host = queryUser((Integer) (activityJson.get(Activity.HOST_ID)));
			    Activity activity = new Activity(activityJson, host);
			    ret.add(activity);
		    }

	    } catch (Exception e) {
		    DataUtils.log(TAG, "queryHostedActivities", e);
	    }
	    return ret;
    }

    public static int queryUserActivityRelation(Integer userId, Integer activityId) {
	    try {
		    if (userId == null) throw new UserNotFoundException();
		    if (activityId == null) throw new ActivityNotFoundException();
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

		    builder.select(UserActivityRelation.RELATION).from(UserActivityRelation.TABLE);
		    builder.where(UserActivityRelation.USER_ID, "=", userId);
		    builder.where(UserActivityRelation.ACTIVITY_ID, "=", activityId);

		    List<JSONObject> records = SQLHelper.select(builder);
		    if (records == null) return UserActivityRelation.invalid;
		    if (records.size() != 1) return UserActivityRelation.invalid;
		    JSONObject record = records.get(0);
		    return (Integer) record.get(UserActivityRelation.RELATION);
	    } catch (Exception e) {
		    DataUtils.log(TAG, "queryUserActivityRelation", e);
	    }
	    return UserActivityRelation.invalid;
    }

    public static Comment queryComment(Integer commentId) {
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] names = {Comment.ID, Comment.CONTENT, Comment.COMMENTER_ID, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.GENERATED_TIME};
            builder.select(names).from(Comment.TABLE).where(Comment.ID, "=", commentId);
            List<JSONObject> commentsJson = SQLHelper.select(builder);
            if (commentsJson == null || commentsJson.size() <= 0) throw new NullPointerException();
            return new Comment(commentsJson.get(0));
        } catch (Exception e) {
            DataUtils.log(TAG, "queryComment", e);
        }
	    return null;
    }

    public static List<Comment> queryTopLevelComments(Integer activityId, String refIndex, String orderKey, String orientation, Integer numItems, Integer direction) {
	    List<Comment> ret = new ArrayList<Comment>();
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

		    // query table Comment
		    String[] names = {Comment.ID, Comment.CONTENT, Comment.COMMENTER_ID, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.GENERATED_TIME};
		    String[] whereCols = {Comment.ACTIVITY_ID, Comment.PARENT_ID};
		    String[] whereOps = {"=", "="};
		    Object[] whereVals = {activityId, INVALID};

		    builder.select(names).from(Comment.TABLE).where(whereCols, whereOps, whereVals);

            List<JSONObject> commentsJson = processOrientationAndDirection(builder, refIndex, orderKey, orientation, direction, numItems);

		    if (commentsJson == null) throw new NullPointerException();
		    for (JSONObject commentJson : commentsJson)	ret.add(new Comment(commentJson));

	    } catch (Exception e) {
		    DataUtils.log(TAG, "queryTopLevelComments", e);
	    }
	    return ret;
    }

    public static List<Comment> querySubComments(Integer parentId, String refIndex, String orderKey, String orientation, Integer numItems, Integer direction) {
	    List<Comment> ret = new ArrayList<Comment>();
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

		    String[] names = {Comment.ID, Comment.CONTENT, Comment.COMMENTER_ID, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.GENERATED_TIME};
		    builder.select(names).from(Comment.TABLE).where(Comment.PARENT_ID, "=", parentId);
            List<JSONObject> commentsJson = processOrientationAndDirection(builder, refIndex, orderKey, orientation, direction, numItems);

		    if (commentsJson == null) throw new NullPointerException();
		    for (JSONObject commentJson : commentsJson)	ret.add(new Comment(commentJson));

	    } catch (Exception e) {
		    DataUtils.log(TAG, "querySubComments", e);
	    }
	    return ret;
    }

    public static Assessment queryAssessment(Integer activityId, Integer from, Integer to) {
        try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    String[] names = {Assessment.ID, Assessment.ACTIVITY_ID, Assessment.FROM, Assessment.TO, Assessment.CONTENT, Assessment.GENERATED_TIME};
		    String[] whereCols = {Assessment.ACTIVITY_ID, Assessment.FROM, Assessment.TO};
		    String[] whereOps = {"=", "=", "="};
		    Object[] whereVals = {activityId, from, to};
		    builder.select(names).where(whereCols, whereOps, whereVals).from(Assessment.TABLE);
		    List<JSONObject> assessmentJsons = SQLHelper.select(builder);
		    if (assessmentJsons == null || assessmentJsons.size() != 1) return null;
            return new Assessment(assessmentJsons.get(0));
	    } catch (Exception e) {
		    DataUtils.log(TAG, "queryAssessment", e);
	    }
	    return null;
    }

    public static List<Assessment> queryAssessments(String refIndex, String orderKey, String orientation, Integer numItems, Integer direction, Integer from, Integer to, Integer activityId) {
	    List<Assessment> ret = new ArrayList<Assessment>();
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    String[] names = {Assessment.ID, Assessment.CONTENT, Assessment.CONTENT, Assessment.FROM, Assessment.ACTIVITY_ID, Assessment.TO, Assessment.GENERATED_TIME};
		    builder.select(names).from(Assessment.TABLE)
			    .where(Assessment.ACTIVITY_ID, "=", activityId);

		    if(from != null) builder.where(Assessment.FROM, "=", from);
		    if(to != null) builder.where(Assessment.TO, "=", to);

            List<JSONObject> assessmentJsons = processOrientationAndDirection(builder, refIndex, orderKey, orientation, direction, numItems);

		    if (assessmentJsons == null) return ret;
		    for (JSONObject assessmentJson : assessmentJsons)	ret.add(new Assessment(assessmentJson));

	    } catch (Exception e) {
		    DataUtils.log(TAG, "queryAssessments", e);
	    }
	    return ret;
    }

    public static boolean updateAssessment(Integer activityId, Integer from, Integer to, String content) {
	    boolean ret = false;
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    String[] whereCols = {Assessment.ACTIVITY_ID, Assessment.FROM, Assessment.TO};
		    String[] whereOps = {"=", "=", "="};
		    Object[] whereVals = {activityId, from, to};
		    builder.update(Assessment.TABLE).set(Assessment.CONTENT, content).where(whereCols, whereOps, whereVals);
		    ret = SQLHelper.update(builder);
	    } catch (Exception e) {

	    }
	    return ret;
    }

    public static int createAssessment(Integer activityId, Integer from, Integer to, String content) {
	    int ret = SQLHelper.INVALID;
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    String[] cols = {Assessment.ACTIVITY_ID, Assessment.FROM, Assessment.TO, Assessment.CONTENT};
		    Object[] vals = {activityId, from, to, content};
		    builder.insert(cols, vals).into(Assessment.TABLE);
		    ret = SQLHelper.insert(builder);
	    } catch (Exception e) {

	    }
	    return ret;
    }

    public static boolean validateOwnership(int userId, int activityId) {
	    Activity activity = queryActivity(activityId);
        return activity != null && validateOwnership(userId, activity);
    }

    public static boolean validateOwnership(int userId, Activity activity) {
	    return (activity != null && activity.getHost().getId() == userId);
    }

    public static boolean isActivityEditable(Integer userId, Integer activityId) {
	    boolean ret = false;
	    try {
		    if (userId == null) throw new UserNotFoundException();
		    if (activityId == null) throw new ActivityNotFoundException();
		    Activity activity = SQLCommander.queryActivity(activityId);
		    ret = isActivityEditable(userId, activity);
	    } catch (Exception e) {
		    DataUtils.log(TAG, "isActivityEditable", e);
	    }
	    return ret;
    }

    public static boolean isActivityEditable(Integer userId, Activity activity) {
	    try {
		    if (userId == null) throw new UserNotFoundException();
		    if (activity == null) throw new ActivityNotFoundException();
		    if (!validateOwnership(userId, activity)) throw new AccessDeniedException();
		    if (activity.getStatus() != Activity.CREATED && activity.getStatus() != Activity.REJECTED) throw new InvalidActivityStatusException();
		    return true;
	    } catch (Exception e) {
		    DataUtils.log(TAG, "isActivityEditable", e);
	    } 
	    return false;
    }

    public static boolean isActivityJoinable(Integer userId, int activityId) {
	    boolean ret = false;
	    try {
		    if (userId == null) throw new UserNotFoundException();
		    Activity activity = queryActivity(activityId);
		    if (activity == null) throw new ActivityNotFoundException();
		    ret = isActivityJoinable(userId, activity);
	    } catch (Exception e) {
		    DataUtils.log(TAG, "isActivityJoinable", e);
	    } 
	    return ret;
    }

    public static boolean isActivityJoinable(User user, Activity activity) {
	    boolean ret = false;
	    try {
		    if (user == null) throw new UserNotFoundException();
		    int userId = user.getId();
		    ret = isActivityJoinable(userId, activity);
	    } catch (Exception e) {
		    DataUtils.log(TAG, "isActivityJoinable", e);
	    } 
	    return ret;
    }

    public static boolean isActivityJoinable(Integer userId, Activity activity) {
	    boolean ret = false;
	    try {
		    if (userId == null) throw new UserNotFoundException();
		    if (activity == null) throw new ActivityNotFoundException();
		    if (activity.getStatus() != Activity.ACCEPTED) throw new InvalidActivityStatusException();
		    if (activity.isDeadlineExpired()) throw new DeadlineHasPassedException();
		    int activityId = activity.getId();
		    int relation = queryUserActivityRelation(userId, activityId);
		    if (relation != UserActivityRelation.invalid) throw new InvalidUserActivityRelationException();
		    ret = true;
	    } catch (Exception e) {
		    DataUtils.log(TAG, "isActivityJoinable", e);
	    } 
	    return ret;
    }

    public static boolean isActivityCommentable(Integer from, Integer to, Integer activityId) {
	    boolean ret = false;
	    try {
		    if (from == null) throw new UserNotFoundException();
		    if (to == null) throw new UserNotFoundException();
		    if (activityId == null) throw new ActivityNotFoundException();
		    Activity activity = queryActivity(activityId);
		    if (activity == null) throw new ActivityNotFoundException();
		    ret = isActivityCommentable(from, to, activity);
	    } catch (Exception e) {
		    DataUtils.log(TAG, "isActivityCommentable", e);
	    } 
	    return ret;
    }

    public static boolean isActivityCommentable(Integer from, Integer to, Activity activity) {
	    boolean ret = false;
	    try {
		    if (from == null) throw new UserNotFoundException();
		    if (to == null) throw new UserNotFoundException();
		    if (activity == null) throw new ActivityNotFoundException();
		    if (activity.hasBegun()) throw new ActivityHasNotBegunException();
		    ret = true;
	    } catch (Exception e) {
		    DataUtils.log(TAG, "isActivityCommentable", e);
	    } 
	    return ret;
    }

    public static boolean isUserAssessable(Integer from, Integer to, Integer activityId) {
	    boolean ret = false;
	    try {
		    if (from == null) throw new UserNotFoundException();
		    if (to == null) throw new UserNotFoundException();
		    if (from.equals(to)) throw new InvalidAssessmentBehaviourException();
		    if (activityId == null) throw new ActivityNotFoundException();
		    Activity activity = queryActivity(activityId);
		    if (activity == null) throw new ActivityNotFoundException();
		    ret = isUserAssessable(from, to, activity);
	    } catch (Exception e) {
		    DataUtils.log(TAG, "isUserAssessable", e);
	    }
	    return ret;
    }

    public static boolean isUserAssessable(Integer from, Integer to, Activity activity) {
	    boolean ret = false;
	    try {
		    if (from == null) throw new UserNotFoundException();
		    if (to == null) throw new UserNotFoundException();
		    if (from.equals(to)) throw new InvalidAssessmentBehaviourException();
		    if (activity == null) throw new ActivityNotFoundException();
		    if (!activity.hasBegun()) throw new ActivityHasNotBegunException();
		    int relation1 = queryUserActivityRelation(from, activity.getId());
		    int relation2 = queryUserActivityRelation(to, activity.getId());
		    if ((relation1 & UserActivityRelation.selected) == 0 || (relation2 & UserActivityRelation.selected) == 0)	throw new InvalidUserActivityRelationException();
		    ret = true;
	    } catch (Exception e) {
		    DataUtils.log(TAG, "isUserAssessable", e);
	    }
	    return ret;
    }

    /*
       Method isActivityMarkable(...) returns UserActivityRelation.invalid if the activity is not markable by
       specified user, or the original relation otherwise.
       */
    public static int isActivityMarkable(Integer userId, Integer activityId, int relation) {
	    int ret = UserActivityRelation.invalid;
	    try {
		    if (userId == null) throw new UserNotFoundException();
		    if (activityId == null) throw new ActivityNotFoundException();
		    Activity activity = queryActivity(activityId);
		    if (activity == null) throw new ActivityNotFoundException();
		    ret = isActivityMarkable(userId, activity, relation);
	    } catch (Exception e) {
		    DataUtils.log(TAG, "isActivityMarkable", e);
	    }
	    return ret;
    }

    public static int isActivityMarkable(Integer userId, Activity activity, int relation) {
	    int ret = UserActivityRelation.invalid;
	    try {
		    if (userId == null) throw new UserNotFoundException();
		    if (activity == null) throw new ActivityNotFoundException();
		    if (!activity.hasBegun()) throw new ActivityHasNotBegunException();
		    int originalRelation = queryUserActivityRelation(userId, activity.getId());
		    if (originalRelation == UserActivityRelation.invalid) throw new InvalidUserActivityRelationException();
		    if ((originalRelation & UserActivityRelation.selected) == 0) throw new InvalidUserActivityRelationException();
		    if ((originalRelation & relation) > 0) throw new InvalidUserActivityRelationException();
		    ret = originalRelation;
	    } catch (Exception e) {
		    DataUtils.log(TAG, "isActivityMarkable", e);
	    }
	    return ret;
    }

    public static boolean acceptActivity(User user, Activity activity) {
	    if (user == null) return false;
	    if (activity == null) return false;
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    builder.update(Activity.TABLE).set(Activity.STATUS, Activity.ACCEPTED).where(Activity.ID, "=", activity.getId());
		    return SQLHelper.update(builder);
	    } catch (Exception e) {
		    System.out.println(SQLCommander.class.getName() + ".acceptActivity, " + e.getMessage());
	    }
	    return false;
    }

    public static boolean rejectActivity(User user, Activity activity) {
	    if (user == null) return false;
	    if (activity == null) return false;
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    builder.update(Activity.TABLE).set(Activity.STATUS, Activity.REJECTED).where(Activity.ID, "=", activity.getId());
		    return SQLHelper.update(builder);
	    } catch (Exception e) {
		    System.out.println(SQLCommander.class.getName() + ".rejectActivity, " + e.getMessage());
	    }
	    return false;
    }

    public static int uploadUserAvatar(User user, String imageURL) {
	    int ret = INVALID;
	    try {
		    EasyPreparedStatementBuilder builderImage = new EasyPreparedStatementBuilder();
		    builderImage.insert(Image.URL, imageURL).into(Image.TABLE);
		    int lastImageId = SQLHelper.insert(builderImage);
		    if (lastImageId == SQLHelper.INVALID) throw new Exception();

		    EasyPreparedStatementBuilder builderUser = new EasyPreparedStatementBuilder();
		    builderUser.update(User.TABLE).set(User.AVATAR, lastImageId).where(User.ID, "=", user.getId());
		    if (!SQLHelper.update(builderUser)) {
			    if (deleteImageRecord(lastImageId)) {
				    System.out.println(SQLCommander.class.getName()+".uploadUserAvatar");
			    }
			    throw new NullPointerException();
		    }
		    ret = lastImageId;
	    } catch (Exception e){
		    System.out.println(SQLCommander.class.getName() + ".uploadUserAvatar, " + e.getMessage());
	    }
	    return ret;
    }

    public static Image queryImage(int imageId) {
	    Image image = null;
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    String[] names = {Image.ID, Image.URL};
		    builder.select(names).from(Image.TABLE).where(Image.ID, "=", imageId);
		    List<JSONObject> images = SQLHelper.select(builder);
		    if (images == null) throw new ImageNotFoundException();
		    Iterator<JSONObject> itImage = images.iterator();
		    if (itImage.hasNext()) {
			    JSONObject imageJson = itImage.next();
			    image = new Image(imageJson);
		    }
	    } catch (Exception e) {

	    }
	    return image;
    }

    public static boolean deleteImageRecord(int imageId) {
	    boolean ret = false;
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    builder.from(Image.TABLE).where(Image.ID, "=", imageId);
		    ret = SQLHelper.delete(builder);
	    } catch (Exception e) {

	    }
	    return ret;
    }

    public static boolean deleteImageRecord(int imageId, int activityId) {
	    boolean ret = false;
	    try {
		    String[] whereCols = {ActivityImageRelation.ACTIVITY_ID, ActivityImageRelation.IMAGE_ID};
		    String[] whereOps = {"=", "="};
		    Object[] whereVals = {activityId, imageId};

		    EasyPreparedStatementBuilder builderRelation = new EasyPreparedStatementBuilder();
		    builderRelation.from(ActivityImageRelation.TABLE).where(whereCols, whereOps, whereVals);

		    if (!SQLHelper.delete(builderRelation)) throw new NullPointerException();

		    EasyPreparedStatementBuilder builderImage = new EasyPreparedStatementBuilder();
		    builderImage.from(Image.TABLE).where(Image.ID, "=", imageId);
		    ret = SQLHelper.delete(builderImage);
	    } catch (Exception e) {
		    System.out.println(SQLCommander.class.getName() + ".deleteImageRecord, " + e.getMessage());
	    }

	    return ret;
    }

    public static List<Image> queryImages(int activityId) {
	    List<Image> images = new LinkedList<Image>();
	    try {
		    String query = "SELECT " + Image.ID + ", " + Image.URL + " FROM " + Image.TABLE + " WHERE EXISTS (SELECT NULL FROM " + ActivityImageRelation.TABLE + " WHERE "
			    + ActivityImageRelation.ACTIVITY_ID + "=? AND " + ActivityImageRelation.TABLE + "." + ActivityImageRelation.IMAGE_ID + "=" + Image.TABLE + "." + Image.ID +
			    ")";
		    PreparedStatement statement = SQLHelper.getConnection().prepareStatement(query);
		    statement.setInt(1, activityId);
		    List<JSONObject> imageRecords = SQLHelper.select(statement);
		    for (JSONObject imageRecord : imageRecords) {
			    Image image = new Image(imageRecord);
			    images.add(image);
		    }

	    } catch (Exception e) {
		    System.out.println(SQLCommander.class.getName()+".queryImages, " + e.getMessage());
	    }
	    return images;
    }

    public static int createImage(User user, final Activity activity, final String imageURL) {
	    int ret = INVALID;
	    try {
		    if (user == null) throw new UserNotFoundException();
		    if (activity == null) throw new ActivityNotFoundException();
		    EasyPreparedStatementBuilder builderImage = new EasyPreparedStatementBuilder();
		    builderImage.insert(Image.URL, imageURL).into(Image.TABLE);
		    int lastImageId = SQLHelper.insert(builderImage);
		    if (lastImageId == INVALID) throw new NullPointerException();
		    ret = lastImageId;

		    String[] cols = {ActivityImageRelation.ACTIVITY_ID, ActivityImageRelation.IMAGE_ID};
		    Object[] vals = {activity.getId(), lastImageId};
		    EasyPreparedStatementBuilder builderRelation = new EasyPreparedStatementBuilder();
		    builderRelation.insert(cols, vals).into(ActivityImageRelation.TABLE);

		    int lastRecordId = SQLHelper.insert(builderRelation);
		    if (lastRecordId == SQLHelper.INVALID) {
			    ret = INVALID;
			    if (deleteImageRecord(lastImageId))	System.out.println(SQLCommander.class.getName() + ".createImage, image " + lastImageId + " reverted");
		    }

	    } catch (Exception e) {
		    System.out.println(SQLCommander.class.getName() + ".createImage, " + e.getMessage());
	    } 
	    return ret;
    }

    public static List<BasicUser> queryUsers(int activityId, int maskedRelation) {
	    List<BasicUser> users = new ArrayList<BasicUser>();
	    try {
		    String[] names = {User.ID, User.EMAIL, User.PASSWORD, User.NAME, User.GROUP_ID, User.AUTHENTICATION_STATUS, User.GENDER, User.LAST_LOGGED_IN_TIME, User.AVATAR};
		    String query = "SELECT ";
		    for (int i = 0; i < names.length; i++) {
			    query += ("`" + names[i] + "`");
			    if (i < names.length - 1) query += ", ";
		    }
		    query += " FROM " + User.TABLE + " WHERE EXISTS (SELECT NULL FROM " + UserActivityRelation.TABLE + " WHERE "
			    + ("`" + UserActivityRelation.ACTIVITY_ID + "`") + "=? AND "
			    + ("`" + UserActivityRelation.RELATION + "`") + "=? AND "
			    + (UserActivityRelation.TABLE + "." + "`" + UserActivityRelation.USER_ID + "`") + "=" + (User.TABLE + "." + "`" + User.ID + "`") + ")";
		    Connection connection = SQLHelper.getConnection();
		    PreparedStatement statement = connection.prepareStatement(query);
		    statement.setInt(1, activityId);
		    statement.setInt(2, maskedRelation);
		    List<JSONObject> records = SQLHelper.select(statement);
		    if (records == null) throw new NullPointerException();

		    for (JSONObject userJson : records) {
			    BasicUser user = new BasicUser(userJson);
			    users.add(user);
		    }

	    } catch (Exception e) {
		    System.out.println(SQLCommander.class.getName() + ".queryUsers: " + e.getMessage());
	    }
	    return users;
    }

    public static boolean updateUserActivityRelation(Integer ownerId, Integer userId, Integer activityId, int relation) {
	    try {
		    java.util.Date date = new java.util.Date();
		    Timestamp currentTime = new Timestamp(date.getTime());
		    String timeStr = currentTime.toString();

		    String timestampFieldName = null;
		    if ((relation & UserActivityRelation.selected) > 0) {
			    timestampFieldName = UserActivityRelation.LAST_ACCEPTED_TIME;
		    } else {
			    timestampFieldName = UserActivityRelation.LAST_REJECTED_TIME;
		    }
		    String[] cols = {UserActivityRelation.RELATION, timestampFieldName};
		    Object[] vals = {relation, timeStr};

		    String[] whereCols = {UserActivityRelation.ACTIVITY_ID, UserActivityRelation.USER_ID};
		    String[] whereOps = {"=", "="};
		    Object[] whereVals = {activityId, userId};

		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    builder.update(UserActivityRelation.TABLE).set(cols, vals).where(whereCols, whereOps, whereVals);
		    if(!SQLHelper.update(builder)) throw new Exception();
		    return true;
	    } catch (Exception e) {
		    return false;
	    }
    }

    public static List<BasicUser> querySelectedParticipants(int activityId) {
	    List<BasicUser> ret = new LinkedList<BasicUser>();

	    int possibleRelation1 = UserActivityRelation.selected;
	    List<BasicUser> lst1 = queryUsers(activityId, possibleRelation1);

	    int possibleRelation11 = possibleRelation1 | UserActivityRelation.present;
	    List<BasicUser> lst11 = queryUsers(activityId, possibleRelation11);

	    int possibleRelation12 = possibleRelation1 | UserActivityRelation.absent;
	    List<BasicUser> lst12 = queryUsers(activityId, possibleRelation12);

	    int possibleRelation111 = possibleRelation11 | UserActivityRelation.assessed;
	    List<BasicUser> lst111 = queryUsers(activityId, possibleRelation111);

	    int possibleRelation121 = possibleRelation12 | UserActivityRelation.assessed;
	    List<BasicUser> lst121 = queryUsers(activityId, possibleRelation121);

	    ret.addAll(lst1);
	    ret.addAll(lst11);
	    ret.addAll(lst12);
	    ret.addAll(lst111);
	    ret.addAll(lst121);

	    return ret;
    }

    public static List<BasicUser> queryPresentParticipants(int activityId) {
	    List<BasicUser> ret = new LinkedList<BasicUser>();

	    int baseRelation1 = UserActivityRelation.selected;
	    int possibleRelation11 = baseRelation1 | UserActivityRelation.present;

	    List<BasicUser> lst11 = queryUsers(activityId, possibleRelation11);

	    int possibleRelation111 = (possibleRelation11 | UserActivityRelation.assessed);

	    List<BasicUser> lst111 = queryUsers(activityId, possibleRelation111);

	    ret.addAll(lst11);
	    ret.addAll(lst111);

	    return ret;
    }

    public static List<BasicUser> queryAssessedParticipants(int activityId) {
	    List<BasicUser> ret = new LinkedList<BasicUser>();

	    int baseRelation1 = UserActivityRelation.selected;

	    int baseRelation11 = baseRelation1 | UserActivityRelation.present;

	    int baseRelation12 = baseRelation1 | UserActivityRelation.absent;

	    int possibleRelation111 = baseRelation11 | UserActivityRelation.assessed;
	    List<BasicUser> lst111 = queryUsers(activityId, possibleRelation111);

	    int possibleRelation121 = baseRelation12 | UserActivityRelation.assessed;
	    List<BasicUser> lst121 = queryUsers(activityId, possibleRelation121);

	    ret.addAll(lst111);
	    ret.addAll(lst121);

	    return ret;
    }

    static List<JSONObject> processOrientationAndDirection(EasyPreparedStatementBuilder builder, String refIndex, String orderKey, String orientation, Integer direction, Integer nItems) {
        if (refIndex.equals(INITIAL_REF_INDEX)) {
            builder.where(orderKey, ">=", Integer.valueOf(INITIAL_REF_INDEX));
            builder.order(orderKey, orientation);
        } else if (direction.equals(DIRECTION_FORWARD) && orientation.equals(SQLHelper.ASCEND)) {
            builder.where(orderKey, ">", refIndex);
            builder.order(orderKey, orientation);
        } else if (direction.equals(DIRECTION_FORWARD) && orientation.equals(SQLHelper.DESCEND)) {
            builder.where(orderKey, "<", refIndex);
            builder.order(orderKey, orientation);
        } else if (direction.equals(DIRECTION_BACKWARD) && orientation.equals(SQLHelper.DESCEND)) {
            builder.where(orderKey, ">", refIndex);
            builder.order(orderKey, SQLHelper.ASCEND);
        } else {
            // direction.equals(DIRECTION_BACKWARD) && orientation.equals(SQLHelper.ASCEND)
            builder.where(orderKey, "<", refIndex);
            builder.order(orderKey, SQLHelper.DESCEND);
        }
        if(nItems != null) builder.limit(nItems);
        List<JSONObject> ret = SQLHelper.select(builder);
        if(direction.equals(DIRECTION_BACKWARD)) Collections.reverse(ret);
        return ret;
    }
}
