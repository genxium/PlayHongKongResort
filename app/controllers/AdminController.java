package controllers;

import model.Activity;
import model.User;
import model.UserActivityRelation;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.DataUtils;

import java.util.Map;

public class AdminController extends Controller {

    public static Result accept() {
        // define response attributes
        response().setContentType("text/plain");
        do {
            try {
                Map<String, String[]> formData = request().body().asFormUrlEncoded();
                Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
                String token = formData.get(User.TOKEN)[0];

                Integer userId = DataUtils.getUserIdByToken(token);
                if (userId == null) break;
                User user = SQLCommander.queryUser(userId);
                if (user == null) break;
                if (!validateAdminAccess(user)) break;

                Activity activity = SQLCommander.queryActivity(activityId);
                if (activity == null) break;

                if(!SQLCommander.acceptActivity(user, activity)) break;

                return ok();
            } catch (Exception e) {
                System.out.println("AdminController.accept: " + e.getMessage());
            }
        } while (false);
        return badRequest("Activity not accepted!");
    }

    public static Result reject() {
        // define response attributes
        response().setContentType("text/plain");
        do {
            try {
                Map<String, String[]> formData = request().body().asFormUrlEncoded();
                Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
                String token = formData.get(User.TOKEN)[0];

                Integer userId = DataUtils.getUserIdByToken(token);
                if (userId == null) break;
                User user = SQLCommander.queryUser(userId);
                if (user == null) break;
                if (!validateAdminAccess(user)) break;

                Activity activity = SQLCommander.queryActivity(activityId);
                if (activity == null) break;

                if(!SQLCommander.rejectActivity(user, activity)) break;
                return ok();
            } catch (Exception e) {
                System.out.println("AdminController.accept: " + e.getMessage());
            }
        } while (false);
        return badRequest("Activity not accepted!");
    }

    public static Result delete() {
        // define response attributes
        response().setContentType("text/plain");
        do {
            try {
                Map<String, String[]> formData = request().body().asFormUrlEncoded();
                Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
                String token = formData.get(User.TOKEN)[0];

                Integer userId = DataUtils.getUserIdByToken(token);
                if (userId == null) break;

                User user = SQLCommander.queryUser(userId);
                if (user == null) break;
                if (!validateAdminAccess(user)) break;

                if(!ExtraCommander.deleteActivity(activityId)) break;

                return ok();
            } catch (Exception e) {
                System.out.println(AdminController.class.getName()+ "," + e.getMessage());
            }
        } while (false);
        return badRequest("Activity not completely deleted!");
    }

    public static boolean validateAdminAccess(User user) {
        boolean ret = false;
        do {
            if (user == null) break;
            if (user.getGroupId() != User.ADMIN) break;
            ret = true;
        } while (false);
        return ret;
    }
}
