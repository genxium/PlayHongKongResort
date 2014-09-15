package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.InvalidLoginParamsException;
import exception.InvalidRegistrationParamsException;
import exception.InvalidUserActivityRelationException;
import exception.UserNotFoundException;
import model.Image;
import model.User;
import model.UserActivityRelation;
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
import views.html.email_verification;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class UserController extends Controller {

    public static final String TAG = UserController.class.getName();

    public static Result showProfile() {
        try {
            Content html = views.html.profile.render();
            return ok(html);
        } catch (Exception e) {
            DataUtils.log(TAG, "showProfile", e);
        }
        return badRequest();
    }

    protected static void sendVerificationEmail(String name, String recipient, String code) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("hongkongresort@126.com", "The HongKongResort Team"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient, name));
            msg.setSubject("Welcome to HongKongResort");
            String link = "http://107.170.251.163/user/email/verify?code=" + code;
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

            session(token, userId.toString());
            ObjectNode result = user.toObjectNode(userId);
            result.put(User.TOKEN, token);
            return ok(result);
        } catch (Exception e) {
            System.out.println("UserController, " + e.getMessage());
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

            if (name == null || (email == null || !General.validateEmail(email)) || (password == null || !General.validatePassword(password)))  throw new InvalidRegistrationParamsException();
            String passwordDigest = Converter.md5(password);
            User user = new User(email, passwordDigest, name);
            int lastId = SQLCommander.registerUser(user);
            if (lastId == SQLCommander.INVALID) throw new NullPointerException();

            String code = generateVerificationCode(user);

            String[] columnNames = {User.VERIFICATION_CODE};
            Object[] columnValues = {code};

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.update(User.TABLE).set(columnNames, columnValues).where(User.ID, "=", lastId);

            if(!SQLHelper.update(builder)) throw new NullPointerException();
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
            Integer userId = DataUtils.getUserIdByToken(token);
            User user = SQLCommander.queryUser(userId);

            if (user == null) throw new UserNotFoundException();
            session(token, userId.toString());

            return ok(user.toObjectNode(userId));
        } catch (Exception e) {
            DataUtils.log(TAG, "status", e);
        }
        return badRequest("User doesn't exist or not logged in");
    }

    public static Result uploadAvatar() {
        // define response attributes
        response().setContentType("text/plain");
        try {
            RequestBody body = request().body();

            // get file data from request body stream
            Http.MultipartFormData data = body.asMultipartFormData();
            Http.MultipartFormData.FilePart avatarFile = data.getFile(User.AVATAR);

            // get user token from request body stream
            String token = DataUtils.getUserToken(data);
            Integer userId = DataUtils.getUserIdByToken(token);
            if (userId == null) throw new UserNotFoundException();
            User user = SQLCommander.queryUser(userId);
            if (user == null) throw new UserNotFoundException();

            if (avatarFile == null) throw new NullPointerException();
            int previousAvatarId = user.getAvatar();
            int newAvatarId = ExtraCommander.saveAvatarFile(avatarFile, user);
            if (newAvatarId == ExtraCommander.INVALID) throw new NullPointerException();

            // delete previous avatar record and file
            Image previousAvatar = SQLCommander.queryImage(previousAvatarId);
            boolean isPreviousAvatarDeleted = ExtraCommander.deleteImageRecordAndFile(previousAvatar);
            if (isPreviousAvatarDeleted) {
                System.out.println("UserController.uploadAvatar: previous avatar file and record deleted.");
            }

            return ok("Avatar uploaded");

        } catch (Exception e) {
            DataUtils.log(TAG, "uploadAvatar", e);
        }
        return badRequest("Avatar not uploaded!");
    }

    public static Result relation(Integer activityId, String token) {
        // define response attributes
        response().setContentType("text/plain");

        try {
            Integer userId = DataUtils.getUserIdByToken(token);
            int relation = SQLCommander.queryUserActivityRelation(userId, activityId);

            if (relation == UserActivityRelation.invalid) throw new InvalidUserActivityRelationException();
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
            session().remove(token);
            return ok();
        } catch (Exception e) {
            DataUtils.log(TAG, "logout", e);
        }
        return badRequest();
    }

    public static Result nameDuplicate(String name) {
        // define response attributes
        response().setContentType("text/plain");
        try {
            if (name == null) throw new NullPointerException();
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.select(User.ID).from(User.TABLE).where(User.NAME, "=", name);
            List<JSONObject> userJsons = SQLHelper.select(builder);
            if (userJsons != null && userJsons.size() > 0) throw new UserNotFoundException();
            return ok();
        } catch (Exception e) {
            DataUtils.log(TAG, "nameDuplicate", e);
        }
        return badRequest();
    }

    public static Result emailDuplicate(String email) {
        // define response attributes
        response().setContentType("text/plain");
        try {
            if (email == null || !General.validateEmail(email)) throw new NullPointerException();
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.select(User.ID).from(User.TABLE).where(User.EMAIL, "=", email);
            List<JSONObject> userJsons = SQLHelper.select(builder);
            if (userJsons != null && userJsons.size() > 0) throw new UserNotFoundException();
            return ok();
        } catch (Exception e) {
            DataUtils.log(TAG, "emailDuplicate", e);
        }
        return badRequest();
    }

    public static Result emailVerification(String code) {
        response().setContentType("text/html");
        try {
            EasyPreparedStatementBuilder builderUpdate = new EasyPreparedStatementBuilder();
            builderUpdate.update(User.TABLE).set(User.GROUP_ID, User.USER).where(User.VERIFICATION_CODE, "=", code);
            boolean res = SQLHelper.update(builderUpdate);

            String[] names = {User.ID, User.EMAIL, User.NAME, User.PASSWORD, User.GROUP_ID, User.AVATAR};
            EasyPreparedStatementBuilder builderSelect = new EasyPreparedStatementBuilder();
            builderSelect.select(names).from(User.TABLE).where(User.VERIFICATION_CODE, "=", code);
            List<JSONObject> userJsons = SQLHelper.select(builderSelect);
            User user = new User(userJsons.get(0));
            Content html = email_verification.render(res, user.getName(), user.getEmail());
            return ok(html);
        } catch (Exception e) {
            DataUtils.log(TAG, "emailVerification", e);
        }
        return badRequest();
    }

    public static Result detail(Integer vieweeId, String token) {
        try {
            response().setContentType("text/plain");
            Integer viewerId = null;
            if (token != null)  viewerId = DataUtils.getUserIdByToken(token);
            User viewee = SQLCommander.queryUser(vieweeId);
            return ok(viewee.toObjectNode(viewerId));
        } catch (Exception e) {
            DataUtils.log(TAG, "detail", e);
            System.out.println(UserController.class.getName() + ".detail, " + e.getMessage());
        }
        return badRequest();
    }

    protected static String generateVerificationCode(User user) {
        String ret = null;
        try {
            java.util.Date date = new java.util.Date();
            Timestamp currentTime = new Timestamp(date.getTime());
            Long epochTime = currentTime.getTime();
            String username = user.getName();
            String tmp = Converter.md5(epochTime.toString() + username);
            int length = tmp.length();
            ret = tmp.substring(0, length / 2);
        } catch (Exception e) {

        }
        return ret;
    }

}
