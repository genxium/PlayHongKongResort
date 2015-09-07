package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import components.StandardFailureResult;
import components.StandardSuccessResult;
import components.TokenExpiredResult;
import dao.SQLBuilder;
import dao.SQLHelper;
import dao.SimpleMap;
import exception.PlayerNotFoundException;
import exception.TokenExpiredException;
import models.AbstractMessage;
import models.Image;
import models.Player;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.CDNHelper;
import utilities.General;
import utilities.Loggy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class ImageController extends Controller {
        public static final String TAG = ImageController.class.getName();

        public static Result qiniuUptoken(String token, String remoteName) {
                try {
                        Matcher matcher = Image.REMOTE_NAME_PATTERN.matcher(remoteName);
                        if (!matcher.matches()) throw new NullPointerException();

                        final Long playerId = DBCommander.queryPlayerId(token);
                        if (playerId == null) throw new PlayerNotFoundException();

                        final Map<String, String> attrs = CDNHelper.getAttr(CDNHelper.QINIU);
                        if (attrs == null) throw new NullPointerException();

                        final String bucket = attrs.get(CDNHelper.BUCKET);
                        final int expires = 3600; // in seconds
                        final Map<String, Object> constraints = new HashMap<>();
                        // constraints reference http://developer.qiniu.com/docs/v6/api/reference/security/put-policy.html
                        constraints.put("fsizeLimit", Image.SINGLE_FILE_SIZE_LIMIT); // in bytes
                        constraints.put("mimeLimit", "image/jpeg;image/png");
                        final Auth auth = (Auth) CDNHelper.getAuth(CDNHelper.QINIU);
                        if (auth == null) throw new NullPointerException();
                        final String uploadToken = auth.uploadToken(bucket, remoteName, expires, new StringMap().putAll(constraints));

                        // Note that remote_name owns player_id and GMT timestamp info by design
                        final SQLBuilder builder = new SQLBuilder();
                        final String[] cols = {Image.META_ID, Image.META_TYPE, Image.REMOTE_NAME, Image.BUCKET, Image.CDN_ID, Image.GENERATED_TIME};
                        final Object[] vals = {playerId, Image.TYPE_OWNER, remoteName, bucket, CDNHelper.QINIU, General.millisec()};
                        builder.insert(cols, vals).into(Image.TABLE).execInsert();

                        final ObjectNode ret = Json.newObject();
                        ret.put(CDNHelper.UPTOKEN, uploadToken);

                        return ok(ret);

                } catch (PlayerNotFoundException e) {
                        Loggy.e(TAG, "qiniuUptoken", e);
                } catch (TokenExpiredException e) {
                        return ok(TokenExpiredResult.get());
                } catch (Exception e) {
                        Loggy.e(TAG, "qiniuUptoken", e);
                }
                return ok(StandardFailureResult.get());
        }

        public static Result qiniuDelete() {
                try {
                        final Http.RequestBody body = request().body();
                        final Map<String, String[]> formData = body.asFormUrlEncoded();

                        final String token = formData.get(Player.TOKEN)[0];
                        if (token == null) throw new NullPointerException();

                        final ObjectMapper mapper = new ObjectMapper();
                        final List<String> remoteNameList = mapper.readValue(formData.get(AbstractMessage.BUNDLE)[0], mapper.getTypeFactory().constructCollectionType(List.class, String.class));

                        final Map<String, String> attrs = CDNHelper.getAttr(CDNHelper.QINIU);
                        if (attrs == null) throw new NullPointerException();

                        final Long playerId = DBCommander.queryPlayerId(token);
                        if (playerId == null) throw new PlayerNotFoundException();

                        // batch deletion
                        final SQLBuilder builderImages = new SQLBuilder();
                        final List<SimpleMap> resList = builderImages.select(Image.QUERY_FIELDS)
                                                                .from(Image.TABLE)
                                                                .where(Image.META_ID, "=", playerId)
                                                                .where(Image.META_TYPE, "=", Image.TYPE_OWNER)
                                                                .where(Image.CDN_ID, "=", CDNHelper.QINIU)
                                                                .where(Image.REMOTE_NAME, "IN", remoteNameList)
                                                                .execSelect();

                        if (resList.size() != remoteNameList.size()) throw new NullPointerException();

                        final List<Image> toDeleteImageList = new LinkedList<>();
                        for (SimpleMap res : resList) toDeleteImageList.add(new Image(res));

                        final Connection connection = SQLHelper.getConnection();
                        boolean transactionSucceeded = true;

                        try {
                                SQLHelper.disableAutoCommit(connection);
                                final SQLBuilder builderDeletion = new SQLBuilder();
                                final PreparedStatement statDeletion = builderDeletion.from(Image.TABLE)
                                                                .where(Image.META_ID, "=", playerId)
                                                                .where(Image.META_TYPE, "=", Image.TYPE_OWNER)
                                                                .where(Image.CDN_ID, "=", CDNHelper.QINIU)
                                                                .where(Image.REMOTE_NAME, "IN", remoteNameList)
                                                                .toDelete(connection);

                                SQLHelper.executeAndCloseStatement(statDeletion);
                                SQLHelper.commit(connection);
                        } catch (Exception e) {
                                Loggy.e(TAG, "qiniuDelete", e);
                                transactionSucceeded = false;
                                SQLHelper.rollback(connection);
                        } finally {
                                SQLHelper.enableAutoCommitAndClose(connection);
                        }

                        if (transactionSucceeded) CDNHelper.deleteRemoteImages(CDNHelper.QINIU, toDeleteImageList);

                        return ok(StandardSuccessResult.get());

                } catch (PlayerNotFoundException e) {
                        Loggy.e(TAG, "qiniuDelete", e);
                } catch (TokenExpiredException e) {
                        return ok(TokenExpiredResult.get());
                } catch (Exception e) {
                        Loggy.e(TAG, "qiniuDelete", e);
                }
                return ok(StandardFailureResult.get());
        }
}
