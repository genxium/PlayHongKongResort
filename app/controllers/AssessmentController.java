package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import components.TokenExpiredResult;
import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.*;
import models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.Converter;
import utilities.DataUtils;
import utilities.Loggy;

import java.util.*;

public class AssessmentController extends Controller {

    public static final String TAG = AssessmentController.class.getName();

    public static Result list(Long to, Integer pageSt, Integer pageEd, Integer numItems, String token) {
        try {
            if (to == null || to.equals(0) || pageSt == null || pageEd == null || numItems == null) throw new InvalidQueryParamsException();

            // anti=cracking by param token
            if (token == null) throw new InvalidQueryParamsException();
	    Long viewerId = SQLCommander.queryUserId(token);
	    if (viewerId == null) throw new UserNotFoundException();
	    User viewer = SQLCommander.queryUser(viewerId);
	    if (viewer == null) throw new UserNotFoundException();

            ObjectNode result = Json.newObject();
	    if (viewerId.equals(to)) return ok(result);

	    List<Assessment> assessmentList = SQLCommander.queryAssessmentList(pageSt, pageEd, numItems, Assessment.GENERATED_TIME, SQLHelper.DESCEND, viewerId, to);

	    for (Assessment assessment : assessmentList)   result.put(String.valueOf(assessment.getId()), assessment.toObjectNodeWithNames());
            return ok(result);
        } catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "list", e);
        }
        return badRequest();
    }

    public static Result query(String refIndex, Integer numItems, Integer direction, String token, Long to, Long activityId) {
        response().setContentType("text/plain");
        try {
		if (to.equals(0L)) to = null;
		Long from = SQLCommander.queryUserId(token);
		if(from.equals(to)) throw new AccessDeniedException();
		List<Assessment> assessments = SQLCommander.queryAssessments(refIndex, Assessment.GENERATED_TIME, SQLHelper.DESCEND, numItems, direction, null, to, activityId);
		ObjectNode result = Json.newObject();
		for (Assessment assessment : assessments)   result.put(String.valueOf(assessment.getId()), assessment.toObjectNodeWithNames());
		return ok(result);
        } catch (Exception e) {
		    Loggy.e(TAG, "query", e);
        }
        return badRequest();
    }

    public static Result submit() {

        try {
            Http.RequestBody body = request().body();

            // get file data from request body stream
            Map<String, String[]> formData = body.asFormUrlEncoded();

            String token = formData.get(User.TOKEN)[0];
            if (token == null) throw new NullPointerException();
            Long userId = SQLCommander.queryUserId(token);

            if (userId == null) throw new UserNotFoundException();
            User user = SQLCommander.queryUser(userId);

            if (user == null) throw new UserNotFoundException();

            Long activityId = Converter.toLong(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
            if (activityId == null) throw new ActivityNotFoundException();

            Activity activity = SQLCommander.queryActivity(activityId);
            if (activity == null) throw new ActivityNotFoundException();
            if (!activity.hasBegun()) throw new ActivityHasNotBegunException();
            if (activity.getStatus() != Activity.ACCEPTED) throw new ActivityNotAcceptedException();

            int originalRelation = SQLCommander.queryUserActivityRelation(userId, activityId);
            if(originalRelation == UserActivityRelation.INVALID) throw new InvalidUserActivityRelationException();

            // Only PRESENT participants can submit assessments (host must be present)
            if ( (originalRelation & UserActivityRelation.PRESENT) == 0) throw new InvalidUserActivityRelationException();

            List<Long> userIdList = new LinkedList<Long>();
            List<Assessment> assessmentList = new LinkedList<Assessment>();
            userIdList.add(userId);

            JSONArray bundle= (JSONArray) JSONValue.parse(formData.get(AbstractMessage.BUNDLE)[0]);
            for (Object obj : bundle) {
                Assessment assessment = new Assessment((JSONObject) obj);
                assessment.setActivityId(activityId);
                assessment.setFrom(userId);
                assessmentList.add(assessment);
                userIdList.add(assessment.getTo());
            }

            List<Integer> relationList = SQLCommander.queryUserActivityRelationList(userIdList, activityId);

            // validation loop
            for (Integer relation : relationList) {
                if (relation == UserActivityRelation.INVALID) throw new InvalidUserActivityRelationException();
                if ((relation & UserActivityRelation.SELECTED) == 0) throw new InvalidUserActivityRelationException();
            }

            SQLCommander.createAssessments(assessmentList);

            int newRelation = UserActivityRelation.maskRelation(UserActivityRelation.ASSESSED, originalRelation);

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.update(UserActivityRelation.TABLE).set(UserActivityRelation.RELATION, newRelation)
                    .where(UserActivityRelation.USER_ID, "=", userId)
                    .where(UserActivityRelation.ACTIVITY_ID, "=", activityId);
            if(!builder.execUpdate()) throw new NullPointerException();

            ObjectNode ret = Json.newObject();
            ret.put(UserActivityRelation.RELATION, newRelation);
            return ok(ret).as("text/plain");
        } catch (TokenExpiredException e) {
            return badRequest(TokenExpiredResult.get());
        } catch (Exception e) {
		    Loggy.e(TAG, "submit", e);
        }
		return badRequest();
    }
}
