package controllers;

import exception.UserNotFoundException;
import models.Image;
import models.User;
import play.mvc.Http;
import play.mvc.Result;
import utilities.DataUtils;

public class AvatarController extends ImageController {

    public static final String TAG = AvatarController.class.getName();

    public static Result upload() {

        // define response attributes
        response().setContentType("text/plain");
        try {
            Http.RequestBody body = request().body();

            // get file data from request body stream
            Http.MultipartFormData data = body.asMultipartFormData();
            Http.MultipartFormData.FilePart avatarFile = data.getFile(User.AVATAR);

            // get user token from request body stream
            String token = DataUtils.getUserToken(data);
            Integer userId = DataUtils.getUserIdByToken(token);
            if (userId == null) throw new UserNotFoundException();
            User user = SQLCommander.queryUser(userId);
            if (user == null) throw new UserNotFoundException();

            if (avatarFile == null) throw new NullPointerException();
            int previousAvatarId = user.getAvatar();
            int newAvatarId = ExtraCommander.saveAvatarFile(avatarFile, user);
            if (newAvatarId == ExtraCommander.INVALID) throw new NullPointerException();

            // delete previous avatar record and file
            Image previousAvatar = SQLCommander.queryImage(previousAvatarId);
            boolean isPreviousAvatarDeleted = ExtraCommander.deleteImageRecordAndFile(previousAvatar);
            if (isPreviousAvatarDeleted) {
                System.out.println(TAG + ".upload, previous avatar file and record deleted.");
            }

            return ok("Avatar uploaded");

        } catch (Exception e) {
            DataUtils.log(TAG, "upload", e);
        }
        return badRequest("Avatar not uploaded!");

    }

}
