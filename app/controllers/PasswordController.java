package controllers;

import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.UserNotFoundException;
import models.User;
import play.mvc.Content;
import play.mvc.Result;
import utilities.Converter;
import utilities.DataUtils;
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
            DataUtils.log(TAG, "index", e);
        }
        return badRequest();
    }

    public static Result request(String email) {
        response().setContentType("text/plain");
        try {
            User user = SQLCommander.queryUserByEmail(email);
            if(user == null) throw new UserNotFoundException();
            String code = generateVerificationCode(user);
  	    EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
	    builder.update(User.TABLE).set(User.PASSWORD_RESET_CODE, code).where(User.EMAIL, "=", email);
	    if(!SQLHelper.update(builder)) throw new NullPointerException();
            sendResetEmail(user.getName(), user.getEmail(), code);
	    return ok();
        } catch (Exception e) {
            DataUtils.log(TAG, "emailRequest", e);
        }
        return badRequest();
    }

    protected static void sendResetEmail(String name, String recipient, String code) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("hongkongresort@126.com", "The HongKongResort Team"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient, name));
            msg.setSubject("Welcome to HongKongResort");
            String link = "http://128.199.168.153/user/password/reset?email=" + recipient + "&code=" + code;
            msg.setText("Dear " + name + ", you can now click the following link to reset your password: " + link);
            Transport.send(msg);
        } catch (Exception e) {
            DataUtils.log(TAG, "sendResetEmail", e);
        }
    }

    protected static Result reset() {
        response().setContentType("text/html");
        try {
            Content html = password_reset.render();
            return ok(html);
        } catch (Exception e) {
            DataUtils.log(TAG, "reset", e);
        }
        return badRequest();
    }

    protected static Result confirm() {
        try {
            Map<String, String[]> formData = request().body().asFormUrlEncoded();
            String email = formData.get(User.EMAIL)[0];
            String code = formData.get(User.PASSWORD_RESET_CODE)[0];
            String password = formData.get(User.PASSWORD)[0];
            String passwordDigest = Converter.md5(password);

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.update(User.TABLE)
                    .set(User.PASSWORD, passwordDigest)
                    .set(User.PASSWORD_RESET_CODE, "")
                    .where(User.EMAIL, "=", email)
                    .where(User.PASSWORD_RESET_CODE, "=", code);

            if (!SQLHelper.update(builder)) throw new NullPointerException();
            return ok();
        } catch (Exception e) {
            DataUtils.log(TAG, "confirm", e);
        }
        return badRequest();
    }

    protected static String generateResetCode(User user) {
	return DataUtils.encryptByTime(user.getEmail());
    }
}
