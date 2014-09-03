package controllers;

import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import model.Activity;
import model.Image;
import model.User;
import exception.*;
import model.UserActivityRelation;
import org.apache.commons.io.FileUtils;
import play.mvc.Http.MultipartFormData.FilePart;
import utilities.DataUtils;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class ExtraCommander extends SQLCommander {

	public static boolean deleteActivity(int activityId) {
		boolean ret = false;
		try {
			// delete records in tables activity_image_relation and image,
			// as well as corresponding image files
			EasyPreparedStatementBuilder builderRelation = new EasyPreparedStatementBuilder();
			builderRelation.from(UserActivityRelation.TABLE).where(UserActivityRelation.ACTIVITY_ID, "=", activityId);
			if(!SQLHelper.delete(builderRelation)) throw new NullPointerException();

			List<Image> previousImages = queryImages(activityId);
			if (previousImages != null && previousImages.size() > 0) {
				// delete previous images
				Iterator<Image> itPreviousImage = previousImages.iterator();
				while (itPreviousImage.hasNext()) {
					Image previousImage = itPreviousImage.next();
					if(!deleteImageRecordAndFile(previousImage, activityId)) break;
				}
			}

			// delete record in table activity
			EasyPreparedStatementBuilder builderActivity = new EasyPreparedStatementBuilder();
			builderActivity.from(Activity.TABLE).where(Activity.ID, "=", activityId);
			ret = SQLHelper.delete(builderActivity);

		} catch (Exception e) {
			System.out.println(ExtraCommander.class.getName() + ".deleteActivity:" + e.getMessage());
		}

		return ret;
	}

	public static boolean deleteComments(Integer activityId) {
		return false;
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
			if (isFileDeleted == false || isRecordDeleted == false) throw new NullPointerException();
			ret = true;
		} catch (Exception e) {
			System.out.println(ExtraCommander.class.getName() + ".deleteImageRecordAndFile: " + e.getMessage());
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
			if (isFileDeleted == false || isRecordDeleted == false) throw new NullPointerException();
			ret = true;
		} catch (Exception e) {
			System.out.println(ExtraCommander.class.getName() + ".deleteImageRecordAndFile: " + e.getMessage());
		}
		return ret;
	}

	public static int saveAvatarFile(FilePart imageFile, User user) {
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

			int imageId = SQLCommander.uploadUserAvatar(user, imageURL);
			if (imageId == INVALID) throw new NullPointerException();

			try {
				// Save renamed file to server storage at the final step
				FileUtils.moveFile(file, new File(imageAbsolutePath));
			} catch (Exception err) {
				imageId = INVALID;
				System.out.println(ExtraCommander.class.getName() + ".saveAvatarFile, " + newImageName + " could not be saved.");
				if(SQLCommander.deleteImageRecord(imageId))	System.out.println(ExtraCommander.class.getName() + ".saveAvatarFile, " + newImageName + " has been reverted");
			}
			ret = imageId;
		} catch (Exception e) {
			System.out.println(ExtraCommander.class.getName() + ".saveAvatarFile, " + e.getMessage());
		}

		return ret;
	}

	public static int saveImageOfActivity(FilePart imageFile, User user, Activity activity) {
		int ret = INVALID;
		String fileName = imageFile.getFilename();
		File file = imageFile.getFile();
		try {
			if (DataUtils.validateImage(imageFile)) throw new NullPointerException();
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
				imageId = INVALID;
				System.out.println(ExtraCommander.class.getName() + ".saveImageOfActivity: " + newImageName + " could not be saved.");
				if(deleteImageRecord(imageId))	System.out.println(ExtraCommander.class.getName() + ".saveImageOfActivity, " + newImageName + " has been reverted");
			}

			ret = imageId;
		} catch (Exception e) {
			System.out.println(ExtraCommander.class.getName() + ".saveImageOfActivity: " + e.getMessage());
		}

		return ret;
	}
}
