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
import models.Player;
import models.PlayerActivityRelation;
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

public class PlayerController extends Controller {

    public static final String TAG = PlayerController.class.getName();

    protected static void sendVerificationEmail(final String lang, final String name, final String recipient, final String code) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        HashMap<String, String> targetMap = Constants.LANG_MAP.get(lang);
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(Constants.ADMIN_EMAIL, targetMap.get(Constants.HONGKONGRESORT_TEAM)));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient, name));
            msg.setSubject(targetMap.get(Constants.WELCOME));
            String link = "http://" + request().host() + "/player/email/verify?email=" + recipient + "&code=" + code;
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
            String email = formData.get(Player.EMAIL)[0];
            String password = formData.get(Player.PASSWORD)[0];

            if ((email == null || !General.validateEmail(email)) || (password == null || !General.validatePassword(password)))
                throw new InvalidLoginParamsException();

            Player player = DBCommander.queryPlayerByEmail(email);
            if (player == null) throw new PlayerNotFoundException();

            String passwordDigest = Converter.md5(password + player.getSalt());
            if (!player.getPassword().equals(passwordDigest)) throw new PswErrException();

            String token = Converter.generateToken(email, password);
            Long playerId = player.getId();

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] cols = {Login.PLAYER_ID, Login.TOKEN, Login.TIMESTAMP};
            Object[] vals = {playerId, token, General.millisec()};
            builder.insert(cols, vals).into(Login.TABLE).execInsert();
            ObjectNode result = player.toObjectNode(playerId);
            result.put(Player.TOKEN, token);
            return ok(result);
        } catch (PlayerNotFoundException e) {
            return ok(StandardFailureResult.get(Constants.INFO_PLAYER_NOT_FOUND));
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
            String name = formData.get(Player.NAME)[0];
            String email = formData.get(Player.EMAIL)[0];
            String password = formData.get(Player.PASSWORD)[0];

            String sid = formData.get(PlayerActivityRelation.SID)[0];
            String captcha = formData.get(PlayerActivityRelation.CAPTCHA)[0];
            if (sid == null || captcha == null) throw new CaptchaNotMatchedException();
            if (session(sid) == null || !captcha.equalsIgnoreCase(session(sid))) throw new CaptchaNotMatchedException();

            if ((name == null || !General.validateName(name)) || (email == null || !General.validateEmail(email)) || (password == null || !General.validatePassword(password)))
                throw new InvalidRegistrationParamsException();
            String code = DBCommander.generateVerificationCode(name);
            String salt = DBCommander.generateSalt(email, password);
            String passwordDigest = Converter.md5(password + salt);
            Player player = new Player(email, name);
            player.setPassword(passwordDigest);

            player.setVerificationCode(code);
            player.setSalt(salt);
            if (DBCommander.registerPlayer(player) == SQLHelper.INVALID) throw new NullPointerException();
            sendVerificationEmail(player.getLang(), player.getName(), player.getEmail(), code);
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
            Long playerId = DBCommander.queryPlayerId(token);
            if (playerId == null) throw new PlayerNotFoundException();
            Player player = DBCommander.queryPlayer(playerId);
            if (player == null) throw new PlayerNotFoundException();
            return ok(player.toObjectNode(playerId));
        } catch (Exception e) {
            if (e instanceof PlayerNotFoundException) return ok(StandardFailureResult.get());
            Loggy.e(TAG, "status", e);
        }
        return ok(StandardFailureResult.get());
    }

    public static Result relation(Long activityId, String token) {
        try {
            Long playerId = DBCommander.queryPlayerId(token);
            int relation = DBCommander.queryPlayerActivityRelation(playerId, activityId);
            if (relation == PlayerActivityRelation.INVALID) throw new InvalidPlayerActivityRelationException();
            ObjectNode ret = Json.newObject();
            ret.put(PlayerActivityRelation.RELATION, String.valueOf(relation));
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
            String token = formData.get(Player.TOKEN)[0];
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
            List<JSONObject> playerJsons = builder.select(Player.ID).from(Player.TABLE).where(Player.NAME, "=", name).execSelect();
            if (playerJsons != null && playerJsons.size() > 0) throw new DuplicateException();
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
            if (token != null) viewerId = DBCommander.queryPlayerId(token);
            Player viewee = DBCommander.queryPlayer(vieweeId);
            if (viewee == null) throw new PlayerNotFoundException();
            return ok(viewee.toObjectNode(viewerId));
        } catch (TokenExpiredException | PlayerNotFoundException e) {
            Loggy.e(TAG, "detail", e);
        }
        return badRequest();
    }

    public static Result save() {
        try {
            Http.RequestBody body = request().body();

            // get file data from request body stream
            Http.MultipartFormData data = body.asMultipartFormData();
            Http.MultipartFormData.FilePart avatarFile = data.getFile(Player.AVATAR);
            if (avatarFile != null && !DataUtils.validateImage(avatarFile)) throw new InvalidRequestParamsException();

            // get player token from request body stream
            String token = DataUtils.getPlayerToken(data);
            Long playerId = DBCommander.queryPlayerId(token);
            if (playerId == null) throw new PlayerNotFoundException();
            Player player = DBCommander.queryPlayer(playerId);
            if (player == null) throw new PlayerNotFoundException();

            Map<String, String[]> formData = data.asFormUrlEncoded();

            String age = formData.get(Player.AGE)[0];
            String gender = formData.get(Player.GENDER)[0];
            String mood = formData.get(Player.MOOD)[0];

            if (!General.validatePlayerAge(age) || !General.validatePlayerGender(gender) || !General.validatePlayerMood(mood))
                throw new InvalidRequestParamsException();

            player.setAge(age);
            player.setGender(gender);
            player.setMood(mood);

            if (avatarFile == null) {
                DBCommander.updatePlayer(player);
                return ok(player.toObjectNode(playerId));
            }


            /**
             * TODO: begin SQL-transaction guard (resembling ActivityController.save)
             * */
            long previousAvatarId = player.getAvatar();
            long newAvatarId = ExtraCommander.saveAvatar(avatarFile, player);
            if (newAvatarId == SQLHelper.INVALID) throw new NullPointerException();

            player.setAvatar(newAvatarId);

            // delete previous avatar record and file
            Image previousAvatar = ExtraCommander.queryImage(previousAvatarId);
            if (previousAvatar == null) {
                // no previous avatar
                DBCommander.updatePlayer(player);
                return ok(player.toObjectNode(playerId));
            }

            boolean isPreviousAvatarDeleted = ExtraCommander.deleteImageRecordAndFile(previousAvatar);
            if (!isPreviousAvatarDeleted) {
                // previous avatar not deleted
                Loggy.e(TAG, "upload", "previous avatar file or record NOT deleted for image id:" + previousAvatarId);
                throw new NullPointerException();
            }

            // previous avatar deleted
            DBCommander.updatePlayer(player);
            /**
             * TODO: end SQL-transaction guard
             * */
            return ok(player.toObjectNode(playerId));

        } catch (Exception e) {
            Loggy.e(TAG, "upload", e);
        }
        return badRequest();

    }
}