package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.EasyPreparedStatementBuilder;
import exception.InvalidLoginParamsException;
import exception.InvalidRegistrationParamsException;
import exception.InvalidUserActivityRelationException;
import exception.UserNotFoundException;
import models.Login;
import models.User;
import models.UserActivityRelation;
import org.json.simple.JSONObject;
import play.libs.Json;
import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import utilities.Converter;
import utilities.DataUtils;
import utilities.General;

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

    public static Result profile() {
        try {
            Content html = views.html.profile.render();
            return ok(html);
        } catch (Exception e) {
            DataUtils.log(TAG, "profile", e);
        }
        return badRequest();
    }

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
            DataUtils.log(TAG, "sendVerificationEmail", e);
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

            String passwordDigest = Converter.md5(password);
            User user = SQLCommander.queryUserByEmail(email);

            if (user == null || !user.getPassword().equals(passwordDigest)) throw new UserNotFoundException();

            String token = Converter.generateToken(email, password);
            Integer userId = user.getId();

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] cols = {Login.USER_ID, Login.TOKEN};
            Object[] vals = {userId, token};
            builder.insert(cols, vals).into(Login.TABLE).execInsert();
            ObjectNode result = user.toObjectNode(userId);
            result.put(User.TOKEN, token);
            return ok(result);
        } catch (Exception e) {
            DataUtils.log(TAG, "login", e);
        }

        return badRequest();
    }

    public static Result register() {
        // define response attributes
        response().setContentType("text/plain");
        try {
            RequestBody body = request().body();
            Map<String, String[]> formData = body.asFormUrlEncoded();
            String name = formData.get(User.NAME)[0];
            String email = formData.get(User.EMAIL)[0];
            String password = formData.get(User.PASSWORD)[0];

            if ((name == null || !General.validateName(name)) || (email == null || !General.validateEmail(email)) || (password == null || !General.validatePassword(password)))  throw new InvalidRegistrationParamsException();
            String passwordDigest = Converter.md5(password);
            User user = new User(email, passwordDigest, name);
            int lastId = SQLCommander.registerUser(user);
            if (lastId == SQLCommander.INVALID) throw new NullPointerException();

            String code = generateVerificationCode(user);

            String[] columnNames = {User.VERIFICATION_CODE};
            Object[] columnValues = {code};

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.update(User.TABLE).set(columnNames, columnValues).where(User.ID, "=", lastId);

            if(!builder.execUpdate()) throw new NullPointerException();
            sendVerificationEmail(user.getName(), user.getEmail(), code);
            return ok();
        } catch (Exception e) {
            DataUtils.log(TAG, "register", e);
        }
        return badRequest();
    }

    public static Result status(String token) {
        // define response attributes
        response().setContentType("text/plain");
        try {
            if (token == null) throw new NullPointerException();
            Integer userId = SQLCommander.queryUserId(token);
            User user = SQLCommander.queryUser(userId);

            if (user == null) throw new UserNotFoundException();
            return ok(user.toObjectNode(userId));
        } catch (Exception e) {
            DataUtils.log(TAG, "status", e);
        }
        return badRequest("User doesn't exist or not logged in");
    }

    public static Result relation(Integer activityId, String token) {
        // define response attributes
        response().setContentType("text/plain");

        try {
            Integer userId = SQLCommander.queryUserId(token);
            int relation = SQLCommander.queryUserActivityRelation(userId, activityId);

            if (relation == UserActivityRelation.INVALID) throw new InvalidUserActivityRelationException();
            ObjectNode ret = Json.newObject();
            ret.put(UserActivityRelation.RELATION, String.valueOf(relation));
            return ok(ret);
        } catch (Exception e) {
            DataUtils.log(TAG, "relation", e);
        }
        return badRequest();
    }

    public static Result logout() {
        // define response attributes
        response().setContentType("text/plain");

        try {
            Map<String, String[]> formData = request().body().asFormUrlEncoded();
            String token = formData.get(User.TOKEN)[0];
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.from(Login.TABLE).where(Login.TOKEN, "=", token);
            if (!builder.execDelete()) throw new NullPointerException();
            return ok();
        } catch (Exception e) {
            DataUtils.log(TAG, "logout", e);
        }
        return badRequest();
    }

    public static Result duplicate(String name) {
        // define response attributes
        response().setContentType("text/plain");
        try {
            if (name == null) throw new NullPointerException();
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<JSONObject> userJsons = builder.select(User.ID).from(User.TABLE).where(User.NAME, "=", name).execSelect();
            if (userJsons != null && userJsons.size() > 0) throw new UserNotFoundException();
            return ok();
        } catch (Exception e) {
            DataUtils.log(TAG, "duplicate", e);
        }
        return badRequest();
    }

    public static Result detail(Integer vieweeId, String token) {
        try {
            response().setContentType("text/plain");
            Integer viewerId = null;
            if (token != null)  viewerId = SQLCommander.queryUserId(token);
            User viewee = SQLCommander.queryUser(vieweeId);
            return ok(viewee.toObjectNode(viewerId));
        } catch (Exception e) {
            DataUtils.log(TAG, "detail", e);
        }
        return badRequest();
    }

    protected static String generateVerificationCode(User user) {
	    return DataUtils.encryptByTime(user.getName());
    }

}
