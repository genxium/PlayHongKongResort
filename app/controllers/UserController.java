package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import components.CaptchaNotMatchedResult;
import components.StandardFailureResult;
import components.StandardSuccessResult;
import components.TokenExpiredResult;
import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.*;
import fixtures.Constants;
import models.Image;
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
import utilities.DataUtils;
import utilities.General;
import utilities.Loggy;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class UserController extends Controller {

    public static final String TAG = UserController.class.getName();

    protected static void sendVerificationEmail(final String lang, final String name, final String recipient, final String code) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
		HashMap<String, String> targetMap = Constants.LANG_MAP.get(lang);
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(Constants.ADMIN_EMAIL, targetMap.get(Constants.HONGKONGRESORT_TEAM)));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient, name));
            msg.setSubject(targetMap.get(Constants.WELCOME));
            String link = "http://" + request().host() + "/user/email/verify?email=" + recipient + "&code=" + code;
			String text = String.format(targetMap.get(Constants.VERIFY_INSTRUCTION), name, link);
            msg.setText(text);
            Transport.send(msg);
        } catch (Exception e) {
            Loggy.e(TAG, "sendVerificationEmail", e);
        }
    }

    public static Result login() {
	    try {
		    Http.RequestBody body = request().body();
		    Map<String, String[]> formData = body.asFormUrlEncoded();
		    String email = formData.get(User.EMAIL)[0];
		    String password = formData.get(User.PASSWORD)[0];

		    if ((email == null || !General.validateEmail(email)) || (password == null || !General.validatePassword(password)))  throw new InvalidLoginParamsException();

		    User user = DBCommander.queryUserByEmail(email);
		    if (user == null) throw new UserNotFoundException();

		    String passwordDigest = Converter.md5(password + user.getSalt());
		    if (!user.getPassword().equals(passwordDigest)) throw new PswErrException();

		    String token = Converter.generateToken(email, password);
		    Long userId = user.getId();

		    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		    String[] cols = {Login.USER_ID, Login.TOKEN, Login.TIMESTAMP};
		    Object[] vals = {userId, token, General.millisec()};
		    builder.insert(cols, vals).into(Login.TABLE).execInsert();
		    ObjectNode result = user.toObjectNode(userId);
		    result.put(User.TOKEN, token);
		    return ok(result);
	    } catch (UserNotFoundException e) {
		    return ok(StandardFailureResult.get(Constants.INFO_USER_NOT_FOUND));
	    } catch (PswErrException e) {
		    return ok(StandardFailureResult.get(Constants.INFO_PSW_ERR));
	    } catch (Exception e) {
		    return badRequest(StandardFailureResult.get());
	    }
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
		    String code = DBCommander.generateVerificationCode(name);
		    String salt = DBCommander.generateSalt(email, password);
		    String passwordDigest = Converter.md5(password + salt);
		    User user = new User(email, passwordDigest, name);
		    user.setVerificationCode(code);
		    user.setSalt(salt);
		    if (DBCommander.registerUser(user) == SQLHelper.INVALID) throw new NullPointerException();
		    sendVerificationEmail(user.getLang(), user.getName(), user.getEmail(), code);
		    return ok();
	    } catch (CaptchaNotMatchedException e) {
		    return ok(CaptchaNotMatchedResult.get());
	    } catch (Exception e) {
		    Loggy.e(TAG, "register", e);
	    }  
	    return badRequest();
    }

    public static Result status(String token) {
	    try {
		    if (token == null) throw new NullPointerException();
		    Long userId = DBCommander.queryUserId(token);
		    if (userId == null) throw new UserNotFoundException();
		    User user = DBCommander.queryUser(userId);
		    if (user == null) throw new UserNotFoundException();
		    return ok(user.toObjectNode(userId));
	    } catch (Exception e) {
		    if (e instanceof UserNotFoundException)	return ok(StandardFailureResult.get());
		    Loggy.e(TAG, "status", e);
	    }
	    return ok(StandardFailureResult.get());
    }

    public static Result relation(Long activityId, String token) {
	    try {
		    Long userId = DBCommander.queryUserId(token);
		    int relation = DBCommander.queryUserActivityRelation(userId, activityId);
		    if (relation == UserActivityRelation.INVALID) throw new InvalidUserActivityRelationException();
		    ObjectNode ret = Json.newObject();
		    ret.put(UserActivityRelation.RELATION, String.valueOf(relation));
		    return ok(ret);
	    } catch (TokenExpiredException e) {
		    return ok(TokenExpiredResult.get());
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
            if (token != null)  viewerId = DBCommander.queryUserId(token);
            User viewee = DBCommander.queryUser(vieweeId);
			if (viewee == null) throw new UserNotFoundException();
            return ok(viewee.toObjectNode(viewerId));
        } catch (TokenExpiredException | UserNotFoundException e) {
            Loggy.e(TAG, "detail", e);
        }
		return badRequest();
    }

	public static Result save() {
		try {
			Http.RequestBody body = request().body();

			// get file data from request body stream
			Http.MultipartFormData data = body.asMultipartFormData();
			Http.MultipartFormData.FilePart avatarFile = data.getFile(User.AVATAR);
			if (avatarFile != null && !DataUtils.validateImage(avatarFile)) throw new InvalidRequestParamsException();

			// get user token from request body stream
			String token = DataUtils.getUserToken(data);
			Long userId = DBCommander.queryUserId(token);
			if (userId == null) throw new UserNotFoundException();
			User user = DBCommander.queryUser(userId);
			if (user == null) throw new UserNotFoundException();

			Map<String, String[]> formData = data.asFormUrlEncoded();

			String age = formData.get(User.AGE)[0];
			String gender = formData.get(User.GENDER)[0];
			String mood = formData.get(User.MOOD)[0];

			if (!General.validateUserAge(age) || !General.validateUserGender(gender) || !General.validateUserMood(mood)) throw new InvalidRequestParamsException();

			user.setAge(age);
			user.setGender(gender);
			user.setMood(mood);

			if (avatarFile == null) {
				DBCommander.updateUser(user);
				return ok(user.toObjectNode(userId));
			}


			/**
			 * TODO: begin SQL-transaction guard (resembling ActivityController.save)
			 * */
			long previousAvatarId = user.getAvatar();
			long newAvatarId = ExtraCommander.saveAvatar(avatarFile, user);
			if (newAvatarId == SQLHelper.INVALID) throw new NullPointerException();

			user.setAvatar(newAvatarId);
				
			// delete previous avatar record and file
			Image previousAvatar = ExtraCommander.queryImage(previousAvatarId);
			if (previousAvatar == null) {
				// no previous avatar
				DBCommander.updateUser(user);
				return ok(user.toObjectNode(userId));
			}

			boolean isPreviousAvatarDeleted = ExtraCommander.deleteImageRecordAndFile(previousAvatar);
			if (!isPreviousAvatarDeleted) {
				// previous avatar not deleted
				Loggy.e(TAG, "upload", "previous avatar file or record NOT deleted for image id:" + previousAvatarId);
				throw new NullPointerException();
			}
			
			// previous avatar deleted
			DBCommander.updateUser(user);
			/**
			 * TODO: end SQL-transaction guard
			 * */
			return ok(user.toObjectNode(userId));

		} catch (Exception e) {
			Loggy.e(TAG, "upload", e);
		}
		return badRequest();

	}
}
