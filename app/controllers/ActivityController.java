package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SQLHelper;
import dao.EasyPreparedStatementBuilder;
import model.*;
import exception.*;
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

	public static final String OLD_IMAGE = "old_image";

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
		try {
			// anti-cracking by param direction
			if (direction == null) throw new NullPointerException();
			if (!direction.equals(SQLCommander.DIRECTION_FORWARD) && !direction.equals(SQLCommander.DIRECTION_BACKWARD))    throw new NullPointerException();

			// anti-cracking by param order
			if (order == null)  throw new NullPointerException();
			String orderStr = SQLHelper.convertOrder(order);
			if (orderStr == null)   throw new NullPointerException();

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
			if (activities == null) throw new NullPointerException();
			ObjectNode result = Json.newObject();
			for (Activity activity : activities) {
				// non-host viewers can only see accepted activities
				if (activity.getStatus() != Activity.ACCEPTED && userId != null && !userId.equals(viewerId))
					continue;
				result.put(String.valueOf(activity.getId()), activity.toObjectNodeWithImages(viewerId));
			}
			return ok(result);
		} catch (Exception e) {
			System.out.println(ActivityController.class.getName() + ".query, " + e.getMessage());
		}
		return badRequest();
	}

	public static Result detail(Integer activityId, String token) {
		response().setContentType("text/plain");
		ObjectNode result = null;
		try {
			ActivityDetail activityDetail = SQLCommander.queryActivityDetail(activityId);
			if (activityDetail == null) throw new NullPointerException();
			Integer userId = null;
			if (token != null) userId = DataUtils.getUserIdByToken(token);
			result = activityDetail.toObjectNode(userId);
			return ok(result);
		} catch (Exception e) {
			System.out.println(ActivityController.class.getName() + ".detail, " + e.getMessage());
		}
		return badRequest();
	}

	public static Result ownership(String token, Integer activityId) {
		try {
			Integer ownerId = DataUtils.getUserIdByToken(token);
			if (ownerId == null) throw new UserNotFoundException();
			if (!SQLCommander.validateOwnership(ownerId, activityId)) throw new AccessDeniedException();
			ObjectNode ret = Json.newObject();
			ret.put(Activity.HOST, String.valueOf(ownerId));
			return ok(ret);
		} catch (Exception e) {
			System.out.println(ActivityController.class.getName() + ".ownership, " + e.getMessage());
		}
		return ok();
	}

	public static Result updateParticipants() {
		// define response attributes
		response().setContentType("text/plain");
		try {
			Map<String, String[]> formData = request().body().asFormUrlEncoded();
			String token = formData.get(User.TOKEN)[0];
			Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
			Activity activity = SQLCommander.queryActivity(activityId);
			if(activity == null) throw new ActivityNotFoundException();
			if(activity.hasBegun()) throw new ActivityHasBegunException();
			// String[] appliedParticipantsJsonStrs = formData.get(ActivityDetail.APPLIED_PARTICIPANTS);
			String[] selectedParticipantsJsonStrs = formData.get(ActivityDetail.SELECTED_PARTICIPANTS);
			// String appliedParticipantsJsonStr = (appliedParticipantsJsonStrs.length > 0) ? appliedParticipantsJsonStrs[0] : "[]";
			String selectedParticipantsJsonStr = (selectedParticipantsJsonStrs.length > 0) ? selectedParticipantsJsonStrs[0] : "[]";

			// JSONArray appliedParticipantsJson = (JSONArray) JSONValue.parse(appliedParticipantsJsonStr);
			JSONArray selectedParticipantsJson = (JSONArray) JSONValue.parse(selectedParticipantsJsonStr);

			Integer viewerId = DataUtils.getUserIdByToken(token);
			if (viewerId == null) throw new UserNotFoundException();
			if (!SQLCommander.validateOwnership(viewerId, activityId)) throw new AccessDeniedException();

			/* Forbid unselecting participants, uncomment corresponding codes to resume */
			/* 			
			for (Object appliedParticipantJson : appliedParticipantsJson) {
				Integer userId = Integer.valueOf((String) appliedParticipantJson);
				if (userId.equals(viewerId)) continue; // anti-cracking by unselecting the host of an activity
				int originalRelation = SQLCommander.queryUserActivityRelation(userId, activityId);
				SQLCommander.updateUserActivityRelation(viewerId, userId, activityId, UserActivityRelation.maskRelation(UserActivityRelation.applied, originalRelation));
			}
			*/

			for (Object selectedParticipantJson : selectedParticipantsJson) {
				Integer userId = Integer.valueOf((String) selectedParticipantJson);
				if (userId.equals(viewerId)) continue; // anti-cracking by selecting the host of an activity
				int originalRelation = SQLCommander.queryUserActivityRelation(userId, activityId);
				SQLCommander.updateUserActivityRelation(viewerId, userId, activityId, UserActivityRelation.maskRelation(UserActivityRelation.selected, originalRelation));
			}
			return ok();
		} catch (Exception e) {
			System.out.println(ActivityController.class.getName() + ".updateParticipants: " + e.getMessage());
		}

		return badRequest();
	}

	public static Result save() {
		// define response attributes
		response().setContentType("text/plain");
		try {
			Http.RequestBody body = request().body();

			// get file data from request body stream
			Http.MultipartFormData data = body.asMultipartFormData();
			List<Http.MultipartFormData.FilePart> imageFiles = data.getFiles();

			Map<String, String[]> formData = data.asFormUrlEncoded();

			String activityTitle = formData.get(Activity.TITLE)[0];
			String activityContent = formData.get(Activity.CONTENT)[0];
			String activityBeginTime = formData.get(Activity.BEGIN_TIME)[0];
			String activityDeadline = formData.get(Activity.DEADLINE)[0];

			Timestamp beginTime = Timestamp.valueOf(activityBeginTime);
			Timestamp deadline = Timestamp.valueOf(activityDeadline);
			
			if(deadline.after(beginTime)) throw new DeadlineAfterBeginTimeException();

			String token = formData.get(User.TOKEN)[0];
			if (token == null) throw new NullPointerException();
			Integer userId = DataUtils.getUserIdByToken(token);
			if (userId == null || userId == null) throw new NullPointerException();
			User user = SQLCommander.queryUser(userId);
			if (user == null) throw new NullPointerException();

			if (DataUtils.validateTitle(activityTitle) == false || DataUtils.validateContent(activityContent) == false) throw new NullPointerException();
			boolean isNewActivity = true;
			Integer activityId = null;
			if (formData.containsKey(UserActivityRelation.ACTIVITY_ID)) {
				activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
				isNewActivity = false;
			}
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
				Iterator<Http.MultipartFormData.FilePart> imageIterator = imageFiles.iterator();
				while (imageIterator.hasNext()) {
					Http.MultipartFormData.FilePart imageFile = imageIterator.next();
					if(ExtraCommander.INVALID == ExtraCommander.saveImageOfActivity(imageFile, user, activity)) break;
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
				Iterator<Image> itPreviousImage = previousImages.iterator();
				while (itPreviousImage.hasNext()) {
					Image previousImage = itPreviousImage.next();
					if (selectedOldImagesSet.contains(previousImage.getImageId())) continue;
					boolean isDeleted = ExtraCommander.deleteImageRecordAndFile(previousImage, activityId);
					if (!isDeleted) break;
				}
			}

			ObjectNode ret = Json.newObject();
			if (isNewActivity)	ret.put(UserActivityRelation.ACTIVITY_ID, activityId.toString());
			return ok(ret);
		} catch (Exception e) {
			System.out.println(ActivityController.class.getName()+".save, "+e.getMessage());
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
		try {
			Map<String, String[]> formData = request().body().asFormUrlEncoded();
			String[] ids = formData.get(UserActivityRelation.ACTIVITY_ID);
			String[] tokens = formData.get(User.TOKEN);

			Integer activityId = Integer.parseInt(ids[0]);
			String token = tokens[0];

			Integer userId = DataUtils.getUserIdByToken(token);
			if (userId == null) throw new NullPointerException();

			Activity activity = SQLCommander.queryActivity(activityId);
			if (!SQLCommander.isActivityEditable(userId, activity)) throw new NullPointerException();

			if(!ExtraCommander.deleteActivity(activityId)) throw new NullPointerException();
			return ok();
		} catch (Exception e) {
			System.out.println(ActivityController.class.getName()+".delete, " + e.getMessage());
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
			if (token == null) throw new NullPointerException();
			Integer relation = Integer.parseInt(formData.get(UserActivityRelation.RELATION)[0]);
			Integer userId = DataUtils.getUserIdByToken(token);
			if (userId == null) throw new UserNotFoundException();

			Activity activity = SQLCommander.queryActivity(activityId);
			if (activity == null) throw new ActivityNotFoundException();
			int originalRelation = SQLCommander.isActivityMarkable(userId, activity, relation);
			if (originalRelation == UserActivityRelation.invalid) throw new InvalidUserActivityRelationException();

			int newRelation = UserActivityRelation.maskRelation(relation, originalRelation);

			String[] names = {UserActivityRelation.RELATION};
			Object[] values = {newRelation};
			EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

			String[] whereCols = {UserActivityRelation.ACTIVITY_ID, UserActivityRelation.USER_ID};
			String[] whereOps = {"=", "="};
			Object[] whereVals = {activityId, userId};
			builder.update(UserActivityRelation.TABLE).set(names, values).where(whereCols, whereOps, whereVals);

			if(!SQLHelper.update(builder)) throw new NullPointerException();

			ObjectNode ret = Json.newObject();
			ret.put(UserActivityRelation.RELATION, newRelation);
			return ok(ret);
		} catch (Exception e) {
			System.out.println(ActivityController.class.getName() + ".mark, " + e.getMessage());
			return badRequest();
		}
	}
}
