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
            List<JSONObject> userJsons = builder.select(User.ID).from(User.TABLE).where(User.EMAIL, "=", email).execSelect();
            if (userJsons != null && userJsons.size() > 0) throw new UserNotFoundException();
            return ok();
        } catch (Exception e) {
            DataUtils.log(TAG, "duplicate", e);
        }
        return badRequest();
    }

    public static Result verify(String email, String code) {
        response().setContentType("text/html");
        try {
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
            Content html = email_verification.render(res, user.getName(), user.getEmail());
            return ok(html);
        } catch (Exception e) {
            DataUtils.log(TAG, "verify", e);
        }
        return badRequest();
    }

}
