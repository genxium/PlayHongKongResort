package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;

import models.Activity;
import models.Comment;
import models.User;
import exception.*;

import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.DataUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class CommentController extends Controller {

    public static final String TAG = CommentController.class.getName();

    public static Result querySingle(Integer commentId) {
	    response().setContentType("text/plain");
	    try {
		    Comment comment = SQLCommander.queryComment(commentId);
		    return ok(comment.toObjectNode(true));
	    } catch (Exception e) {
		    DataUtils.log(TAG, "query", e);
	    }
	    return badRequest();
	
    }

    public static Result list(Integer activityId, Integer page_st, Integer page_ed, Integer numItems) {
        response().setContentType("text/plain");
        try {
            List<Comment> comments = SQLCommander.queryTopLevelComments(activityId, page_st, page_ed, Comment.ID, SQLHelper.DESCEND, numItems);

            ObjectNode result = Json.newObject();
            result.put(Comment.COUNT, 0);
            result.put(Comment.PAGE_ST, page_st);

            ArrayNode commentsNode = new ArrayNode(JsonNodeFactory.instance);
            for (Comment comment : comments)	commentsNode.add(comment.toObjectNode(false));

            result.put(Comment.COMMENTS, commentsNode);
            return ok(result);
        } catch (Exception e) {
            DataUtils.log(TAG, "query", e);
        }
        return badRequest();
    }

    public static Result query(Integer activityId, String refIndex, Integer page, Integer numItems, Integer direction) {
	    response().setContentType("text/plain");
	    try {
		    List<Comment> comments = SQLCommander.queryTopLevelComments(activityId, refIndex, Comment.ID, SQLHelper.DESCEND, numItems, direction);

		    ObjectNode result = Json.newObject();
		    result.put(Comment.COUNT, 0);
		    result.put(Comment.PAGE, page);

		    ArrayNode commentsNode = new ArrayNode(JsonNodeFactory.instance);
		    for (Comment comment : comments)	commentsNode.add(comment.toObjectNode(false));
			
	 	    result.put(Comment.COMMENTS, commentsNode);
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
		    String content = formData.get(Comment.CONTENT)[0];
		    if (content == null || content.length() <= Comment.MIN_CONTENT_LENGTH) throw new NullPointerException();

		    String token = formData.get(User.TOKEN)[0];
		    if (token == null) throw new InvalidCommentParamsException();

		    Integer activityId = Integer.valueOf(formData.get(Comment.ACTIVITY_ID)[0]);
		    Activity activity = SQLCommander.queryActivity(activityId);

		    if (activity == null) throw new ActivityNotFoundException();

		    Integer from = SQLCommander.queryUserId(token);
		    if (from == null) throw new UserNotFoundException();

            SQLCommander.isActivityCommentable(from, activity);

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

		    String[] columnNames = {Comment.CONTENT, Comment.ACTIVITY_ID, Comment.FROM};
		    List<String> cols = new LinkedList<String>(Arrays.asList(columnNames));

		    Object[] columnValues = {content, activityId, from};
		    List<Object> vals = new LinkedList<Object>(Arrays.asList(columnValues));

		    builder.insert(cols, vals).into(Comment.TABLE);
		    if (SQLHelper.INVALID == builder.execInsert()) throw new NullPointerException();

		    return ok();

	    } catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (Exception e) {
		    DataUtils.log(TAG, "submit", e);
	    }
	    return badRequest();
    }

    public static Result view() {
	    try {
		    Content html = views.html.comment.render();
		    return ok(html);
	    } catch (Exception e) {
		    DataUtils.log(TAG, "view", e);
            }
	    return badRequest();	
    }
}
