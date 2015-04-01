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

		    Long userId = DBCommander.queryUserId(token);
		    if (userId == null) throw new UserNotFoundException();
		    User user = DBCommander.queryUser(userId);
		    if (user == null) throw new UserNotFoundException();
		    if (!DBCommander.validateAdminAccess(user)) throw new AccessDeniedException();

		    Activity activity = DBCommander.queryActivity(activityId);
		    if (activity == null) throw new ActivityNotFoundException();

		    if(!DBCommander.acceptActivity(user, activity)) throw new NullPointerException();

		    return ok();
	    } catch (TokenExpiredException e) {
            return ok(TokenExpiredResult.get());
        } catch (Exception e) {
		    Loggy.e(TAG, "accept", e);
	    }
	    return badRequest("");
    }

    public static Result reject() {
	    try {
            Map<String, String[]> formData = request().body().asFormUrlEncoded();
            Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
            String token = formData.get(User.TOKEN)[0];

            Long userId = DBCommander.queryUserId(token);
            if (userId == null) throw new UserNotFoundException();
            User user = DBCommander.queryUser(userId);
            if (user == null) throw new UserNotFoundException();
            if (!DBCommander.validateAdminAccess(user)) throw new AccessDeniedException();

            Activity activity = DBCommander.queryActivity(activityId);
            if (activity == null) throw new ActivityNotFoundException();

            if(!DBCommander.rejectActivity(user, activity)) throw new NullPointerException();
            return ok();
        } catch (TokenExpiredException e) {
            return ok(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "reject", e);
        }
	    return badRequest();
    }

    public static Result delete() {
	    try {
		    Map<String, String[]> formData = request().body().asFormUrlEncoded();
		    Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
		    String token = formData.get(User.TOKEN)[0];

		    Long userId = DBCommander.queryUserId(token);
		    if (userId == null) throw new UserNotFoundException();

		    User user = DBCommander.queryUser(userId);
		    if (user == null) throw new UserNotFoundException();
		    if (!DBCommander.validateAdminAccess(user)) throw new AccessDeniedException();

		    if(!ExtraCommander.deleteActivity(activityId)) throw new NullPointerException();

		    return ok();
	    } catch (TokenExpiredException e) {
            return ok(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "delete", e);
	    }
	    return badRequest();
    }
}
