package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.*;
import models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import play.libs.Json;
import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.DataUtils;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParticipantController extends UserController {

	public static final String TAG = ParticipantController.class.getName();

	public static Result update() {
		// define response attributes
		response().setContentType("text/plain");
		try {
			Map<String, String[]> formData = request().body().asFormUrlEncoded();
			String token = formData.get(User.TOKEN)[0];
			Integer activityId = Integer.valueOf(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
			Activity activity = SQLCommander.queryActivity(activityId);
			if(activity == null) throw new ActivityNotFoundException();
			if(activity.hasBegun()) throw new ActivityHasBegunException();
			// String[] appliedParticipantsJsonStrs = formData.get(ActivityDetail.APPLIED_PARTICIPANTS);
			String[] selectedParticipantsJsonStrs = formData.get(ActivityDetail.SELECTED_PARTICIPANTS);
			// String appliedParticipantsJsonStr = (appliedParticipantsJsonStrs.length > 0) ? appliedParticipantsJsonStrs[0] : "[]";
			String selectedParticipantsJsonStr = (selectedParticipantsJsonStrs.length > 0) ? selectedParticipantsJsonStrs[0] : "[]";

			// JSONArray appliedParticipantsJson = (JSONArray) JSONValue.parse(appliedParticipantsJsonStr);
			JSONArray selectedParticipantsJson = (JSONArray) JSONValue.parse(selectedParticipantsJsonStr);

			Integer viewerId = DataUtils.getUserIdByToken(token);
			if (viewerId == null) throw new UserNotFoundException();
			if (!SQLCommander.validateOwnership(viewerId, activityId)) throw new AccessDeniedException();

			/* Forbid unselecting participants, uncomment corresponding codes to resume */
			/* 			
			for (Object appliedParticipantJson : appliedParticipantsJson) {
				Integer userId = Integer.valueOf((String) appliedParticipantJson);
				if (userId.equals(viewerId)) continue; // anti-cracking by unselecting the host of an activity
				int originalRelation = SQLCommander.queryUserActivityRelation(userId, activityId);
				SQLCommander.updateUserActivityRelation(viewerId, userId, activityId, UserActivityRelation.maskRelation(UserActivityRelation.applied, originalRelation));
			}
			*/

			for (Object selectedParticipantJson : selectedParticipantsJson) {
				Integer userId = Integer.valueOf((String) selectedParticipantJson);
				if (userId.equals(viewerId)) continue; // anti-cracking by selecting the host of an activity
				int originalRelation = SQLCommander.queryUserActivityRelation(userId, activityId);
				SQLCommander.updateUserActivityRelation(viewerId, userId, activityId, UserActivityRelation.maskRelation(UserActivityRelation.selected, originalRelation));
			}
			return ok();
		} catch (Exception e) {
			DataUtils.log(TAG, "update", e);
		}

		return badRequest();
	}
}
