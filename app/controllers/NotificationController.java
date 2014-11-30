package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.*;
import models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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

public class NotificationController extends Controller {
	
	public static String TAG = NotificationController.class.getName();

    public static Result count(String token, Integer isRead) {
        response().setContentType("text/plain");
        try {
            if (token == null) throw new InvalidQueryParamsException();
            Integer userId = SQLCommander.queryUserId(token);
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> idJsonList = builder.select(Notification.ID)
                    .from(Notification.TABLE)
                    .where(Notification.TO, "=", userId)
                    .where(Notification.IS_READ, "=", isRead).execSelect();
            ObjectNode result = Json.newObject();
            if (idJsonList == null) throw new NullPointerException();
            result.put(Notification.COUNT, idJsonList.size());
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
			Integer to = SQLCommander.queryUserId(token);

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
