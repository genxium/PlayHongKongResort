package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import components.StandardFailureResult;
import components.StandardSuccessResult;
import components.TokenExpiredResult;
import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.InvalidQueryParamsException;
import exception.TokenExpiredException;
import exception.UserNotFoundException;
import models.AbstractMessage;
import models.Notification;
import models.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.Converter;
import utilities.Loggy;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NotificationController extends Controller {
	
	public static String TAG = NotificationController.class.getName();

	public static Result count(String token, Integer isRead) {
		try {
			if (token == null) throw new InvalidQueryParamsException();
			Long userId = SQLCommander.queryUserId(token);
			User user = SQLCommander.queryUser(userId);
			ObjectNode result = Json.newObject();
			result.put(Notification.COUNT, user.getUnreadCount());
			return ok(result);
		} catch (TokenExpiredException e) {
			return ok(TokenExpiredResult.get());
		} catch (Exception e) {
			Loggy.e(TAG, "count", e);
		}
		return badRequest();
	}
	
	public static Result list(Integer page_st, Integer page_ed, Integer numItems, Integer orientation, String token, Integer isRead) {
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
			return ok(TokenExpiredResult.get());
		} catch (Exception e) {
			Loggy.e(TAG, "list", e);
		}
		return badRequest();
	}
	
	public static Result delete() {
		try {

			Http.RequestBody body = request().body();

			// get file data from request body stream
			Map<String, String[]> formData = body.asFormUrlEncoded();

			String token = formData.get(User.TOKEN)[0];
			if (token == null) throw new NullPointerException();
			Long userId = SQLCommander.queryUserId(token);
			if (userId == null) throw new UserNotFoundException();
			User user = SQLCommander.queryUser(userId);
			if (user == null) throw new UserNotFoundException();
			
			List<Long> notificationIdList = new LinkedList<Long>();
			
			JSONArray bundle= (JSONArray) JSONValue.parse(formData.get(AbstractMessage.BUNDLE)[0]);
			for (Object obj : bundle) {
				Long notificationId = Converter.toLong(obj);
				notificationIdList.add(notificationId);
			}

			/**
			 * TODO: move the query to SQLCommander with proper wrapping
			 * */
			EasyPreparedStatementBuilder query = new EasyPreparedStatementBuilder();
			List<JSONObject> results = query.select(Notification.QUERY_FIELDS)
					.from(Notification.TABLE)
					.where(Notification.ID, "IN", notificationIdList)
					.where(Notification.TO, "=", userId)
					.where(Notification.IS_READ, "=", 0)
					.execSelect();
			if (results == null) throw new NullPointerException();
			if (results.size() > 0) {
				EasyPreparedStatementBuilder decrement = new EasyPreparedStatementBuilder();
				boolean rs = decrement.update(User.TABLE).decrease(User.UNREAD_COUNT, 1).where(User.ID, "=", userId).execUpdate();
				if (!rs) throw new NullPointerException();
			}

			EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
			
			builder.from(Notification.TABLE)
					.where(Notification.ID, "IN", notificationIdList)
					.where(Notification.TO, "=", userId);		

			boolean res = builder.execDelete();		
			if (res) return ok(StandardSuccessResult.get());
			else ok(StandardFailureResult.get());

		} catch (TokenExpiredException e) {
            return ok(TokenExpiredResult.get());
        } catch (Exception e) {
			Loggy.e(TAG, "delete", e);
		}
		return ok(StandardFailureResult.get());
	}
}
