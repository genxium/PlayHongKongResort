package controllers;

import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import models.*;
import exception.*;
import org.apache.commons.io.FileUtils;
import play.mvc.Http.MultipartFormData.FilePart;
import utilities.DataUtils;
import utilities.Logger;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class ExtraCommander extends SQLCommander {

    public static final String TAG = ExtraCommander.class.getName();

	public static boolean deleteActivity(int activityId) {
		boolean ret = false;
		try {
			// delete records in tables activity_image_relation and image,
			// as well as corresponding image files
			EasyPreparedStatementBuilder builderRelation = new EasyPreparedStatementBuilder();
			builderRelation.from(UserActivityRelation.TABLE).where(UserActivityRelation.ACTIVITY_ID, "=", activityId);
			if(!builderRelation.execDelete()) throw new NullPointerException();

			List<Image> previousImages = queryImages(activityId);
			if (previousImages != null && previousImages.size() > 0) {
				// delete previous images
                for (Image previousImage : previousImages)  deleteImageRecordAndFile(previousImage, activityId);
			}

            deleteComments(activityId);
            deleteAssessments(activityId);

			// delete record in table activity
			EasyPreparedStatementBuilder builderActivity = new EasyPreparedStatementBuilder();
            ret = builderActivity.from(Activity.TABLE).where(Activity.ID, "=", activityId).execDelete();

		} catch (Exception e) {
			Logger.e(TAG, "deleteActivity", e);
		}

		return ret;
	}

	public static boolean deleteImageRecordAndFile(Image image) {
		boolean ret = false;
		try {
			if (image == null) throw new ImageNotFoundException();
			int imageId = image.getImageId();
			String imageAbsolutePath = image.getAbsolutePath();
			File imageFile = new File(imageAbsolutePath);
			boolean isFileDeleted = imageFile.delete();
			boolean isRecordDeleted = deleteImageRecord(imageId);
			if (!isFileDeleted || !isRecordDeleted) throw new NullPointerException();
			ret = true;
		} catch (Exception e) {
			Logger.e(TAG, "deleteImageRecordAndFile", e);
		}
		return ret;
	}

	public static boolean deleteImageRecordAndFile(Image image, int activityId) {
		boolean ret = false;
		try {
			if (image == null) throw new ImageNotFoundException();
			int imageId = image.getImageId();
			String imageAbsolutePath = image.getAbsolutePath();
			File imageFile = new File(imageAbsolutePath);
			boolean isFileDeleted = imageFile.delete();
			boolean isRecordDeleted = deleteImageRecord(imageId, activityId);
			if (!isFileDeleted || !isRecordDeleted) throw new NullPointerException();
			ret = true;
		} catch (Exception e) {
			Logger.e(TAG, "deleteImageRecordAndFile", e);
		}
		return ret;
	}

	public static int saveAvatar(FilePart imageFile, User user) {
		int ret = INVALID;
		String fileName = imageFile.getFilename();
		File file = imageFile.getFile();
		try {
			if (!DataUtils.validateImage(imageFile)) throw new NullPointerException();
			if (user == null) throw new UserNotFoundException();

			Integer userId = user.getId();
			String newImageName = DataUtils.generateUploadedImageName(fileName, userId);
			String imageURL = Image.getUrlPrefix() + newImageName;

			String imageAbsolutePath = Image.getFolderPath() + newImageName;

			int imageId = SQLCommander.uploadAvatar(user, imageURL);
			if (imageId == INVALID) throw new NullPointerException();

			try {
				// Save renamed file to server storage at the final step
				FileUtils.moveFile(file, new File(imageAbsolutePath));
			} catch (Exception err) {
				imageId = INVALID;
				System.out.println(ExtraCommander.class.getName() + ".saveAvatar, " + newImageName + " could not be saved.");
				if(SQLCommander.deleteImageRecord(imageId))	System.out.println(ExtraCommander.class.getName() + ".saveAvatar, " + newImageName + " has been reverted");
			}
			ret = imageId;
		} catch (Exception e) {
			Logger.e(TAG, "saveAvatar", e);
		}

		return ret;
	}

	public static int saveImageOfActivity(FilePart imageFile, User user, Activity activity) {
		int ret = INVALID;
		String fileName = imageFile.getFilename();
		File file = imageFile.getFile();
		try {
			if (!DataUtils.validateImage(imageFile)) throw new InvalidImageException();
			if (user == null) throw new UserNotFoundException();
			if (activity == null) throw new ActivityNotFoundException();

			Integer userId = user.getId();

			String newImageName = DataUtils.generateUploadedImageName(fileName, userId);
			String imageURL = Image.getUrlPrefix() + newImageName;

			String imageAbsolutePath = Image.getFolderPath() + newImageName;

			int imageId = SQLCommander.createImage(user, activity, imageURL);
			if (imageId == SQLCommander.INVALID) throw new FileIOException();

			try {
				// Save renamed file to server storage at the final step
				FileUtils.moveFile(file, new File(imageAbsolutePath));
			} catch (Exception err) {
                Logger.e(TAG, "saveImageOfActivity", err);
                imageId = INVALID;
                if(deleteImageRecord(imageId))	System.out.println(TAG + ".saveImageOfActivity, " + newImageName + " has been reverted");
			}

			ret = imageId;
		} catch (Exception e) {
			Logger.e(TAG, "saveImageOfActivity", e);
		}

		return ret;
	}

    public static boolean deleteComments(Integer activityId) {
        try {
            if(activityId == null) throw new NullPointerException();
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.from(Comment.TABLE).where(Comment.ACTIVITY_ID, "=", activityId);
            if(!builder.execDelete()) throw new NullPointerException();
            return true;
        } catch (Exception e) {
            Logger.e(TAG, "deleteComments", e);
        }
        return false;
    }

    public static boolean deleteAssessments(Integer activityId) {
        try {
            if(activityId == null) throw new NullPointerException();
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.from(Assessment.TABLE).where(Assessment.ACTIVITY_ID, "=", activityId);
            if(!builder.execDelete()) throw new NullPointerException();
            return true;
        } catch (Exception e) {
            Logger.e(TAG, "deleteAssessments", e);
        }
        return false;
    }
}
