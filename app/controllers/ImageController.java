package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qiniu.util.Auth;
import components.StandardFailureResult;
import components.StandardSuccessResult;
import components.TokenExpiredResult;
import exception.PlayerNotFoundException;
import exception.TokenExpiredException;
import models.Player;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.CDNHelper;
import utilities.Converter;
import utilities.Loggy;

import java.util.Map;

public class ImageController extends Controller {
        public static final String TAG = ImageController.class.getName();

        public static Result qiniuUptoken(String token, String remoteName, String maxSize) {
                try {
                        final int maxSizeInt = Converter.toInteger(maxSize);
                        final Long playerId = DBCommander.queryPlayerId(token);
                        if (playerId == null) throw new PlayerNotFoundException();

                        final Map<String, String> attrs = CDNHelper.getAttr(CDNHelper.QINIU);
                        if (attrs == null) throw new NullPointerException();

                        final Auth auth = Auth.create(attrs.get(CDNHelper.APP_ID), attrs.get(CDNHelper.APP_KEY));

                        // TODO: add maxSizeInt constraint to token
                        final String bucket = attrs.get(CDNHelper.BUCKET);
                        final String uploadToken = auth.uploadToken(attrs.get(CDNHelper.BUCKET), remoteName);

                        // TODO: add <token, remote_name> mapping in database for delete-validation

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

                        final String remoteName = formData.get(CDNHelper.REMOTE_NAME)[0];
                        if (remoteName == null) throw new NullPointerException();

                        final Long playerId = DBCommander.queryPlayerId(token);
                        if (playerId == null) throw new PlayerNotFoundException();

                        // TODO: validate owner of <token, remote_name>

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
