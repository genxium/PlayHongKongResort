package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.ActivityHasBegunException;
import exception.ActivityNotFoundException;
import exception.InvalidCommentParamsException;
import exception.UserNotFoundException;
import models.Activity;
import models.Comment;
import models.User;
import play.mvc.Result;
import utilities.Converter;
import utilities.DataUtils;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SubCommentController extends CommentController {

    public static final String TAG = SubCommentController.class.getName();

    public static Result query(Integer parentId, String refIndex, Integer numItems, Integer direction) {
        response().setContentType("text/plain");
        try {
            List<Comment> comments = SQLCommander.querySubComments(parentId, refIndex, Comment.ID, SQLHelper.DESCEND, numItems, direction);
            ArrayNode result = new ArrayNode(JsonNodeFactory.instance);
            for (Comment comment : comments)	result.add(comment.toObjectNode());
            return ok(result);
        } catch (Exception e) {
            DataUtils.log(TAG, "query", e);
        }
        return badRequest();
    }

    public static Result submit() {
        // define response attributes
        response().setContentType("text/plain");
        try {
            Map<String, String[]> formData = request().body().asFormUrlEncoded();
            if (!formData.containsKey(Comment.PREDECESSOR_ID)) throw new InvalidCommentParamsException();
            if (!formData.containsKey(Comment.PARENT_ID)) throw new InvalidCommentParamsException();
	        if (!formData.containsKey(Comment.TO)) throw new InvalidCommentParamsException();

            String content = formData.get(Comment.CONTENT)[0];
            if (content == null || content.length() <= Comment.MIN_CONTENT_LENGTH) throw new NullPointerException();

            String token = formData.get(User.TOKEN)[0];
            if (token == null) throw new InvalidCommentParamsException();

            Integer activityId = Integer.valueOf(formData.get(Comment.ACTIVITY_ID)[0]);
            Activity activity = SQLCommander.queryActivity(activityId);

            if(activity == null) throw new ActivityNotFoundException();
            if(activity.hasBegun()) throw new ActivityHasBegunException();

            Integer from = SQLCommander.queryUserId(token);
            if (from == null) throw new UserNotFoundException();

            Integer to = Converter.toInteger(formData.get(Comment.TO)[0]);
            if (to == null) throw new InvalidCommentParamsException();

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

            String[] columnNames = {Comment.CONTENT, Comment.ACTIVITY_ID, Comment.FROM, Comment.TO};
            List<String> cols = new LinkedList<String>(Arrays.asList(columnNames));

            Object[] columnValues = {content, activityId, from, to};
            List<Object> vals = new LinkedList<Object>(Arrays.asList(columnValues));

            Integer predecessorId = Integer.valueOf(formData.get(Comment.PREDECESSOR_ID)[0]);
            cols.add(Comment.PREDECESSOR_ID);
            vals.add(predecessorId);

            Integer parentId = Integer.valueOf(formData.get(Comment.PARENT_ID)[0]);
            cols.add(Comment.PARENT_ID);
            vals.add(parentId);

            builder.insert(cols, vals).into(Comment.TABLE);
            int lastId = SQLHelper.insert(builder);
            if (lastId == SQLHelper.INVALID) throw new NullPointerException();

            EasyPreparedStatementBuilder increment = new EasyPreparedStatementBuilder();
            builder.update(Comment.TABLE).increase(Comment.NUM_CHILDREN, 1).where(Comment.ID, "=", parentId);
            if (!SQLHelper.update(increment)) throw new NullPointerException();
            return ok();

        } catch (Exception e) {
            DataUtils.log(TAG, "submit", e);
        }
        return badRequest();
    }
}
