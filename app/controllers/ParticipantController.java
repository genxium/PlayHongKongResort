package controllers;

import components.StandardFailureResult;
import components.StandardSuccessResult;
import dao.EasyPreparedStatementBuilder;
import exception.AccessDeniedException;
import exception.ActivityHasBegunException;
import exception.ActivityNotFoundException;
import exception.UserNotFoundException;
import models.AbstractMessage;
import models.Activity;
import models.User;
import models.UserActivityRelation;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import play.mvc.Result;
import utilities.Converter;
import utilities.Loggy;

import java.util.Map;

public class ParticipantController extends UserController {

	public static final String TAG = ParticipantController.class.getName();

	public static Result update() {
		try {
			Map<String, String[]> formData = request().body().asFormUrlEncoded();
			String token = formData.get(User.TOKEN)[0];
			Long activityId = Converter.toLong(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
			Activity activity = SQLCommander.queryActivity(activityId);
			if(activity == null) throw new ActivityNotFoundException();
			if(activity.hasBegun()) throw new ActivityHasBegunException();

			Long viewerId = SQLCommander.queryUserId(token);
			if (viewerId == null) throw new UserNotFoundException();
			if (!SQLCommander.validateOwnership(viewerId, activity)) throw new AccessDeniedException();

            /**
             * TODO: select user_id, relation from user_activity_relation where ..., check if returned records matches requested user list
             * */
			int count = 0;
			JSONArray bundle = (JSONArray) JSONValue.parse(formData.get(AbstractMessage.BUNDLE)[0]);
			for (Object selectedParticipantJson : bundle) {
				Long userId = Converter.toLong(selectedParticipantJson);
				if (userId.equals(viewerId)) continue; // anti-cracking by selecting the host of an activity
				int originalRelation = SQLCommander.queryUserActivityRelation(userId, activityId);
				if (originalRelation != UserActivityRelation.APPLIED) continue; // // anti-cracking by verifying original relation
				if (!SQLCommander.updateUserActivityRelation(userId, activityId, UserActivityRelation.maskRelation(UserActivityRelation.SELECTED, originalRelation))) continue;
				++count;
			}

			EasyPreparedStatementBuilder change = new EasyPreparedStatementBuilder();
			change.update(Activity.TABLE)
                    .decrease(Activity.NUM_APPLIED, count)
                    .increase(Activity.NUM_SELECTED, count)
                    .where(Activity.ID, "=", activityId);
			if (!change.execUpdate()) throw new NullPointerException();

			return ok(StandardSuccessResult.get());
		} catch (Exception e) {
			Loggy.e(TAG, "update", e);
		}

		return ok(StandardFailureResult.get());
	}
}
