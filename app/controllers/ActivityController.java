package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import components.CaptchaNotMatchedResult;
import components.StandardFailureResult;
import components.StandardSuccessResult;
import components.TokenExpiredResult;
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

import java.util.*;

public class ActivityController extends Controller {

    public static final String TAG = ActivityController.class.getName();

	public static final String OLD_IMAGE = "old_image";

    public static Result list(Integer pageSt, Integer pageEd, Integer numItems, Integer orientation, String token, Long vieweeId, Integer relation, Integer status) {
        try {
            if (pageSt == null || pageEd == null || numItems == null) throw new InvalidQueryParamsException();

            // anti-cracking by param order
            if (orientation == null)  throw new InvalidQueryParamsException();
            String orientationStr = SQLHelper.convertOrientation(orientation);
            if (orientationStr == null)   throw new InvalidQueryParamsException();
            Set<Integer> validRelations = new HashSet<>();
			validRelations.add(UserActivityRelation.HOSTED);
            validRelations.add(UserActivityRelation.PRESENT);
            validRelations.add(UserActivityRelation.ABSENT);
            validRelations.add(UserActivityRelation.PRESENT);

            if (relation != null && !validRelations.contains(relation)) throw new InvalidQueryParamsException();

            // anti=cracking by param token
            Long viewerId = null;
            User viewer = null;
            if (token != null) {
                viewerId = SQLCommander.queryUserId(token);
                viewer = SQLCommander.queryUser(viewerId);
            }
            if (vieweeId.equals(0L)) vieweeId = null;

            List<Activity> activities = null;
            String orderKey = Activity.ID;
            if (status != null && status.equals(Activity.ACCEPTED)) {
                orderKey = Activity.LAST_ACCEPTED_TIME;
            }
            if (status != null && status.equals(Activity.REJECTED)) {
                orderKey = Activity.LAST_REJECTED_TIME;
            }

            String cacheKey = "ActivityController";
            cacheKey = DataUtils.appendCacheKey(cacheKey, AbstractModel.PAGE_ST, pageSt);
            cacheKey = DataUtils.appendCacheKey(cacheKey, AbstractModel.PAGE_ED, pageEd);
            cacheKey = DataUtils.appendCacheKey(cacheKey, AbstractModel.NUM_ITEMS, numItems);
            cacheKey = DataUtils.appendCacheKey(cacheKey, AbstractModel.ORIENTATION, orientation);
            if (relation != null)   cacheKey = DataUtils.appendCacheKey(cacheKey, UserActivityRelation.RELATION, relation);
            if (vieweeId != null)   cacheKey = DataUtils.appendCacheKey(cacheKey, UserActivityRelation.VIEWEE_ID, vieweeId);
            if (status != null)     cacheKey = DataUtils.appendCacheKey(cacheKey, Activity.STATUS, status);

            if (relation != null && relation != UserActivityRelation.HOSTED && vieweeId != null) {
                cacheKey = DataUtils.appendCacheKey(cacheKey, AbstractModel.ORDER, orderKey);
                // activities = (List<Activity>) play.cache.Cache.get(cacheKey);
                if (activities == null) {
					List<Integer> maskedRelationList = new LinkedList<>();
                    if (relation == UserActivityRelation.PRESENT) {
                        for (int aRelation : UserActivityRelation.PRESENT_STATES) {
                            maskedRelationList.add(aRelation);
                        }
                    }
                    if (relation == UserActivityRelation.ABSENT) {
                        for (int aRelation : UserActivityRelation.ABSENT_STATES) {
                            maskedRelationList.add(aRelation);
                        }
                    }
                    if (relation == UserActivityRelation.PRESENT) {
                        for (int aRelation : UserActivityRelation.PRESENT_STATES) {
                            maskedRelationList.add(aRelation);
                        }
                    }
                    activities = SQLCommander.queryActivities(pageSt, pageEd, orderKey, orientationStr, numItems, vieweeId, maskedRelationList);
					if (activities != null) play.cache.Cache.set(cacheKey, activities, DataUtils.CACHE_DURATION);
				}
            } else if (relation != null && relation == UserActivityRelation.HOSTED && vieweeId != null) {
                cacheKey = DataUtils.appendCacheKey(cacheKey, AbstractModel.ORDER, Activity.ID);
                // activities = (List<Activity>) play.cache.Cache.get(cacheKey);
                if (activities == null) {
					activities = SQLCommander.queryHostedActivities(vieweeId, viewerId, pageSt, pageEd, Activity.ID, orientationStr, numItems);
					if (activities != null) play.cache.Cache.set(cacheKey, activities, DataUtils.CACHE_DURATION);
				}
            } else if (status != null) {
                cacheKey = DataUtils.appendCacheKey(cacheKey, AbstractModel.ORDER, orderKey);
                // activities = (List<Activity>) play.cache.Cache.get(cacheKey);
                if (activities == null) {
					activities = SQLCommander.queryActivities(pageSt, pageEd, orderKey, orientationStr, numItems, status);
					if (activities != null) play.cache.Cache.set(cacheKey, activities, DataUtils.CACHE_DURATION);
				}
            } else throw new InvalidQueryParamsException();

            if (activities == null) throw new NullPointerException();

            ObjectNode result = Json.newObject();
            result.put(AbstractModel.COUNT, 0);
            result.put(AbstractModel.PAGE_ST, pageSt);
            result.put(AbstractModel.PAGE_ED, pageEd);

            boolean isAdmin = false;
            if (viewer != null && SQLCommander.validateAdminAccess(viewer)) isAdmin = true;

            ArrayNode activitiesNode = new ArrayNode(JsonNodeFactory.instance);
            for (Activity activity : activities) {
                boolean isHost = (viewerId != null && viewer != null && activity.getHost().getId().equals(viewerId));
                // only hosts and admins can view non-accepted activities
                if (activity.getStatus() != Activity.ACCEPTED
                        &&
                    (!isHost && !isAdmin))	continue;
                activitiesNode.add(activity.toObjectNodeWithImagesAndSelectedParticipants(viewerId));
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

	public static Result detail(Long activityId, String token) {
		try {
			ActivityDetail activityDetail = ExtraCommander.queryActivityDetail(activityId);
			if (activityDetail == null) throw new ActivityNotFoundException();
			Long userId = null;
			if (token != null) userId = SQLCommander.queryUserId(token);
			if ((userId == null || !userId.equals(activityDetail.getHost().getId())) && activityDetail.getStatus() != Activity.ACCEPTED) return badRequest();
			return ok(activityDetail.toObjectNode(userId));
		} catch (Exception e) {
			Loggy.e(TAG, "detail", e);
		}
		return badRequest();
	}

	public static Result save() {
		try {
			Http.RequestBody body = request().body();

			// get file data from request body stream
			Http.MultipartFormData data = body.asMultipartFormData();
			List<Http.MultipartFormData.FilePart> imageFiles = data.getFiles();

			Map<String, String[]> formData = data.asFormUrlEncoded();

			String activityTitle = formData.get(Activity.TITLE)[0];
			String activityAddress = formData.get(Activity.ADDRESS)[0];
			String activityContent = formData.get(Activity.CONTENT)[0];
			
			int length = activityTitle.length();
			if (length == 0 || length > Activity.TITLE_MAX_COUNT) throw new InvalidQueryParamsException();
			length = activityAddress.length();
			if (length == 0 || length > Activity.ADDR_MAX_COUNT) throw new InvalidQueryParamsException();
			length = activityContent.length();
			if (length == 0 || length > Activity.CONTENT_MAX_COUNT) throw new InvalidQueryParamsException();

			long beginTime = Converter.toLong(formData.get(Activity.BEGIN_TIME)[0]);
			long deadline = Converter.toLong(formData.get(Activity.DEADLINE)[0]);

			if(deadline > beginTime) throw new DeadlineAfterBeginTimeException();

			// check new images
			if (imageFiles != null && imageFiles.size() > 0) {
				for (Http.MultipartFormData.FilePart imageFile : imageFiles) {
					if (!DataUtils.validateImage(imageFile)) throw new InvalidImageException();
				}
			}

			String token = formData.get(User.TOKEN)[0];
			if (token == null) throw new NullPointerException();

			boolean isNewActivity = true;
			Long activityId = null;
			if (formData.containsKey(UserActivityRelation.ACTIVITY_ID)) {
				activityId = Converter.toLong(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
				isNewActivity = false;
			}
			if (isNewActivity) {
				String sid = formData.get(UserActivityRelation.SID)[0];
				String captcha = formData.get(UserActivityRelation.CAPTCHA)[0];  
				if (sid == null || captcha == null) throw new CaptchaNotMatchedException(); 
				if (session(sid) == null || !captcha.equalsIgnoreCase(session(sid))) throw new CaptchaNotMatchedException(); 
			}

			Long userId = SQLCommander.queryUserId(token);
			if (userId == null) throw new UserNotFoundException();
			User user = SQLCommander.queryUser(userId);
			if (user == null) throw new UserNotFoundException();

			Activity activity = null;
			long now = General.millisec();
			if (isNewActivity) {
				activity = SQLCommander.createActivity(user, now);
				activityId = activity.getId();
			}
			if (activity == null || activityId == null || activityId.equals(SQLHelper.INVALID)) throw new ActivityNotFoundException();
			// update activity
			if (!SQLCommander.isActivityEditable(userId, activity)) throw new AccessDeniedException();
			activity.setTitle(activityTitle);
			activity.setAddress(activityAddress);
			activity.setContent(activityContent);
			activity.setBeginTime(beginTime);
			activity.setDeadline(deadline);

			if(!SQLCommander.updateActivity(activity))	throw new SQLUpdateException();

			// save new images
			List<Image> previousImages = ExtraCommander.queryImages(activityId);
			if (imageFiles != null && imageFiles.size() > 0) {
				for (Http.MultipartFormData.FilePart imageFile : imageFiles) {
					if (SQLHelper.INVALID == ExtraCommander.saveImageOfActivity(imageFile, user, activity)) break;
				}
			}

			// selected old images
			Set<Long> selectedOldImagesSet = new HashSet<>();

			if (formData.containsKey(OLD_IMAGE)) {
				JSONArray selectedOldImagesJson = (JSONArray) JSONValue.parse(formData.get(OLD_IMAGE)[0]);
				for (Object selectedOldImageJson : selectedOldImagesJson) {
					Long imageId = Converter.toLong(selectedOldImageJson);
					selectedOldImagesSet.add(imageId);
				}
			}

			// delete previous images
			if (previousImages != null && previousImages.size() > 0) {
				for (Image previousImage : previousImages) {
				    if (selectedOldImagesSet.contains(previousImage.getId())) continue;
				    boolean isDeleted = ExtraCommander.deleteImageRecordAndFile(previousImage, activityId);
				    if (!isDeleted) break;
				}
			}

			return ok(activity.toObjectNodeWithImages(userId));
		} catch (TokenExpiredException e) {
			return badRequest(TokenExpiredResult.get());
		} catch (CaptchaNotMatchedException e) {
			return badRequest(CaptchaNotMatchedResult.get());
		} catch (Exception e) {
			System.out.println("Exception on save.");
			Loggy.e(TAG, "save", e);
		}
		return badRequest();
	}

	public static Result submit() {
		try {
			Http.RequestBody body = request().body();

			// get user token and activity id from request body stream
			Map<String, String[]> formData = body.asFormUrlEncoded();

			String token = formData.get(User.TOKEN)[0];
			Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);

			Long userId = SQLCommander.queryUserId(token);
			if (userId == null) throw new UserNotFoundException();

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
		try {
			Map<String, String[]> formData = request().body().asFormUrlEncoded();
			String[] ids = formData.get(UserActivityRelation.ACTIVITY_ID);
			String[] tokens = formData.get(User.TOKEN);

			Integer activityId = Integer.parseInt(ids[0]);
			String token = tokens[0];

			Long userId = SQLCommander.queryUserId(token);
			if (userId == null) throw new UserNotFoundException();

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
		try {
			Map<String, String[]> formData = request().body().asFormUrlEncoded();
			Integer activityId = Integer.parseInt(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
			String token = formData.get(User.TOKEN)[0];
			if (token == null) throw new Exception();
			Long userId = SQLCommander.queryUserId(token);
			if (userId == null) throw new Exception();

			Activity activity = SQLCommander.queryActivity(activityId);
			if (activity == null) throw new ActivityNotFoundException();
			if (!SQLCommander.isActivityJoinable(userId, activity))	throw new NullPointerException();

            long now = General.millisec();
            String[] names = {UserActivityRelation.ACTIVITY_ID, UserActivityRelation.USER_ID, UserActivityRelation.RELATION, UserActivityRelation.GENERATED_TIME, UserActivityRelation.LAST_APPLYING_TIME};
			Object[] values = {activityId, userId, UserActivityRelation.maskRelation(UserActivityRelation.APPLIED, null), now, now};
			EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
			builder.insert(names, values).into(UserActivityRelation.TABLE).execInsert();

			EasyPreparedStatementBuilder increment = new EasyPreparedStatementBuilder();
			increment.update(Activity.TABLE).increase(Activity.NUM_APPLIED, 1).where(Activity.ID, "=", activityId);
			if (!increment.execUpdate()) throw new NullPointerException();
			return ok(StandardSuccessResult.get());
		} catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (Exception e) {
			Loggy.e(TAG, "join", e);
		}
        return ok(StandardFailureResult.get());
	}

	public static Result mark() {
		try {
			Map<String, String[]> formData = request().body().asFormUrlEncoded();
			Integer activityId = Integer.parseInt(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
			String token = formData.get(User.TOKEN)[0];
			if (token == null) throw new NullPointerException();
			Integer relation = Converter.toInteger(formData.get(UserActivityRelation.RELATION)[0]);
			Long userId = SQLCommander.queryUserId(token);
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
