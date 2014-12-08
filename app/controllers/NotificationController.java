package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SQLHelper;
import exception.InvalidQueryParamsException;
import exception.TokenExpiredException;
import exception.TokenExpiredResult;
import models.Notification;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Loggy;

import java.util.List;

public class NotificationController extends Controller {
	
	public static String TAG = NotificationController.class.getName();

    public static Result count(String token, Integer isRead) {
        response().setContentType("text/plain");
        try {
            if (token == null) throw new InvalidQueryParamsException();
            Long userId = SQLCommander.queryUserId(token);
            User user = SQLCommander.queryUser(userId);
            ObjectNode result = Json.newObject();
            result.put(Notification.COUNT, user.getUnreadCount());
            return ok(result);
        } catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "count", e);
        }
        return badRequest();
    }
	
	public static Result list(Integer page_st, Integer page_ed, Integer numItems, Integer orientation, String token, Integer isRead) {
		response().setContentType("text/plain");
		try {

			// anti-cracking by param order
			if (orientation == null)  throw new NullPointerException();
			String orientationStr = SQLHelper.convertOrientation(orientation);
			if (orientationStr == null)   throw new NullPointerException();

			// anti=cracking by param token
			if (token == null) throw new InvalidQueryParamsException(); 
			Long to = SQLCommander.queryUserId(token);

			List<Notification> notifications = null;
			notifications = SQLCommander.queryNotifications(to, isRead, page_st, page_ed, Notification.ID, orientationStr, numItems);

			if (notifications == null) throw new NullPointerException();
			ObjectNode result = Json.newObject();
			result.put(Notification.COUNT, notifications.size());
			result.put(Notification.PAGE_ST, page_st);
			result.put(Notification.PAGE_ED, page_ed);

			ArrayNode notificationArrayNode = new ArrayNode(JsonNodeFactory.instance);
			for (Notification notification : notifications) {
				notificationArrayNode.add(notification.toObjectNode());
			}
			result.put(Notification.NOTIFICATIONS, notificationArrayNode);
			return ok(result);
		} catch (TokenExpiredException e) {
			return badRequest(TokenExpiredResult.get());
		} catch (Exception e) {
			Loggy.e(TAG, "list", e);
		}
		return badRequest();
	}
}
