package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.*;
import models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.Converter;
import utilities.DataUtils;
import utilities.General;
import utilities.Loggy;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActivityController extends Controller {

    public static final String TAG = ActivityController.class.getName();

	public static final String OLD_IMAGE = "old_image";

    public static Result list(Integer page_st, Integer page_ed, Integer numItems, Integer orientation, String token, Integer vieweeId, Integer relation, Integer status) {
        response().setContentType("text/plain");
        try {

            // anti-cracking by param order
            if (orientation == null)  throw new NullPointerException();
            String orientationStr = SQLHelper.convertOrientation(orientation);
            if (orientationStr == null)   throw new NullPointerException();

            // anti=cracking by param token
            Integer viewerId = null;
            User viewer = null;
            if (token != null) {
                viewerId = SQLCommander.queryUserId(token);
                viewer = SQLCommander.queryUser(viewerId);
            }
            List<Activity> activities = null;
            String orderKey = Activity.ID;
            if (status != null && status.equals(Activity.ACCEPTED)) {
                orderKey = Activity.LAST_ACCEPTED_TIME;
            }
            if (status != null && status.equals(Activity.REJECTED)) {
                orderKey = Activity.LAST_REJECTED_TIME;
            }
            if (relation != null && relation != UserActivityRelation.HOSTED && vieweeId != null) {
                activities = SQLCommander.queryActivities(page_st, page_ed, orderKey, orientationStr, numItems, vieweeId, UserActivityRelation.maskRelation(relation, null));
            } else if (relation != null && relation == UserActivityRelation.HOSTED && vieweeId != null) {
                activities = SQLCommander.queryHostedActivities(vieweeId, viewerId, page_st, page_ed, Activity.ID, orientationStr, numItems);
            } else if (status != null){
                activities = SQLCommander.queryActivities(page_st, page_ed, orderKey, orientationStr, numItems, status);
            } else {
                activities = null;
            }
            if (activities == null) throw new NullPointerException();
            ObjectNode result = Json.newObject();
            result.put(Activity.COUNT, 0);
            result.put(Activity.PAGE_ST, page_st);
            result.put(Activity.PAGE_ED, page_ed);

            boolean isAdmin = false;
            if (viewer != null && SQLCommander.validateAdminAccess(viewer)) isAdmin = true;

            ArrayNode activitiesNode = new ArrayNode(JsonNodeFactory.instance);
            for (Activity activity : activities) {
                boolean isHost = (viewerId != null && viewer != null && activity.getHost().getId() == viewerId);
                // only hosts and admins can view non-accepted activities
                if (activity.getStatus() != Activity.ACCEPTED
                        &&
                    (!isHost && !isAdmin))	continue;
                activitiesNode.add(activity.toObjectNodeWithImages(viewerId));
            }
            result.put(Activity.ACTIVITIES, activitiesNode);
            return ok(result);
        } catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "list", e);
        }
        return badRequest();
    }

	public static Result query(String refIndex, Integer page, Integer numItems, Integer orientation, Integer direction, String token, Integer vieweeId, Integer relation, Integer status) {
		response().setContentType("text/plain");
		try {
			// anti-cracking by param direction
			if (direction == null) throw new InvalidQueryParamsException();
			if (!direction.equals(SQLCommander.DIRECTION_FORWARD) && !direction.equals(SQLCommander.DIRECTION_BACKWARD))    throw new NullPointerException();

			// anti-cracking by param order
			if (orientation == null)  throw new InvalidQueryParamsException();
			String orientationStr = SQLHelper.convertOrientation(orientation);
			if (orientationStr == null)   throw new InvalidQueryParamsException();

			// anti=cracking by param token
			Integer viewerId = null;
            User viewer = null;
			if (token != null) {
                viewerId = SQLCommander.queryUserId(token);
                viewer = SQLCommander.queryUser(viewerId);
            }
            String orderKey = Activity.ID;
            if (status != null && status.equals(Activity.ACCEPTED)) {
                orderKey = Activity.LAST_ACCEPTED_TIME;
            }
            if (status != null && status.equals(Activity.REJECTED)) {
                orderKey = Activity.LAST_REJECTED_TIME;
            }
			List<Activity> activities = null;
			if (relation != null && relation != UserActivityRelation.HOSTED && vieweeId != null) {
				activities = SQLCommander.queryActivities(refIndex, orderKey, orientationStr, numItems, direction, vieweeId, UserActivityRelation.maskRelation(relation, null));
			} else if (relation != null && relation == UserActivityRelation.HOSTED && vieweeId != null) {
				activities = SQLCommander.queryHostedActivities(vieweeId, viewerId, refIndex, Activity.ID, orientationStr, numItems, direction);
			} else if (status != null) {
				activities = SQLCommander.queryActivities(refIndex, orderKey, orientationStr, numItems, direction, status);
			} else {
                activities = null;
            }
			if (activities == null) throw new NullPointerException();
			ObjectNode result = Json.newObject();
			result.put(Activity.COUNT, 0);
			result.put(Activity.PAGE, page.toString());

            boolean isAdmin = false;
            if (viewer != null && SQLCommander.validateAdminAccess(viewer)) isAdmin = true;

			ArrayNode activitiesNode = new ArrayNode(JsonNodeFactory.instance);
			for (Activity activity : activities) {
				boolean isHost = (viewerId != null && viewer != null && activity.getHost().getId() == viewerId);
                // only hosts and admins can view non-accepted activities
				if (activity.getStatus() != Activity.ACCEPTED
                        &&
                    (!isHost && !isAdmin))	continue;
				activitiesNode.add(activity.toObjectNodeWithImages(viewerId));
			}
			result.put(Activity.ACTIVITIES, activitiesNode);
			return ok(result);
		} catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (Exception e) {
			Loggy.e(TAG, "query", e);
		}
		return badRequest();
	}

	public static Result detail(Integer activityId, String token) {
		response().setContentType("text/plain");
		try {
			ActivityDetail activityDetail = SQLCommander.queryActivityDetail(activityId);
			if (activityDetail == null) throw new ActivityNotFoundException();
			Integer userId = null;
			if (token != null) userId = SQLCommander.queryUserId(token);
			return ok(activityDetail.toObjectNode(userId));
		} catch (Exception e) {
			Loggy.e(TAG, "detail", e);
		}
		return badRequest();
	}

	public static Result ownership(String token, Integer activityId) {
		try {
			Integer ownerId = SQLCommander.queryUserId(token);
			if (ownerId == null) throw new UserNotFoundException();
			if (!SQLCommander.validateOwnership(ownerId, activityId)) throw new AccessDeniedException();
			ObjectNode ret = Json.newObject();
			ret.put(Activity.HOST, String.valueOf(ownerId));
			return ok(ret);
		} catch (Exception e) {
			Loggy.e(TAG, "ownership", e);
		}
		return ok();
	}

	public static Result save() {
		// define response attributes
		try {
			Http.RequestBody body = request().body();

			// get file data from request body stream
			Http.MultipartFormData data = body.asMultipartFormData();
			List<Http.MultipartFormData.FilePart> imageFiles = data.getFiles();

			Map<String, String[]> formData = data.asFormUrlEncoded();

			String activityTitle = formData.get(Activity.TITLE)[0];
			String activityContent = formData.get(Activity.CONTENT)[0];
			long beginTime = Converter.toLong(formData.get(Activity.BEGIN_TIME)[0]);
			long deadline = Converter.toLong(formData.get(Activity.DEADLINE)[0]);

			if(deadline > beginTime) throw new DeadlineAfterBeginTimeException();

			String token = formData.get(User.TOKEN)[0];
			if (token == null) throw new NullPointerException();

			boolean isNewActivity = true;
			Integer activityId = null;
			if (formData.containsKey(UserActivityRelation.ACTIVITY_ID)) {
				activityId = Converter.toInteger(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
				isNewActivity = false;
			}
			if (isNewActivity) {
				String sid = formData.get(UserActivityRelation.SID)[0];
				String captcha = formData.get(UserActivityRelation.CAPTCHA)[0];  
				if (sid == null || captcha == null) throw new CaptchaNotMatchedException(); 
				if (session(sid) == null || !captcha.equalsIgnoreCase(session(sid))) throw new CaptchaNotMatchedException(); 
			}

			Integer userId = SQLCommander.queryUserId(token);
			if (userId == null) throw new NullPointerException();
			User user = SQLCommander.queryUser(userId);
			if (user == null) throw new NullPointerException();

			if (!DataUtils.validateTitle(activityTitle) || !DataUtils.validateContent(activityContent)) throw new NullPointerException();
			Activity activity = null;

			if (isNewActivity)	activityId = SQLCommander.createActivity(activityTitle, activityContent, userId);
			if (activityId == null || activityId.equals(SQLHelper.INVALID)) throw new ActivityNotFoundException();

			// update activity
			activity = SQLCommander.queryActivity(activityId);
			if (activity == null) throw new ActivityNotFoundException();
			if (!SQLCommander.isActivityEditable(userId, activity)) throw new AccessDeniedException();

			activity.setTitle(activityTitle);
			activity.setContent(activityContent);
			activity.setBeginTime(beginTime);
			activity.setDeadline(deadline);

			if(!SQLCommander.updateActivity(activity))	throw new NullPointerException();

			// save new images
			List<Image> previousImages = SQLCommander.queryImages(activityId);
			if (imageFiles != null && imageFiles.size() > 0) {
				for (Http.MultipartFormData.FilePart imageFile : imageFiles) {
				    if (ExtraCommander.INVALID == ExtraCommander.saveImageOfActivity(imageFile, user, activity)) break;
				}
			}

			// selected old images
			Set<Integer> selectedOldImagesSet = new HashSet<Integer>();

			if (formData.containsKey(OLD_IMAGE)) {
				JSONArray selectedOldImagesJson = (JSONArray) JSONValue.parse(formData.get(OLD_IMAGE)[0]);
				for (Object selectedOldImageJson : selectedOldImagesJson) {
					Integer imageId = ((Long) selectedOldImageJson).intValue();
					selectedOldImagesSet.add(imageId);
				}
			}

			// delete previous images
			if (previousImages != null && previousImages.size() > 0) {
				for (Image previousImage : previousImages) {
				    if (selectedOldImagesSet.contains(previousImage.getImageId())) continue;
				    boolean isDeleted = ExtraCommander.deleteImageRecordAndFile(previousImage, activityId);
				    if (!isDeleted) break;
				}
			}

			activity = SQLCommander.queryActivity(activityId);
			return ok(activity.toObjectNodeWithImages(userId)).as("text/plain");
		} catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (CaptchaNotMatchedException e) {
			return badRequest(CaptchaNotMatchedResult.get());
		} catch (Exception e) {
			Loggy.e(TAG, "save", e);
		}
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

            Integer userId = SQLCommander.queryUserId(token);
            if (userId == null) throw new Exception();
            User user = SQLCommander.queryUser(userId);
            if (user == null) throw new Exception();

            Activity activity = SQLCommander.queryActivity(activityId);
            if (!SQLCommander.isActivityEditable(userId, activity)) throw new Exception();

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

            String[] names = {Activity.STATUS};
            Object[] values = {Activity.PENDING};

            builder.update(Activity.TABLE).set(names, values).where(Activity.ID, "=", activity.getId());
            if (!builder.execUpdate()) throw new NullPointerException();

            return ok();
        } catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
		} catch (Exception e) {
			Loggy.e(TAG, "submit", e);
		}

		return badRequest();
	}


	public static Result delete() {
		// define response attributes
		response().setContentType("text/plain");
		try {
			Map<String, String[]> formData = request().body().asFormUrlEncoded();
			String[] ids = formData.get(UserActivityRelation.ACTIVITY_ID);
			String[] tokens = formData.get(User.TOKEN);

			Integer activityId = Integer.parseInt(ids[0]);
			String token = tokens[0];

			Integer userId = SQLCommander.queryUserId(token);
			if (userId == null) throw new NullPointerException();

			Activity activity = SQLCommander.queryActivity(activityId);
			if (!SQLCommander.isActivityEditable(userId, activity)) throw new NullPointerException();

			if(!ExtraCommander.deleteActivity(activityId)) throw new NullPointerException();
			return ok();
		} catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "delete", e);
		}
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
			Integer userId = SQLCommander.queryUserId(token);
			if (userId == null) throw new Exception();

			Activity activity = SQLCommander.queryActivity(activityId);
			if (activity == null) throw new ActivityNotFoundException();
			boolean joinable = SQLCommander.isActivityJoinable(userId, activity);
			if (!joinable) throw new Exception();

            long now = General.millisec();
            String[] names = {UserActivityRelation.ACTIVITY_ID, UserActivityRelation.USER_ID, UserActivityRelation.RELATION, UserActivityRelation.GENERATED_TIME, UserActivityRelation.LAST_APPLYING_TIME};
			Object[] values = {activityId, userId, UserActivityRelation.maskRelation(UserActivityRelation.APPLIED, null), now, now};
			EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
			builder.insert(names, values).into(UserActivityRelation.TABLE).execInsert();

			EasyPreparedStatementBuilder increment = new EasyPreparedStatementBuilder();
			increment.update(Activity.TABLE).increase(Activity.NUM_APPLIED, 1).where(Activity.ID, "=", activityId);
			if (!increment.execUpdate()) throw new NullPointerException();
			return ok();
		} catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (Exception e) {
			Loggy.e(TAG, "join", e);
		}
        return badRequest();
	}

	public static Result mark() {
		// define response attributes
		response().setContentType("text/plain");
		try {
			Map<String, String[]> formData = request().body().asFormUrlEncoded();
			Integer activityId = Integer.parseInt(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
			String token = formData.get(User.TOKEN)[0];
			if (token == null) throw new NullPointerException();
			Integer relation = Converter.toInteger(formData.get(UserActivityRelation.RELATION)[0]);
			Integer userId = SQLCommander.queryUserId(token);
			if (userId == null) throw new UserNotFoundException();

			Activity activity = SQLCommander.queryActivity(activityId);
			if (activity == null) throw new ActivityNotFoundException();
			int originalRelation = SQLCommander.isActivityMarkable(userId, activity, relation);
			if (originalRelation == UserActivityRelation.INVALID) throw new InvalidUserActivityRelationException();

			int newRelation = UserActivityRelation.maskRelation(relation, originalRelation);

			String[] names = {UserActivityRelation.RELATION};
			Object[] values = {newRelation};
			EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

			String[] whereCols = {UserActivityRelation.ACTIVITY_ID, UserActivityRelation.USER_ID};
			String[] whereOps = {"=", "="};
			Object[] whereVals = {activityId, userId};
			builder.update(UserActivityRelation.TABLE).set(names, values).where(whereCols, whereOps, whereVals);

			if(!builder.execUpdate()) throw new NullPointerException();

			ObjectNode ret = Json.newObject();
			ret.put(UserActivityRelation.RELATION, newRelation);
			return ok(ret);
		} catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "mark", e);
		}
        return badRequest();
	}
}
