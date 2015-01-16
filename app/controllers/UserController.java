package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import components.CaptchaNotMatchedResult;
import components.StandardFailureResult;
import components.StandardSuccessResult;
import components.TokenExpiredResult;
import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.*;
import models.Login;
import models.User;
import models.UserActivityRelation;
import org.json.simple.JSONObject;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import utilities.Converter;
import utilities.General;
import utilities.Loggy;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class UserController extends Controller {

    public static final String TAG = UserController.class.getName();

    protected static void sendVerificationEmail(String name, String recipient, String code) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("admin@hongkongresort.com", "The HongKongResort Team"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient, name));
            msg.setSubject("Welcome to HongKongResort");
            String link = "http://" + request().host() + "/user/email/verify?email=" + recipient + "&code=" + code;
            msg.setText("Dear " + name + ", you're our member now! Please click the following link to complete email verification: " + link);
            Transport.send(msg);
        } catch (Exception e) {
            Loggy.e(TAG, "sendVerificationEmail", e);
        }
    }

    public static Result login() {
        // define response attributes
        response().setContentType("text/plain");
        try {
            Http.RequestBody body = request().body();
            Map<String, String[]> formData = body.asFormUrlEncoded();
            String email = formData.get(User.EMAIL)[0];
            String password = formData.get(User.PASSWORD)[0];

            if ((email == null || !General.validateEmail(email)) || (password == null || !General.validatePassword(password)))  throw new InvalidLoginParamsException();

            User user = SQLCommander.queryUserByEmail(email);
            if (user == null) throw new UserNotFoundException();

            String passwordDigest = Converter.md5(password + user.getSalt());
            if (!user.getPassword().equals(passwordDigest)) throw new UserNotFoundException();

            String token = Converter.generateToken(email, password);
            Long userId = user.getId();

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] cols = {Login.USER_ID, Login.TOKEN, Login.TIMESTAMP};
            Object[] vals = {userId, token, General.millisec()};
            builder.insert(cols, vals).into(Login.TABLE).execInsert();
            ObjectNode result = user.toObjectNode(userId);
            result.put(User.TOKEN, token);
            return ok(result);
        } catch (Exception e) {
            Loggy.e(TAG, "login", e);
        }

        return badRequest();
    }

    public static Result register() {
	    try {
		    RequestBody body = request().body();
		    Map<String, String[]> formData = body.asFormUrlEncoded();
		    String name = formData.get(User.NAME)[0];
		    String email = formData.get(User.EMAIL)[0];
		    String password = formData.get(User.PASSWORD)[0];

		    String sid = formData.get(UserActivityRelation.SID)[0];
		    String captcha = formData.get(UserActivityRelation.CAPTCHA)[0];  
		    if (sid == null || captcha == null) throw new CaptchaNotMatchedException(); 
		    if (session(sid) == null || !captcha.equalsIgnoreCase(session(sid))) throw new CaptchaNotMatchedException(); 

		    if ((name == null || !General.validateName(name)) || (email == null || !General.validateEmail(email)) || (password == null || !General.validatePassword(password)))  throw new InvalidRegistrationParamsException();
		    String code = SQLCommander.generateVerificationCode(name);
		    String salt = SQLCommander.generateSalt(email, password);
		    String passwordDigest = Converter.md5(password + salt);
		    User user = new User(email, passwordDigest, name);
		    user.setVerificationCode(code);
		    user.setSalt(salt);
		    if (SQLCommander.registerUser(user) == SQLHelper.INVALID) throw new NullPointerException();
		    sendVerificationEmail(user.getName(), user.getEmail(), code);
		    return ok().as("text/plain");
	    } catch (CaptchaNotMatchedException e) {
		    return badRequest(CaptchaNotMatchedResult.get());
	    } catch (Exception e) {
		    Loggy.e(TAG, "register", e);
	    }  
	    return badRequest();
    }

    public static Result status(String token) {
	    try {
		    if (token == null) throw new NullPointerException();
		    Long userId = SQLCommander.queryUserId(token);
		    if (userId == null) throw new UserNotFoundException();
		    User user = SQLCommander.queryUser(userId);
		    if (user == null) throw new UserNotFoundException();
		    return ok(user.toObjectNode(userId)).as("text/plain");
	    } catch (Exception e) {
		    if (e instanceof UserNotFoundException)	return ok(StandardFailureResult.get());
		    Loggy.e(TAG, "status", e);
	    }
	    return ok(StandardFailureResult.get());
    }

    public static Result relation(Long activityId, String token) {
	    try {
		    Long userId = SQLCommander.queryUserId(token);
		    int relation = SQLCommander.queryUserActivityRelation(userId, activityId);
		    if (relation == UserActivityRelation.INVALID) throw new InvalidUserActivityRelationException();
		    ObjectNode ret = Json.newObject();
		    ret.put(UserActivityRelation.RELATION, String.valueOf(relation));
		    return ok(ret);
	    } catch (TokenExpiredException e) {
		    return badRequest(TokenExpiredResult.get());
	    } catch (Exception e) {
		    Loggy.e(TAG, "relation", e);
	    }
	    return badRequest();
    }

    public static Result logout() {

        try {
            Map<String, String[]> formData = request().body().asFormUrlEncoded();
            String token = formData.get(User.TOKEN)[0];
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.from(Login.TABLE).where(Login.TOKEN, "=", token);
            if (!builder.execDelete()) throw new NullPointerException();
            return ok();
        } catch (Exception e) {
            Loggy.e(TAG, "logout", e);
        }
        return badRequest();
    }

    public static Result duplicate(String name) {
        try {
            if (name == null) throw new NullPointerException();
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> userJsons = builder.select(User.ID).from(User.TABLE).where(User.NAME, "=", name).execSelect();
            if (userJsons != null && userJsons.size() > 0) throw new DuplicateException();
            return ok(StandardSuccessResult.get());
        } catch (DuplicateException e) {
            ok(StandardFailureResult.get());
        } catch (Exception e) {
            Loggy.e(TAG, "duplicate", e);
        }
        return ok(StandardFailureResult.get());
    }

    public static Result detail(Long vieweeId, String token) {
        try {
            if (vieweeId.equals(0L)) vieweeId = null;
            Long viewerId = null;
            if (token != null)  viewerId = SQLCommander.queryUserId(token);
            User viewee = SQLCommander.queryUser(vieweeId);
            return ok(viewee.toObjectNode(viewerId)).as("text/plain");
        } catch (TokenExpiredException e) {
            Loggy.e(TAG, "detail", e);
        }
        return badRequest();
    }
}
