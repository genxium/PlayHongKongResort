package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import components.CaptchaNotMatchedResult;
import components.StandardFailureResult;
import components.StandardSuccessResult;
import components.TokenExpiredResult;
import dao.SQLBuilder;
import dao.SQLHelper;
import exception.*;
import fixtures.Constants;
import models.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.CDNHelper;
import utilities.Converter;
import utilities.General;
import utilities.Loggy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class ActivityController extends Controller {

        public static final String TAG = ActivityController.class.getName();

        public static final String OLD_IMAGE = "old_image";
        public static final String NEW_IMAGE = "new_image";

        public static Result list(Integer pageSt, Integer pageEd, Integer numItems, String orderKey, Integer orientation, String token, Long vieweeId, Integer relation, Integer status) {
                try {
                        if (pageSt == null || pageEd == null || numItems == null) throw new InvalidRequestParamsException();

                        // anti-cracking by param order
                        if (orientation == null) throw new InvalidRequestParamsException();
                        String orientationStr = SQLHelper.convertOrientation(orientation);
                        if (orientationStr == null) throw new InvalidRequestParamsException();
                        Set<Integer> validRelations = new HashSet<>();
                        validRelations.add(PlayerActivityRelation.HOSTED);
                        validRelations.add(PlayerActivityRelation.PRESENT);
                        validRelations.add(PlayerActivityRelation.ABSENT);
                        validRelations.add(PlayerActivityRelation.PRESENT);

                        if (relation != null && !validRelations.contains(relation)) throw new InvalidRequestParamsException();

                        // anti-cracking by param token
                        Long viewerId = null;
                        Player viewer = null;
                        if (token != null) {
                                viewerId = DBCommander.queryPlayerId(token);
                                viewer = DBCommander.queryPlayer(viewerId);
                        }
                        if (vieweeId != null && vieweeId.equals(0L)) vieweeId = null;

                        List<Activity> activities = null;
                        if (orderKey == null || orderKey.isEmpty()) orderKey = Activity.LAST_ACCEPTED_TIME;

                        // for admin only
                        if (status != null && status.equals(Activity.ACCEPTED)) {
                                orderKey = Activity.LAST_ACCEPTED_TIME;
                        } else if (status != null && status.equals(Activity.REJECTED)) {
                                orderKey = Activity.LAST_REJECTED_TIME;
                        } else if (status != null && status.equals(Activity.PENDING)) {
                                orderKey = Activity.CREATED_TIME;
                        }

                        if (relation != null && relation != PlayerActivityRelation.HOSTED && vieweeId != null) {
                                List<Integer> maskedRelationList = new LinkedList<>();
                                if (relation == PlayerActivityRelation.PRESENT) {
                                        for (int aRelation : PlayerActivityRelation.PRESENT_STATES) {
                                                maskedRelationList.add(aRelation);
                                        }
                                }
                                if (relation == PlayerActivityRelation.ABSENT) {
                                        for (int aRelation : PlayerActivityRelation.ABSENT_STATES) {
                                                maskedRelationList.add(aRelation);
                                        }
                                }
                                if (relation == PlayerActivityRelation.PRESENT) {
                                        for (int aRelation : PlayerActivityRelation.PRESENT_STATES) {
                                                maskedRelationList.add(aRelation);
                                        }
                                }
                                activities = DBCommander.queryActivities(pageSt, pageEd, orderKey, orientationStr, numItems, vieweeId, maskedRelationList);
                        } else if (relation != null && relation == PlayerActivityRelation.HOSTED && vieweeId != null) {
                                activities = DBCommander.queryHostedActivities(vieweeId, viewerId, pageSt, pageEd, Activity.ID, orientationStr, numItems);
                        } else {
                                int offset = 0;
                                List<Activity> prioritizedActivities = null;
                                if (status == null || status == Activity.ACCEPTED) {
                                        // when status == null, case falls in general homepage query, set it to Activity.ACCEPTED first
                                        status = Activity.ACCEPTED;
                                        // trial for querying prioritized activities
                                        List<Integer> maskList = new LinkedList<>();
                                        for (int mask : Activity.LAST_ACCEPTED_TIME_MASK_LIST) {
                                                maskList.add(mask);
                                        }
                                        prioritizedActivities = DBCommander.queryPrioritizedActivities(maskList, numItems);
                                        // 'prioritizedActivities' is never null after the above query
                                        // NOTE: hereby assumes that number of prioritized activities doesn't exceed numItems
                                        offset = prioritizedActivities.size();
                                }
                                activities = DBCommander.queryActivities(pageSt, pageEd, orderKey, orientationStr, numItems, status, offset);
                                // 'activities' is never null after the above query

                                if (prioritizedActivities != null) {
                                        prioritizedActivities.addAll(activities);
                                        activities = prioritizedActivities;
                                }
                        }

                        if (activities == null) throw new NullPointerException();

                        ObjectNode result = Json.newObject();
                        result.put(AbstractModel.COUNT, 0);
                        result.put(AbstractModel.PAGE_ST, pageSt);
                        result.put(AbstractModel.PAGE_ED, pageEd);

                        boolean isAdmin = false;
                        if (viewer != null && DBCommander.validateAdminAccess(viewer)) isAdmin = true;
                        DBCommander.appendImageInfoForActivity(activities);
                        DBCommander.appendParticipantInfoForActivity(activities);

                        ArrayNode activitiesNode = new ArrayNode(JsonNodeFactory.instance);
                        for (Activity activity : activities) {
                                boolean isHost = (viewerId != null && viewer != null && activity.getHost().getId().equals(viewerId));
                                // only hosts and admins can view non-accepted activities
                                if (activity.getStatus() != Activity.ACCEPTED
                                        &&
                                        (!isHost && !isAdmin)) continue;
                                if (viewer != null) activity.setViewer(viewer);
                                activitiesNode.add(activity.toObjectNode(viewerId));
                        }
                        result.put(Activity.ACTIVITIES, activitiesNode);
                        return ok(result);
                } catch (TokenExpiredException e) {
                        return ok(TokenExpiredResult.get());
                } catch (Exception e) {
                        Loggy.e(TAG, "list", e);
                }
                return badRequest();
        }

        public static Result detail(Long activityId, String token) {
                try {
                        ActivityDetail activityDetail = ExtraCommander.queryActivityDetail(activityId);
                        if (activityDetail == null) throw new ActivityNotFoundException();
                        Long playerId = null;
                        if (token != null) playerId = DBCommander.queryPlayerId(token);
                        if ((playerId == null || !playerId.equals(activityDetail.getHost().getId())) && activityDetail.getStatus() != Activity.ACCEPTED)
                                return badRequest();
                        return ok(activityDetail.toObjectNode(playerId));
                } catch (Exception e) {
                        Loggy.e(TAG, "detail", e);
                }
                return badRequest();
        }

        public static Result save() {
                try {
                        final Http.RequestBody body = request().body();
                        final Map<String, String[]> formData = body.asFormUrlEncoded();

                        final String activityTitle = formData.get(Activity.TITLE)[0];
                        final String activityAddress = formData.get(Activity.ADDRESS)[0];
                        final String activityContent = formData.get(Activity.CONTENT)[0];

                        if (!General.validateActivityTitle(activityTitle) || !General.validateActivityAddress(activityAddress) || !General.validateActivityContent(activityContent))
                                throw new InvalidRequestParamsException();

                        final Long beginTime = Converter.toLong(formData.get(Activity.BEGIN_TIME)[0]);
                        final Long deadline = Converter.toLong(formData.get(Activity.DEADLINE)[0]);

                        if (beginTime == null || deadline == null || deadline < 0 || beginTime < 0)
                                throw new InvalidRequestParamsException();
                        if (deadline > beginTime) throw new DeadlineAfterBeginTimeException();

                        final String token = formData.get(Player.TOKEN)[0];
                        if (token == null) throw new NullPointerException();

                        boolean isNewActivity = true;
                        Long activityId = null;
                        if (formData.containsKey(PlayerActivityRelation.ACTIVITY_ID)) {
                                activityId = Converter.toLong(formData.get(PlayerActivityRelation.ACTIVITY_ID)[0]);
                                isNewActivity = false;
                        }
                        if (isNewActivity) {
                                String sid = formData.get(PlayerActivityRelation.SID)[0];
                                String captcha = formData.get(PlayerActivityRelation.CAPTCHA)[0];
                                if (sid == null || captcha == null) throw new CaptchaNotMatchedException();
                                if (session(sid) == null || !captcha.equalsIgnoreCase(session(sid)))
                                        throw new CaptchaNotMatchedException();
                        }

                        Long playerId = DBCommander.queryPlayerId(token);
                        if (playerId == null) throw new PlayerNotFoundException();

                        Player player = DBCommander.queryPlayer(playerId);
                        if (player == null) throw new PlayerNotFoundException();

                        if (isNewActivity && player.getGroupId() == Player.VISITOR) throw new AccessDeniedException();

                        Activity activity = null;
                        long now = General.millisec();
                        if (isNewActivity) {
                                activity = DBCommander.createActivity(player, now);
                                if (activity == null) throw new ActivityNotFoundException();
                                activityId = activity.getId();
                        } else if (activityId != null) {
                                activity = DBCommander.queryActivity(activityId);
                        } else
                                throw new ActivityNotFoundException();

                        if (activity == null || activityId == null || activityId.equals(SQLHelper.INVALID))
                                throw new ActivityNotFoundException();

                        // update activity
                        if (!DBCommander.isActivityEditable(playerId, activity)) throw new AccessDeniedException();
                        activity.setTitle(activityTitle);
                        activity.setAddress(activityAddress);
                        activity.setContent(activityContent);
                        activity.setBeginTime(beginTime);
                        activity.setDeadline(deadline);

                        Set<Long> selectedOldImagesSet = new HashSet<>();
                        if (formData.containsKey(OLD_IMAGE)) {
                                final ObjectMapper mapper = new ObjectMapper();
                                selectedOldImagesSet = mapper.readValue(formData.get(OLD_IMAGE)[0], mapper.getTypeFactory().constructCollectionType(Set.class, Long.class));
                        }
                        List<Image> previousImageList = ExtraCommander.queryImages(activityId);
                        final List<Image> toDeleteImageList = new LinkedList<>();
                        final List<Long> toDeleteImageIdList = new LinkedList<>();

                        List<String> newRemoteNameList = new LinkedList<>();
                        List<Image> newImageList = new LinkedList<>();
                        if (formData.containsKey(NEW_IMAGE)) {
                                final ObjectMapper mapper = new ObjectMapper();
                                newRemoteNameList.addAll((List<String>)mapper.readValue(formData.get(NEW_IMAGE)[0], mapper.getTypeFactory().constructCollectionType(List.class, String.class)));
                                // TODO: combine into transaction
                                newImageList = ExtraCommander.queryImages(playerId, Image.TYPE_OWNER, newRemoteNameList);
                        }

                        /**
                         * begin SQL-transaction guard
                         * */
                        boolean transactionSucceeded = true;
                        Connection connection = SQLHelper.getConnection();

                        try {
                                if (connection == null) throw new NullPointerException();
                                SQLHelper.disableAutoCommit(connection);

                                // update activity
                                final String[] cols1 = {Activity.TITLE, Activity.ADDRESS, Activity.CONTENT, Activity.BEGIN_TIME, Activity.DEADLINE, Activity.CAPACITY};
                                final Object[] values1 = {activity.getTitle(), activity.getAddress(), activity.getContent(), activity.getBeginTime(), activity.getDeadline(), activity.getCapacity()};
                                final SQLBuilder builderUpdate = new SQLBuilder();
                                final PreparedStatement statUpdate = builderUpdate.update(Activity.TABLE)
                                                                                        .set(cols1, values1)
                                                                                        .where(Activity.ID, "=", activity.getId())
                                                                                        .toUpdate(connection);
                                SQLHelper.executeAndCloseStatement(statUpdate);

                                if (newImageList.size() > 0) {
                                        String[] cols2 = {Image.URL, Image.META_ID, Image.META_TYPE};
                                        final String urlProtocolPrefix = "http://";
                                        for (Image newImage : newImageList) {
                                                final String url = urlProtocolPrefix + CDNHelper.getDomain(newImage.getCDNId(), newImage.getBucket()) + "/" + newImage.getRemoteName();
                                                final SQLBuilder builderUpdate2 = new SQLBuilder();
                                                Object[] values2 = {url, activity.getId(), Image.TYPE_ACTIVITY};
                                                final PreparedStatement statUpdate2 = builderUpdate2.update(Image.TABLE)
                                                                                                        .set(cols2, values2)
                                                                                                        .where(Image.META_ID, "=", playerId)
                                                                                                        .where(Image.META_TYPE, "=", Image.TYPE_OWNER)
                                                                                                        .where(Image.REMOTE_NAME, "=", newImage.getRemoteName())
                                                                                                        .toUpdate(connection);
                                                SQLHelper.executeAndCloseStatement(statUpdate2);
                                        }
                                }

                                // delete selected old images RECORDS
                                for (Image previousImage : previousImageList) {
                                        if (selectedOldImagesSet.contains(previousImage.getId())) continue;
                                        toDeleteImageList.add(previousImage);
                                        toDeleteImageIdList.add(previousImage.getId());
                                }
                                if (toDeleteImageList.size() > 0) {
                                        final SQLBuilder builderDeletion = new SQLBuilder();
                                        final PreparedStatement statDeletion = builderDeletion.from(Image.TABLE)
                                                                                        .where(Image.ID, "IN", toDeleteImageIdList)
                                                                                        .where(Image.META_ID, "=", activityId)
                                                                                        .where(Image.META_TYPE, "=", Image.TYPE_ACTIVITY)
                                                                                        .toDelete(connection);

                                        SQLHelper.executeAndCloseStatement(statDeletion);
                                }
                                SQLHelper.commit(connection);
                        } catch (Exception e) {
                                Loggy.e(TAG, "save", e);
                                transactionSucceeded = false;
                                SQLHelper.rollback(connection);
                        } finally {
                                SQLHelper.enableAutoCommitAndClose(connection);
                        }
                        /**
                         * end SQL-transaction guard
                         * */

                        if (transactionSucceeded) CDNHelper.deleteRemoteImages(CDNHelper.QINIU, toDeleteImageList);
                        List<Activity> tmp = new LinkedList<>();
                        tmp.add(activity);
                        DBCommander.appendImageInfoForActivity(tmp);

                        return ok(activity.toObjectNode(playerId));
                } catch (TokenExpiredException e) {
                        return ok(TokenExpiredResult.get());
                } catch (CaptchaNotMatchedException e) {
                        return ok(CaptchaNotMatchedResult.get());
                } catch (ActivityCreationLimitException e) {
                        return ok(StandardFailureResult.get(Constants.INFO_ACTIVITY_CREATION_LIMIT));
                } catch (Exception e) {
                        Loggy.e(TAG, "save", e);
                }
                return badRequest();
        }

        public static Result submit() {
                try {
                        Http.RequestBody body = request().body();

                        // get player token and activity id from request body stream
                        Map<String, String[]> formData = body.asFormUrlEncoded();

                        String token = formData.get(Player.TOKEN)[0];
                        Integer activityId = Converter.toInteger(formData.get(PlayerActivityRelation.ACTIVITY_ID)[0]);

                        Long playerId = DBCommander.queryPlayerId(token);
                        if (playerId == null) throw new PlayerNotFoundException();

                        Activity activity = DBCommander.queryActivity(activityId);
                        if (!DBCommander.isActivityEditable(playerId, activity)) throw new Exception();

                        SQLBuilder builder = new SQLBuilder();

                        String[] names = {Activity.STATUS};
                        Object[] values = {Activity.PENDING};

                        builder.update(Activity.TABLE).set(names, values).where(Activity.ID, "=", activity.getId());
                        if (!builder.execUpdate()) throw new NullPointerException();

                        return ok(StandardSuccessResult.get());
                } catch (TokenExpiredException e) {
                        return ok(TokenExpiredResult.get());
                } catch (Exception e) {
                        Loggy.e(TAG, "submit", e);
                }
                return ok(StandardFailureResult.get());
        }


        public static Result delete() {
                try {
                        final Map<String, String[]> formData = request().body().asFormUrlEncoded();

                        final Long activityId = Converter.toLong(formData.get(PlayerActivityRelation.ACTIVITY_ID)[0]);
                        String token = formData.get(Player.TOKEN)[0];

                        Long playerId = DBCommander.queryPlayerId(token);
                        if (playerId == null) throw new PlayerNotFoundException();

                        Activity activity = DBCommander.queryActivity(activityId);
                        if (!DBCommander.isActivityEditable(playerId, activity)) throw new NullPointerException();

                        /**
                         * begin SQL-transaction guard
                         * */

                        List<Image> previousImages = ExtraCommander.queryImages(activity.getId());

                        Connection connection = SQLHelper.getConnection();
                        try {
                                SQLHelper.disableAutoCommit(connection);

                                // delete associated player-activity-relation records
                                SQLBuilder relationBuilder = new SQLBuilder();
                                PreparedStatement relationStat = relationBuilder.from(PlayerActivityRelation.TABLE)
                                        .where(PlayerActivityRelation.ACTIVITY_ID, "=", activityId)
                                        .toDelete(connection);
                                SQLHelper.executeAndCloseStatement(relationStat);

                                if (previousImages != null && previousImages.size() > 0) {
                                        // delete associated images RECORDS
                                        for (Image previousImage : previousImages) {
                                                SQLBuilder imageBuilder = new SQLBuilder();
                                                PreparedStatement imageStat = imageBuilder.from(Image.TABLE)
                                                        .where(Image.ID, "=", previousImage.getId())
                                                        .where(Image.META_ID, "=", activityId)
                                                        .where(Image.META_TYPE, "=", Image.TYPE_ACTIVITY)
                                                        .toDelete(connection);
                                                SQLHelper.executeAndCloseStatement(imageStat);
                                        }
                                }

                                // delete associated comments
                                SQLBuilder commentsBuilder = new SQLBuilder();
                                PreparedStatement commentsStat = commentsBuilder.from(Comment.TABLE)
                                        .where(Comment.ACTIVITY_ID, "=", activityId)
                                        .toDelete(connection);
                                SQLHelper.executeAndCloseStatement(commentsStat);

                                // delete associated assessments
                                SQLBuilder assessmentsBuilder = new SQLBuilder();
                                PreparedStatement assessmentsStat = assessmentsBuilder.from(Assessment.TABLE)
                                        .where(Assessment.ACTIVITY_ID, "=", activityId)
                                        .toDelete(connection);
                                SQLHelper.executeAndCloseStatement(assessmentsStat);

                                // delete record in table activity
                                SQLBuilder activityBuilder = new SQLBuilder();
                                PreparedStatement activityStat = activityBuilder.from(Activity.TABLE)
                                        .where(Activity.ID, "=", activityId)
                                        .toDelete(connection);
                                SQLHelper.executeAndCloseStatement(activityStat);

                                SQLHelper.commit(connection);
                        } catch (SQLException e) {
                                SQLHelper.rollback(connection);
                        } finally {
                                SQLHelper.enableAutoCommitAndClose(connection);
                        }
                        /**
                         * end SQL-transaction guard
                         * */
                        return ok();
                } catch (TokenExpiredException e) {
                        return ok(TokenExpiredResult.get());
                } catch (Exception e) {
                        Loggy.e(TAG, "delete", e);
                }
                return badRequest();
        }

        public static Result join() {
                try {
                        Map<String, String[]> formData = request().body().asFormUrlEncoded();
                        Integer activityId = Integer.parseInt(formData.get(PlayerActivityRelation.ACTIVITY_ID)[0]);
                        String token = formData.get(Player.TOKEN)[0];
                        if (token == null) throw new InvalidRequestParamsException();
                        Long playerId = DBCommander.queryPlayerId(token);
                        if (playerId == null) throw new PlayerNotFoundException();
                        Player player = DBCommander.queryPlayer(playerId);
                        if (player == null) throw new PlayerNotFoundException();

                        Activity activity = DBCommander.queryActivity(activityId);
                        if (activity == null) throw new ActivityNotFoundException();

                        if (activity.getNumApplied() + 1 > Activity.MAX_APPLIED) throw new NumberLimitExceededException();
                        if (!DBCommander.isActivityJoinable(player, activity)) return ok(StandardFailureResult.get());

                        /**
                         * begin SQL-transaction guard
                         * */
                        Connection connection = SQLHelper.getConnection();

                        long now = General.millisec();
                        String[] names = {PlayerActivityRelation.ACTIVITY_ID, PlayerActivityRelation.PLAYER_ID, PlayerActivityRelation.RELATION, PlayerActivityRelation.GENERATED_TIME, PlayerActivityRelation.LAST_APPLYING_TIME};
                        Object[] values = {activityId, playerId, PlayerActivityRelation.maskRelation(PlayerActivityRelation.APPLIED, null), now, now};

                        try {
                                SQLHelper.disableAutoCommit(connection);

                                SQLBuilder relationBuilder = new SQLBuilder();
                                PreparedStatement relationStat = relationBuilder.insert(names, values)
                                        .into(PlayerActivityRelation.TABLE)
                                        .toInsert(connection);
                                SQLHelper.executeAndCloseStatement(relationStat);

                                SQLBuilder incrementBuilder = new SQLBuilder();
                                PreparedStatement incrementStat = incrementBuilder.update(Activity.TABLE)
                                        .increase(Activity.NUM_APPLIED, 1)
                                        .where(Activity.ID, "=", activityId)
                                        .toUpdate(connection);
                                SQLHelper.executeAndCloseStatement(incrementStat);
                                SQLHelper.commit(connection);
                        } catch (Exception e) {
                                SQLHelper.rollback(connection);
                        } finally {
                                SQLHelper.enableAutoCommitAndClose(connection);
                        }
                        /**
                         * end SQL-transaction guard
                         * */

                        return ok(StandardSuccessResult.get());
                } catch (TokenExpiredException e) {
                        return ok(TokenExpiredResult.get());
                } catch (NumberLimitExceededException e) {
                        return ok(StandardFailureResult.get(Constants.INFO_ACTIVITY_APPLIED_LIMIT));
                } catch (Exception e) {
                        Loggy.e(TAG, "join", e);
                }
                return ok(StandardFailureResult.get());
        }

        public static Result mark() {
                try {
                        final Map<String, String[]> formData = request().body().asFormUrlEncoded();
                        final Integer activityId = Converter.toInteger(formData.get(PlayerActivityRelation.ACTIVITY_ID)[0]);

                        final String token = formData.get(Player.TOKEN)[0];
                        if (token == null) throw new NullPointerException();

                        final Integer relation = Converter.toInteger(formData.get(PlayerActivityRelation.RELATION)[0]);
                        if (relation == null) throw new NullPointerException();

                        final Long playerId = DBCommander.queryPlayerId(token);
                        if (playerId == null) throw new PlayerNotFoundException();

                        Activity activity = DBCommander.queryActivity(activityId);
                        if (activity == null) throw new ActivityNotFoundException();

                        int originalRelation = DBCommander.isActivityMarkable(playerId, activity, relation);
                        if (originalRelation == PlayerActivityRelation.INVALID) throw new InvalidPlayerActivityRelationException();

                        int newRelation = PlayerActivityRelation.maskRelation(relation, originalRelation);

                        String[] names = {PlayerActivityRelation.RELATION};
                        Object[] values = {newRelation};
                        final SQLBuilder builder = new SQLBuilder();

                        String[] whereCols = {PlayerActivityRelation.ACTIVITY_ID, PlayerActivityRelation.PLAYER_ID};
                        String[] whereOps = {"=", "="};
                        Object[] whereVals = {activityId, playerId};
                        builder.update(PlayerActivityRelation.TABLE).set(names, values).where(whereCols, whereOps, whereVals);

                        if (!builder.execUpdate()) throw new NullPointerException();

                        final ObjectNode ret = Json.newObject();
                        ret.put(PlayerActivityRelation.RELATION, newRelation);
                        return ok(ret);
                } catch (TokenExpiredException e) {
                        return ok(TokenExpiredResult.get());
                } catch (Exception e) {
                        Loggy.e(TAG, "mark", e);
                }
                return badRequest();
        }
}
