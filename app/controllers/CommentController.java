package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;

import model.Comment;
import model.User;

import play.mvc.Controller;
import play.mvc.Result;
import utilities.DataUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class CommentController extends Controller {

    public static Result query(Integer activityId, String refIndex, Integer numItems, Integer direction, String token) {
        response().setContentType("text/plain");
        do {
            try {
                Integer userId = null;
                if (token != null) userId = DataUtils.getUserIdByToken(token);

                List<Comment> comments = SQLCommander.queryTopLevelComments(activityId, refIndex, Comment.ID, SQLHelper.DESCEND, numItems, direction);

                ArrayNode result = new ArrayNode(JsonNodeFactory.instance);
                for (Comment comment : comments) {
                    result.add(comment.toObjectNodeWithSubComments());
                }
                return ok(result);
            } catch (Exception e) {
                System.out.println(CommentController.class.getName() + ".query, " + e.getMessage());
            }
        } while (false);
        return badRequest();
    }

    public static Result submit() {
        // define response attributes
        response().setContentType("text/plain");
        do {
            try {
                Map<String, String[]> formData = request().body().asFormUrlEncoded();
                String content = formData.get(Comment.CONTENT)[0];
                if (content == null || content.length() <= Comment.MIN_CONTENT_LENGTH) break;

                String token = formData.get(User.TOKEN)[0];
                Integer activityId = Integer.valueOf(formData.get(Comment.ACTIVITY_ID)[0]);

                if (token == null) break;
                Integer userId = DataUtils.getUserIdByToken(token);
                if (userId == null) break;

                int lastCommentId = SQLHelper.INVALID;

                EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

                String[] columnNames = {Comment.CONTENT, Comment.ACTIVITY_ID, Comment.COMMENTER_ID};
                List<String> cols = new LinkedList<String>(Arrays.asList(columnNames));

                Object[] columnValues = {content, activityId, userId};
                List<Object> vals = new LinkedList<Object>(Arrays.asList(columnValues));

                if (formData.containsKey(Comment.PREDECESSOR_ID)) {
                    Integer predecessorId = Integer.valueOf(formData.get(Comment.PREDECESSOR_ID)[0]);
                    cols.add(Comment.PREDECESSOR_ID);
                    vals.add(predecessorId);
                }
                if (formData.containsKey(Comment.PARENT_ID)) {
                    Integer parentId = Integer.valueOf(formData.get(Comment.PARENT_ID)[0]);
                    cols.add(Comment.PARENT_ID);
                    vals.add(parentId);
                }
                builder.insert(cols, vals).into(Comment.TABLE);
                lastCommentId = SQLHelper.insert(builder);
                if (lastCommentId == SQLHelper.INVALID) break;

                return ok();

            } catch (Exception e) {
                System.out.println(CommentController.class.getName() + ".submit, " + e.getMessage());
            }

        } while (false);

        return badRequest();
    }
}
