package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qiniu.util.Auth;
import components.StandardFailureResult;
import components.TokenExpiredResult;
import exception.PlayerNotFoundException;
import exception.TokenExpiredException;
import models.Player;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.CDNHelper;
import utilities.Loggy;

import java.util.Map;

public class ImageController extends Controller {
        public static final String TAG = ImageController.class.getName();

        public static Result qiniuUptoken() {
                try {
                        final Http.RequestBody body = request().body();
                        final Map<String, String[]> formData = body.asFormUrlEncoded();

                        final String token = formData.get(Player.TOKEN)[0];
                        if (token == null) throw new NullPointerException();

                        final Long playerId = DBCommander.queryPlayerId(token);
                        if (playerId == null) throw new PlayerNotFoundException();

                        final Map<String, String> attrs = CDNHelper.getAttr(CDNHelper.QINIU);
                        if (attrs == null) throw new NullPointerException();

                        final Auth auth = Auth.create(attrs.get(CDNHelper.APP_ID), attrs.get(CDNHelper.APP_KEY));
                        final String uploadToken = auth.uploadToken(attrs.get(CDNHelper.BUCKET));

                        final ObjectNode ret = Json.newObject();
                        ret.put(CDNHelper.UPTOKEN, uploadToken);

                        return ok(ret);

                } catch (PlayerNotFoundException e) {
                        Loggy.e(TAG, "qiniuUploadToken", e);
                } catch (TokenExpiredException e) {
                        return ok(TokenExpiredResult.get());
                } catch (Exception e) {
                        Loggy.e(TAG, "qiniuUploadToken", e);
                }
                return ok(StandardFailureResult.get());
        }
}
