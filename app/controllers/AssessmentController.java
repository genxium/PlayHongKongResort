package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SQLHelper;
import dao.EasyPreparedStatementBuilder;
import models.*;
import exception.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.DataUtils;

import java.util.*;

public class AssessmentController extends Controller {

    public static final String BUNDLE = "bundle";

    public static Result query(String refIndex, Integer numItems, Integer direction, String token, Integer to, Integer activityId) {
        response().setContentType("text/plain");
        try {
		    Integer from = SQLCommander.queryUserId(token);
		    if(from.equals(to)) throw new AccessDeniedException();
		    List<Assessment> assessments = SQLCommander.queryAssessments(refIndex, Assessment.GENERATED_TIME, SQLHelper.DESCEND, numItems, direction, null, to, activityId);
            ObjectNode result = Json.newObject();
            for (Assessment assessment : assessments)   result.put(String.valueOf(assessment.getId()), assessment.toObjectNodeWithNames());
            return ok(result);
        } catch (Exception e) {
		    System.out.println(AssessmentController.class.getName() + ".query, " + e.getMessage());
        }
        return badRequest();
    }

    public static Result submit() {
        // define response attributes
        response().setContentType("text/plain");

        try {
		Http.RequestBody body = request().body();

		// get file data from request body stream
		Map<String, String[]> formData = body.asFormUrlEncoded();

		String token = formData.get(User.TOKEN)[0];
		if (token == null) throw new NullPointerException();
		Integer userId = SQLCommander.queryUserId(token);
		if (userId == null) throw new UserNotFoundException();
		User user = SQLCommander.queryUser(userId);
		if (user == null) throw new UserNotFoundException();
		Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
		if (activityId == null) throw new ActivityNotFoundException();
        Activity activitiy = SQLCommander.queryActivity(activityId);
        if(activitiy == null) throw new ActivityNotFoundException();

		Integer relation = SQLCommander.queryUserActivityRelation(userId, activityId);

        // Only present participants and host can submit assessments
		if ((relation & UserActivityRelation.present) == 0 && activitiy.getHost().getId() != userId) throw new InvalidUserActivityRelationException();

		String bundle = formData.get(BUNDLE)[0];
		JSONArray assessmentJsons = (JSONArray) JSONValue.parse(bundle);
		for (Object obj : assessmentJsons) {
			Assessment assessment = new Assessment((JSONObject) obj);
			assessment.setActivityId(activityId);
			assessment.setFrom(userId);
			Assessment existingAssessment = SQLCommander.queryAssessment(assessment.getActivityId(), assessment.getFrom(), assessment.getTo());
			if (existingAssessment != null) continue;
			if (!SQLCommander.isUserAssessable(assessment.getFrom(), assessment.getTo(), assessment.getActivityId()))   continue;
			SQLCommander.createAssessment(assessment.getActivityId(), assessment.getFrom(), assessment.getTo(), assessment.getContent());
		}

		int originalRelation = SQLCommander.queryUserActivityRelation(userId, activityId);
		if(originalRelation == UserActivityRelation.invalid) throw new InvalidUserActivityRelationException();

		int newRelation = UserActivityRelation.maskRelation(UserActivityRelation.assessed, originalRelation);

		EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		builder.update(UserActivityRelation.TABLE).set(UserActivityRelation.RELATION, newRelation);
		builder.where(UserActivityRelation.USER_ID, "=", userId);
		builder.where(UserActivityRelation.ACTIVITY_ID, "=", activityId);
		if(!SQLHelper.update(builder)) throw new NullPointerException();

		ObjectNode ret = Json.newObject();
		ret.put(UserActivityRelation.RELATION, newRelation);
		return ok(ret);
        } catch (Exception e) {
		System.out.println(AssessmentController.class.getName()+".submit, " + e.getMessage());
        }
	return badRequest();
    }
}
