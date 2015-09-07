package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import components.StandardFailureResult;
import components.StandardSuccessResult;
import components.TokenExpiredResult;
import dao.SQLBuilder;
import exception.*;
import fixtures.Constants;
import models.AbstractMessage;
import models.Activity;
import models.Player;
import models.PlayerActivityRelation;
import play.mvc.Result;
import utilities.Converter;
import utilities.Loggy;

import java.util.List;
import java.util.Map;

public class ParticipantController extends PlayerController {

        public static final String TAG = ParticipantController.class.getName();

        public static Result update() {
                try {
                        Map<String, String[]> formData = request().body().asFormUrlEncoded();
                        String token = formData.get(Player.TOKEN)[0];
                        Long activityId = Converter.toLong(formData.get(PlayerActivityRelation.ACTIVITY_ID)[0]);
                        if (activityId == null) throw new NullPointerException();

                        Activity activity = DBCommander.queryActivity(activityId);
                        if (activity == null) throw new ActivityNotFoundException();
                        if (activity.hasBegun()) throw new ActivityHasBegunException();

                        Long viewerId = DBCommander.queryPlayerId(token);
                        if (viewerId == null) throw new PlayerNotFoundException();
                        if (!DBCommander.validateOwnership(viewerId, activity)) throw new AccessDeniedException();

                        ObjectMapper mapper = new ObjectMapper();
                        List<Long> playerIdList = mapper.readValue(formData.get(AbstractMessage.BUNDLE)[0], mapper.getTypeFactory().constructCollectionType(List.class, Long.class));
                        for (Long playerId : playerIdList) {
                                if (playerId.equals(viewerId))
                                        throw new InvalidRequestParamsException(); // anti-cracking by selecting the host of an activity
                        }
                        if (playerIdList.size() + activity.getNumSelected() > Activity.MAX_SELECTED)
                                throw new NumberLimitExceededException();

                        List<Integer> relationList = DBCommander.queryPlayerActivityRelationList(playerIdList, activityId);

                        if (relationList == null) throw new NullPointerException();

                        // validation loop
                        for (Integer relation : relationList) {
                                if (relation == PlayerActivityRelation.INVALID) throw new InvalidPlayerActivityRelationException();
                                if (relation != PlayerActivityRelation.APPLIED) throw new InvalidPlayerActivityRelationException();
                        }

                        /**
                         * TODO: begin SQL-transaction guard
                         * */
                        if (!DBCommander.updatePlayerActivityRelation(playerIdList, activityId, PlayerActivityRelation.maskRelation(PlayerActivityRelation.SELECTED, PlayerActivityRelation.APPLIED)))
                                throw new NullPointerException();

                        int count = playerIdList.size();
                        SQLBuilder change = new SQLBuilder();
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
