package controllers;

import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.UserNotFoundException;
import exception.InvalidPasswordException;
import models.User;
import play.mvc.Content;
import play.mvc.Result;
import play.mvc.Http.Request;
import utilities.Converter;
import utilities.DataUtils;
import utilities.General;
import utilities.Logger;
import views.html.password_index;
import views.html.password_reset;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

public class PasswordController extends UserController {

    public static final String TAG = PasswordController.class.getName();

    public static Result index() {
        response().setContentType("text/html");
        try {
            Content html = password_index.render("hongkongresort@126.com");
            return ok(html);
        } catch (Exception e) {
            Logger.e(TAG, "index", e);
        }
        return badRequest();
    }

    public static Result request(String email) {
        response().setContentType("text/plain");
        try {
            User user = SQLCommander.queryUserByEmail(email);
            if(user == null) throw new UserNotFoundException();
            String code = SQLCommander.generateVerificationCode(email);
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.update(User.TABLE).set(User.PASSWORD_RESET_CODE, code).where(User.EMAIL, "=", email);
            if(!builder.execUpdate()) throw new NullPointerException();
            sendResetEmail(user.getName(), user.getEmail(), code);
            return ok();
        } catch (Exception e) {
            Logger.e(TAG, "request", e);
        }
        return badRequest();
    }

    protected static void sendResetEmail(String name, String recipient, String code) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("admin@hongkongresort.com", "The HongKongResort Team"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient, name));
            msg.setSubject("HongKongResort");
            String link = "http://" + request().host() + "/user/password/reset?email=" + recipient + "&code=" + code;
            msg.setText("Dear " + name + ", you can now click the following link to reset your password: " + link);
            Transport.send(msg);
        } catch (Exception e) {
            Logger.e(TAG, "sendResetEmail", e);
        }
    }

    public static Result reset() {
        response().setContentType("text/html");
        try {
            Content html = password_reset.render();
            return ok(html);
        } catch (Exception e) {
            Logger.e(TAG, "reset", e);
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

            User user = SQLCommander.queryUserByEmail(email);
            if (user == null) throw new UserNotFoundException();

            String passwordDigest = Converter.md5(password + user.getSalt());

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.update(User.TABLE)
                    .set(User.PASSWORD, passwordDigest)
                    .set(User.PASSWORD_RESET_CODE, "")
                    .where(User.EMAIL, "=", email)
                    .where(User.PASSWORD_RESET_CODE, "=", code);

            if (!builder.execUpdate()) throw new NullPointerException();
            return ok();
        } catch (Exception e) {
            Logger.e(TAG, "confirm", e);
            return badRequest();
        }
    }

}
