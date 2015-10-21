package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import components.CaptchaNotMatchedResult;
import components.StandardFailureResult;
import components.StandardSuccessResult;
import dao.SQLBuilder;
import dao.SQLHelper;
import dao.SimpleMap;
import exception.*;
import fixtures.Constants;
import models.Image;
import models.Login;
import models.Player;
import models.PlayerActivityRelation;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.*;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class PlayerController extends Controller {

        public static final String TAG = PlayerController.class.getName();

        protected static void sendVerificationEmail(final String lang, final String name, final String recipient, final String code) {
                final Properties props = new Properties();
                final Session session = Session.getDefaultInstance(props, null);
                final HashMap<String, String> targetMap = Constants.LANG_MAP.get(lang);
                try {
                        final Message msg = new MimeMessage(session);
                        msg.setFrom(new InternetAddress(Constants.ADMIN_EMAIL, targetMap.get(Constants.HONGKONGRESORT_TEAM)));
                        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient, name));
                        msg.setSubject(targetMap.get(Constants.WELCOME));

                        final String protocolPrefix = "http://";
                        final String host = request().host();
                        final String path = "/player/email/verify";

                        final Map<String, Object> params = new HashMap<>();
                        params.put(Player.EMAIL, recipient);
                        params.put("code", code);
                        String req = protocolPrefix + host + path + "?" + DataUtils.toUrlParams(params);
                        String text = String.format(targetMap.get(Constants.VERIFY_INSTRUCTION), name, req);
                        msg.setText(text);
                        Transport.send(msg);
                } catch (Exception e) {
                        Loggy.e(TAG, "sendVerificationEmail", e);
                }
        }

        private static ObjectNode login(final String email, final String password) throws InvalidLoginParamsException, PlayerNotFoundException, PswErrException {
                if ((email == null || !General.validateEmail(email)) || (password == null || !General.validatePassword(password)))
                        throw new InvalidLoginParamsException();

                final Player player = DBCommander.queryPlayerByEmail(email);
                if (player == null) throw new PlayerNotFoundException();

                final String passwordDigest = Converter.md5(password + player.getSalt());
                if (!player.getPassword().equals(passwordDigest)) throw new PswErrException();

                final String token = Converter.generateToken(email, password);

                final SQLBuilder builder = new SQLBuilder();
                final String[] cols = {Login.PLAYER_ID, Login.TOKEN, Login.TIMESTAMP};
                final Object[] vals = {player.getId(), token, General.millisec()};
                builder.insert(cols, vals).into(Login.TABLE).execInsert();
                final ObjectNode result = player.toObjectNode(player.getId());
                result.put(Player.TOKEN, token);
                return result;
        }

        public static ObjectNode register(final String name, final String email, final String password, final String sid, final String captcha) throws InvalidRegistrationParamsException, CaptchaNotMatchedException {
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
                return StandardSuccessResult.get();
        }

        public static ObjectNode status(final String token) throws TokenExpiredException, PlayerNotFoundException {
                if (token == null) throw new NullPointerException();
                final Long playerId = DBCommander.queryPlayerId(token);

                if (playerId == null) throw new PlayerNotFoundException();
                final Player player = DBCommander.queryPlayer(playerId);

                if (player == null) throw new PlayerNotFoundException();

		// echo token
		final ObjectNode ret = player.toObjectNode(playerId);
		ret.put(Player.TOKEN, token);
                return ret;
        }

        public static ObjectNode logout(final String token) {
                final SQLBuilder builder = new SQLBuilder();
                builder.from(Login.TABLE).where(Login.TOKEN, "=", token);
                if (!builder.execDelete()) throw new NullPointerException();
                return StandardSuccessResult.get();
        }

        public static Result duplicate(String name) {
                try {
                        if (name == null) throw new NullPointerException();
                        SQLBuilder builder = new SQLBuilder();
                        final List<SimpleMap> data = builder.select(Player.ID).from(Player.TABLE).where(Player.NAME, "=", name).execSelect();
                        if (data != null && data.size() > 0) throw new DuplicateException();
                        return ok(StandardSuccessResult.get());
                } catch (DuplicateException e) {
                        ok(StandardFailureResult.get());
                } catch (Exception e) {
                        Loggy.e(TAG, "duplicate", e);
                }
                return ok(StandardFailureResult.get());
        }

        public static ObjectNode detail(final Long vieweeId, final String token) throws TokenExpiredException, PlayerNotFoundException {
                final Long viewerId = (token == null ? null : DBCommander.queryPlayerId(token));
                final Player viewee = DBCommander.queryPlayer(vieweeId);
                if (viewee == null) throw new PlayerNotFoundException();
                return viewee.toObjectNode(viewerId);
        }

        public static ObjectNode save(final String token, final String age, final String gender, final String mood, final String avatarRemoteName) throws SQLException, TokenExpiredException, InvalidRequestParamsException, PlayerNotFoundException {
                final Long playerId = DBCommander.queryPlayerId(token);
                if (playerId == null) throw new PlayerNotFoundException();

                final Player player = DBCommander.queryPlayer(playerId);
                if (player == null) throw new PlayerNotFoundException();

                if (!General.validatePlayerAge(age) || !General.validatePlayerGender(gender) || !General.validatePlayerMood(mood))
                        throw new InvalidRequestParamsException();

                player.setAge(age);
                player.setGender(gender);
                player.setMood(mood);

                if (avatarRemoteName == null) {
                        DBCommander.updatePlayer(player);
                        return player.toObjectNode(playerId);
                }

                long previousAvatarId = player.getAvatar();
                // TODO: combine to transaction block
                final List<Image> toDeleteImageList = new LinkedList<>();
                if (previousAvatarId > 0) {
                        final Image previousAvatar = ExtraCommander.queryImage(previousAvatarId);
                        toDeleteImageList.add(previousAvatar);
                }

                // TODO: combine into transaction block
                final Image avatar = ExtraCommander.queryImage(playerId, Image.TYPE_OWNER, avatarRemoteName);
                if (avatar == null) throw new NullPointerException();

                final Connection connection = SQLHelper.getConnection();
                boolean transactionSucceeded = true;

                /**
                 * begin SQL-transaction guard
                 * */
                try {
                        SQLHelper.disableAutoCommit(connection);

                        final String urlProtocolPrefix = "http://";
                        final String url = urlProtocolPrefix + CDNHelper.getDomain(avatar.getCDNId(), avatar.getBucket()) + "/" + avatarRemoteName;

                        String[] cols = {Image.URL, Image.META_ID, Image.META_TYPE};
                        Object[] vals = {url, player.getId(), Image.TYPE_PLAYER};

                        final SQLBuilder builderUpdate = new SQLBuilder();
                        final PreparedStatement statUpdate = builderUpdate.update(Image.TABLE)
                                                                        .set(cols, vals)
                                                                        .where(Image.META_ID, "=", playerId)
                                                                        .where(Image.META_TYPE, "=", Image.TYPE_OWNER)
                                                                        .where(Image.REMOTE_NAME, "=", avatarRemoteName)
                                                                        .toUpdate(connection);
                        SQLHelper.executeAndCloseStatement(statUpdate);

                        // delete previous avatar
                        final SQLBuilder builderDeletion = new SQLBuilder();
                        final PreparedStatement statDeletion = builderDeletion.from(Image.TABLE)
                                                                        .where(Image.ID, "=", previousAvatarId)
                                                                        .where(Image.META_ID, "=", playerId)
                                                                        .where(Image.META_TYPE, "=", Image.TYPE_PLAYER)
                                                                        .toDelete(connection);
                        SQLHelper.executeAndCloseStatement(statDeletion);

                        SQLHelper.commit(connection);

                } catch (Exception e) {
                        Loggy.e(TAG, "save", e);
                        transactionSucceeded = false;
                        SQLHelper.rollback(connection);
                } finally {
                        SQLHelper.enableAutoCommitAndClose(connection);
                }
                /**
                 * end SQL-transaction guard
                 * */

                // TODO: combine into transaction block
                try {
                        if (!transactionSucceeded) throw new NullPointerException();
                        player.setAvatar(avatar.getId());
                        if (toDeleteImageList.size() > 0)	CDNHelper.deleteRemoteImages(CDNHelper.QINIU, toDeleteImageList);
                } catch (Exception e) {
			Loggy.e(TAG, "save", e);
                }
                DBCommander.updatePlayer(player);
                return player.toObjectNode(playerId);
        }

        public static Result misc(String act) {
                try {
                        final Map<String, String[]> formData = request().body().asFormUrlEncoded();
                        switch (act) {
                                case "login": {
                                        final String email = formData.get(Player.EMAIL)[0];
                                        final String password = formData.get(Player.PASSWORD)[0];
                                        return ok(login(email, password));
                                }
                                case "status": {
                                        final String token = formData.get(Player.TOKEN)[0];
                                        return ok(status(token));
                                }
                                case "logout": {
                                        final String token = formData.get(Player.TOKEN)[0];
                                        return ok(logout(token));
                                }
                                case "register": {
                                        final String name = formData.get(Player.NAME)[0];
                                        final String email = formData.get(Player.EMAIL)[0];
                                        final String password = formData.get(Player.PASSWORD)[0];

                                        final String sid = formData.get(PlayerActivityRelation.SID)[0];
                                        final String captcha = formData.get(PlayerActivityRelation.CAPTCHA)[0];
                                        return ok(register(name, email, password, sid, captcha));
                                }
                                case "detail": {
                                        final Long vieweeId = Converter.toLong(formData.get(PlayerActivityRelation.VIEWEE_ID)[0]);
                                        final String token = (formData.containsKey(Player.TOKEN) ? formData.get(Player.TOKEN)[0] : null);
                                        return ok(detail(vieweeId, token));
                                }
                                case "save": {
                                        final String token = formData.get(Player.TOKEN)[0];
                                        final String age = formData.get(Player.AGE)[0];
                                        final String gender = formData.get(Player.GENDER)[0];
                                        final String mood = formData.get(Player.MOOD)[0];
                                        final String avatarRemoteName = (formData.containsKey(Player.AVATAR) ? formData.get(Player.AVATAR)[0] :  null);
                                        return ok(save(token, age, gender, mood, avatarRemoteName));
                                }
                                default:
                                        return badRequest();
                        }
                } catch (CaptchaNotMatchedException e) {
                        return ok(CaptchaNotMatchedResult.get());
                } catch (PlayerNotFoundException e) {
                        return ok(StandardFailureResult.get(Constants.INFO_PLAYER_NOT_FOUND));
                } catch (PswErrException e) {
                        return ok(StandardFailureResult.get(Constants.INFO_PSW_ERR));
                } catch (Exception e) {
                        Loggy.e(TAG, "misc", e);
                }
                return ok(StandardFailureResult.get());
        }
}
