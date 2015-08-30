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
import utilities.General;
import utilities.Loggy;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AssessmentController extends Controller {

    public static final String TAG = AssessmentController.class.getName();

    public static Result list(Long to, Integer pageSt, Integer pageEd, Integer numItems, String token) {
        try {
            if (to == null || to.equals(0L) || pageSt == null || pageEd == null || numItems == null)
                throw new InvalidRequestParamsException();

            // anti=cracking by param token
            if (token == null) throw new InvalidRequestParamsException();
            Long viewerId = DBCommander.queryPlayerId(token);
            if (viewerId == null) throw new PlayerNotFoundException();

            ArrayNode result = new ArrayNode(JsonNodeFactory.instance);
            if (viewerId.equals(to)) return ok(result);

            List<Assessment> assessmentList = DBCommander.queryAssessmentList(pageSt, pageEd, numItems, Assessment.GENERATED_TIME, SQLHelper.DESCEND, viewerId, to);
            for (Assessment assessment : assessmentList) result.add(assessment.toObjectNode(viewerId));
            return ok(result);
        } catch (TokenExpiredException e) {
            return ok(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "list", e);
        }
        return badRequest();
    }

    public static Result query(String refIndex, Integer numItems, Integer direction, String token, Long to, Long activityId) {
        try {
            if (token == null) throw new InvalidRequestParamsException();
            if (to.equals(0L)) to = null;
            Long viewerId = DBCommander.queryPlayerId(token);
            if (viewerId.equals(to)) throw new AccessDeniedException();
            List<Assessment> assessments = DBCommander.queryAssessments(refIndex, Assessment.GENERATED_TIME, SQLHelper.DESCEND, numItems, direction, null, to, activityId);
            ArrayNode result = new ArrayNode(JsonNodeFactory.instance);
            for (Assessment assessment : assessments) result.add(assessment.toObjectNode(viewerId));
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

            String token = formData.get(Player.TOKEN)[0];
            if (token == null) throw new InvalidRequestParamsException();
            Long playerId = DBCommander.queryPlayerId(token);

            if (playerId == null) throw new PlayerNotFoundException();
            Player player = DBCommander.queryPlayer(playerId);

            if (player == null) throw new PlayerNotFoundException();

            Long activityId = Converter.toLong(formData.get(PlayerActivityRelation.ACTIVITY_ID)[0]);
            if (activityId == null) throw new ActivityNotFoundException();

            Activity activity = DBCommander.queryActivity(activityId);
            if (activity == null) throw new ActivityNotFoundException();
            if (!activity.hasBegun()) throw new ActivityHasNotBegunException();
            if (activity.getStatus() != Activity.ACCEPTED) throw new ActivityNotAcceptedException();

            int originalRelation = DBCommander.queryPlayerActivityRelation(playerId, activityId);
            if (originalRelation == PlayerActivityRelation.INVALID) throw new InvalidPlayerActivityRelationException();

            // Only PRESENT participants can submit assessments (host must be present)
            if ((originalRelation & PlayerActivityRelation.PRESENT) == 0)
                throw new InvalidPlayerActivityRelationException();

            List<Long> playerIdList = new LinkedList<>();
            List<Assessment> assessmentList = new LinkedList<>();
            playerIdList.add(playerId); // validate whether the submitting participant has been selected

            JSONArray bundle = (JSONArray) JSONValue.parse(formData.get(AbstractMessage.BUNDLE)[0]);
            for (Object obj : bundle) {
                Assessment assessment = new Assessment((JSONObject) obj);
                if (!General.validateAssessmentContent(assessment.getContent()))
                    throw new InvalidRequestParamsException();
                if (assessment.getTo().equals(playerId)) throw new InvalidAssessmentBehaviourException();
                assessment.setActivityId(activityId);
                assessment.setFrom(playerId);
                assessmentList.add(assessment);
                playerIdList.add(assessment.getTo());
            }

            List<Integer> relationList = DBCommander.queryPlayerActivityRelationList(playerIdList, activityId);

            // validation loop
            for (Integer relation : relationList) {
                if (relation == PlayerActivityRelation.INVALID) throw new InvalidPlayerActivityRelationException();
                if ((relation & PlayerActivityRelation.SELECTED) == 0)
                    throw new InvalidPlayerActivityRelationException();
            }

            DBCommander.createAssessments(assessmentList);

            int newRelation = PlayerActivityRelation.maskRelation(PlayerActivityRelation.ASSESSED, originalRelation);

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.update(PlayerActivityRelation.TABLE)
                    .set(PlayerActivityRelation.RELATION, newRelation)
                    .where(PlayerActivityRelation.PLAYER_ID, "=", playerId)
                    .where(PlayerActivityRelation.ACTIVITY_ID, "=", activityId);
            if (!builder.execUpdate()) throw new NullPointerException();

            ObjectNode ret = Json.newObject();
            ret.put(PlayerActivityRelation.RELATION, newRelation);
            return ok(ret);
        } catch (TokenExpiredException e) {
            return ok(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "submit", e);
        }
        return badRequest();
    }
}
