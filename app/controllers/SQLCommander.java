package controllers;

import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.*;
import models.*;
import org.json.simple.JSONObject;
import utilities.Converter;
import utilities.DataUtils;
import utilities.General;
import utilities.Loggy;

import java.util.*;

/*
 * Note that the relation (a.k.a UserActivityRelation.RELATION) in this class is always referring to masked relation
 * */

public class SQLCommander {

    public static final String TAG = SQLCommander.class.getName();

    public static final String INITIAL_REF_INDEX = "0";
    public static final int DIRECTION_FORWARD = (+1);
    public static final int DIRECTION_BACKWARD = (-1);

    public static User queryUser(Long userId) {

	    User user = null;
	    try {
		    String[] names = {User.ID, User.EMAIL, User.PASSWORD, User.NAME, User.GROUP_ID, User.AUTHENTICATION_STATUS, User.GENDER, User.AVATAR, User.UNREAD_COUNT, User.UNASSESSED_COUNT};
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> results = builder.select(names).from(User.TABLE).where(User.ID, "=", userId).execSelect();
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
		    String[] names = {User.ID, User.EMAIL, User.PASSWORD, User.SALT, User.NAME, User.GROUP_ID, User.AUTHENTICATION_STATUS, User.GENDER, User.AVATAR};
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> results = builder.select(names).from(User.TABLE).where(User.EMAIL, "=", email).execSelect();
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

    public static long registerUser(User user) {
	    long ret = SQLHelper.INVALID;
	    try {
		    String[] cols = {User.EMAIL, User.PASSWORD, User.NAME, User.GROUP_ID, User.VERIFICATION_CODE, User.SALT};
		    Object[] values = {user.getEmail(), user.getPassword(), user.getName(), user.getGroupId(), user.getVerificationCode(), user.getSalt()};

		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    ret = builder.insert(cols, values).into(User.TABLE).execInsert();
	    } catch (Exception e) {
		    Loggy.e(TAG, "registerUser", e);
	    }
	    return ret;
    }

    public static Activity createActivity(final User host, final long now) {

	    Long lastActivityId = null;

	    String[] names = {Activity.HOST_ID, Activity.CREATED_TIME};
	    Object[] values = {host.getId(), now};

	    EasyPreparedStatementBuilder builderActivity = new EasyPreparedStatementBuilder();
	    lastActivityId = Converter.toLong(builderActivity.insert(names, values).into(Activity.TABLE).execInsert());
	    if (lastActivityId == null || lastActivityId.equals(SQLHelper.INVALID)) return null;
		
	    Activity activity = new Activity();
	    activity.setId(lastActivityId); 	
	    activity.setHost(host);
	    activity.setCreatedTime(now);

	    String[] names2 = {UserActivityRelation.ACTIVITY_ID, UserActivityRelation.USER_ID, UserActivityRelation.RELATION, UserActivityRelation.GENERATED_TIME};
	    Object[] values2 = {lastActivityId, host.getId(), UserActivityRelation.SELECTED | UserActivityRelation.PRESENT, now};

	    EasyPreparedStatementBuilder builderRelation = new EasyPreparedStatementBuilder();
	    builderRelation.insert(names2, values2).into(UserActivityRelation.TABLE).execInsert();

	    return activity;
    }

    public static boolean updateActivity(Activity activity) {
	    try {
		    String[] cols = {Activity.TITLE, Activity.ADDRESS, Activity.CONTENT, Activity.BEGIN_TIME, Activity.DEADLINE, Activity.CAPACITY};
		    Object[] values = {activity.getTitle(), activity.getAddress(), activity.getContent(), activity.getBeginTime(), activity.getDeadline(), activity.getCapacity()};
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    return builder.update(Activity.TABLE).set(cols, values).where(Activity.ID, "=", activity.getId()).execUpdate();
	    } catch (Exception e) {
		    Loggy.e(TAG, "updateActivity", e);
	    }
	    return false;
    }

    /* querying activities */
    public static Activity queryActivity(long activityId) {

	    Activity activity = null;
	    try {
		    String[] names = Activity.QUERY_FIELDS;
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> results = builder.select(names).from(Activity.TABLE).where(Activity.ID, "=", activityId).execSelect();
		    if (results == null || results.size() != 1) throw new ActivityNotFoundException();
		    JSONObject activityJson = results.get(0);
		    User host = queryUser(Converter.toLong(activityJson.get(Activity.HOST_ID)));
		    activity = new Activity(activityJson, host);
	    } catch (Exception e) {
		    Loggy.e(TAG, "queryActivity", e);
	    }
	    return activity;

    }

    public static List<Activity> queryActivities(Integer page_st, Integer page_ed, String orderKey, String orientation, Integer numItems, Long vieweeId, List<Integer> maskedRelationList) {
	    List<Activity> ret = new ArrayList<>();
	    try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] names = Activity.QUERY_FIELDS;
            String[] onCols = {UserActivityRelation.USER_ID, UserActivityRelation.ACTIVITY_ID, UserActivityRelation.RELATION};
            String[] onOps = {"=", "=", "IN"};

            Object[] onVals = {vieweeId, new EasyPreparedStatementBuilder.PrimaryTableField(Activity.ID), maskedRelationList};

            List<JSONObject> activityJsonList = builder.select(names)
                                                    .from(Activity.TABLE)
                                                    .join(UserActivityRelation.TABLE, onCols, onOps, onVals)
                                                    .order(orderKey, orientation)
                                                    .limit((page_st - 1) * numItems, page_ed * numItems).execSelect();
		    if (activityJsonList == null) return null;
		    for (JSONObject activityJson : activityJsonList) {
			    User host = queryUser(Converter.toLong(activityJson.get(Activity.HOST_ID)));
			    ret.add(new Activity(activityJson, host));
		    }
	    } catch (Exception e) {
		    Loggy.e(TAG, "queryActivities", e);
	    }
	    return ret;
    }

    public static List<Activity> queryActivities(String refIndex, String orderKey, String orientation, Integer numItems, Integer direction, int status) {
	    List<Activity> ret = new ArrayList<>();
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    String[] names = Activity.QUERY_FIELDS;
		    builder.select(names)
                   .from(Activity.TABLE)
                   .where(Activity.STATUS, "=", status);

            List<JSONObject> activityJsonList = processAdvancedQuery(builder, refIndex, orderKey, orientation, direction, numItems);

		    if (activityJsonList == null)	return null;
		    for (JSONObject activityJson : activityJsonList) {
			    User host = queryUser(Converter.toLong(activityJson.get(Activity.HOST_ID)));
			    ret.add(new Activity(activityJson, host));
		    }

	    } catch (Exception e) {
		    Loggy.e(TAG, "queryActivities", e);
	    }
	    return ret;
    }

    public static List<Activity> queryActivities(Integer page_st, Integer page_ed, String orderKey, String orientation, Integer numItems, int status) {
        List<Activity> ret = new ArrayList<>();
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] names = Activity.QUERY_FIELDS;
            builder.select(names)
                    .from(Activity.TABLE)
                    .order(orderKey, orientation)
                    .where(Activity.STATUS, "=", status)
                    .limit((page_st - 1) * numItems, page_ed * numItems);
			if (status == Activity.PENDING) {
				// ONLY admin queries should be accessing this closure	
				builder.where(Activity.DEADLINE, ">", General.millisec());
			}
            List<JSONObject> activityJsonList = builder.execSelect();
            if (activityJsonList == null)	return null;
            for (JSONObject activityJson : activityJsonList) {
                User host = queryUser(Converter.toLong(activityJson.get(Activity.HOST_ID)));
                ret.add(new Activity(activityJson, host));
            }
        } catch (Exception e) {
            Loggy.e(TAG, "queryActivities", e);
        }
        return ret;
    }

    public static List<Activity> queryHostedActivities(Long hostId, Long viewerId, String refIndex, String orderKey, String orientation, Integer numItems, Integer direction){
        List<Activity> ret = new ArrayList<>();
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] names = Activity.QUERY_FIELDS;
            builder.select(names).from(Activity.TABLE);
            // extra where criterion
            builder.where(Activity.HOST_ID, "=", hostId);
            if(viewerId == null || !hostId.equals(viewerId)) builder.where(Activity.STATUS, "=", Activity.ACCEPTED);

            List<JSONObject> activityJsonList = processAdvancedQuery(builder, refIndex, orderKey, orientation, direction, numItems);

            if (activityJsonList == null) return null;
            for (JSONObject activityJson : activityJsonList) {
                User host = queryUser(Converter.toLong(activityJson.get(Activity.HOST_ID)));
                Activity activity = new Activity(activityJson, host);
                ret.add(activity);
            }

        } catch (Exception e) {
            Loggy.e(TAG, "queryHostedActivities", e);
        }
        return ret;
    }

    public static List<Activity> queryHostedActivities(Long hostId, Long viewerId, Integer page_st, Integer page_ed, String orderKey, String orientation, Integer numItems){
        List<Activity> ret = new ArrayList<Activity>();
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] names = Activity.QUERY_FIELDS;
            builder.select(names)
                    .from(Activity.TABLE)
                    .order(orderKey, orientation)
                    .where(Activity.HOST_ID, "=", hostId)
                    .limit((page_st - 1) * numItems, page_ed * numItems);

            if(viewerId == null || !hostId.equals(viewerId)) builder.where(Activity.STATUS, "=", Activity.ACCEPTED);

            List<JSONObject> activityJsonList = builder.execSelect();

            if (activityJsonList == null) return null;
            for (JSONObject activityJson : activityJsonList) {
                User host = queryUser(Converter.toLong(activityJson.get(Activity.HOST_ID)));
                Activity activity = new Activity(activityJson, host);
                ret.add(activity);
            }

        } catch (Exception e) {
            Loggy.e(TAG, "queryHostedActivities", e);
        }
        return ret;
    }

    public static int queryUserActivityRelation(Long userId, Long activityId) {
	    try {
		    if (userId == null) throw new UserNotFoundException();
		    if (activityId == null) throw new ActivityNotFoundException();
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

            List<JSONObject> records = builder.select(UserActivityRelation.RELATION).from(UserActivityRelation.TABLE)
                    .where(UserActivityRelation.USER_ID, "=", userId)
                    .where(UserActivityRelation.ACTIVITY_ID, "=", activityId)
                    .execSelect();

		    if (records == null) return UserActivityRelation.INVALID;
		    if (records.size() != 1) return UserActivityRelation.INVALID;
		    JSONObject record = records.get(0);
		    return (Integer) record.get(UserActivityRelation.RELATION);
	    } catch (Exception e) {
		    Loggy.e(TAG, "queryUserActivityRelation", e);
	    }
	    return UserActivityRelation.INVALID;
    }

    public static List<Integer> queryUserActivityRelations(List<Long> userIdList, Long activityId) {
        try {
            if (userIdList == null) throw new UserNotFoundException();
            if (activityId == null) throw new ActivityNotFoundException();
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

            List<JSONObject> records = builder.select(UserActivityRelation.RELATION).from(UserActivityRelation.TABLE)
                    .where(UserActivityRelation.USER_ID, "IN", userIdList)
                    .where(UserActivityRelation.ACTIVITY_ID, "=", activityId)
                    .execSelect();

            if (records == null) return null;

            List<Integer> ret = new ArrayList<Integer>();
            for (JSONObject record : records) ret.add(Converter.toInteger(record.get(UserActivityRelation.RELATION)));
            return ret;
        } catch (Exception e) {
            Loggy.e(TAG, "queryUserActivityRelation", e);
        }
        return null;
    }

    public static Comment queryComment(Integer commentId) {
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] names = {Comment.ID, Comment.CONTENT, Comment.FROM, Comment.TO, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.NUM_CHILDREN, Comment.GENERATED_TIME};
            List<JSONObject> commentJsonList = builder.select(names).from(Comment.TABLE).where(Comment.ID, "=", commentId).execSelect();
            if (commentJsonList == null || commentJsonList.size() <= 0) throw new NullPointerException();
            return new Comment(commentJsonList.get(0));
        } catch (Exception e) {
            Loggy.e(TAG, "queryComment", e);
        }
	    return null;
    }

    public static List<Comment> queryTopLevelComments(Long activityId, String refIndex, String orderKey, String orientation, Integer numItems, Integer direction) {
	    List<Comment> ret = new ArrayList<Comment>();
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

		    // query table Comment
		    String[] names = {Comment.ID, Comment.CONTENT, Comment.FROM, Comment.TO, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.NUM_CHILDREN, Comment.GENERATED_TIME};
		    String[] whereCols = {Comment.ACTIVITY_ID, Comment.PARENT_ID};
		    String[] whereOps = {"=", "="};
		    Object[] whereVals = {activityId, SQLHelper.INVALID};

		    builder.select(names).from(Comment.TABLE).where(whereCols, whereOps, whereVals);

		    List<JSONObject> commentJsonList = processAdvancedQuery(builder, refIndex, orderKey, orientation, direction, numItems);

		    if (commentJsonList == null) throw new NullPointerException();
		    for (JSONObject commentJson : commentJsonList)	ret.add(new Comment(commentJson));

	    } catch (Exception e) {
		    Loggy.e(TAG, "queryTopLevelComments", e);
	    }
	    return ret;
    }

    public static List<Comment> queryTopLevelComments(Long activityId, Integer page_st, Integer page_ed, String orderKey, String orientation, Integer numItems) {
        List<Comment> ret = new ArrayList<Comment>();
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

            // query table Comment
            String[] names = {Comment.ID, Comment.CONTENT, Comment.FROM, Comment.TO, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.NUM_CHILDREN, Comment.GENERATED_TIME};
            String[] whereCols = {Comment.ACTIVITY_ID, Comment.PARENT_ID};
            String[] whereOps = {"=", "="};
            Object[] whereVals = {activityId, SQLHelper.INVALID};

            builder.select(names)
                    .from(Comment.TABLE)
                    .where(whereCols, whereOps, whereVals)
					.order(orderKey, orientation)
                    .limit((page_st - 1) * numItems, page_ed * numItems);

            List<JSONObject> commentJsonList = builder.execSelect();

            if (commentJsonList == null) throw new NullPointerException();
            for (JSONObject commentJson : commentJsonList)	ret.add(new Comment(commentJson));

        } catch (Exception e) {
            Loggy.e(TAG, "queryTopLevelComments", e);
        }
        return ret;
    }

    public static List<Comment> querySubComments(Long parentId, String refIndex, String orderKey, String orientation, Integer numItems, Integer direction) {
	    List<Comment> ret = new ArrayList<Comment>();
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

		    String[] names = {Comment.ID, Comment.CONTENT, Comment.FROM, Comment.TO, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.GENERATED_TIME};
		    builder.select(names).from(Comment.TABLE).where(Comment.PARENT_ID, "=", parentId);
            List<JSONObject> commentJsonList = processAdvancedQuery(builder, refIndex, orderKey, orientation, direction, numItems);

		    if (commentJsonList == null) throw new NullPointerException();
		    for (JSONObject commentJson : commentJsonList)	ret.add(new Comment(commentJson));

	    } catch (Exception e) {
		    Loggy.e(TAG, "querySubComments", e);
	    }
	    return ret;
    }

    public static List<Comment> querySubComments(Long parentId, Integer page_st, Integer page_ed, String orderKey, String orientation, Integer numItems) {
        List<Comment> ret = new ArrayList<Comment>();
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

            String[] names = {Comment.ID, Comment.CONTENT, Comment.FROM, Comment.TO, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.GENERATED_TIME};
            builder.select(names)
                    .from(Comment.TABLE)
                    .where(Comment.PARENT_ID, "=", parentId)
					.order(orderKey, orientation)
                    .limit((page_st - 1) * numItems, page_ed * numItems);

            List<JSONObject> commentJsonList = builder.execSelect();

            if (commentJsonList == null) throw new NullPointerException();
            for (JSONObject commentJson : commentJsonList)	ret.add(new Comment(commentJson));

        } catch (Exception e) {
            Loggy.e(TAG, "querySubComments", e);
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
            List<JSONObject> assessmentJsonList = builder.select(names).where(whereCols, whereOps, whereVals).from(Assessment.TABLE).execSelect();
		    if (assessmentJsonList == null || assessmentJsonList.size() != 1) return null;
            return new Assessment(assessmentJsonList.get(0));
	    } catch (Exception e) {
		    Loggy.e(TAG, "queryAssessment", e);
	    }
	    return null;
    }

	public static List<Assessment> queryAssessmentList(Integer pageSt, Integer pageEd, Integer numItems, String orderKey, String orientation, Long viewerId, Long vieweeId) {
		List<Assessment> ret = new ArrayList<Assessment>();
		try {
			EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
			String[] names = {Assessment.ID, Assessment.CONTENT, Assessment.CONTENT, Assessment.FROM, Assessment.ACTIVITY_ID, Assessment.TO, Assessment.GENERATED_TIME};

			List<JSONObject> records = builder.select(names)
							.from(Assessment.TABLE)
							.where(Assessment.TO, "=", vieweeId)
							.order(orderKey, orientation)
							.limit((pageSt - 1) * numItems, pageEd * numItems).execSelect();

			if (records == null) return ret;
			for (JSONObject record : records)	ret.add(new Assessment(record));
		} catch(Exception e) {
			Loggy.e(TAG, "queryAssessmentList", e);
		}
		return ret; 
	}

	public static List<Assessment> queryAssessments(String refIndex, String orderKey, String orientation, Integer numItems, Integer direction, Long from, Long to, Long activityId) {
		List<Assessment> ret = new ArrayList<Assessment>();
		try {
			EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
			String[] names = {Assessment.ID, Assessment.CONTENT, Assessment.CONTENT, Assessment.FROM, Assessment.ACTIVITY_ID, Assessment.TO, Assessment.GENERATED_TIME};
			builder.select(names).from(Assessment.TABLE)
				.where(Assessment.ACTIVITY_ID, "=", activityId);

			if(from != null) builder.where(Assessment.FROM, "=", from);
			if(to != null) builder.where(Assessment.TO, "=", to);

			List<JSONObject> records = processAdvancedQuery(builder, refIndex, orderKey, orientation, direction, numItems);

			if (records == null) return ret;
			for (JSONObject record : records)	ret.add(new Assessment(record));

		} catch (Exception e) {
			Loggy.e(TAG, "queryAssessments", e);
		}
		return ret;
	}

    public static boolean updateAssessment(Integer activityId, Integer from, Integer to, String content) {
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    String[] whereCols = {Assessment.ACTIVITY_ID, Assessment.FROM, Assessment.TO};
		    String[] whereOps = {"=", "=", "="};
		    Object[] whereVals = {activityId, from, to};
		    return builder.update(Assessment.TABLE).set(Assessment.CONTENT, content).where(whereCols, whereOps, whereVals).execUpdate();
	    } catch (Exception e) {

	    }
	    return false;
    }

    public static void createAssessments(List<Assessment> assessmentList) {
	    try {
		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    long now = General.millisec();
		    String[] cols = {Assessment.ACTIVITY_ID, Assessment.FROM, Assessment.TO, Assessment.CONTENT, Assessment.GENERATED_TIME};
		    for (Assessment assessment : assessmentList) {
			    Object[] vals = {assessment.getActivityId(), assessment.getFrom(), assessment.getTo(), assessment.getContent(), now};
			    builder.insert(cols, vals);
		    }
		    builder.ignore(true).into(Assessment.TABLE).execInsert();
	    } catch (Exception e) {
		    Loggy.e(TAG, "createAssessment", e);
	    }
    }

    public static List<Notification> queryNotifications(Long to, Integer isRead, Integer page_st, Integer page_ed, String orderKey, String orientation, Integer numItems) {
	    List<Notification> ret = new ArrayList<Notification>();

	    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

	    String[] names = {Notification.ID, Notification.IS_READ, Notification.ACTIVITY_ID, Notification.CONTENT, Notification.FROM, Notification.TO, Notification.CMD, Notification.GENERATED_TIME};
	    builder.select(names)
		    .from(Notification.TABLE)
		    .where(Notification.TO, "=", to)
		    .order(orderKey, orientation)
		    .limit((page_st - 1) * numItems, page_ed * numItems);

	    if (isRead != null) builder.where(Notification.IS_READ, "=", isRead);

	    List<JSONObject> notificationJsonList = builder.execSelect();
	    if (notificationJsonList == null)	return ret;
	    for (JSONObject notificationJson : notificationJsonList) {
		    Notification notification = new Notification(notificationJson);
		    ret.add(notification);
	    }
	    return ret;
    }

    public static boolean validateOwnership(long userId, Activity activity) {
		return (activity != null && activity.getHost().getId() == userId);
	}

	public static boolean isActivityEditable(Long userId, Long activityId) throws UserNotFoundException, ActivityNotFoundException, AccessDeniedException, InvalidActivityStatusException {
		if (userId == null) throw new UserNotFoundException();
		if (activityId == null) throw new ActivityNotFoundException();
		Activity activity = SQLCommander.queryActivity(activityId);
		return isActivityEditable(userId, activity);
	}

	public static boolean isActivityEditable(Long userId, Activity activity) throws UserNotFoundException, ActivityNotFoundException, AccessDeniedException, InvalidActivityStatusException {
		if (userId == null) throw new UserNotFoundException();
		if (activity == null) throw new ActivityNotFoundException();
		if (!validateOwnership(userId, activity)) throw new AccessDeniedException();
		if (activity.getStatus() != Activity.CREATED && activity.getStatus() != Activity.REJECTED) throw new InvalidActivityStatusException();
		return true;
	}

	public static boolean isActivityJoinable(Long userId, int activityId) throws UserNotFoundException, ActivityNotFoundException, InvalidActivityStatusException, InvalidUserActivityRelationException, DeadlineHasPassedException {
		if (userId == null) throw new UserNotFoundException();
		Activity activity = queryActivity(activityId);
		if (activity == null) throw new ActivityNotFoundException();
		return isActivityJoinable(userId, activity);
	}

	public static boolean isActivityJoinable(User user, Activity activity) throws UserNotFoundException, InvalidUserActivityRelationException, InvalidActivityStatusException, ActivityNotFoundException, DeadlineHasPassedException {
		if (user == null) throw new UserNotFoundException();
		return isActivityJoinable(user.getId(), activity);
	}

	public static boolean isActivityJoinable(Long userId, Activity activity) throws UserNotFoundException, ActivityNotFoundException, InvalidActivityStatusException, DeadlineHasPassedException, InvalidUserActivityRelationException {
		if (userId == null) throw new UserNotFoundException();
		if (activity == null) throw new ActivityNotFoundException();
		if (activity.getStatus() != Activity.ACCEPTED) throw new InvalidActivityStatusException();
		if (activity.isDeadlineExpired()) throw new DeadlineHasPassedException();
		int relation = queryUserActivityRelation(userId, activity.getId());
		if (relation != UserActivityRelation.INVALID) throw new InvalidUserActivityRelationException();
		return true;
	}

	public static boolean isActivityCommentable(Long from, Long activityId) throws UserNotFoundException, ActivityNotFoundException, ActivityHasNotBegunException, ActivityNotAcceptedException {
		if (from == null) throw new UserNotFoundException();
		if (activityId == null) throw new ActivityNotFoundException();
		Activity activity = queryActivity(activityId);
		if (activity == null) throw new ActivityNotFoundException();
		return isActivityCommentable(from, activity);
	}

	public static boolean isActivityCommentable(Long from, Activity activity) throws UserNotFoundException, ActivityNotFoundException, ActivityHasNotBegunException, ActivityNotAcceptedException {
		if (from == null) throw new UserNotFoundException();
		if (activity == null) throw new ActivityNotFoundException();
		if (activity.hasBegun()) throw new ActivityHasNotBegunException();
		if (activity.getStatus() != Activity.ACCEPTED) throw new ActivityNotAcceptedException();
		return true;
	}

	public static boolean isActivityCommentable(Long from, Long to, Long activityId) throws UserNotFoundException, ActivityNotFoundException, ActivityHasNotBegunException, ActivityNotAcceptedException {
		if (from == null) throw new UserNotFoundException();
		if (to == null) throw new UserNotFoundException();
		if (activityId == null) throw new ActivityNotFoundException();
		Activity activity = queryActivity(activityId);
		if (activity == null) throw new ActivityNotFoundException();
		return isActivityCommentable(from, to, activity);
	}

	public static boolean isActivityCommentable(Long from, Long to, Activity activity) throws UserNotFoundException, ActivityHasNotBegunException, ActivityNotFoundException, ActivityNotAcceptedException {
		if (from == null) throw new UserNotFoundException();
		if (to == null) throw new UserNotFoundException();
		if (activity == null) throw new ActivityNotFoundException();
		if (activity.hasBegun()) throw new ActivityHasNotBegunException();
		if (activity.getStatus() != Activity.ACCEPTED) throw new ActivityNotAcceptedException();
		return true;
	}

	public static boolean isUserAssessable(Long from, Long to, Long activityId) throws UserNotFoundException, InvalidAssessmentBehaviourException, ActivityNotFoundException, ActivityHasNotBegunException, InvalidUserActivityRelationException {
		if (from == null) throw new UserNotFoundException();
		if (to == null) throw new UserNotFoundException();
		if (from.equals(to)) throw new InvalidAssessmentBehaviourException();
		if (activityId == null) throw new ActivityNotFoundException();
		Activity activity = queryActivity(activityId);
		if (activity == null) throw new ActivityNotFoundException();
		return isUserAssessable(from, to, activity);
	}

	public static boolean isUserAssessable(Long from, Long to, Activity activity) throws UserNotFoundException, InvalidAssessmentBehaviourException, ActivityNotFoundException, ActivityHasNotBegunException, InvalidUserActivityRelationException {
		if (from == null) throw new UserNotFoundException();
		if (to == null) throw new UserNotFoundException();
		if (from.equals(to)) throw new InvalidAssessmentBehaviourException();
		if (activity == null) throw new ActivityNotFoundException();
		if (!activity.hasBegun()) throw new ActivityHasNotBegunException();
		int relation1 = queryUserActivityRelation(from, activity.getId());
		int relation2 = queryUserActivityRelation(to, activity.getId());
		if ((relation1 & UserActivityRelation.SELECTED) == 0 || (relation2 & UserActivityRelation.SELECTED) == 0)	throw new InvalidUserActivityRelationException();
		return true;
	}

	/*
	   Method isActivityMarkable(...) returns UserActivityRelation.INVALID if the activity is not markable by
	   specified user, or the original relation otherwise.
	   */
	public static int isActivityMarkable(Long userId, Long activityId, int relation) {
		int ret = UserActivityRelation.INVALID;
		try {
			if (userId == null) throw new UserNotFoundException();
			if (activityId == null) throw new ActivityNotFoundException();
			Activity activity = queryActivity(activityId);
			if (activity == null) throw new ActivityNotFoundException();
			ret = isActivityMarkable(userId, activity, relation);
		} catch (Exception e) {
			Loggy.e(TAG, "isActivityMarkable", e);
		}
		return ret;
	}

	public static int isActivityMarkable(Long userId, Activity activity, int relation) {
		int ret = UserActivityRelation.INVALID;
		try {
			if (userId == null) throw new UserNotFoundException();
			if (activity == null) throw new ActivityNotFoundException();
			if (!activity.hasBegun()) throw new ActivityHasNotBegunException();
			int originalRelation = queryUserActivityRelation(userId, activity.getId());
			if (originalRelation == UserActivityRelation.INVALID) throw new InvalidUserActivityRelationException();
			if ((originalRelation & UserActivityRelation.SELECTED) == 0) throw new InvalidUserActivityRelationException();
			if ((originalRelation & relation) > 0) throw new InvalidUserActivityRelationException();
			ret = originalRelation;
		} catch (Exception e) {
			Loggy.e(TAG, "isActivityMarkable", e);
		}
		return ret;
	}

	public static boolean acceptActivity(User user, Activity activity) {
		if (user == null) return false;
		if (activity == null) return false;
		try {
			long now = General.millisec();
			EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
			return builder.update(Activity.TABLE)
				.set(Activity.STATUS, Activity.ACCEPTED)
				.set(Activity.LAST_ACCEPTED_TIME, now)
				.where(Activity.ID, "=", activity.getId())
				.execUpdate();
		} catch (Exception e) {
			Loggy.e(TAG, "acceptActivity", e);
		}
		return false;
	}

	public static boolean rejectActivity(User user, Activity activity) {
        if (user == null) return false;
        if (activity == null) return false;
        try {
            long now = General.millisec();
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            return builder.update(Activity.TABLE)
                    .set(Activity.STATUS, Activity.REJECTED)
                    .set(Activity.LAST_REJECTED_TIME, now)
                    .where(Activity.ID, "=", activity.getId())
                    .execUpdate();
        } catch (Exception e) {
            Loggy.e(TAG, "rejectActivity", e);
        }
        return false;
    }

    public static List<BasicUser> queryUsers(long activityId, List<Integer> maskedRelationList) {
        List<BasicUser> users = new ArrayList<>();
        try {
            String[] onCols = {UserActivityRelation.ACTIVITY_ID, UserActivityRelation.USER_ID, UserActivityRelation.RELATION};
            String[] onOps = {"=", "=", "IN"};
            Object[] onVals = {activityId, new EasyPreparedStatementBuilder.PrimaryTableField(User.ID), maskedRelationList};

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> records = builder.select(User.QUERY_FILEDS)
                    .from(User.TABLE)
                    .join(UserActivityRelation.TABLE, onCols, onOps, onVals).execSelect();

            if (records == null) throw new NullPointerException();

            for (JSONObject userJson : records) {
                BasicUser user = new BasicUser(userJson);
                users.add(user);
            }

        } catch (Exception e) {
            Loggy.e(TAG, "queryUsers", e);
        }
        return users;
    }

	public static boolean updateUserActivityRelation(Long userId, Long activityId, int relation) {
		try {
			EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
			builder.update(UserActivityRelation.TABLE)
				.set(UserActivityRelation.RELATION, relation)
				.where(UserActivityRelation.ACTIVITY_ID, "=", activityId)
				.where(UserActivityRelation.USER_ID, "=", userId);

			if ((relation & UserActivityRelation.SELECTED) > 0) builder.set(UserActivityRelation.LAST_SELECTED_TIME, General.millisec());
			return builder.execUpdate();

		} catch (Exception e) {
			return false;
		}
	}

    public static List<BasicUser> queryAppliedParticipants(long activityId) {
        List<Integer> relationList = new LinkedList<>();
        for (int relation : UserActivityRelation.APPLIED_STATES) relationList.add(relation);
        return queryUsers(activityId, relationList);
    }

	public static List<BasicUser> querySelectedParticipants(long activityId) {
		List<Integer> relationList = new LinkedList<>();
        for (int relation : UserActivityRelation.SELECTED_STATES) relationList.add(relation);
		return queryUsers(activityId, relationList);
	}

	public static List<BasicUser> queryPresentParticipants(long activityId) {
        List<Integer> relationList = new LinkedList<>();
        for (int relation : UserActivityRelation.PRESENT_STATES) relationList.add(relation);
        return queryUsers(activityId, relationList);
	}

	static List<JSONObject> processAdvancedQuery(EasyPreparedStatementBuilder builder, String refIndex, String orderKey, String orientation, Integer direction, Integer nItems) {
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
		List<JSONObject> ret = builder.execSelect();
		if(direction.equals(DIRECTION_BACKWARD)) Collections.reverse(ret);
		return ret;
	}

	public static Long queryUserId(String token) throws TokenExpiredException {

		EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		List<JSONObject> allJson = builder.select(Login.USER_ID).from(Login.TABLE).where(Login.TOKEN, "=", token).execSelect();
		if (allJson == null || allJson.size() != 1) return null;
		JSONObject loginJson = allJson.get(0);
		Login login = new Login(loginJson);
		if (login.hasExpired()) throw new TokenExpiredException();
		return login.getUserId();

	}

	public static boolean validateAdminAccess(User user) {
		return (user != null && user.getGroupId() == User.ADMIN);
	}

	protected static String generateVerificationCode(String email) {
		return DataUtils.encryptByTime(email);
	}

	public static String generateSalt(String email, String password) {
		return DataUtils.encryptByTime(email + password);
	}
}
