package controllers;

import components.StandardFailureResult;
import components.StandardSuccessResult;
import components.TokenExpiredResult;
import dao.EasyPreparedStatementBuilder;
import exception.*;
import fixtures.Constants;
import models.AbstractMessage;
import models.Activity;
import models.User;
import models.UserActivityRelation;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import play.mvc.Result;
import utilities.Converter;
import utilities.Loggy;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ParticipantController extends UserController {

	public static final String TAG = ParticipantController.class.getName();

	public static Result update() {
		try {
			Map<String, String[]> formData = request().body().asFormUrlEncoded();
			String token = formData.get(User.TOKEN)[0];
			Long activityId = Converter.toLong(formData.get(UserActivityRelation.ACTIVITY_ID)[0]);
            if (activityId == null) throw new NullPointerException();

			Activity activity = DBCommander.queryActivity(activityId);
			if(activity == null) throw new ActivityNotFoundException();
			if(activity.hasBegun()) throw new ActivityHasBegunException();

			Long viewerId = DBCommander.queryUserId(token);
			if (viewerId == null) throw new UserNotFoundException();
			if (!DBCommander.validateOwnership(viewerId, activity)) throw new AccessDeniedException();

			List<Long> userIdList = new LinkedList<>();
			JSONArray bundle = (JSONArray) JSONValue.parse(formData.get(AbstractMessage.BUNDLE)[0]);
			for (Object selectedParticipantJson : bundle) {
				Long userId = Converter.toLong(selectedParticipantJson);
				if (userId.equals(viewerId)) throw new InvalidRequestParamsException(); // anti-cracking by selecting the host of an activity
				userIdList.add(userId);	
			}
			if (userIdList.size() + activity.getNumSelected() > Activity.MAX_SELECTED) throw new NumberLimitExceededException();

			List<Integer> relationList = DBCommander.queryUserActivityRelationList(userIdList, activityId);

			if (relationList == null) throw new NullPointerException();

			// validation loop
			for (Integer relation : relationList) {
				if (relation == UserActivityRelation.INVALID) throw new InvalidUserActivityRelationException();
				if (relation != UserActivityRelation.APPLIED) throw new InvalidUserActivityRelationException();
			}

			/**
			 * TODO: begin SQL-transaction guard
			 * */
			if (!DBCommander.updateUserActivityRelation(userIdList, activityId, UserActivityRelation.maskRelation(UserActivityRelation.SELECTED, UserActivityRelation.APPLIED))) throw new NullPointerException();

			int count = userIdList.size();
			EasyPreparedStatementBuilder change = new EasyPreparedStatementBuilder();
			change.update(Activity.TABLE)
			    .decrease(Activity.NUM_APPLIED, count)
			    .increase(Activity.NUM_SELECTED, count)
			    .where(Activity.ID, "=", activityId);
			if (!change.execUpdate()) throw new NullPointerException();
			/**
			 * TODO: end SQL-transaction guard
			 * */

			return ok(StandardSuccessResult.get());
		} catch (TokenExpiredException e) {
            return ok(TokenExpiredResult.get());
        } catch (NumberLimitExceededException e) {
			return ok(StandardFailureResult.get(Constants.INFO_ACTIVITY_SELECTED_LIMIT));
		} catch (Exception e) {
			Loggy.e(TAG, "update", e);
		}

		return ok(StandardFailureResult.get());
	}
}
