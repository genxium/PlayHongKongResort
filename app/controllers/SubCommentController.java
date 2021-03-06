package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import components.TokenExpiredResult;
import dao.SQLBuilder;
import dao.SQLHelper;
import exception.*;
import models.Activity;
import models.Comment;
import models.Player;
import play.libs.Json;
import play.mvc.Result;
import utilities.Converter;
import utilities.General;
import utilities.Loggy;

import java.util.List;
import java.util.Map;

public class SubCommentController extends CommentController {

    public static final String TAG = SubCommentController.class.getName();

    public static Result list(Long parentId, Integer pageSt, Integer pageEd, Integer numItems) {
        try {
            if (parentId.equals(0L)) parentId = null;
            List<Comment> comments = DBCommander.querySubComments(parentId, pageSt, pageEd, Comment.ID, SQLHelper.DESCEND, numItems);

            ObjectNode result = Json.newObject();
            result.put(Comment.COUNT, 0);
            result.put(Comment.PAGE_ST, pageSt);
            result.put(Comment.PAGE_ED, pageEd);

            ArrayNode commentsNode = new ArrayNode(JsonNodeFactory.instance);
            for (Comment comment : comments) commentsNode.add(comment.toSubCommentObjectNode(null));
            result.put(Comment.SUB_COMMENTS, commentsNode);
            return ok(result);
        } catch (Exception e) {
            Loggy.e(TAG, "list", e);
        }
        return badRequest();
    }

    public static Result query(Long parentId, String refIndex, Integer page, Integer numItems, Integer direction) {
        try {
            if (parentId.equals(0L)) parentId = null;
            List<Comment> comments = DBCommander.querySubComments(parentId, refIndex, Comment.ID, SQLHelper.DESCEND, numItems, direction);

            ObjectNode result = Json.newObject();
            result.put(Comment.COUNT, 0);
            result.put(Comment.PAGE, page);

            ArrayNode commentsNode = new ArrayNode(JsonNodeFactory.instance);
            for (Comment comment : comments) commentsNode.add(comment.toSubCommentObjectNode(null));
            result.put(Comment.SUB_COMMENTS, commentsNode);
            return ok(result);
        } catch (Exception e) {
            Loggy.e(TAG, "query", e);
        }
        return badRequest();
    }

    public static Result submit() {
        // define response attributes
        try {
            Map<String, String[]> formData = request().body().asFormUrlEncoded();
            if (!formData.containsKey(Comment.CONTENT)) throw new InvalidCommentParamsException();
            if (!formData.containsKey(Comment.ACTIVITY_ID)) throw new InvalidCommentParamsException();
            if (!formData.containsKey(Player.TOKEN)) throw new InvalidCommentParamsException();
            if (!formData.containsKey(Comment.PREDECESSOR_ID)) throw new InvalidCommentParamsException();
            if (!formData.containsKey(Comment.PARENT_ID)) throw new InvalidCommentParamsException();
            if (!formData.containsKey(Comment.TO)) throw new InvalidCommentParamsException();

            String content = formData.get(Comment.CONTENT)[0];
            if (content == null || !General.validateCommentContent(content)) throw new InvalidCommentParamsException();

            String token = formData.get(Player.TOKEN)[0];
            if (token == null) throw new InvalidCommentParamsException();

            Long from = DBCommander.queryPlayerId(token);
            if (from == null) throw new PlayerNotFoundException();

            Long activityId = Converter.toLong(formData.get(Comment.ACTIVITY_ID)[0]);
            if (activityId == null) throw new InvalidCommentParamsException();
            Activity activity = DBCommander.queryActivity(activityId);

            if (activity == null) throw new ActivityNotFoundException();
            if (activity.hasBegun()) throw new ActivityHasBegunException();
            if (activity.getStatus() != Activity.ACCEPTED) throw new ActivityNotAcceptedException();

            Long to = Converter.toLong(formData.get(Comment.TO)[0]);
            if (to == null) throw new InvalidCommentParamsException();

            Integer predecessorId = Converter.toInteger(formData.get(Comment.PREDECESSOR_ID)[0]);
            if (predecessorId == null) throw new InvalidCommentParamsException();

            Integer parentId = Converter.toInteger(formData.get(Comment.PARENT_ID)[0]);
            if (parentId == null) throw new InvalidCommentParamsException();

            String[] cols = {Comment.CONTENT, Comment.ACTIVITY_ID, Comment.FROM, Comment.TO, Comment.GENERATED_TIME, Comment.PREDECESSOR_ID, Comment.PARENT_ID};
            Object[] vals = {content, activityId, from, to, General.millisec(), predecessorId, parentId};

            /**
             * TODO: begin SQL-transaction guard
             * */
            SQLBuilder builder = new SQLBuilder();
            long lastId = builder.insert(cols, vals).into(Comment.TABLE).execInsert();
            if (lastId == SQLHelper.INVALID) throw new NullPointerException();

            SQLBuilder increment = new SQLBuilder();
            increment.update(Comment.TABLE).increase(Comment.NUM_CHILDREN, 1).where(Comment.ID, "=", parentId);
            if (!increment.execUpdate()) throw new NullPointerException();
            /**
             * TODO: end SQL-transaction guard
             * */
            return ok();
        } catch (TokenExpiredException e) {
            return ok(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "submit", e);
        }
        return badRequest();
    }
}
