package controllers;

import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.UserNotFoundException;
import models.User;
import org.json.simple.JSONObject;
import play.mvc.Content;
import play.mvc.Result;
import utilities.DataUtils;
import utilities.General;
import views.html.email_verification;

import java.util.List;

public class EmailController extends UserController {

    public static final String TAG = EmailController.class.getName();

    public static Result duplicate(String email) {
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
            DataUtils.log(TAG, "duplicate", e);
        }
        return badRequest();
    }

    public static Result verify(String code) {
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
            DataUtils.log(TAG, "verify", e);
        }
        return badRequest();
    }

}
