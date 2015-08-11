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

public class DBCommander {

    public static final String TAG = DBCommander.class.getName();

    public static final String INITIAL_REF_INDEX = "0";
    public static final int DIRECTION_FORWARD = (+1);
    public static final int DIRECTION_BACKWARD = (-1);

    public static class SpecialUserRecord {
        public BasicUser user;
        public Long activityId;
        public SpecialUserRecord(final JSONObject record) {
            // record must be guaranteed to contain correct fields
            user = new BasicUser(record);
            activityId = Converter.toLong(record.get(UserActivityRelation.ACTIVITY_ID));
        }
    }

    public static User queryUser(final Long userId) {

        try {
            String[] names = User.QUERY_FILEDS;
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> results = builder.select(names).from(User.TABLE).where(User.ID, "=", userId).execSelect();
            if (results == null || results.size() <= 0) return null;
            Iterator<JSONObject> it = results.iterator();
            if (!it.hasNext()) return null;
            JSONObject userJson = it.next();
            return new User(userJson);
        } catch (Exception e) {
            Loggy.e(TAG, "queryUserList", e);
        }
        return null;
    }

    public static List<User> queryUserList(final List<Long> userIdList) {
        List<User> ret = new ArrayList<>();
        try {
            if(userIdList == null || userIdList.size() == 0) return ret;
            String[] names = User.QUERY_FILEDS;
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> records = builder.select(names).from(User.TABLE).where(User.ID, "IN", userIdList).execSelect();
            if (records == null || records.size() <= 0) return null;
            for (JSONObject record : records) {
                ret.add(new User(record));
            }
        } catch (Exception e) {
            Loggy.e(TAG, "queryUserList", e);
        }
        return ret;
    }

    public static User queryUserByEmail(String email) {
        User user = null;
        try {
            String[] names = User.QUERY_FILEDS;
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> results = builder.select(names).from(User.TABLE).where(User.EMAIL, "=", email).execSelect();
            if (results == null || results.size() <= 0) return null;
            Iterator<JSONObject> it = results.iterator();
            if (!it.hasNext()) return null;
            JSONObject userJson = it.next();
            user = new User(userJson);
        } catch (Exception e) {
            System.out.println(DBCommander.class.getName() + ".queryUserByEmail, " + e.getMessage());

        }
        return user;
    }

    public static long registerUser(final User user) {
        try {
            String[] cols = {User.EMAIL, User.PASSWORD, User.NAME, User.GROUP_ID, User.VERIFICATION_CODE, User.SALT};
            Object[] vals = {user.getEmail(), user.getPassword(), user.getName(), user.getGroupId(), user.getVerificationCode(), user.getSalt()};

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            return builder.insert(cols, vals).into(User.TABLE).execInsert();
        } catch (Exception e) {
            Loggy.e(TAG, "registerUser", e);
        }
        return SQLHelper.INVALID;
    }

    public static boolean updateUser(final User user) {
        try {
            String[] cols = {User.EMAIL, User.PASSWORD, User.NAME, User.GROUP_ID, User.AGE, User.GENDER, User.MOOD, User.VERIFICATION_CODE};
            Object[] values = {user.getEmail(), user.getPassword(), user.getName(), user.getGroupId(), user.getAge(), user.getGender(), user.getMood(), user.getVerificationCode()};

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            return builder.update(User.TABLE).set(cols, values).where(User.ID, "=", user.getId()).execUpdate();
        } catch (Exception e) {
            Loggy.e(TAG, "updateUser", e);
        }
        return false;
    }

    public static Activity createActivity(final User host, final long now) throws ActivityCreationLimitException {

        if (!ableToCreateActivity(host, now)) throw new ActivityCreationLimitException();

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

    protected static boolean ableToCreateActivity(final User host, final long now) {
        if (host == null || host.getGroupId() == User.VISITOR) return false;
        List<Activity> criticallyCreatedActivities = queryHostedActivities(host.getId(), host.getId(), 1, 1, Activity.CREATED_TIME, SQLHelper.DESCEND, Activity.CREATION_CRITICAL_NUMBER);
        if (criticallyCreatedActivities == null) return true;
        if (criticallyCreatedActivities.size() < Activity.CREATION_CRITICAL_NUMBER) return true;
        Activity criticalActivity = criticallyCreatedActivities.get(Activity.CREATION_CRITICAL_NUMBER - 1);
        return (now - criticalActivity.getCreatedTime() >= Activity.CREATION_CRITICAL_TIME_INTERVAL_MILLIS);
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
    public static Activity queryActivity(final long activityId) {
        try {
            String[] names = Activity.QUERY_FIELDS;
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> results = builder.select(names).from(Activity.TABLE).where(Activity.ID, "=", activityId).execSelect();
            if (results == null || results.size() != 1) throw new ActivityNotFoundException();
            JSONObject activityJson = results.get(0);
            User host = queryUser(Converter.toLong(activityJson.get(Activity.HOST_ID)));
            Activity activity = new Activity(activityJson);
            activity.setHost(host);
            return activity;
        } catch (Exception e) {
            Loggy.e(TAG, "queryActivity", e);
        }
        return null;
    }

    public static List<Activity> queryActivities(final Integer pageSt, final Integer pageEd, final String orderKey, final String orientation, final Integer numItems, final Long vieweeId, final List<Integer> maskedRelationList) {
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
                    .limit((pageSt - 1) * numItems, (pageEd - pageSt + 1) * numItems).execSelect();
            if (activityJsonList == null) return null;

            for (JSONObject activityJson : activityJsonList) {
                Activity activity = new Activity(activityJson);
                ret.add(activity);
            }
            if (ret.size() == 0) return ret;
            appendUserInfoForActivity(ret, null);
        } catch (Exception e) {
            Loggy.e(TAG, "queryActivities", e);
        }
        return ret;
    }

    public static List<Activity> queryActivities(final Integer pageSt, final Integer pageEd, final String orderKey, final String orientation, final Integer numItems, final int status, final int offset) {
        List<Activity> ret = new ArrayList<>();
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] names = Activity.QUERY_FIELDS;
            builder.select(names)
                    .from(Activity.TABLE)
                    .order(orderKey, orientation)
                    .where(Activity.STATUS, "=", status)
                    .where(Activity.PRIORITY, "=", 0)
                    .limit((pageSt - 1) * numItems + offset, (pageEd - pageSt + 1) * numItems + offset);
            if (status == Activity.PENDING) {
                // ONLY admin queries should be accessing this closure
                builder.where(Activity.DEADLINE, ">", General.millisec());
            }
            List<JSONObject> activityJsonList = builder.execSelect();
            if (activityJsonList == null)	return null;
            for (JSONObject activityJson : activityJsonList) {
                Activity activity = new Activity(activityJson);
                ret.add(activity);
            }
            if (ret.size() == 0) return ret;
            appendUserInfoForActivity(ret, null);
        } catch (Exception e) {
            Loggy.e(TAG, "queryActivities", e);
        }
        return ret;
    }

    public static List<Activity> queryHostedActivities(final Long hostId, final Long viewerId, final Integer pageSt, final Integer pageEd, final String orderKey, final String orientation, final Integer numItems){
        List<Activity> ret = new ArrayList<>();
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] names = Activity.QUERY_FIELDS;
            builder.select(names)
                    .from(Activity.TABLE)
                    .order(orderKey, orientation)
                    .where(Activity.HOST_ID, "=", hostId)
                    .limit((pageSt - 1) * numItems, (pageEd - pageSt + 1) * numItems);

            if(viewerId == null || !hostId.equals(viewerId)) builder.where(Activity.STATUS, "=", Activity.ACCEPTED);

            List<JSONObject> activityJsonList = builder.execSelect();

            if (activityJsonList == null) return null;
            for (JSONObject activityJson : activityJsonList) {
                Activity activity = new Activity(activityJson);
                ret.add(activity);
            }
            if (ret.size() == 0) return ret;
            appendUserInfoForActivity(ret, null);
        } catch (Exception e) {
            Loggy.e(TAG, "queryHostedActivities", e);
        }
        return ret;
    }

    public static List<Activity> queryPrioritizedActivities(final List<Integer> orderMaskList, final Integer numberItems) {
        List<Activity> ret = new ArrayList<>();
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] names = Activity.QUERY_FIELDS;
            builder.select(names)
                    .from(Activity.TABLE)
                    .where(Activity.ORDER_MASK, "IN", orderMaskList)
                    .where(Activity.PRIORITY, ">", 0)
                    .where(Activity.STATUS, "=", Activity.ACCEPTED)
                    .order(Activity.PRIORITY, SQLHelper.DESCEND)
                    .limit(numberItems);

            List<JSONObject> activityJsonList = builder.execSelect();

            if (activityJsonList == null) return null;
            for (JSONObject activityJson : activityJsonList) {
                Activity activity = new Activity(activityJson);
                ret.add(activity);
            }
            if (ret.size() == 0) return ret;
            appendUserInfoForActivity(ret, null);
        } catch (Exception e) {
            Loggy.e(TAG, "queryPrioritizedActivities", e);
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

    public static List<Integer> queryUserActivityRelationList(List<Long> userIdList, Long activityId) {
        try {
            if (userIdList == null) throw new UserNotFoundException();
            if (activityId == null) throw new ActivityNotFoundException();
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

            List<JSONObject> records = builder.select(UserActivityRelation.RELATION).from(UserActivityRelation.TABLE)
                    .where(UserActivityRelation.USER_ID, "IN", userIdList)
                    .where(UserActivityRelation.ACTIVITY_ID, "=", activityId)
                    .execSelect();

            if (records == null) return null;

            List<Integer> ret = new ArrayList<>();
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

    public static List<Comment> queryTopLevelComments(Long activityId, Integer pageSt, Integer pageEd, String orderKey, String orientation, Integer numItems) {
        List<Comment> ret = new ArrayList<>();
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
                    .limit((pageSt - 1) * numItems, (pageEd - pageSt + 1) * numItems);

            List<JSONObject> commentJsonList = builder.execSelect();

            if (commentJsonList == null) throw new NullPointerException();
            for (JSONObject commentJson : commentJsonList)	ret.add(new Comment(commentJson));
            if (ret.size() == 0) return ret;
            appendUserInfoForTopLevelComment(ret);

        } catch (Exception e) {
            Loggy.e(TAG, "queryTopLevelComments", e);
        }
        return ret;
    }

    public static List<Comment> querySubComments(Long parentId, String refIndex, String orderKey, String orientation, Integer numItems, Integer direction) {
        List<Comment> ret = new ArrayList<>();
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] names = {Comment.ID, Comment.CONTENT, Comment.FROM, Comment.TO, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.GENERATED_TIME};
            builder.select(names).from(Comment.TABLE).where(Comment.PARENT_ID, "=", parentId);
            List<JSONObject> commentJsonList = processAdvancedQuery(builder, refIndex, orderKey, orientation, direction, numItems);

            if (commentJsonList == null) throw new NullPointerException();
            for (JSONObject commentJson : commentJsonList)	ret.add(new Comment(commentJson));
            if (ret.size() == 0) return ret;
            appendUserInfoForSubComment(ret);
        } catch (Exception e) {
            Loggy.e(TAG, "querySubComments", e);
        }
        return ret;
    }

    public static List<Comment> querySubComments(Long parentId, Integer pageSt, Integer pageEd, String orderKey, String orientation, Integer numItems) {
        List<Comment> ret = new ArrayList<>();
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

            String[] names = {Comment.ID, Comment.CONTENT, Comment.FROM, Comment.TO, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.GENERATED_TIME};
            builder.select(names)
                    .from(Comment.TABLE)
                    .where(Comment.PARENT_ID, "=", parentId)
                    .order(orderKey, orientation)
                    .limit((pageSt - 1) * numItems, (pageEd -pageSt + 1) * numItems);

            List<JSONObject> commentJsonList = builder.execSelect();

            if (commentJsonList == null) throw new NullPointerException();
            for (JSONObject commentJson : commentJsonList)	ret.add(new Comment(commentJson));
            if (ret.size() == 0) return ret;
            appendUserInfoForSubComment(ret);
        } catch (Exception e) {
            Loggy.e(TAG, "querySubComments", e);
        }
        return ret;
    }

    public static List<Assessment> queryAssessmentList(Integer pageSt, Integer pageEd, Integer numItems, String orderKey, String orientation, Long viewerId, Long to) {
        List<Assessment> ret = new ArrayList<>();
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] names = {Assessment.ID, Assessment.CONTENT, Assessment.CONTENT, Assessment.FROM, Assessment.ACTIVITY_ID, Assessment.TO, Assessment.GENERATED_TIME};

            List<JSONObject> records = builder.select(names)
                    .from(Assessment.TABLE)
                    .where(Assessment.TO, "=", to)
                    .order(orderKey, orientation)
                    .limit((pageSt - 1) * numItems, (pageEd - pageSt + 1) * numItems).execSelect();

            if (records == null) return ret;
            for (JSONObject record : records)	ret.add(new Assessment(record));
            if (ret.size() == 0) return ret;
            appendUserInfoForAssessemnt(ret);
        } catch(Exception e) {
            Loggy.e(TAG, "queryAssessmentList", e);
        }
        return ret;
    }

    public static List<Assessment> queryAssessments(String refIndex, String orderKey, String orientation, Integer numItems, Integer direction, Long from, Long to, Long activityId) {
        List<Assessment> ret = new ArrayList<>();
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
            if (ret.size() == 0) return ret;
            appendUserInfoForAssessemnt(ret);
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

    public static List<Notification> queryNotifications(Long to, Integer isRead, Integer pageSt, Integer pageEd, String orderKey, String orientation, Integer numItems) {
        List<Notification> ret = new ArrayList<>();

        EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

        String[] names = {Notification.ID, Notification.IS_READ, Notification.ACTIVITY_ID, Notification.CONTENT, Notification.FROM, Notification.TO, Notification.CMD, Notification.GENERATED_TIME};
        builder.select(names)
                .from(Notification.TABLE)
                .where(Notification.TO, "=", to)
                .order(orderKey, orientation)
                .limit((pageSt - 1) * numItems, (pageEd - pageSt + 1) * numItems);

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

    public static boolean isActivityEditable(final Long userId, final Activity activity) throws UserNotFoundException, ActivityNotFoundException, AccessDeniedException, InvalidActivityStatusException {
        if (userId == null) throw new UserNotFoundException();
        if (activity == null) throw new ActivityNotFoundException();
        if (!validateOwnership(userId, activity)) throw new AccessDeniedException();
        if (activity.getStatus() != Activity.CREATED && activity.getStatus() != Activity.REJECTED) throw new InvalidActivityStatusException();
        return true;
    }

    public static boolean isActivityJoinable(final Long userId, final int activityId) throws UserNotFoundException, ActivityNotFoundException, InvalidActivityStatusException, InvalidUserActivityRelationException, DeadlineHasPassedException, AccessDeniedException {
        if (userId == null) throw new UserNotFoundException();
        User user = queryUser(userId);
        if (user == null) throw new UserNotFoundException();
        Activity activity = queryActivity(activityId);
        if (activity == null) throw new ActivityNotFoundException();
        return isActivityJoinable(user, activity);
    }

    public static boolean isActivityJoinable(final User user, final Activity activity) throws UserNotFoundException, InvalidUserActivityRelationException, InvalidActivityStatusException, ActivityNotFoundException, DeadlineHasPassedException, AccessDeniedException {
        /**
         * TODO: enable VISITOR group checking
         * */
        if (user == null) throw new UserNotFoundException();
//        if (user.getGroupId() == User.VISITOR) throw  new AccessDeniedException();
        if (activity.getStatus() != Activity.ACCEPTED) throw new InvalidActivityStatusException();
        if (activity.isDeadlineExpired()) throw new DeadlineHasPassedException();
        int relation = queryUserActivityRelation(user.getId(), activity.getId());
        if (relation != UserActivityRelation.INVALID) throw new InvalidUserActivityRelationException();
        return true;
    }

    public static boolean isActivityCommentable(final Long from, Long activityId) throws UserNotFoundException, ActivityNotFoundException, ActivityHasNotBegunException, ActivityNotAcceptedException, AccessDeniedException {
        if (from == null) throw new UserNotFoundException();
        if (activityId == null) throw new ActivityNotFoundException();
        User fromUser = queryUser(from);
        if (fromUser == null) throw new UserNotFoundException();
        Activity activity = queryActivity(activityId);
        if (activity == null) throw new ActivityNotFoundException();
        return isActivityCommentable(fromUser, activity);
    }

    public static boolean isActivityCommentable(final User fromUser, final Activity activity) throws UserNotFoundException, ActivityNotFoundException, ActivityHasNotBegunException, ActivityNotAcceptedException, AccessDeniedException {
        /**
         * TODO: enable VISITOR group checking
         * */
        if (fromUser == null) throw new UserNotFoundException();
        if (activity == null) throw new ActivityNotFoundException();
//        if (fromUser.getGroupId() == User.VISITOR) throw new AccessDeniedException();
        if (activity.hasBegun()) throw new ActivityHasNotBegunException();
        if (activity.getStatus() != Activity.ACCEPTED) throw new ActivityNotAcceptedException();
        return true;
    }

    public static boolean isActivityCommentable(final Long from, final Long to, final Long activityId) throws UserNotFoundException, ActivityNotFoundException, ActivityHasNotBegunException, ActivityNotAcceptedException, AccessDeniedException {
        if (from == null) throw new UserNotFoundException();
        if (to == null) throw new UserNotFoundException();
        if (activityId == null) throw new ActivityNotFoundException();
        User fromUser = queryUser(from);
        User toUser = queryUser(to);
        if (fromUser == null || toUser == null) throw new UserNotFoundException();
        Activity activity = queryActivity(activityId);
        if (activity == null) throw new ActivityNotFoundException();
        return isActivityCommentable(fromUser, toUser, activity);
    }

    public static boolean isActivityCommentable(final User fromUser, final User toUser, final Activity activity) throws UserNotFoundException, ActivityHasNotBegunException, ActivityNotFoundException, ActivityNotAcceptedException, AccessDeniedException {
        /**
         * TODO: enable VISITOR group checking
         * */
        if (fromUser == null || toUser == null) throw new UserNotFoundException();
//		if (fromUser.getGroupId() == User.VISITOR || toUser.getGroupId() == User.VISITOR) throw new AccessDeniedException();
        if (activity == null) throw new ActivityNotFoundException();
        if (activity.hasBegun()) throw new ActivityHasNotBegunException();
        if (activity.getStatus() != Activity.ACCEPTED) throw new ActivityNotAcceptedException();
        return true;
    }

    public static boolean isUserAssessable(final Long from, final Long to, final Long activityId) throws UserNotFoundException, InvalidAssessmentBehaviourException, ActivityNotFoundException, ActivityHasNotBegunException, InvalidUserActivityRelationException, AccessDeniedException {
        if (from == null) throw new UserNotFoundException();
        if (to == null) throw new UserNotFoundException();
        if (from.equals(to)) throw new InvalidAssessmentBehaviourException();

        User fromUser = queryUser(from);
        User toUser = queryUser(to);
        if (fromUser == null || toUser == null) throw new UserNotFoundException();

        if (activityId == null) throw new ActivityNotFoundException();
        Activity activity = queryActivity(activityId);
        if (activity == null) throw new ActivityNotFoundException();
        return isUserAssessable(fromUser, toUser, activity);
    }

    public static boolean isUserAssessable(final User fromUser, final User toUser, final Activity activity) throws UserNotFoundException, InvalidAssessmentBehaviourException, ActivityNotFoundException, ActivityHasNotBegunException, InvalidUserActivityRelationException, AccessDeniedException {
        /**
         * TODO: enable VISITOR group checking
         * */
        if (fromUser == null || toUser == null) throw new UserNotFoundException();
        if (fromUser.getId().equals(toUser.getId())) throw new InvalidAssessmentBehaviourException();
//        if (fromUser.getGroupId() == User.VISITOR || toUser.getGroupId() == User.VISITOR) throw new AccessDeniedException();
        if (activity == null) throw new ActivityNotFoundException();
        if (!activity.hasBegun()) throw new ActivityHasNotBegunException();
        int relation1 = queryUserActivityRelation(fromUser.getId(), activity.getId());
        int relation2 = queryUserActivityRelation(toUser.getId(), activity.getId());
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

    public static boolean acceptActivity(final User user, final Activity activity) {
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

    public static boolean rejectActivity(final User user, final Activity activity) {
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

    public static List<BasicUser> queryUsers(final long activityId, final List<Integer> maskedRelationList) {
        List<BasicUser> users = new ArrayList<>();
        if (maskedRelationList == null || maskedRelationList.size() == 0) return users;
        try {
            String[] onCols = {UserActivityRelation.ACTIVITY_ID, UserActivityRelation.USER_ID, UserActivityRelation.RELATION};
            String[] onOps = {"=", "=", "IN"};
            Object[] onVals = {activityId, new EasyPreparedStatementBuilder.PrimaryTableField(User.ID), maskedRelationList};

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> records = builder.select(User.QUERY_FILEDS)
                    .from(User.TABLE)
                    .join(UserActivityRelation.TABLE, onCols, onOps, onVals)
                    .execSelect();

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

    public static List<SpecialUserRecord> queryUsers(final List<Long> activityIdList, final List<Integer> maskedRelationList) {
        List<SpecialUserRecord> ret = new ArrayList<>();
        if (activityIdList == null || activityIdList.size() == 0) return ret;
        if (maskedRelationList == null || maskedRelationList.size() == 0) return ret;
        try {
            String[] onCols = {UserActivityRelation.ACTIVITY_ID, UserActivityRelation.USER_ID, UserActivityRelation.RELATION};
            String[] onOps = {"IN", "=", "IN"};
            Object[] onVals = {activityIdList, new EasyPreparedStatementBuilder.PrimaryTableField(User.ID), maskedRelationList};

            List<String> fields = new ArrayList<>();
            for (String field : User.QUERY_FILEDS) fields.add(field);
            fields.add(UserActivityRelation.ACTIVITY_ID);
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> records = builder.select(fields)
                    .from(User.TABLE)
                    .join(UserActivityRelation.TABLE, onCols, onOps, onVals).execSelect();

            if (records == null) throw new NullPointerException();

            for (JSONObject record : records) {
                ret.add(new SpecialUserRecord(record));
            }
        } catch (Exception e) {
            Loggy.e(TAG, "queryUsers", e);
        }
        return ret;
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
            Loggy.e(TAG, "updateUserActivityRelation", e);
        }
        return false;
    }

    public static boolean updateUserActivityRelation(List<Long> userIdList, Long activityId, int relation) {
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.update(UserActivityRelation.TABLE)
                    .set(UserActivityRelation.RELATION, relation)
                    .where(UserActivityRelation.ACTIVITY_ID, "=", activityId)
                    .where(UserActivityRelation.USER_ID, "IN", userIdList);

            if ((relation & UserActivityRelation.SELECTED) > 0) builder.set(UserActivityRelation.LAST_SELECTED_TIME, General.millisec());
            return builder.execUpdate();

        } catch (Exception e) {
            Loggy.e(TAG, "updateUserActivityRelation", e);
        }
        return false;
    }

    public static List<BasicUser> queryAppliedParticipants(long activityId) {
        List<Integer> relationList = new ArrayList<>();
        for (int relation : UserActivityRelation.APPLIED_STATES) relationList.add(relation);
        return queryUsers(activityId, relationList);
    }

    public static List<BasicUser> querySelectedParticipants(long activityId) {
        List<Integer> relationList = new ArrayList<>();
        for (int relation : UserActivityRelation.SELECTED_STATES) relationList.add(relation);
        return queryUsers(activityId, relationList);
    }

    public static List<BasicUser> queryPresentParticipants(long activityId) {
        List<Integer> relationList = new ArrayList<>();
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

    public static boolean appendUserInfoForActivity(final List<Activity> activityList, final Long viewerId) {
        // host and viewer
        List<Long> hostIdList = new ArrayList<>();
        for (Activity activity : activityList) {
            hostIdList.add(activity.getHostId());
        }
        List<User> hostList = queryUserList(hostIdList);
        if (hostList == null) return false;

        Map<Long, User> tmp = new HashMap<>();
        for (User host : hostList) {
            tmp.put(host.getId(), host);
        }

        User viewer = (viewerId == null ? null : queryUser(viewerId));
        for (Activity activity : activityList) {
            User host = tmp.get(activity.getHostId());
            activity.setHost(host);
            if (viewer != null) activity.setViewer(viewer);
        }
        return true;
    }

    public static boolean appendImageInfoForActivity(final List<Activity> activityList) {
        Map<Long, Activity> tmp = new HashMap<>();
        List<Long> activityIdList = new ArrayList<>();
        for (Activity activity : activityList) {
            tmp.put(activity.getId(), activity);
            activityIdList.add(activity.getId());
        }
        List<Image> imageList = ExtraCommander.queryImages(activityIdList);
        for (Image image : imageList) {
            Activity activity = tmp.get(image.getMetaId());
            activity.addImage(image);
        }
        return true;
    }

    public static boolean appendParticipantInfoForActivity(final List<Activity> activityList) {
        Map<Long, Activity> tmp = new HashMap<>();
        List<Long> activityIdList = new ArrayList<>();
        for (Activity activity : activityList) {
            tmp.put(activity.getId(), activity);
            activityIdList.add(activity.getId());
        }
        List<Integer> selectedStates = new LinkedList<>();
        for (Integer state : UserActivityRelation.SELECTED_STATES) selectedStates.add(state);

        List<SpecialUserRecord> selectedList = queryUsers(activityIdList, selectedStates);
        for (SpecialUserRecord record : selectedList) {
            Activity activity = tmp.get(record.activityId);
            activity.addSelectedParticipant(record.user);
        }
        return true;
    }

    public static boolean appendUserInfoForTopLevelComment(final List<Comment> commentList) {
        List<Long> userIdList = new ArrayList<>();
        for (Comment comment : commentList) {
            userIdList.add(comment.getFrom());
            // TODO: optimization by "GROUP BY" limits? reference: http://www.xaprb.com/blog/2006/12/07/how-to-select-the-firstleastmax-row-per-group-in-sql/
            comment.setSubCommentList(querySubComments(comment.getId(), DBCommander.INITIAL_REF_INDEX, Comment.ID, SQLHelper.DESCEND, 3, DBCommander.DIRECTION_FORWARD));
        }

        // for top level comments
        List<User> userList = queryUserList(userIdList);
        if (userList == null) return false;
        Map<Long, User> tmp = new HashMap<>();
        for (User fromUser : userList) {
            tmp.put(fromUser.getId(), fromUser);
        }
        for (Comment comment : commentList) {
            User user = tmp.get(comment.getFrom());
            comment.setFromUser(user);
        }

        return true;
    }

    public static boolean appendUserInfoForSubComment(final List<Comment> subCommentList) {
        if (subCommentList == null) return false;

        List<Long> fromList = new ArrayList<>();
        List<Long> toList = new ArrayList<>();
        for (Comment comment : subCommentList) {
            fromList.add(comment.getFrom());
            toList.add(comment.getTo());
        }

        if (fromList.size() != toList.size()) return false;

        List<User> fromUserList = queryUserList(fromList);
        List<User> toUserList = queryUserList(toList);

        Map<Long, User> tmpFrom = new HashMap<>();
        for (User fromUser : fromUserList) {
            tmpFrom.put(fromUser.getId(), fromUser);
        }

        Map<Long, User> tmpTo = new HashMap<>();
        for (User toUser: toUserList) {
            tmpTo.put(toUser.getId(), toUser);
        }

        for (Comment comment : subCommentList) {
            User fromUser = tmpFrom.get(comment.getFrom());
            User toUser = tmpTo.get(comment.getTo());
            comment.setFromUser(fromUser);
            comment.setToUser(toUser);
        }
        return true;
    }

    public static boolean appendUserInfoForAssessemnt(final List<Assessment> assessmentList) {
        if (assessmentList == null) return false;

        List<Long> fromList = new ArrayList<>();
        List<Long> toList = new ArrayList<>();
        for (Assessment assessment : assessmentList) {
            fromList.add(assessment.getFrom());
            toList.add(assessment.getTo());
        }

        if (fromList.size() != toList.size()) return false;

        List<User> fromUserList = queryUserList(fromList);
        List<User> toUserList = queryUserList(toList);

        Map<Long, User> tmpFrom = new HashMap<>();
        for (User fromUser : fromUserList) {
            tmpFrom.put(fromUser.getId(), fromUser);
        }

        Map<Long, User> tmpTo = new HashMap<>();
        for (User toUser: toUserList) {
            tmpTo.put(toUser.getId(), toUser);
        }

        for (Assessment assessment : assessmentList) {
            User fromUser = tmpFrom.get(assessment.getFrom());
            User toUser = tmpTo.get(assessment.getTo());
            assessment.setFromUser(fromUser);
            assessment.setToUser(toUser);
        }
        return true;
    }

    public static PermForeignParty queryPermForeignParty(final Long partyId, final Integer party) {
        try {
            String[] names = PermForeignParty.QUERY_FIELDS;
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> results = builder.select(names)
                    .from(PermForeignParty.TABLE)
                    .where(PermForeignParty.ID, "=", partyId)
                    .where(PermForeignParty.PARTY, "=", party)
                    .execSelect();
            if (results == null || results.size() != 1) throw new NullPointerException();
            JSONObject json = results.get(0);
            return new PermForeignParty(json);
        } catch (Exception e) {
            Loggy.e(TAG, "queryPermForeignParty", e);
        }
        return null;
    }

    public static TempForeignParty queryTempForeignParty(final String accessToken, final Integer party) {
        try {
            String[] names = TempForeignParty.QUERY_FIELDS;
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> results = builder.select(names)
                    .from(TempForeignParty.TABLE)
                    .where(TempForeignParty.ACCESS_TOKEN, "=", accessToken)
                    .where(TempForeignParty.PARTY, "=", party)
                    .execSelect();
            if (results == null || results.size() != 1) throw new NullPointerException();
            JSONObject json = results.get(0);
            return new TempForeignParty(json);
        } catch (Exception e) {
            Loggy.e(TAG, "queryTempForeignParty", e);
        }
        return null;
    }

    public static boolean createTempForeignParty(final String accessToken, final Integer party, final Long partyId, final String email) {
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] cols = {TempForeignParty.ACCESS_TOKEN, TempForeignParty.PARTY, TempForeignParty.PARTY_ID, TempForeignParty.EMAIL, TempForeignParty.TIMESTAMP};
            Object[] vals = {accessToken, party, partyId, email, General.millisec()};
            builder.insert(cols, vals).into(TempForeignParty.TABLE).execInsert();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
