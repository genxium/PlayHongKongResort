package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import components.TokenExpiredResult;
import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.*;
import models.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Converter;
import utilities.DataUtils;
import utilities.General;
import utilities.Loggy;

import java.util.List;
import java.util.Map;

public class CommentController extends Controller {

    public static final String TAG = CommentController.class.getName();

    public static Result querySingle(Integer commentId) {
	    try {
		    Comment comment = SQLCommander.queryComment(commentId);
		    return ok(comment.toObjectNode(true, null));
	    } catch (Exception e) {
		    Loggy.e(TAG, "query", e);
	    }
	    return badRequest();
	
    }

    @SuppressWarnings("unchecked")
    public static Result list(Long activityId, Integer pageSt, Integer pageEd, Integer numItems) {
        try {
            if (activityId == null || pageSt == null || pageEd == null || numItems == null) throw new InvalidQueryParamsException();

            String cacheKey = "CommentController";
            cacheKey = DataUtils.appendCacheKey(cacheKey, AbstractActivityMessage.ACTIVITY_ID, activityId);
            cacheKey = DataUtils.appendCacheKey(cacheKey, AbstractModel.PAGE_ST, pageSt);
            cacheKey = DataUtils.appendCacheKey(cacheKey, AbstractModel.PAGE_ED, pageEd);
            cacheKey = DataUtils.appendCacheKey(cacheKey, AbstractModel.NUM_ITEMS, numItems);

            // List<Comment> comments = (List<Comment>)play.cache.Cache.get(cacheKey);
            List<Comment> comments = null;
            if (comments == null)   {
		    comments = SQLCommander.queryTopLevelComments(activityId, pageSt, pageEd, Comment.ID, SQLHelper.DESCEND, numItems);
		    if (comments != null) play.cache.Cache.set(cacheKey, comments, DataUtils.CACHE_DURATION);
	    }
            if (comments == null)   throw new NullPointerException();

            ObjectNode result = Json.newObject();
            result.put(Comment.COUNT, 0);
            result.put(Comment.PAGE_ST, pageSt);
            result.put(Comment.PAGE_ED, pageEd);

            ArrayNode commentsNode = new ArrayNode(JsonNodeFactory.instance);
            for (Comment comment : comments)	commentsNode.add(comment.toObjectNode(false, null));
            result.put(Comment.COMMENTS, commentsNode);
            return ok(result);
        } catch (Exception e) {
            Loggy.e(TAG, "query", e);
        }
        return badRequest();
    }

    public static Result submit() {
	    try {
		    Map<String, String[]> formData = request().body().asFormUrlEncoded();
			if (!formData.containsKey(Comment.CONTENT)) throw new InvalidCommentParamsException();
			if (!formData.containsKey(Comment.ACTIVITY_ID)) throw new InvalidCommentParamsException();
			if (!formData.containsKey(User.TOKEN)) throw new InvalidCommentParamsException();

		    String content = formData.get(Comment.CONTENT)[0];
		    if (content == null || !General.validateCommentContent(content)) throw new InvalidCommentParamsException();

		    String token = formData.get(User.TOKEN)[0];
		    if (token == null) throw new InvalidCommentParamsException();

		    Long from = SQLCommander.queryUserId(token);
		    if (from == null) throw new UserNotFoundException();
            User fromUser = SQLCommander.queryUser(from);
            if (fromUser == null) throw new UserNotFoundException();

		    Long activityId = Converter.toLong(formData.get(Comment.ACTIVITY_ID)[0]);
            if (activityId == null) throw new InvalidCommentParamsException();
		    Activity activity = SQLCommander.queryActivity(activityId);

		    if (activity == null) throw new ActivityNotFoundException();

            SQLCommander.isActivityCommentable(fromUser, activity);

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();

		    String[] cols = {Comment.CONTENT, Comment.ACTIVITY_ID, Comment.FROM, Comment.GENERATED_TIME};
		    Object[] vals = {content, activityId, from, General.millisec()};

		    builder.insert(cols, vals).into(Comment.TABLE);
		    if (SQLHelper.INVALID == builder.execInsert()) throw new NullPointerException();
		    return ok();
	    } catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (Exception e) {
		    Loggy.e(TAG, "submit", e);
	    }
	    return badRequest();
    }
}
