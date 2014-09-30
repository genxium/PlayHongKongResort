package controllers;

import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.AccessDeniedException;
import exception.ActivityHasBegunException;
import exception.ActivityNotFoundException;
import exception.UserNotFoundException;
import models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import play.mvc.Result;
import utilities.Converter;
import utilities.DataUtils;

import java.sql.PreparedStatement;
import java.util.Map;

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
			String[] selectedParticipantsJsonStrs = formData.get(ActivityDetail.SELECTED_PARTICIPANTS);
			String selectedParticipantsJsonStr = (selectedParticipantsJsonStrs.length > 0) ? selectedParticipantsJsonStrs[0] : "[]";

			JSONArray selectedParticipantsJson = (JSONArray) JSONValue.parse(selectedParticipantsJsonStr);

			Integer viewerId = SQLCommander.queryUserId(token);
			if (viewerId == null) throw new UserNotFoundException();
			if (!SQLCommander.validateOwnership(viewerId, activityId)) throw new AccessDeniedException();

			int count = 0;
			for (Object selectedParticipantJson : selectedParticipantsJson) {
				Integer userId = Converter.toInteger(selectedParticipantJson);
				if (userId.equals(viewerId)) continue; // anti-cracking by selecting the host of an activity
				int originalRelation = SQLCommander.queryUserActivityRelation(userId, activityId);
				if ((originalRelation & UserActivityRelation.selected) > 0) continue;
				if (!SQLCommander.updateUserActivityRelation(viewerId, userId, activityId, UserActivityRelation.maskRelation(UserActivityRelation.selected, originalRelation))) continue;
				++count;
			}

			EasyPreparedStatementBuilder change = new EasyPreparedStatementBuilder();
			change.update(Activity.TABLE).
			decrease(Activity.NUM_APPLIED, count).
			increase(Activity.NUM_SELECTED, count).
			where(Activity.ID, "=", activityId);
			if (!SQLHelper.update(change)) throw new NullPointerException();

			return ok();
		} catch (Exception e) {
			DataUtils.log(TAG, "update", e);
		}

		return badRequest();
	}
}
