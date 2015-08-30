package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import components.StandardFailureResult;
import components.StandardSuccessResult;
import components.TokenExpiredResult;
import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.InvalidRequestParamsException;
import exception.TokenExpiredException;
import exception.PlayerNotFoundException;
import models.AbstractMessage;
import models.Notification;
import models.Player;
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
            if (token == null) throw new InvalidRequestParamsException();
            Long playerId = DBCommander.queryPlayerId(token);
            Player player = DBCommander.queryPlayer(playerId);
            if (player == null) throw new PlayerNotFoundException();
            ObjectNode result = Json.newObject();
            result.put(Notification.COUNT, player.getUnreadCount());
            return ok(result);
        } catch (TokenExpiredException e) {
            return ok(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "count", e);
        }
        return badRequest();
    }

    public static Result list(Integer pageSt, Integer pageEd, Integer numItems, Integer orientation, String token, Integer isRead) {
        try {
            // anti-cracking by param order
            if (orientation == null) throw new NullPointerException();
            String orientationStr = SQLHelper.convertOrientation(orientation);
            if (orientationStr == null) throw new NullPointerException();

            // anti=cracking by param token
            if (token == null) throw new InvalidRequestParamsException();
            Long to = DBCommander.queryPlayerId(token);

            List<Notification> notifications = null;
            notifications = DBCommander.queryNotifications(to, isRead, pageSt, pageEd, Notification.ID, orientationStr, numItems);

            if (notifications == null) throw new NullPointerException();
            ObjectNode result = Json.newObject();
            result.put(Notification.COUNT, notifications.size());
            result.put(Notification.PAGE_ST, pageSt);
            result.put(Notification.PAGE_ED, pageEd);

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

            String token = formData.get(Player.TOKEN)[0];
            if (token == null) throw new NullPointerException();
            Long playerId = DBCommander.queryPlayerId(token);
            if (playerId == null) throw new PlayerNotFoundException();
            Player player = DBCommander.queryPlayer(playerId);
            if (player == null) throw new PlayerNotFoundException();

            List<Long> notificationIdList = new LinkedList<Long>();

            JSONArray bundle = (JSONArray) JSONValue.parse(formData.get(AbstractMessage.BUNDLE)[0]);
            for (Object obj : bundle) {
                Long notificationId = Converter.toLong(obj);
                notificationIdList.add(notificationId);
            }

            EasyPreparedStatementBuilder query = new EasyPreparedStatementBuilder();
            List<JSONObject> results = query.select(Notification.QUERY_FIELDS)
                    .from(Notification.TABLE)
                    .where(Notification.ID, "IN", notificationIdList)
                    .where(Notification.TO, "=", playerId)
                    .where(Notification.IS_READ, "=", 0)
                    .execSelect();
            if (results == null) throw new NullPointerException();

            /**
             * TODO: begin SQL-transaction guard
             * */
            EasyPreparedStatementBuilder decrement = new EasyPreparedStatementBuilder();
            boolean rs = decrement.update(Player.TABLE).decrease(Player.UNREAD_COUNT, results.size()).where(Player.ID, "=", playerId).execUpdate();
            if (!rs) throw new NullPointerException();

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

            builder.from(Notification.TABLE)
                    .where(Notification.ID, "IN", notificationIdList)
                    .where(Notification.TO, "=", playerId);

            boolean res = builder.execDelete();
            /**
             * TODO: end SQL-transaction guard
             * */

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
