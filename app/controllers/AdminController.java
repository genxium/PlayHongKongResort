package controllers;

import components.TokenExpiredResult;
import exception.AccessDeniedException;
import exception.ActivityNotFoundException;
import exception.TokenExpiredException;
import exception.UserNotFoundException;
import models.Activity;
import models.User;
import models.UserActivityRelation;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Loggy;

import java.util.Map;

public class AdminController extends Controller {

    public static final String TAG = AdminController.class.getName();

    public static Result accept() {
	    try {
		    Map<String, String[]> formData = request().body().asFormUrlEncoded();
		    Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
		    String token = formData.get(User.TOKEN)[0];

		    Long userId = SQLCommander.queryUserId(token);
		    if (userId == null) throw new UserNotFoundException();
		    User user = SQLCommander.queryUser(userId);
		    if (user == null) throw new UserNotFoundException();
		    if (!SQLCommander.validateAdminAccess(user)) throw new AccessDeniedException();

		    Activity activity = SQLCommander.queryActivity(activityId);
		    if (activity == null) throw new ActivityNotFoundException();

		    if(!SQLCommander.acceptActivity(user, activity)) throw new NullPointerException();

		    return ok();
	    } catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (Exception e) {
		    Loggy.e(TAG, "accept", e);
	    }
	    return badRequest("Activity not accepted!");
    }

    public static Result reject() {
	    try {
            Map<String, String[]> formData = request().body().asFormUrlEncoded();
            Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
            String token = formData.get(User.TOKEN)[0];

            Long userId = SQLCommander.queryUserId(token);
            if (userId == null) throw new UserNotFoundException();
            User user = SQLCommander.queryUser(userId);
            if (user == null) throw new UserNotFoundException();
            if (!SQLCommander.validateAdminAccess(user)) throw new AccessDeniedException();

            Activity activity = SQLCommander.queryActivity(activityId);
            if (activity == null) throw new ActivityNotFoundException();

            if(!SQLCommander.rejectActivity(user, activity)) throw new NullPointerException();
            return ok();
        } catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "reject", e);
        }
	    return badRequest("Activity not accepted!");
    }

    public static Result delete() {
	    try {
		    Map<String, String[]> formData = request().body().asFormUrlEncoded();
		    Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
		    String token = formData.get(User.TOKEN)[0];

		    Long userId = SQLCommander.queryUserId(token);
		    if (userId == null) throw new UserNotFoundException();

		    User user = SQLCommander.queryUser(userId);
		    if (user == null) throw new UserNotFoundException();
		    if (!SQLCommander.validateAdminAccess(user)) throw new AccessDeniedException();

		    if(!ExtraCommander.deleteActivity(activityId)) throw new NullPointerException();

		    return ok();
	    } catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "delete", e);
	    }
	    return badRequest();
    }
}
