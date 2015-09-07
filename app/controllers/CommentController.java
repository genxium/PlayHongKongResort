package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import components.TokenExpiredResult;
import dao.SQLBuilder;
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
            Comment comment = DBCommander.queryComment(commentId);
            if (comment == null) throw new NullPointerException();
            ObjectNode objNode = comment.toObjectNode(true, null);
            if (objNode == null) throw new NullPointerException();
            return ok(objNode);
        } catch (Exception e) {
            Loggy.e(TAG, "query", e);
        }
        return badRequest();

    }

    @SuppressWarnings("unchecked")
    public static Result list(Long activityId, Integer pageSt, Integer pageEd, Integer numItems) {
        try {
            if (activityId == null || pageSt == null || pageEd == null || numItems == null)
                throw new InvalidRequestParamsException();

            String cacheKey = "CommentController";
            cacheKey = DataUtils.appendCacheKey(cacheKey, AbstractActivityMessage.ACTIVITY_ID, activityId);
            cacheKey = DataUtils.appendCacheKey(cacheKey, AbstractModel.PAGE_ST, pageSt);
            cacheKey = DataUtils.appendCacheKey(cacheKey, AbstractModel.PAGE_ED, pageEd);
            cacheKey = DataUtils.appendCacheKey(cacheKey, AbstractModel.NUM_ITEMS, numItems);

            // List<Comment> comments = (List<Comment>)play.cache.Cache.get(cacheKey);
            List<Comment> comments = null;
            if (comments == null) {
                comments = DBCommander.queryTopLevelComments(activityId, pageSt, pageEd, Comment.ID, SQLHelper.DESCEND, numItems);
                if (comments != null) play.cache.Cache.set(cacheKey, comments, DataUtils.CACHE_DURATION);
            }
            if (comments == null) throw new NullPointerException();

            ObjectNode result = Json.newObject();
            result.put(Comment.COUNT, 0);
            result.put(Comment.PAGE_ST, pageSt);
            result.put(Comment.PAGE_ED, pageEd);

            ArrayNode commentsNode = new ArrayNode(JsonNodeFactory.instance);
            for (Comment comment : comments) {
                ObjectNode objNode = comment.toObjectNode(false, null);
                if (objNode == null) continue;
                commentsNode.add(objNode);
            }
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
            if (!formData.containsKey(Player.TOKEN)) throw new InvalidCommentParamsException();

            String content = formData.get(Comment.CONTENT)[0];
            if (content == null || !General.validateCommentContent(content)) throw new InvalidCommentParamsException();

            String token = formData.get(Player.TOKEN)[0];
            if (token == null) throw new InvalidCommentParamsException();

            Long from = DBCommander.queryPlayerId(token);
            if (from == null) throw new PlayerNotFoundException();
            Player fromPlayer = DBCommander.queryPlayer(from);
            if (fromPlayer == null) throw new PlayerNotFoundException();

            Long activityId = Converter.toLong(formData.get(Comment.ACTIVITY_ID)[0]);
            if (activityId == null) throw new InvalidCommentParamsException();
            Activity activity = DBCommander.queryActivity(activityId);

            if (activity == null) throw new ActivityNotFoundException();

            DBCommander.isActivityCommentable(fromPlayer, activity);

            SQLBuilder builder = new SQLBuilder();

            String[] cols = {Comment.CONTENT, Comment.ACTIVITY_ID, Comment.FROM, Comment.GENERATED_TIME};
            Object[] vals = {content, activityId, from, General.millisec()};

            builder.insert(cols, vals).into(Comment.TABLE);
            if (SQLHelper.INVALID == builder.execInsert()) throw new NullPointerException();
            return ok();
        } catch (TokenExpiredException e) {
            return ok(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "submit", e);
        }
        return badRequest();
    }
}
