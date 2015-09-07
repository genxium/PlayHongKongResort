package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import components.StandardFailureResult;
import components.StandardSuccessResult;
import components.TokenExpiredResult;
import dao.SQLBuilder;
import dao.SQLHelper;
import dao.SimpleMap;
import exception.InvalidRequestParamsException;
import exception.PlayerNotFoundException;
import exception.TokenExpiredException;
import models.AbstractMessage;
import models.Notification;
import models.Player;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.Loggy;

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

            ObjectMapper mapper = new ObjectMapper();
            List<Long> notificationIdList = mapper.readValue(formData.get(AbstractMessage.BUNDLE)[0], mapper.getTypeFactory().constructCollectionType(List.class, Long.class));

            SQLBuilder query = new SQLBuilder();
            List<SimpleMap> results = query.select(Notification.QUERY_FIELDS)
                    .from(Notification.TABLE)
                    .where(Notification.ID, "IN", notificationIdList)
                    .where(Notification.TO, "=", playerId)
                    .where(Notification.IS_READ, "=", 0)
                    .execSelect();
            if (results == null) throw new NullPointerException();

            /**
             * TODO: begin SQL-transaction guard
             * */
            SQLBuilder decrement = new SQLBuilder();
            boolean rs = decrement.update(Player.TABLE).decrease(Player.UNREAD_COUNT, results.size()).where(Player.ID, "=", playerId).execUpdate();
            if (!rs) throw new NullPointerException();

            SQLBuilder builder = new SQLBuilder();

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
