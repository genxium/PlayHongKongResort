package controllers;

import dao.SQLBuilder;
import dao.SQLHelper;
import dao.SimpleMap;
import exception.*;
import models.*;
import utilities.Converter;
import utilities.DataUtils;
import utilities.General;
import utilities.Loggy;

import java.util.*;

/*
 * Note that the relation (a.k.a PlayerActivityRelation.RELATION) in this class is always referring to masked relation
 * */

public class DBCommander {

        public static final String TAG = DBCommander.class.getName();

        public static final String INITIAL_REF_INDEX = "0";
        public static final int DIRECTION_FORWARD = (+1);
        public static final int DIRECTION_BACKWARD = (-1);

        public static class SpecialPlayerRecord {
                public BasicPlayer player;
                public Long activityId;

                public SpecialPlayerRecord(final SimpleMap data) {
                        // record must be guaranteed to contain correct fields
                        player = new BasicPlayer(data);
                        activityId = data.getLong(PlayerActivityRelation.ACTIVITY_ID);
                }
        }

        public static Player queryPlayer(final Long playerId) {
                try {
                        final String[] names = Player.QUERY_FILEDS;
                        final SQLBuilder builder = new SQLBuilder();
                        final List<SimpleMap> results = builder.select(names).from(Player.TABLE).where(Player.ID, "=", playerId).execSelect();
                        if (results == null || results.size() <= 0) return null;
                        final Iterator<SimpleMap> it = results.iterator();
                        if (!it.hasNext()) return null;
                        final SimpleMap playerData = it.next();
                        return new Player(playerData);
                } catch (Exception e) {
                        Loggy.e(TAG, "queryPlayerList", e);
                }
                return null;
        }

        public static List<Player> queryPlayerList(final List<Long> playerIdList) {
                List<Player> ret = new ArrayList<>();
                try {
                        if (playerIdList == null || playerIdList.size() == 0) return ret;
                        String[] names = Player.QUERY_FILEDS;
                        SQLBuilder builder = new SQLBuilder();
                        List<SimpleMap> records = builder.select(names).from(Player.TABLE).where(Player.ID, "IN", playerIdList).execSelect();
                        if (records == null || records.size() <= 0) return null;
                        for (SimpleMap record : records) {
                                ret.add(new Player(record));
                        }
                } catch (Exception e) {
                        Loggy.e(TAG, "queryPlayerList", e);
                }
                return ret;
        }

        public static Player queryPlayerByEmail(String email) {
                Player player = null;
                try {
                        String[] names = Player.QUERY_FILEDS;
                        SQLBuilder builder = new SQLBuilder();
                        List<SimpleMap> results = builder.select(names).from(Player.TABLE).where(Player.EMAIL, "=", email).execSelect();
                        if (results == null || results.size() <= 0) return null;
                        Iterator<SimpleMap> it = results.iterator();
                        if (!it.hasNext()) return null;
                        SimpleMap playerData = it.next();
                        player = new Player(playerData);
                } catch (Exception e) {
                        Loggy.e(TAG, "queryPlayerByEmail", e);

                }
                return player;
        }

        public static long registerPlayer(final Player player) {
                try {
                        String[] cols = {Player.EMAIL, Player.PASSWORD, Player.NAME, Player.GROUP_ID, Player.VERIFICATION_CODE, Player.SALT};
                        Object[] vals = {player.getEmail(), player.getPassword(), player.getName(), player.getGroupId(), player.getVerificationCode(), player.getSalt()};

                        SQLBuilder builder = new SQLBuilder();
                        return builder.insert(cols, vals).into(Player.TABLE).execInsert();
                } catch (Exception e) {
                        Loggy.e(TAG, "registerPlayer", e);
                }
                return SQLHelper.INVALID;
        }

        public static boolean updatePlayer(final Player player) {
                try {
                        String[] cols = {Player.EMAIL, Player.PASSWORD, Player.NAME, Player.AVATAR, Player.GROUP_ID, Player.AGE, Player.GENDER, Player.MOOD, Player.VERIFICATION_CODE};
                        Object[] values = {player.getEmail(), player.getPassword(), player.getName(), player.getAvatar(), player.getGroupId(), player.getAge(), player.getGender(), player.getMood(), player.getVerificationCode()};

                        SQLBuilder builder = new SQLBuilder();
                        return builder.update(Player.TABLE).set(cols, values).where(Player.ID, "=", player.getId()).execUpdate();
                } catch (Exception e) {
                        Loggy.e(TAG, "updatePlayer", e);
                }
                return false;
        }

        public static Activity createActivity(final Player host, final long now) throws ActivityCreationLimitException {

                if (!ableToCreateActivity(host, now)) throw new ActivityCreationLimitException();

                Long lastActivityId = null;

                String[] names = {Activity.HOST_ID, Activity.CREATED_TIME};
                Object[] values = {host.getId(), now};

                SQLBuilder builderActivity = new SQLBuilder();
                lastActivityId = Converter.toLong(builderActivity.insert(names, values).into(Activity.TABLE).execInsert());
                if (lastActivityId == null || lastActivityId.equals(SQLHelper.INVALID)) return null;

                Activity activity = new Activity();
                activity.setId(lastActivityId);
                activity.setHost(host);
                activity.setCreatedTime(now);

                String[] names2 = {PlayerActivityRelation.ACTIVITY_ID, PlayerActivityRelation.PLAYER_ID, PlayerActivityRelation.RELATION, PlayerActivityRelation.GENERATED_TIME};
                Object[] values2 = {lastActivityId, host.getId(), PlayerActivityRelation.SELECTED | PlayerActivityRelation.PRESENT, now};

                SQLBuilder builderRelation = new SQLBuilder();
                builderRelation.insert(names2, values2).into(PlayerActivityRelation.TABLE).execInsert();

                return activity;
        }

        protected static boolean ableToCreateActivity(final Player host, final long now) {
                if (host == null || host.getGroupId() == Player.VISITOR) return false;
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
                        SQLBuilder builder = new SQLBuilder();
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
                        SQLBuilder builder = new SQLBuilder();
                        List<SimpleMap> results = builder.select(names).from(Activity.TABLE).where(Activity.ID, "=", activityId).execSelect();
                        if (results == null || results.size() != 1) throw new ActivityNotFoundException();
                        SimpleMap activityData = results.get(0);
                        Player host = queryPlayer(activityData.getLong(Activity.HOST_ID));
                        Activity activity = new Activity(activityData);
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
                        SQLBuilder builder = new SQLBuilder();
                        String[] names = Activity.QUERY_FIELDS;
                        String[] onCols = {PlayerActivityRelation.PLAYER_ID, PlayerActivityRelation.ACTIVITY_ID, PlayerActivityRelation.RELATION};
                        String[] onOps = {"=", "=", "IN"};

                        Object[] onVals = {vieweeId, new SQLBuilder.PrimaryTableField(Activity.ID), maskedRelationList};

                        List<SimpleMap> activityDataList = builder.select(names)
                                .from(Activity.TABLE)
                                .join(PlayerActivityRelation.TABLE, onCols, onOps, onVals)
                                .order(orderKey, orientation)
                                .limit((pageSt - 1) * numItems, (pageEd - pageSt + 1) * numItems).execSelect();
                        if (activityDataList == null) return null;

                        for (SimpleMap activityData : activityDataList) {
                                Activity activity = new Activity(activityData);
                                ret.add(activity);
                        }
                        if (ret.size() == 0) return ret;
                        appendPlayerInfoForActivity(ret, null);
                } catch (Exception e) {
                        Loggy.e(TAG, "queryActivities", e);
                }
                return ret;
        }

        public static List<Activity> queryActivities(final Integer pageSt, final Integer pageEd, final String orderKey, final String orientation, final Integer numItems, final int status, final int offset) {
                List<Activity> ret = new ArrayList<>();
                try {
                        SQLBuilder builder = new SQLBuilder();
                        String[] names = Activity.QUERY_FIELDS;
                        builder.select(names)
                                .from(Activity.TABLE)
                                .order(orderKey, orientation)
                                .where(Activity.STATUS, "=", status)
                                .where(Activity.PRIORITY, "=", 0)
                                .limit((pageSt - 1) * numItems, (pageEd - pageSt + 1) * numItems - offset);
                        if (status == Activity.PENDING) {
                                // ONLY admin queries should be accessing this closure
                                builder.where(Activity.DEADLINE, ">", General.millisec());
                        }
                        List<SimpleMap> activityDataList = builder.execSelect();
                        if (activityDataList == null) return ret;

                        for (SimpleMap activityData : activityDataList) {
                                Activity activity = new Activity(activityData);
                                ret.add(activity);
                        }
                        if (ret.size() == 0) return ret;
                        appendPlayerInfoForActivity(ret, null);
                } catch (Exception e) {
                        Loggy.e(TAG, "queryActivities", e);
                }
                return ret;
        }

        public static List<Activity> queryHostedActivities(final Long hostId, final Long viewerId, final Integer pageSt, final Integer pageEd, final String orderKey, final String orientation, final Integer numItems) {
                List<Activity> ret = new ArrayList<>();
                try {
                        SQLBuilder builder = new SQLBuilder();
                        String[] names = Activity.QUERY_FIELDS;
                        builder.select(names)
                                .from(Activity.TABLE)
                                .order(orderKey, orientation)
                                .where(Activity.HOST_ID, "=", hostId)
                                .limit((pageSt - 1) * numItems, (pageEd - pageSt + 1) * numItems);

                        if (viewerId == null || !hostId.equals(viewerId)) builder.where(Activity.STATUS, "=", Activity.ACCEPTED);

                        List<SimpleMap> activityDataList = builder.execSelect();

                        if (activityDataList == null) return null;
                        for (SimpleMap activityData : activityDataList) {
                                Activity activity = new Activity(activityData);
                                ret.add(activity);
                        }
                        if (ret.size() == 0) return ret;
                        appendPlayerInfoForActivity(ret, null);
                } catch (Exception e) {
                        Loggy.e(TAG, "queryHostedActivities", e);
                }
                return ret;
        }

        public static List<Activity> queryPrioritizedActivities(final List<Integer> orderMaskList, final Integer numberItems) {
                List<Activity> ret = new ArrayList<>();
                try {
                        SQLBuilder builder = new SQLBuilder();
                        String[] names = Activity.QUERY_FIELDS;
                        builder.select(names)
                                .from(Activity.TABLE)
                                .where(Activity.ORDER_MASK, "IN", orderMaskList)
                                .where(Activity.PRIORITY, ">", 0)
                                .where(Activity.STATUS, "=", Activity.ACCEPTED)
                                .order(Activity.PRIORITY, SQLHelper.DESCEND)
                                .limit(numberItems);

                        List<SimpleMap> activityDataList = builder.execSelect();

                        if (activityDataList == null || activityDataList.size() == 0) return ret;
                        for (SimpleMap activityData : activityDataList) {
                                Activity activity = new Activity(activityData);
                                ret.add(activity);
                        }
                        appendPlayerInfoForActivity(ret, null);
                } catch (Exception e) {
                        Loggy.e(TAG, "queryPrioritizedActivities", e);
                }
                return ret;
        }

        public static int queryPlayerActivityRelation(Long playerId, Long activityId) {
                try {
                        if (playerId == null) throw new PlayerNotFoundException();
                        if (activityId == null) throw new ActivityNotFoundException();
                        SQLBuilder builder = new SQLBuilder();

                        List<SimpleMap> records = builder.select(PlayerActivityRelation.RELATION).from(PlayerActivityRelation.TABLE)
                                .where(PlayerActivityRelation.PLAYER_ID, "=", playerId)
                                .where(PlayerActivityRelation.ACTIVITY_ID, "=", activityId)
                                .execSelect();

                        if (records == null) return PlayerActivityRelation.INVALID;
                        if (records.size() != 1) return PlayerActivityRelation.INVALID;
                        SimpleMap record = records.get(0);
                        return (Integer) record.getInt(PlayerActivityRelation.RELATION);
                } catch (Exception e) {
                        Loggy.e(TAG, "queryPlayerActivityRelation", e);
                }
                return PlayerActivityRelation.INVALID;
        }

        public static List<Integer> queryPlayerActivityRelationList(List<Long> playerIdList, Long activityId) {
                List<Integer> ret = new ArrayList<>();
                try {
                        if (playerIdList == null) throw new PlayerNotFoundException();
                        if (activityId == null) throw new ActivityNotFoundException();
                        SQLBuilder builder = new SQLBuilder();

                        List<SimpleMap> records = builder.select(PlayerActivityRelation.RELATION).from(PlayerActivityRelation.TABLE)
                                .where(PlayerActivityRelation.PLAYER_ID, "IN", playerIdList)
                                .where(PlayerActivityRelation.ACTIVITY_ID, "=", activityId)
                                .execSelect();

                        if (records == null) return ret;
                        for (SimpleMap record : records) ret.add(record.getInt(PlayerActivityRelation.RELATION));
                } catch (Exception e) {
                        Loggy.e(TAG, "queryPlayerActivityRelation", e);
                }
                return ret;
        }

        public static Comment queryComment(Integer commentId) {
                try {
                        SQLBuilder builder = new SQLBuilder();
                        String[] names = {Comment.ID, Comment.CONTENT, Comment.FROM, Comment.TO, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.NUM_CHILDREN, Comment.GENERATED_TIME};
                        List<SimpleMap> commentDataList = builder.select(names).from(Comment.TABLE).where(Comment.ID, "=", commentId).execSelect();
                        if (commentDataList == null || commentDataList.size() <= 0) throw new NullPointerException();
                        return new Comment(commentDataList.get(0));
                } catch (Exception e) {
                        Loggy.e(TAG, "queryComment", e);
                }
                return null;
        }

        public static List<Comment> queryTopLevelComments(Long activityId, Integer pageSt, Integer pageEd, String orderKey, String orientation, Integer numItems) {
                List<Comment> ret = new ArrayList<>();
                try {
                        SQLBuilder builder = new SQLBuilder();

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

                        List<SimpleMap> commentDataList = builder.execSelect();

                        if (commentDataList == null) throw new NullPointerException();
                        for (SimpleMap commentData : commentDataList) ret.add(new Comment(commentData));
                        if (ret.size() == 0) return ret;
                        appendPlayerInfoForTopLevelComment(ret);

                } catch (Exception e) {
                        Loggy.e(TAG, "queryTopLevelComments", e);
                }
                return ret;
        }

        public static List<Comment> querySubComments(Long parentId, String refIndex, String orderKey, String orientation, Integer numItems, Integer direction) {
                List<Comment> ret = new ArrayList<>();
                try {
                        SQLBuilder builder = new SQLBuilder();
                        String[] names = {Comment.ID, Comment.CONTENT, Comment.FROM, Comment.TO, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.GENERATED_TIME};
                        builder.select(names).from(Comment.TABLE).where(Comment.PARENT_ID, "=", parentId);
                        List<SimpleMap> commentDataList = processAdvancedQuery(builder, refIndex, orderKey, orientation, direction, numItems);

                        if (commentDataList == null) throw new NullPointerException();
                        for (SimpleMap commentData : commentDataList) ret.add(new Comment(commentData));
                        if (ret.size() == 0) return ret;
                        appendPlayerInfoForSubComment(ret);
                } catch (Exception e) {
                        Loggy.e(TAG, "querySubComments", e);
                }
                return ret;
        }

        public static List<Comment> querySubComments(Long parentId, Integer pageSt, Integer pageEd, String orderKey, String orientation, Integer numItems) {
                List<Comment> ret = new ArrayList<>();
                try {
                        SQLBuilder builder = new SQLBuilder();

                        String[] names = {Comment.ID, Comment.CONTENT, Comment.FROM, Comment.TO, Comment.PARENT_ID, Comment.PREDECESSOR_ID, Comment.ACTIVITY_ID, Comment.GENERATED_TIME};
                        builder.select(names)
                                .from(Comment.TABLE)
                                .where(Comment.PARENT_ID, "=", parentId)
                                .order(orderKey, orientation)
                                .limit((pageSt - 1) * numItems, (pageEd - pageSt + 1) * numItems);

                        List<SimpleMap> commentDataList = builder.execSelect();

                        if (commentDataList == null) throw new NullPointerException();
                        for (SimpleMap commentData : commentDataList) ret.add(new Comment(commentData));
                        if (ret.size() == 0) return ret;
                        appendPlayerInfoForSubComment(ret);
                } catch (Exception e) {
                        Loggy.e(TAG, "querySubComments", e);
                }
                return ret;
        }

        public static List<Assessment> queryAssessmentList(Integer pageSt, Integer pageEd, Integer numItems, String orderKey, String orientation, Long viewerId, Long to) {
                List<Assessment> ret = new ArrayList<>();
                try {
                        SQLBuilder builder = new SQLBuilder();
                        String[] names = {Assessment.ID, Assessment.CONTENT, Assessment.CONTENT, Assessment.FROM, Assessment.ACTIVITY_ID, Assessment.TO, Assessment.GENERATED_TIME};

                        List<SimpleMap> records = builder.select(names)
                                .from(Assessment.TABLE)
                                .where(Assessment.TO, "=", to)
                                .order(orderKey, orientation)
                                .limit((pageSt - 1) * numItems, (pageEd - pageSt + 1) * numItems).execSelect();

                        if (records == null) return ret;
                        for (SimpleMap record : records) ret.add(new Assessment(record));
                        if (ret.size() == 0) return ret;
                        appendPlayerInfoForAssessemnt(ret);
                } catch (Exception e) {
                        Loggy.e(TAG, "queryAssessmentList", e);
                }
                return ret;
        }

        public static List<Assessment> queryAssessments(String refIndex, String orderKey, String orientation, Integer numItems, Integer direction, Long from, Long to, Long activityId) {
                List<Assessment> ret = new ArrayList<>();
                try {
                        SQLBuilder builder = new SQLBuilder();
                        String[] names = {Assessment.ID, Assessment.CONTENT, Assessment.CONTENT, Assessment.FROM, Assessment.ACTIVITY_ID, Assessment.TO, Assessment.GENERATED_TIME};
                        builder.select(names).from(Assessment.TABLE)
                                .where(Assessment.ACTIVITY_ID, "=", activityId);

                        if (from != null) builder.where(Assessment.FROM, "=", from);
                        if (to != null) builder.where(Assessment.TO, "=", to);

                        List<SimpleMap> records = processAdvancedQuery(builder, refIndex, orderKey, orientation, direction, numItems);

                        if (records == null) return ret;

                        for (SimpleMap record : records) ret.add(new Assessment(record));
                        if (ret.size() == 0) return ret;
                        appendPlayerInfoForAssessemnt(ret);
                } catch (Exception e) {
                        Loggy.e(TAG, "queryAssessments", e);
                }
                return ret;
        }

        public static boolean updateAssessment(Integer activityId, Integer from, Integer to, String content) {
                try {
                        SQLBuilder builder = new SQLBuilder();
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
                        SQLBuilder builder = new SQLBuilder();
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

                SQLBuilder builder = new SQLBuilder();

                String[] names = {Notification.ID, Notification.IS_READ, Notification.ACTIVITY_ID, Notification.CONTENT, Notification.FROM, Notification.TO, Notification.CMD, Notification.GENERATED_TIME};
                builder.select(names)
                        .from(Notification.TABLE)
                        .where(Notification.TO, "=", to)
                        .order(orderKey, orientation)
                        .limit((pageSt - 1) * numItems, (pageEd - pageSt + 1) * numItems);

                if (isRead != null) builder.where(Notification.IS_READ, "=", isRead);

                List<SimpleMap> notificationDataList = builder.execSelect();
                if (notificationDataList == null) return ret;
                for (SimpleMap notificationData : notificationDataList) {
                        Notification notification = new Notification(notificationData);
                        ret.add(notification);
                }
                return ret;
        }

        public static boolean validateOwnership(long playerId, Activity activity) {
                return (activity != null && activity.getHost().getId() == playerId);
        }

        public static boolean isActivityEditable(final Long playerId, final Activity activity) throws PlayerNotFoundException, ActivityNotFoundException, AccessDeniedException, InvalidActivityStatusException {
                if (playerId == null) throw new PlayerNotFoundException();
                if (activity == null) throw new ActivityNotFoundException();
                if (!validateOwnership(playerId, activity)) throw new AccessDeniedException();
                if (activity.getStatus() != Activity.CREATED && activity.getStatus() != Activity.REJECTED)
                        throw new InvalidActivityStatusException();
                return true;
        }

        public static boolean isActivityJoinable(final Long playerId, final int activityId) throws PlayerNotFoundException, ActivityNotFoundException, InvalidActivityStatusException, InvalidPlayerActivityRelationException, DeadlineHasPassedException, AccessDeniedException {
                if (playerId == null) throw new PlayerNotFoundException();
                Player player = queryPlayer(playerId);
                if (player == null) throw new PlayerNotFoundException();
                Activity activity = queryActivity(activityId);
                if (activity == null) throw new ActivityNotFoundException();
                return isActivityJoinable(player, activity);
        }

        public static boolean isActivityJoinable(final Player player, final Activity activity) throws PlayerNotFoundException, InvalidPlayerActivityRelationException, InvalidActivityStatusException, ActivityNotFoundException, DeadlineHasPassedException, AccessDeniedException {
                /**
                 * TODO: enable VISITOR group checking
                 * */
                if (player == null) throw new PlayerNotFoundException();
                //        if (player.getGroupId() == Player.VISITOR) throw  new AccessDeniedException();
                if (activity.getStatus() != Activity.ACCEPTED) throw new InvalidActivityStatusException();
                if (activity.isDeadlineExpired()) throw new DeadlineHasPassedException();
                int relation = queryPlayerActivityRelation(player.getId(), activity.getId());
                if (relation != PlayerActivityRelation.INVALID) throw new InvalidPlayerActivityRelationException();
                return true;
        }

        public static boolean isActivityCommentable(final Long from, Long activityId) throws PlayerNotFoundException, ActivityNotFoundException, ActivityHasNotBegunException, ActivityNotAcceptedException, AccessDeniedException {
                if (from == null) throw new PlayerNotFoundException();
                if (activityId == null) throw new ActivityNotFoundException();
                Player fromPlayer = queryPlayer(from);
                if (fromPlayer == null) throw new PlayerNotFoundException();
                Activity activity = queryActivity(activityId);
                if (activity == null) throw new ActivityNotFoundException();
                return isActivityCommentable(fromPlayer, activity);
        }

        public static boolean isActivityCommentable(final Player fromPlayer, final Activity activity) throws PlayerNotFoundException, ActivityNotFoundException, ActivityHasNotBegunException, ActivityNotAcceptedException, AccessDeniedException {
                /**
                 * TODO: enable VISITOR group checking
                 * */
                if (fromPlayer == null) throw new PlayerNotFoundException();
                if (activity == null) throw new ActivityNotFoundException();
                //        if (fromPlayer.getGroupId() == Player.VISITOR) throw new AccessDeniedException();
                if (activity.hasBegun()) throw new ActivityHasNotBegunException();
                if (activity.getStatus() != Activity.ACCEPTED) throw new ActivityNotAcceptedException();
                return true;
        }

        public static boolean isActivityCommentable(final Long from, final Long to, final Long activityId) throws PlayerNotFoundException, ActivityNotFoundException, ActivityHasNotBegunException, ActivityNotAcceptedException, AccessDeniedException {
                if (from == null) throw new PlayerNotFoundException();
                if (to == null) throw new PlayerNotFoundException();
                if (activityId == null) throw new ActivityNotFoundException();
                Player fromPlayer = queryPlayer(from);
                Player toPlayer = queryPlayer(to);
                if (fromPlayer == null || toPlayer == null) throw new PlayerNotFoundException();
                Activity activity = queryActivity(activityId);
                if (activity == null) throw new ActivityNotFoundException();
                return isActivityCommentable(fromPlayer, toPlayer, activity);
        }

        public static boolean isActivityCommentable(final Player fromPlayer, final Player toPlayer, final Activity activity) throws PlayerNotFoundException, ActivityHasNotBegunException, ActivityNotFoundException, ActivityNotAcceptedException, AccessDeniedException {
                /**
                 * TODO: enable VISITOR group checking
                 * */
                if (fromPlayer == null || toPlayer == null) throw new PlayerNotFoundException();
                //		if (fromPlayer.getGroupId() == Player.VISITOR || toPlayer.getGroupId() == Player.VISITOR) throw new AccessDeniedException();
                if (activity == null) throw new ActivityNotFoundException();
                if (activity.hasBegun()) throw new ActivityHasNotBegunException();
                if (activity.getStatus() != Activity.ACCEPTED) throw new ActivityNotAcceptedException();
                return true;
        }

        public static boolean isPlayerAssessable(final Long from, final Long to, final Long activityId) throws PlayerNotFoundException, InvalidAssessmentBehaviourException, ActivityNotFoundException, ActivityHasNotBegunException, InvalidPlayerActivityRelationException, AccessDeniedException {
                if (from == null) throw new PlayerNotFoundException();
                if (to == null) throw new PlayerNotFoundException();
                if (from.equals(to)) throw new InvalidAssessmentBehaviourException();

                Player fromPlayer = queryPlayer(from);
                Player toPlayer = queryPlayer(to);
                if (fromPlayer == null || toPlayer == null) throw new PlayerNotFoundException();

                if (activityId == null) throw new ActivityNotFoundException();
                Activity activity = queryActivity(activityId);
                if (activity == null) throw new ActivityNotFoundException();
                return isPlayerAssessable(fromPlayer, toPlayer, activity);
        }

        public static boolean isPlayerAssessable(final Player fromPlayer, final Player toPlayer, final Activity activity) throws PlayerNotFoundException, InvalidAssessmentBehaviourException, ActivityNotFoundException, ActivityHasNotBegunException, InvalidPlayerActivityRelationException, AccessDeniedException {
                /**
                 * TODO: enable VISITOR group checking
                 * */
                if (fromPlayer == null || toPlayer == null) throw new PlayerNotFoundException();
                if (fromPlayer.getId().equals(toPlayer.getId())) throw new InvalidAssessmentBehaviourException();
                //        if (fromPlayer.getGroupId() == Player.VISITOR || toPlayer.getGroupId() == Player.VISITOR) throw new AccessDeniedException();
                if (activity == null) throw new ActivityNotFoundException();
                if (!activity.hasBegun()) throw new ActivityHasNotBegunException();
                int relation1 = queryPlayerActivityRelation(fromPlayer.getId(), activity.getId());
                int relation2 = queryPlayerActivityRelation(toPlayer.getId(), activity.getId());
                if ((relation1 & PlayerActivityRelation.SELECTED) == 0 || (relation2 & PlayerActivityRelation.SELECTED) == 0)
                        throw new InvalidPlayerActivityRelationException();
                return true;
        }

        /*
           Method isActivityMarkable(...) returns PlayerActivityRelation.INVALID if the activity is not markable by
           specified player, or the original relation otherwise.
           */
        public static int isActivityMarkable(Long playerId, Long activityId, int relation) {
                int ret = PlayerActivityRelation.INVALID;
                try {
                        if (playerId == null) throw new PlayerNotFoundException();
                        if (activityId == null) throw new ActivityNotFoundException();
                        Activity activity = queryActivity(activityId);
                        if (activity == null) throw new ActivityNotFoundException();
                        ret = isActivityMarkable(playerId, activity, relation);
                } catch (Exception e) {
                        Loggy.e(TAG, "isActivityMarkable", e);
                }
                return ret;
        }

        public static int isActivityMarkable(Long playerId, Activity activity, int relation) {
                int ret = PlayerActivityRelation.INVALID;
                try {
                        if (playerId == null) throw new PlayerNotFoundException();
                        if (activity == null) throw new ActivityNotFoundException();
                        if (!activity.hasBegun()) throw new ActivityHasNotBegunException();
                        int originalRelation = queryPlayerActivityRelation(playerId, activity.getId());
                        if (originalRelation == PlayerActivityRelation.INVALID) throw new InvalidPlayerActivityRelationException();
                        if ((originalRelation & PlayerActivityRelation.SELECTED) == 0)
                                throw new InvalidPlayerActivityRelationException();
                        if ((originalRelation & relation) > 0) throw new InvalidPlayerActivityRelationException();
                        ret = originalRelation;
                } catch (Exception e) {
                        Loggy.e(TAG, "isActivityMarkable", e);
                }
                return ret;
        }

        public static boolean acceptActivity(final Player player, final Activity activity) {
                if (player == null) return false;
                if (activity == null) return false;
                try {
                        long now = General.millisec();
                        SQLBuilder builder = new SQLBuilder();
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

        public static boolean rejectActivity(final Player player, final Activity activity) {
                if (player == null) return false;
                if (activity == null) return false;
                try {
                        long now = General.millisec();
                        SQLBuilder builder = new SQLBuilder();
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

        public static List<BasicPlayer> queryPlayers(final long activityId, final List<Integer> maskedRelationList) {
                List<BasicPlayer> players = new ArrayList<>();
                if (maskedRelationList == null || maskedRelationList.size() == 0) return players;
                try {
                        String[] onCols = {PlayerActivityRelation.ACTIVITY_ID, PlayerActivityRelation.PLAYER_ID, PlayerActivityRelation.RELATION};
                        String[] onOps = {"=", "=", "IN"};
                        Object[] onVals = {activityId, new SQLBuilder.PrimaryTableField(Player.ID), maskedRelationList};

                        SQLBuilder builder = new SQLBuilder();
                        List<SimpleMap> records = builder.select(Player.QUERY_FILEDS)
                                .from(Player.TABLE)
                                .join(PlayerActivityRelation.TABLE, onCols, onOps, onVals)
                                .execSelect();

                        if (records == null) throw new NullPointerException();

                        for (SimpleMap playerData : records) {
                                BasicPlayer player = new BasicPlayer(playerData);
                                players.add(player);
                        }
                } catch (Exception e) {
                        Loggy.e(TAG, "queryPlayers", e);
                }
                return players;
        }

        public static List<SpecialPlayerRecord> queryPlayers(final List<Long> activityIdList, final List<Integer> maskedRelationList) {
                List<SpecialPlayerRecord> ret = new ArrayList<>();
                if (activityIdList == null || activityIdList.size() == 0) return ret;
                if (maskedRelationList == null || maskedRelationList.size() == 0) return ret;
                try {
                        String[] onCols = {PlayerActivityRelation.ACTIVITY_ID, PlayerActivityRelation.PLAYER_ID, PlayerActivityRelation.RELATION};
                        String[] onOps = {"IN", "=", "IN"};
                        Object[] onVals = {activityIdList, new SQLBuilder.PrimaryTableField(Player.ID), maskedRelationList};

                        List<String> fields = new ArrayList<>();
                        for (String field : Player.QUERY_FILEDS) fields.add(field);
                        fields.add(PlayerActivityRelation.ACTIVITY_ID);
                        SQLBuilder builder = new SQLBuilder();
                        List<SimpleMap> records = builder.select(fields)
							.from(Player.TABLE)
							.join(PlayerActivityRelation.TABLE, onCols, onOps, onVals).execSelect();

                        if (records == null) throw new NullPointerException();

                        for (SimpleMap record : records) {
                                ret.add(new SpecialPlayerRecord(record));
                        }
                } catch (Exception e) {
                        Loggy.e(TAG, "queryPlayers", e);
                }
                return ret;
        }

        public static boolean updatePlayerActivityRelation(Long playerId, Long activityId, int relation) {
                try {
                        SQLBuilder builder = new SQLBuilder();
                        builder.update(PlayerActivityRelation.TABLE)
                                .set(PlayerActivityRelation.RELATION, relation)
                                .where(PlayerActivityRelation.ACTIVITY_ID, "=", activityId)
                                .where(PlayerActivityRelation.PLAYER_ID, "=", playerId);

                        if ((relation & PlayerActivityRelation.SELECTED) > 0)
                                builder.set(PlayerActivityRelation.LAST_SELECTED_TIME, General.millisec());
                        return builder.execUpdate();

                } catch (Exception e) {
                        Loggy.e(TAG, "updatePlayerActivityRelation", e);
                }
                return false;
        }

        public static boolean updatePlayerActivityRelation(List<Long> playerIdList, Long activityId, int relation) {
                try {
                        SQLBuilder builder = new SQLBuilder();
                        builder.update(PlayerActivityRelation.TABLE)
                                .set(PlayerActivityRelation.RELATION, relation)
                                .where(PlayerActivityRelation.ACTIVITY_ID, "=", activityId)
                                .where(PlayerActivityRelation.PLAYER_ID, "IN", playerIdList);

                        if ((relation & PlayerActivityRelation.SELECTED) > 0)
                                builder.set(PlayerActivityRelation.LAST_SELECTED_TIME, General.millisec());
                        return builder.execUpdate();

                } catch (Exception e) {
                        Loggy.e(TAG, "updatePlayerActivityRelation", e);
                }
                return false;
        }

        public static List<BasicPlayer> queryAppliedParticipants(long activityId) {
                List<Integer> relationList = new ArrayList<>();
                for (int relation : PlayerActivityRelation.APPLIED_STATES) relationList.add(relation);
                return queryPlayers(activityId, relationList);
        }

        public static List<BasicPlayer> querySelectedParticipants(long activityId) {
                List<Integer> relationList = new ArrayList<>();
                for (int relation : PlayerActivityRelation.SELECTED_STATES) relationList.add(relation);
                return queryPlayers(activityId, relationList);
        }

        public static List<BasicPlayer> queryPresentParticipants(long activityId) {
                List<Integer> relationList = new ArrayList<>();
                for (int relation : PlayerActivityRelation.PRESENT_STATES) relationList.add(relation);
                return queryPlayers(activityId, relationList);
        }

        static List<SimpleMap> processAdvancedQuery(SQLBuilder builder, String refIndex, String orderKey, String orientation, Integer direction, Integer nItems) {
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
                if (nItems != null) builder.limit(nItems);
                List<SimpleMap> ret = builder.execSelect();
                if (direction.equals(DIRECTION_BACKWARD)) Collections.reverse(ret);
                return ret;
        }

        public static Long queryPlayerId(String token) throws TokenExpiredException {

                SQLBuilder builder = new SQLBuilder();
                List<SimpleMap> allData = builder.select(Login.PLAYER_ID).from(Login.TABLE).where(Login.TOKEN, "=", token).execSelect();
                if (allData == null || allData.size() != 1) return null;
                SimpleMap loginData = allData.get(0);
                Login login = new Login(loginData);
                if (login.hasExpired()) throw new TokenExpiredException();
                return login.getPlayerId();

        }

        public static boolean validateAdminAccess(Player player) {
                return (player != null && player.getGroupId() == Player.ADMIN);
        }

        protected static String generateVerificationCode(String email) {
                return DataUtils.encryptByTime(email);
        }

        public static String generateSalt(String email, String password) {
                return DataUtils.encryptByTime(email + password);
        }

        public static boolean appendPlayerInfoForActivity(final List<Activity> activityList, final Long viewerId) {
                // host and viewer
                List<Long> hostIdList = new ArrayList<>();
                for (Activity activity : activityList)  hostIdList.add(activity.getHostId());
                List<Player> hostList = queryPlayerList(hostIdList);
                if (hostList == null) return false;

                Map<Long, Player> tmp = new HashMap<>();
                for (Player host : hostList) {
                        tmp.put(host.getId(), host);
                }

                Player viewer = (viewerId == null ? null : queryPlayer(viewerId));
                for (Activity activity : activityList) {
                        Player host = tmp.get(activity.getHostId());
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
                for (Integer state : PlayerActivityRelation.SELECTED_STATES) selectedStates.add(state);

                List<SpecialPlayerRecord> selectedList = queryPlayers(activityIdList, selectedStates);
                for (SpecialPlayerRecord record : selectedList) {
                        Activity activity = tmp.get(record.activityId);
                        activity.addSelectedParticipant(record.player);
                }
                return true;
        }

        public static boolean appendPlayerInfoForTopLevelComment(final List<Comment> commentList) {
                List<Long> playerIdList = new ArrayList<>();
                for (Comment comment : commentList) {
                        playerIdList.add(comment.getFrom());
                        // TODO: optimization by "GROUP BY" limits? reference: http://www.xaprb.com/blog/2006/12/07/how-to-select-the-firstleastmax-row-per-group-in-sql/
                        comment.setSubCommentList(querySubComments(comment.getId(), DBCommander.INITIAL_REF_INDEX, Comment.ID, SQLHelper.DESCEND, 3, DBCommander.DIRECTION_FORWARD));
                }

                // for top level comments
                List<Player> playerList = queryPlayerList(playerIdList);
                if (playerList == null) return false;
                Map<Long, Player> tmp = new HashMap<>();
                for (Player fromPlayer : playerList) {
                        tmp.put(fromPlayer.getId(), fromPlayer);
                }
                for (Comment comment : commentList) {
                        Player player = tmp.get(comment.getFrom());
                        comment.setFromPlayer(player);
                }

                return true;
        }

        public static boolean appendPlayerInfoForSubComment(final List<Comment> subCommentList) {
                if (subCommentList == null) return false;

                List<Long> fromList = new ArrayList<>();
                List<Long> toList = new ArrayList<>();
                for (Comment comment : subCommentList) {
                        fromList.add(comment.getFrom());
                        toList.add(comment.getTo());
                }

                if (fromList.size() != toList.size()) return false;

                List<Player> fromPlayerList = queryPlayerList(fromList);
                List<Player> toPlayerList = queryPlayerList(toList);

                Map<Long, Player> tmpFrom = new HashMap<>();
                for (Player fromPlayer : fromPlayerList) {
                        tmpFrom.put(fromPlayer.getId(), fromPlayer);
                }

                Map<Long, Player> tmpTo = new HashMap<>();
                for (Player toPlayer : toPlayerList) {
                        tmpTo.put(toPlayer.getId(), toPlayer);
                }

                for (Comment comment : subCommentList) {
                        Player fromPlayer = tmpFrom.get(comment.getFrom());
                        Player toPlayer = tmpTo.get(comment.getTo());
                        comment.setFromPlayer(fromPlayer);
                        comment.setToPlayer(toPlayer);
                }
                return true;
        }

        public static boolean appendPlayerInfoForAssessemnt(final List<Assessment> assessmentList) {
                if (assessmentList == null) return false;

                List<Long> fromList = new ArrayList<>();
                List<Long> toList = new ArrayList<>();
                for (Assessment assessment : assessmentList) {
                        fromList.add(assessment.getFrom());
                        toList.add(assessment.getTo());
                }

                if (fromList.size() != toList.size()) return false;

                List<Player> fromPlayerList = queryPlayerList(fromList);
                List<Player> toPlayerList = queryPlayerList(toList);

                Map<Long, Player> tmpFrom = new HashMap<>();
                for (Player fromPlayer : fromPlayerList) {
                        tmpFrom.put(fromPlayer.getId(), fromPlayer);
                }

                Map<Long, Player> tmpTo = new HashMap<>();
                for (Player toPlayer : toPlayerList) {
                        tmpTo.put(toPlayer.getId(), toPlayer);
                }

                for (Assessment assessment : assessmentList) {
                        Player fromPlayer = tmpFrom.get(assessment.getFrom());
                        Player toPlayer = tmpTo.get(assessment.getTo());
                        assessment.setFromPlayer(fromPlayer);
                        assessment.setToPlayer(toPlayer);
                }
                return true;
        }

        public static PermForeignParty queryPermForeignParty(final String partyId, final Integer party) {
                try {
                        String[] names = PermForeignParty.QUERY_FIELDS;
                        SQLBuilder builder = new SQLBuilder();
                        List<SimpleMap> results = builder.select(names)
                                .from(PermForeignParty.TABLE)
                                .where(PermForeignParty.ID, "=", partyId)
                                .where(PermForeignParty.PARTY, "=", party)
                                .execSelect();
                        if (results == null || results.size() != 1) return null;
                        SimpleMap data = results.get(0);
                        return new PermForeignParty(data);
                } catch (Exception e) {
                        Loggy.e(TAG, "queryPermForeignParty", e);
                }
                return null;
        }

        public static TempForeignParty queryTempForeignParty(final String accessToken, final Integer party) {
                try {
                        String[] cols = TempForeignParty.QUERY_FIELDS;
                        SQLBuilder builder = new SQLBuilder();
                        List<SimpleMap> results = builder.select(cols)
                                .from(TempForeignParty.TABLE)
                                .where(TempForeignParty.ACCESS_TOKEN, "=", accessToken)
                                .where(TempForeignParty.PARTY, "=", party)
                                .execSelect();
                        if (results == null || results.size() != 1) throw new NullPointerException();
                        SimpleMap data = results.get(0);
                        return new TempForeignParty(data);
                } catch (Exception e) {
                        Loggy.e(TAG, "queryTempForeignParty", e);
                }
                return null;
        }

        public static long createTempForeignParty(final String accessToken, final Integer party, final String partyId) {
                try {
                        SQLBuilder builder = new SQLBuilder();
                        String[] cols = {TempForeignParty.ACCESS_TOKEN, TempForeignParty.PARTY, TempForeignParty.PARTY_ID, TempForeignParty.TIMESTAMP};
                        Object[] vals = {accessToken, party, partyId, General.millisec()};
                        return builder.insert(cols, vals).into(TempForeignParty.TABLE).execInsert();
                } catch (Exception e) {
                        Loggy.e(TAG, "createTempForeignParty", e);
                }
                return SQLHelper.INVALID;
        }

        public static boolean deleteTempForeignParty(final String accessToken, final Integer party) {
                try {
                        SQLBuilder builder = new SQLBuilder();
                        return builder.from(TempForeignParty.TABLE)
                                .where(TempForeignParty.ACCESS_TOKEN, "=", accessToken)
                                .where(TempForeignParty.PARTY, "=", party)
                                .execDelete();
                } catch (Exception e) {
                        return false;
                }
        }
}
