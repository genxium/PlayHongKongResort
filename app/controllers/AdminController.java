package controllers;

import models.Activity;
import models.User;
import models.UserActivityRelation;
import exception.*;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.DataUtils;

import java.util.Map;

public class AdminController extends Controller {

    public static Result accept() {
	    // define response attributes
	    response().setContentType("text/plain");
	    try {
		    Map<String, String[]> formData = request().body().asFormUrlEncoded();
		    Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
		    String token = formData.get(User.TOKEN)[0];

		    Integer userId = DataUtils.getUserIdByToken(token);
		    if (userId == null) throw new UserNotFoundException();
		    User user = SQLCommander.queryUser(userId);
		    if (user == null) throw new UserNotFoundException();
		    if (!validateAdminAccess(user)) throw new AccessDeniedException();

		    Activity activity = SQLCommander.queryActivity(activityId);
		    if (activity == null) throw new ActivityNotFoundException();

		    if(!SQLCommander.acceptActivity(user, activity)) throw new NullPointerException();

		    return ok();
	    } catch (Exception e) {
		    System.out.println("AdminController.accept: " + e.getMessage());
	    }
	    return badRequest("Activity not accepted!");
    }

    public static Result reject() {
	    // define response attributes
	    response().setContentType("text/plain");
            try {
                Map<String, String[]> formData = request().body().asFormUrlEncoded();
                Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
                String token = formData.get(User.TOKEN)[0];

                Integer userId = DataUtils.getUserIdByToken(token);
                if (userId == null) throw new UserNotFoundException();
                User user = SQLCommander.queryUser(userId);
                if (user == null) throw new UserNotFoundException();
                if (!validateAdminAccess(user)) throw new AccessDeniedException();

                Activity activity = SQLCommander.queryActivity(activityId);
                if (activity == null) throw new ActivityNotFoundException();

                if(!SQLCommander.rejectActivity(user, activity)) throw new NullPointerException();
                return ok();
            } catch (Exception e) {
                System.out.println("AdminController.accept: " + e.getMessage());
            }
	    return badRequest("Activity not accepted!");
    }

    public static Result delete() {
	    // define response attributes
	    response().setContentType("text/plain");
	    try {
		    Map<String, String[]> formData = request().body().asFormUrlEncoded();
		    Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
		    String token = formData.get(User.TOKEN)[0];

		    Integer userId = DataUtils.getUserIdByToken(token);
		    if (userId == null) throw new UserNotFoundException();

		    User user = SQLCommander.queryUser(userId);
		    if (user == null) throw new UserNotFoundException();
		    if (!validateAdminAccess(user)) throw new AccessDeniedException();

		    if(!ExtraCommander.deleteActivity(activityId)) throw new NullPointerException();

		    return ok();
	    } catch (Exception e) {
		    System.out.println(AdminController.class.getName()+ ".delete, " + e.getMessage());
	    }
	    return badRequest("Activity not completely deleted!");
    }

    public static boolean validateAdminAccess(User user) {
	    if (user == null) return false;
	    if (user.getGroupId() != User.ADMIN) return false;
	    return true;
    }
}
