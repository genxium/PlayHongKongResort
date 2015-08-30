package controllers;

import components.TokenExpiredResult;
import dao.EasyPreparedStatementBuilder;
import exception.*;
import models.Activity;
import models.Player;
import models.PlayerActivityRelation;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Converter;
import utilities.Loggy;

import java.util.Map;

public class AdminController extends Controller {

    public static final String TAG = AdminController.class.getName();

    public static Result accept() {
        try {
            Map<String, String[]> formData = request().body().asFormUrlEncoded();
            String token = formData.get(Player.TOKEN)[0];

            Long playerId = DBCommander.queryPlayerId(token);
            if (playerId == null) throw new PlayerNotFoundException();
            Player player = DBCommander.queryPlayer(playerId);
            if (player == null) throw new PlayerNotFoundException();
            if (!DBCommander.validateAdminAccess(player)) throw new AccessDeniedException();

            Long activityId = Converter.toLong(formData.get(PlayerActivityRelation.ACTIVITY_ID)[0]);
            if (activityId == null) throw new InvalidRequestParamsException();
            Activity activity = DBCommander.queryActivity(activityId);
            if (activity == null) throw new ActivityNotFoundException();

            if (!DBCommander.acceptActivity(player, activity)) throw new NullPointerException();

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
            String token = formData.get(Player.TOKEN)[0];

            Long playerId = DBCommander.queryPlayerId(token);
            if (playerId == null) throw new PlayerNotFoundException();
            Player player = DBCommander.queryPlayer(playerId);
            if (player == null) throw new PlayerNotFoundException();
            if (!DBCommander.validateAdminAccess(player)) throw new AccessDeniedException();

            Long activityId = Converter.toLong(formData.get(PlayerActivityRelation.ACTIVITY_ID)[0]);
            if (activityId == null) throw new InvalidRequestParamsException();
            Activity activity = DBCommander.queryActivity(activityId);
            if (activity == null) throw new ActivityNotFoundException();

            if (!DBCommander.rejectActivity(player, activity)) throw new NullPointerException();
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
            String token = formData.get(Player.TOKEN)[0];

            Long playerId = DBCommander.queryPlayerId(token);
            if (playerId == null) throw new PlayerNotFoundException();

            Player player = DBCommander.queryPlayer(playerId);
            if (player == null) throw new PlayerNotFoundException();
            if (!DBCommander.validateAdminAccess(player)) throw new AccessDeniedException();

            Long activityId = Converter.toLong(formData.get(PlayerActivityRelation.ACTIVITY_ID)[0]);
            if (activityId == null) throw new InvalidRequestParamsException();
            if (!ExtraCommander.deleteActivity(activityId)) throw new NullPointerException();

            return ok();
        } catch (TokenExpiredException e) {
            return ok(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "delete", e);
        }
        return badRequest();
    }

    public static Result prioritize() {
        try {
            Map<String, String[]> formData = request().body().asFormUrlEncoded();
            String token = formData.get(Player.TOKEN)[0];

            Long playerId = DBCommander.queryPlayerId(token);
            if (playerId == null) throw new PlayerNotFoundException();

            Player player = DBCommander.queryPlayer(playerId);
            if (player == null) throw new PlayerNotFoundException();
            if (!DBCommander.validateAdminAccess(player)) throw new AccessDeniedException();

            Integer priority = Converter.toInteger(formData.get(Activity.PRIORITY)[0]);
            if (priority == null) throw new InvalidRequestParamsException();

            Integer orderMask = Converter.toInteger(formData.get(Activity.ORDER_MASK)[0]);
            if (orderMask == null) throw new InvalidRequestParamsException();

            Long activityId = Converter.toLong(formData.get(PlayerActivityRelation.ACTIVITY_ID)[0]);
            if (activityId == null) throw new InvalidRequestParamsException();

            Activity activity = DBCommander.queryActivity(activityId);
            if (activity == null) throw new ActivityNotFoundException();

            if (activity.getStatus() != Activity.ACCEPTED) throw new ActivityNotAcceptedException();

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.update(Activity.TABLE)
                    .set(Activity.PRIORITY, priority)
                    .set(Activity.ORDER_MASK, orderMask)
                    .where(Activity.ID, "=", activityId);
            if (!builder.execUpdate()) throw new NullPointerException();
            return ok();
        } catch (TokenExpiredException e) {
            return ok(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "prioritize", e);
        }
        return badRequest();
    }
}
