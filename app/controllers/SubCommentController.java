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
import utilities.DataUtils;

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
            for (Comment comment : comments)	result.add(comment.toObjectNodeWithSubComments());
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

            String content = formData.get(Comment.CONTENT)[0];
            if (content == null || content.length() <= Comment.MIN_CONTENT_LENGTH) throw new NullPointerException();

            String token = formData.get(User.TOKEN)[0];
            if (token == null) throw new InvalidCommentParamsException();

            Integer activityId = Integer.valueOf(formData.get(Comment.ACTIVITY_ID)[0]);
            Activity activity = SQLCommander.queryActivity(activityId);

            if(activity == null) throw new ActivityNotFoundException();
            if(activity.hasBegun()) throw new ActivityHasBegunException();

            Integer userId = DataUtils.getUserIdByToken(token);
            if (userId == null) throw new UserNotFoundException();

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

            String[] columnNames = {Comment.CONTENT, Comment.ACTIVITY_ID, Comment.COMMENTER_ID};
            List<String> cols = new LinkedList<String>(Arrays.asList(columnNames));

            Object[] columnValues = {content, activityId, userId};
            List<Object> vals = new LinkedList<Object>(Arrays.asList(columnValues));

            Integer predecessorId = Integer.valueOf(formData.get(Comment.PREDECESSOR_ID)[0]);
            cols.add(Comment.PREDECESSOR_ID);
            vals.add(predecessorId);

            Integer parentId = Integer.valueOf(formData.get(Comment.PARENT_ID)[0]);
            cols.add(Comment.PARENT_ID);
            vals.add(parentId);

            builder.insert(cols, vals).into(Comment.TABLE);
            if (SQLHelper.INVALID == SQLHelper.insert(builder)) throw new NullPointerException();

            return ok();

        } catch (Exception e) {
            DataUtils.log(TAG, "submit", e);
        }
        return badRequest();
    }
}
