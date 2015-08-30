package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import components.StandardFailureResult;
import components.StandardSuccessResult;
import components.TokenExpiredResult;
import dao.EasyPreparedStatementBuilder;
import exception.DuplicateException;
import exception.InvalidRequestParamsException;
import exception.TokenExpiredException;
import exception.PlayerNotFoundException;
import models.Player;
import org.json.simple.JSONObject;
import play.libs.Json;
import play.mvc.Content;
import play.mvc.Http;
import play.mvc.Result;
import utilities.DataUtils;
import utilities.General;
import utilities.Loggy;
import views.html.email_verification;

import java.util.List;
import java.util.Map;

public class EmailController extends PlayerController {

    public static final String TAG = EmailController.class.getName();

    public static Result resend() {
        try {
            Http.RequestBody body = request().body();
            // get player token and activity id from request body stream
            Map<String, String[]> formData = body.asFormUrlEncoded();
            final String token = formData.get(Player.TOKEN)[0];
            if (token == null) throw new NullPointerException();
            Long playerId = DBCommander.queryPlayerId(token);
            if (playerId == null) throw new PlayerNotFoundException();
            Player player = DBCommander.queryPlayer(playerId);
            if (player == null) throw new PlayerNotFoundException();
            final String code = DBCommander.generateVerificationCode(player.getName());
            player.setVerificationCode(code);
            if (!DBCommander.updatePlayer(player)) throw new NullPointerException();
            sendVerificationEmail(player.getLang(), player.getName(), player.getEmail(), code);
            ObjectNode ret = Json.newObject();
            ret.put(Player.EMAIL, player.getEmail());
            return ok(ret);
        } catch (TokenExpiredException e) {
            return ok(TokenExpiredResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "resend", e);
        }
        return ok(StandardFailureResult.get());
    }

    public static Result duplicate(final String email) {
        try {
            if (email == null || !General.validateEmail(email)) throw new InvalidRequestParamsException();
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> playerJsons = builder.select(Player.ID).from(Player.TABLE).where(Player.EMAIL, "=", email).execSelect();
            if (playerJsons != null && playerJsons.size() > 0) throw new DuplicateException();
            return ok(StandardSuccessResult.get());
        } catch (DuplicateException e) {
            return ok(StandardFailureResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "duplicate", e);
        }
        return ok(StandardFailureResult.get());
    }

    public static Result verify(final String email, final String code) {
        try {
            if (email == null || code == null) throw new NullPointerException();
            if (email.isEmpty() || code.isEmpty()) throw new InvalidRequestParamsException();
            if (!General.validateEmail(email)) throw new InvalidRequestParamsException();

            EasyPreparedStatementBuilder builderUpdate = new EasyPreparedStatementBuilder();
            boolean res = builderUpdate.update(Player.TABLE)
                    .set(Player.GROUP_ID, Player.USER)
                    .set(Player.VERIFICATION_CODE, "")
                    .where(Player.EMAIL, "=", email)
                    .where(Player.VERIFICATION_CODE, "=", code).execUpdate();

            String[] names = {Player.ID, Player.EMAIL, Player.NAME, Player.PASSWORD, Player.GROUP_ID, Player.AVATAR};
            EasyPreparedStatementBuilder builderSelect = new EasyPreparedStatementBuilder();
            List<JSONObject> playerJsons = builderSelect.select(names).from(Player.TABLE).where(Player.EMAIL, "=", email).execSelect();

            if (playerJsons == null || playerJsons.size() != 1) throw new PlayerNotFoundException();

            Player player = new Player(playerJsons.get(0));
            if (res) {
                return redirect("http://" + request().host() + "/player/email#success?name=" + DataUtils.encodeUtf8(player.getName()) + "&email=" + DataUtils.encodeUtf8(player.getEmail()));
            } else {
                return redirect("http://" + request().host() + "/player/email#failure?name=" + DataUtils.encodeUtf8(player.getName()) + "&email=" + DataUtils.encodeUtf8(player.getEmail()));
            }
        } catch (Exception e) {
            Loggy.e(TAG, "verify", e);
        }
        return badRequest();
    }

    public static Result index() {
        Content html = email_verification.render();
        return ok(html);
    }

}
