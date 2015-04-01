package controllers;

import dao.EasyPreparedStatementBuilder;
import exception.InvalidPasswordException;
import exception.UserNotFoundException;
import fixtures.Constants;
import models.User;
import play.mvc.Content;
import play.mvc.Result;
import utilities.Converter;
import utilities.General;
import utilities.Loggy;
import views.html.password_index;
import views.html.password_reset;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PasswordController extends UserController {

    public static final String TAG = PasswordController.class.getName();

    public static Result index() {
        try {
            Content html = password_index.render();
            return ok(html);
        } catch (Exception e) {
            Loggy.e(TAG, "index", e);
        }
        return badRequest();
    }

    public static Result request(String email) {
        try {
            User user = DBCommander.queryUserByEmail(email);
            if(user == null) throw new UserNotFoundException();
            String code = DBCommander.generateVerificationCode(email);
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.update(User.TABLE).set(User.PASSWORD_RESET_CODE, code).where(User.EMAIL, "=", email);
            if(!builder.execUpdate()) throw new NullPointerException();
            sendResetEmail(user.getLang(), user.getName(), user.getEmail(), code);
            return ok();
        } catch (Exception e) {
            Loggy.e(TAG, "request", e);
        }
        return badRequest();
    }

    protected static void sendResetEmail(final String lang, final String name, final String recipient, final String code) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        HashMap<String, String> targetMap = Constants.LANG_MAP.get(lang);
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(Constants.ADMIN_EMAIL, targetMap.get(Constants.HONGKONGRESORT_TEAM)));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient, name));
            msg.setSubject(targetMap.get(Constants.RESET_PASSWORD_TITLE));
            String link = "http://" + request().host() + "/user/password/reset#default?email=" + recipient + "&code=" + code;
            String text = String.format(targetMap.get(Constants.RESET_PASSWORD_INSTRUCTION), name, link);
            msg.setText(text);
            Transport.send(msg);
        } catch (Exception e) {
            Loggy.e(TAG, "sendResetEmail", e);
        }
    }

    public static Result reset() {
        try {
            Content html = password_reset.render();
            return ok(html);
        } catch (Exception e) {
            Loggy.e(TAG, "reset", e);
        }
        return badRequest();
    }

    public static Result confirm() {
        try {
            Map<String, String[]> formData = request().body().asFormUrlEncoded();
            String email = formData.get(User.EMAIL)[0];
            String code = formData.get(User.PASSWORD_RESET_CODE)[0];
            String password = formData.get(User.PASSWORD)[0];

            if(!General.validatePassword(password)) throw new InvalidPasswordException();

            User user = DBCommander.queryUserByEmail(email);
            if (user == null) throw new UserNotFoundException();

            String passwordDigest = Converter.md5(password + user.getSalt());
	    Loggy.d(TAG, "confirm", password + ", " + passwordDigest);

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.update(User.TABLE)
                    .set(User.PASSWORD, passwordDigest)
                    .set(User.PASSWORD_RESET_CODE, "")
                    .where(User.EMAIL, "=", email)
                    .where(User.PASSWORD_RESET_CODE, "=", code);

            if (!builder.execUpdate()) throw new NullPointerException();
            return ok();
        } catch (Exception e) {
            Loggy.e(TAG, "confirm", e);
            return badRequest();
        }
    }

}
