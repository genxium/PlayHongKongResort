package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;

import models.Activity;
import models.Comment;
import models.User;
import exception.*;

import play.mvc.Controller;
import play.mvc.Result;
import utilities.DataUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class CommentController extends Controller {

    public static final String TAG = CommentController.class.getName();

    public static Result query(Integer activityId, String refIndex, Integer numItems, Integer direction) {
	    response().setContentType("text/plain");
	    try {
		    List<Comment> comments = SQLCommander.queryTopLevelComments(activityId, refIndex, Comment.ID, SQLHelper.DESCEND, numItems, direction);

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

		    builder.insert(cols, vals).into(Comment.TABLE);
		    if (SQLHelper.INVALID == SQLHelper.insert(builder)) throw new NullPointerException();

		    return ok();

	    } catch (Exception e) {
		    DataUtils.log(TAG, "submit", e);
	    }
	    return badRequest();
    }
}
