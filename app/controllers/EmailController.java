package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import components.StandardFailureResult;
import components.StandardSuccessResult;
import components.TokenExpiredResult;
import dao.EasyPreparedStatementBuilder;
import dao.SimpleMap;
import exception.DuplicateException;
import exception.InvalidRequestParamsException;
import exception.PlayerNotFoundException;
import exception.TokenExpiredException;
import models.Player;
import play.libs.Json;
import play.mvc.Content;
import play.mvc.Http;
import play.mvc.Result;
import utilities.DataUtils;
import utilities.General;
import utilities.Loggy;
import views.html.email_verification;

import java.util.HashMap;
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
			List<SimpleMap> data = builder.select(Player.ID).from(Player.TABLE).where(Player.EMAIL, "=", email).execSelect();
			if (data != null && data.size() > 0) throw new DuplicateException();
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

			String[] names = Player.QUERY_FILEDS;            EasyPreparedStatementBuilder builderSelect = new EasyPreparedStatementBuilder();
			List<SimpleMap> data = builderSelect.select(names).from(Player.TABLE).where(Player.EMAIL, "=", email).execSelect();

			if (data == null || data.size() != 1) throw new PlayerNotFoundException();

			Player player = new Player(data.get(0));

			EasyPreparedStatementBuilder builderUpdate = new EasyPreparedStatementBuilder();
			boolean res = builderUpdate.update(Player.TABLE)
				.set(Player.GROUP_ID, Player.USER)
				.set(Player.AUTHENTICATION_STATUS, (player.getAuthenticationStatus() | Player.EMAIL_AUTHENTICATED))
				.set(Player.VERIFICATION_CODE, "")
				.where(Player.EMAIL, "=", email)
				.where(Player.VERIFICATION_CODE, "=", code).execUpdate();

                        final String protocolPrefix = "http://";
                        final String host = request().host();
                        final String path = "/player/email";

                        final Map<String, Object> params = new HashMap<>();
                        params.put(Player.NAME, player.getName());
                        params.put(Player.EMAIL, player.getEmail());

			if (res) {
                                final String hash = "success";
                                final String url = protocolPrefix + host + path + "#" + hash + "?" + DataUtils.toUrlParams(params);
				return redirect(url);
			} else {
                                final String hash = "failure";
                                final String url = protocolPrefix + host + path + "#" + hash + "?" + DataUtils.toUrlParams(params);
				return redirect(url);
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
