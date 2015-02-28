package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import components.StandardFailureResult;
import components.StandardSuccessResult;
import components.TokenExpiredResult;
import dao.EasyPreparedStatementBuilder;
import exception.DuplicateException;
import exception.InvalidQueryParamsException;
import exception.TokenExpiredException;
import exception.UserNotFoundException;
import models.User;
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

public class EmailController extends UserController {

    public static final String TAG = EmailController.class.getName();

    public static Result resend() {
        try {
            Http.RequestBody body = request().body();
            // get user token and activity id from request body stream
            Map<String, String[]> formData = body.asFormUrlEncoded();
            final String token = formData.get(User.TOKEN)[0];
            if (token == null) throw new NullPointerException();
            Long userId = SQLCommander.queryUserId(token);
            if (userId == null) throw new UserNotFoundException();
            User user = SQLCommander.queryUser(userId);
            if (user == null) throw new UserNotFoundException();
            final String code = SQLCommander.generateVerificationCode(user.getName());
            user.setVerificationCode(code);
            if (!SQLCommander.updateUser(user)) throw new NullPointerException();
            sendVerificationEmail(user.getName(), user.getEmail(), code);
            ObjectNode ret = Json.newObject();
            ret.put(User.EMAIL, user.getEmail());
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
            if (email == null || !General.validateEmail(email)) throw new NullPointerException();
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> userJsons = builder.select(User.ID).from(User.TABLE).where(User.EMAIL, "=", email).execSelect();
            if (userJsons != null && userJsons.size() > 0) throw new DuplicateException();
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
            if (email.isEmpty() || code.isEmpty()) throw new InvalidQueryParamsException();
            EasyPreparedStatementBuilder builderUpdate = new EasyPreparedStatementBuilder();
            boolean res = builderUpdate.update(User.TABLE)
                                        .set(User.GROUP_ID, User.USER)
                                        .set(User.VERIFICATION_CODE, "")
                                        .where(User.EMAIL, "=", email)
                                        .where(User.VERIFICATION_CODE, "=", code).execUpdate();

            String[] names = {User.ID, User.EMAIL, User.NAME, User.PASSWORD, User.GROUP_ID, User.AVATAR};
            EasyPreparedStatementBuilder builderSelect = new EasyPreparedStatementBuilder();
            List<JSONObject> userJsons = builderSelect.select(names).from(User.TABLE).where(User.EMAIL, "=", email).execSelect();

            if(userJsons == null || userJsons.size() != 1) throw new UserNotFoundException();

            User user = new User(userJsons.get(0));
			if (res) {
				return redirect("http://" + request().host() + "/user/email#success?name=" + DataUtils.encodeUtf8(user.getName()) + "&email=" + DataUtils.encodeUtf8(user.getEmail()));
			} else {
                return redirect("http://" + request().host() + "/user/email#failure?name=" + DataUtils.encodeUtf8(user.getName()) + "&email=" + DataUtils.encodeUtf8(user.getEmail()));
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
