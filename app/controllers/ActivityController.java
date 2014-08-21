package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SQLHelper;
import dao.EasyPreparedStatementBuilder;
import model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import play.libs.Json;
import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.DataUtils;

import java.sql.Timestamp;
import java.util.*;

public class ActivityController extends Controller {

    public static Result showDetail() {
        try {
            Content html = views.html.detail.render();
            return ok(html);
        } catch (Exception e) {
            return badRequest();
        }
    }

    public static Result query(String refIndex, Integer numItems, Integer order, Integer direction, String token, Integer userId, Integer relation, Integer status) {
        response().setContentType("text/plain");
        do {
            try {
                // anti-cracking by param direction
                if (direction == null) break;
                if (!direction.equals(SQLCommander.DIRECTION_FORWARD) && !direction.equals(SQLCommander.DIRECTION_BACKWARD))    break;

                // anti-cracking by param order
                if (order == null)  break;
                String orderStr = SQLHelper.convertOrder(order);
                if (orderStr == null)   break;

                // anti=cracking by param token
                Integer viewerId = null;
                if (token != null) viewerId = DataUtils.getUserIdByToken(token);
                List<Activity> activities = null;
                if (relation != null && relation != UserActivityRelation.hosted && userId != null) {
                    activities = SQLCommander.queryActivities(userId, UserActivityRelation.maskRelation(relation, null));
                } else if (relation != null && relation == UserActivityRelation.hosted && userId != null && viewerId != null) {
                    activities = SQLCommander.queryHostedActivities(userId, viewerId, refIndex, Activity.ID, orderStr, numItems, direction);
                } else {
                    activities = SQLCommander.queryActivities(refIndex, Activity.ID, orderStr, numItems, direction, status);
                }
                if (activities == null) break;
                ObjectNode result = Json.newObject();
                for (Activity activity : activities) {
                    // non-host viewers can only see accepted activities
                    if (activity.getStatus() != Activity.ACCEPTED && userId != null && !userId.equals(viewerId))
                        continue;
                    result.put(String.valueOf(activity.getId()), activity.toObjectNodeWithImages(viewerId));
                }
                return ok(result);
            } catch (Exception e) {
                System.out.println(ActivityController.class.getName() + ".query, " + e.getCause());
            }
        } while (false);
        return badRequest();
    }

    public static Result detail(Integer activityId, String token) {
        response().setContentType("text/plain");
        do {
            ObjectNode result = null;
            try {
                ActivityDetail activityDetail = SQLCommander.queryActivityDetail(activityId);
                if (activityDetail == null) break;
                Integer userId = null;
                if (token != null) userId = DataUtils.getUserIdByToken(token);
                result = activityDetail.toObjectNode(userId);
                return ok(result);
            } catch (Exception e) {
                System.out.println(ActivityController.class.getName() + ".detail, " + e.getMessage());
            }
        } while (false);
        return badRequest();
    }

    public static Result ownership(String token, Integer activityId) {
        do {
            try {
                Integer ownerId = DataUtils.getUserIdByToken(token);
                if (ownerId == null) break;
                if (!SQLCommander.validateOwnership(ownerId, activityId)) break;
                return ok();
            } catch (Exception e) {
                System.out.println(ActivityController.class.getName() + ".ownership, " + e.getMessage());
            }
        } while (false);
        return badRequest();
    }

    public static Result updateParticipants() {
        // define response attributes
        response().setContentType("text/plain");
        do {
            try {
                Map<String, String[]> formData = request().body().asFormUrlEncoded();
                String token = formData.get(User.TOKEN)[0];
                Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);

                String[] appliedParticipantsJsonStrs = formData.get(ActivityDetail.APPLIED_PARTICIPANTS);
                String[] selectedParticipantsJsonStrs = formData.get(ActivityDetail.SELECTED_PARTICIPANTS);
                String appliedParticipantsJsonStr = (appliedParticipantsJsonStrs.length > 0) ? appliedParticipantsJsonStrs[0] : "[]";
                String selectedParticipantsJsonStr = (selectedParticipantsJsonStrs.length > 0) ? selectedParticipantsJsonStrs[0] : "[]";

                JSONArray appliedParticipantsJson = (JSONArray) JSONValue.parse(appliedParticipantsJsonStr);
                JSONArray selectedParticipantsJson = (JSONArray) JSONValue.parse(selectedParticipantsJsonStr);

                Integer viewerId = DataUtils.getUserIdByToken(token);
                if (viewerId == null) break;
                if (!SQLCommander.validateOwnership(viewerId, activityId)) break;

                for (Object appliedParticipantJson : appliedParticipantsJson) {
                    Integer userId = Integer.valueOf((String) appliedParticipantJson);
                    if (userId.equals(viewerId)) continue; // anti-cracking by unselecting the host of an activity
                    SQLCommander.updateUserActivityRelation(viewerId, userId, activityId, UserActivityRelation.maskRelation(UserActivityRelation.applied, null));
                }

                for (Object selectedParticipantJson : selectedParticipantsJson) {
                    Integer userId = Integer.valueOf((String) selectedParticipantJson);
                    if (userId.equals(viewerId)) continue; // anti-cracking by selecting the host of an activity
                    SQLCommander.updateUserActivityRelation(viewerId, userId, activityId, UserActivityRelation.maskRelation(UserActivityRelation.selected, null));
                }
                return ok();
            } catch (Exception e) {
                System.out.println(ActivityController.class.getName() + ".updateParticipants: " + e.getMessage());
            }

        } while (false);

        return badRequest();
    }

    public static Result save() {
        // define response attributes
        response().setContentType("text/plain");

        do {
            try {
                Http.RequestBody body = request().body();

                // get file data from request body stream
                Http.MultipartFormData data = body.asMultipartFormData();
                List<Http.MultipartFormData.FilePart> imageFiles = data.getFiles();

                Map<String, String[]> formData = data.asFormUrlEncoded();

                String token = formData.get(User.TOKEN)[0];
                if (token == null) break;
                Integer userId = DataUtils.getUserIdByToken(token);
                if (userId == null || userId == null) break;
                User user = SQLCommander.queryUser(userId);
                if (user == null) break;

                String activityTitle = formData.get(Activity.TITLE)[0];
                String activityContent = formData.get(Activity.CONTENT)[0];
                String activityBeginTime = formData.get(Activity.BEGIN_TIME)[0];
                String activityDeadline = formData.get(Activity.DEADLINE)[0];

                if (DataUtils.validateTitle(activityTitle) == false || DataUtils.validateContent(activityContent) == false)
                    break;

                boolean isNewActivity = true;
                Integer activityId = null;
                if (formData.containsKey(UserActivityRelation.ACTIVITY_ID) == true) {
                    activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
                    isNewActivity = false;
                }
                Activity activity = null;

                if (isNewActivity == true) {
                    // create activity
                    activityId = SQLCommander.createActivity(activityTitle, activityContent, userId);
                    if (activityId == null || activityId.equals(SQLHelper.INVALID)) break;
                }

                // update activity
                activity = SQLCommander.queryActivity(activityId);
                if (SQLCommander.isActivityEditable(userId, activity) == false) break;

                activity.setTitle(activityTitle);
                activity.setContent(activityContent);
                activity.setBeginTime(Timestamp.valueOf(activityBeginTime));
                activity.setDeadline(Timestamp.valueOf(activityDeadline));

                boolean res = SQLCommander.updateActivity(activity);

                if (res == false) break;

                // save new images
                List<Image> previousImages = SQLCommander.queryImages(activityId);
                if (imageFiles != null && imageFiles.size() > 0) {
                    Iterator<Http.MultipartFormData.FilePart> imageIterator = imageFiles.iterator();
                    while (imageIterator.hasNext()) {
                        Http.MultipartFormData.FilePart imageFile = imageIterator.next();
                        int newImageId = ExtraCommander.saveImageOfActivity(imageFile, user, activity);
                        if (newImageId == ExtraCommander.INVALID) break;
                    }
                }

                // selected old images
                Set<Integer> selectedOldImagesSet = new HashSet<Integer>();

                if (formData.containsKey("indexOldImage")) {
                    JSONArray selectedOldImagesJson = (JSONArray) JSONValue.parse(formData.get("indexOldImage")[0]);
                    for (int i = 0; i < selectedOldImagesJson.size(); i++) {
                        Integer imageId = ((Long) selectedOldImagesJson.get(i)).intValue();
                        selectedOldImagesSet.add(imageId);
                    }
                }

                // delete previous images
                if (previousImages != null && previousImages.size() > 0) {
                    Iterator<Image> itPreviousImage = previousImages.iterator();
                    while (itPreviousImage.hasNext()) {
                        Image previousImage = itPreviousImage.next();
                        if (!selectedOldImagesSet.contains(previousImage.getImageId())) {
                            boolean isDeleted = ExtraCommander.deleteImageRecordAndFile(previousImage, activityId);
                            if (!isDeleted) break;
                        }
                    }
                }

                ObjectNode ret = Json.newObject();
                if (isNewActivity) {
                    ret.put(UserActivityRelation.ACTIVITY_ID, activityId.toString());
                }
                return ok(ret);
            } catch (Exception e) {
                System.out.println(ActivityController.class.getName()+", "+e.getMessage());
            }
        } while (false);
        return badRequest();
    }

    public static Result submit() {
        // define response attributes
        response().setContentType("text/plain");

        try {
            Http.RequestBody body = request().body();

            // get user token and activity id from request body stream
            Map<String, String[]> formData = body.asFormUrlEncoded();

            String token = formData.get(User.TOKEN)[0];
            Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);

            Integer userId = DataUtils.getUserIdByToken(token);
            if (userId == null) throw new Exception();
            User user = SQLCommander.queryUser(userId);
            if (user == null) throw new Exception();

            Activity activity = SQLCommander.queryActivity(activityId);
            if (!SQLCommander.isActivityEditable(userId, activity)) throw new Exception();

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

            String[] names = {Activity.STATUS};
            Object[] values = {Activity.PENDING};

            builder.update(Activity.TABLE).set(names, values).where(Activity.ID, "=", activity.getId());
            if(!SQLHelper.update(builder)) throw new Exception();

            return ok();

        } catch (Exception e) {
            System.out.println(ActivityController.class.getName()+", "+e.getMessage());
        }

        return badRequest();
    }


    public static Result delete() {
        // define response attributes
        response().setContentType("text/plain");
        do {
            try {
                Map<String, String[]> formData = request().body().asFormUrlEncoded();
                String[] ids = formData.get(UserActivityRelation.ACTIVITY_ID);
                String[] tokens = formData.get(User.TOKEN);

                Integer activityId = Integer.parseInt(ids[0]);
                String token = tokens[0];

                Integer userId = DataUtils.getUserIdByToken(token);
                if (userId == null) break;

                Activity activity = SQLCommander.queryActivity(activityId);
                if (SQLCommander.isActivityEditable(userId, activity) == false) break;

                boolean res = ExtraCommander.deleteActivity(activityId);
                if (res == false) break;
            } catch (Exception e) {

            }
            return ok();
        } while (false);
        return badRequest();
    }

    public static Result join() {
        // define response attributes
        response().setContentType("text/plain");

        try {
            Map<String, String[]> formData = request().body().asFormUrlEncoded();
            Integer activityId = Integer.parseInt(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
            String token = formData.get(User.TOKEN)[0];
            if (token == null) throw new Exception();
            Integer userId = DataUtils.getUserIdByToken(token);
            if (userId == null) throw new Exception();

            Activity activity = SQLCommander.queryActivity(activityId);
            if (activity == null) throw new Exception();
            boolean joinable = SQLCommander.isActivityJoinable(userId, activity);
            if (!joinable) throw new Exception();

            String[] names = {UserActivityRelation.ACTIVITY_ID, UserActivityRelation.USER_ID, UserActivityRelation.RELATION};
            Object[] values = {activityId, userId, UserActivityRelation.maskRelation(UserActivityRelation.applied, null)};
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.insert(names, values).into(UserActivityRelation.TABLE);

            int lastRelationTableId = SQLHelper.insert(builder);
            if (lastRelationTableId == SQLHelper.INVALID) throw new Exception();

            return ok();
        } catch (Exception e) {
            System.out.println(ActivityController.class.getName() + ".join, " + e.getMessage());
            return badRequest();
        }
    }

    public static Result mark() {
        // define response attributes
        response().setContentType("text/plain");
        try {
            Map<String, String[]> formData = request().body().asFormUrlEncoded();
            Integer activityId = Integer.parseInt(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
            String token = formData.get(User.TOKEN)[0];
            if (token == null) throw new Exception();
            Integer relation = Integer.parseInt(formData.get(UserActivityRelation.RELATION)[0]);
            Integer userId = DataUtils.getUserIdByToken(token);
            if (userId == null) throw new Exception();

            Activity activity = SQLCommander.queryActivity(activityId);
            if (activity == null) throw new Exception();
            int originalRelation = SQLCommander.isActivityMarkable(userId, activity, relation);
            if (originalRelation == UserActivityRelation.invalid) throw new Exception();

            String[] names = {UserActivityRelation.RELATION};
            Object[] values = {UserActivityRelation.maskRelation(relation, originalRelation)};
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

            String[] whereCols = {UserActivityRelation.ACTIVITY_ID, UserActivityRelation.USER_ID};
            String[] whereOps = {"=", "="};
            Object[] whereVals = {activityId, userId};
            builder.update(UserActivityRelation.TABLE).set(names, values).where(whereCols, whereOps, whereVals);

            if(!SQLHelper.update(builder)) throw new Exception();

            return ok();
        } catch (Exception e) {
            System.out.println(ActivityController.class.getName() + ".mark, " + e.getMessage());
            return badRequest();
        }
    }
}
