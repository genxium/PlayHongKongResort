package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import components.TokenExpiredResult;
import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.*;
import models.Activity;
import models.Comment;
import models.User;
import play.libs.Json;
import play.mvc.Result;
import utilities.Converter;
import utilities.General;
import utilities.Loggy;

import java.util.List;
import java.util.Map;

public class SubCommentController extends CommentController {

    public static final String TAG = SubCommentController.class.getName();

    public static Result list(Long parentId, Integer page_st, Integer page_ed, Integer numItems) {
        try {
            if (parentId.equals(0L)) parentId = null;
            List<Comment> comments = SQLCommander.querySubComments(parentId, page_st, page_ed, Comment.ID, SQLHelper.DESCEND, numItems);

            ObjectNode result = Json.newObject();
            result.put(Comment.COUNT, 0);
            result.put(Comment.PAGE_ST, page_st);
            result.put(Comment.PAGE_ED, page_ed);

            ArrayNode commentsNode = new ArrayNode(JsonNodeFactory.instance);
            for (Comment comment : comments)	commentsNode.add(comment.toSubCommentObjectNode());
            result.put(Comment.SUB_COMMENTS, commentsNode);
            return ok(result).as("text/plain");
        } catch (Exception e) {
            Loggy.e(TAG, "query", e);
        }
        return badRequest();
    }

    public static Result query(Long parentId, String refIndex, Integer page, Integer numItems, Integer direction) {
        try {
            if (parentId.equals(0L)) parentId = null;
            List<Comment> comments = SQLCommander.querySubComments(parentId, refIndex, Comment.ID, SQLHelper.DESCEND, numItems, direction);

            ObjectNode result = Json.newObject();
            result.put(Comment.COUNT, 0);
            result.put(Comment.PAGE, page);

            ArrayNode commentsNode = new ArrayNode(JsonNodeFactory.instance);
            for (Comment comment : comments)	commentsNode.add(comment.toSubCommentObjectNode());
	    result.put(Comment.SUB_COMMENTS, commentsNode);
            return ok(result).as("text/plain");
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
			if (!formData.containsKey(User.TOKEN)) throw new InvalidCommentParamsException();
            if (!formData.containsKey(Comment.PREDECESSOR_ID)) throw new InvalidCommentParamsException();
            if (!formData.containsKey(Comment.PARENT_ID)) throw new InvalidCommentParamsException();
	        if (!formData.containsKey(Comment.TO)) throw new InvalidCommentParamsException();

            String content = formData.get(Comment.CONTENT)[0];
            if (content == null || content.length() <= Comment.MIN_CONTENT_LENGTH) throw new NullPointerException();

            String token = formData.get(User.TOKEN)[0];
            if (token == null) throw new InvalidCommentParamsException();

            Long from = SQLCommander.queryUserId(token);
            if (from == null) throw new UserNotFoundException();

            Integer activityId = Integer.valueOf(formData.get(Comment.ACTIVITY_ID)[0]);
            Activity activity = SQLCommander.queryActivity(activityId);

            if(activity == null) throw new ActivityNotFoundException();
            if(activity.hasBegun()) throw new ActivityHasBegunException();
            if(activity.getStatus() != Activity.ACCEPTED) throw new ActivityNotAcceptedException();

            Long to = Converter.toLong(formData.get(Comment.TO)[0]);
            if (to == null) throw new InvalidCommentParamsException();

            Integer predecessorId = Converter.toInteger(formData.get(Comment.PREDECESSOR_ID)[0]);
            if (predecessorId == null) throw new InvalidCommentParamsException();

            Integer parentId = Converter.toInteger(formData.get(Comment.PARENT_ID)[0]);
            if (parentId == null) throw new InvalidCommentParamsException();

            String[] cols = {Comment.CONTENT, Comment.ACTIVITY_ID, Comment.FROM, Comment.TO, Comment.GENERATED_TIME, Comment.PREDECESSOR_ID, Comment.PARENT_ID};
            Object[] vals = {content, activityId, from, to, General.millisec(), predecessorId, parentId};

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            long lastId = builder.insert(cols, vals).into(Comment.TABLE).execInsert();
            if (lastId == SQLHelper.INVALID) throw new NullPointerException();

            EasyPreparedStatementBuilder increment = new EasyPreparedStatementBuilder();
            increment.update(Comment.TABLE).increase(Comment.NUM_CHILDREN, 1).where(Comment.ID, "=", parentId);
            if (!increment.execUpdate()) throw new NullPointerException();
            return ok().as("text/plain");
        } catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "submit", e);
        }
        return badRequest();
    }
}
