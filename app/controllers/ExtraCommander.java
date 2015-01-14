package controllers;

import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.*;
import models.*;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import play.mvc.Http.MultipartFormData.FilePart;
import utilities.DataUtils;
import utilities.General;
import utilities.Loggy;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ExtraCommander extends SQLCommander {

    public static final String TAG = ExtraCommander.class.getName();

	public static ActivityDetail queryActivityDetail(Long activityId) {
		ActivityDetail activityDetail = null;
		try {
			Activity activity = queryActivity(activityId);
			List<Image> images = queryImages(activityId);
			List<BasicUser> appliedParticipants = queryAppliedParticipants(activityId);
			List<BasicUser> selectedParticipants = querySelectedParticipants(activityId);
			List<BasicUser> presentParticipants = new LinkedList<>(); // not used
			activityDetail = new ActivityDetail(activity, images, appliedParticipants, selectedParticipants, presentParticipants);
		} catch (Exception e) {
			Loggy.e(TAG, "queryActivityDetail", e);
		}
		return activityDetail;
	}

	public static boolean deleteActivity(long activityId) {
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
            return builderActivity.from(Activity.TABLE).where(Activity.ID, "=", activityId).execDelete();

		} catch (Exception e) {
			Loggy.e(TAG, "deleteActivity", e);
		}

		return false;
	}

	public static long uploadAvatar(User user, String imageURL) {
		try {
			EasyPreparedStatementBuilder builderImage = new EasyPreparedStatementBuilder();
			String[] names = {Image.URL, Image.META_ID, Image.META_TYPE, Image.GENERATED_TIME};
			Object[] vals = {imageURL, user.getId(), Image.TYPE_USER, General.millisec()};
			long lastImageId = builderImage.insert(names, vals)
										.into(Image.TABLE)
										.execInsert();
			if (lastImageId == SQLHelper.INVALID) throw new ImageNotFoundException();

			EasyPreparedStatementBuilder builderUser = new EasyPreparedStatementBuilder();
			builderUser.update(User.TABLE)
					.set(User.AVATAR, lastImageId)
					.where(User.ID, "=", user.getId());
			if (!builderUser.execUpdate()) {
				deleteImageRecord(lastImageId);
				throw new NullPointerException();
			}
			return lastImageId;
		} catch (Exception e){
			Loggy.e(TAG, "uploadAvatar", e);
		}
		return SQLHelper.INVALID;
	}

	public static Image queryImage(int imageId) {
		if (imageId == 0) return null;
		try {
			EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
			String[] names = {Image.ID, Image.URL};
			List<JSONObject> records = builder.select(names)
											.from(Image.TABLE)
											.where(Image.ID, "=", imageId)
											.execSelect();
			if (records == null || records.size() != 1) throw new ImageNotFoundException();
			return new Image(records.get(0));
		} catch (Exception e) {
			Loggy.e(TAG, "queryImage", e);
		}
		return null;
	}

	public static List<Image> queryImages(Long activityId) {
		List<Image> images = new LinkedList<>();
		try {
			EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
			List<JSONObject> records = builder.select(Image.QUERY_FIELDS)
											.from(Image.TABLE)
											.where(Image.META_TYPE, "=", Image.TYPE_ACTIVITY)
											.where(Image.META_ID, "=", activityId).execSelect();

			for (JSONObject record : records) {
				Image image = new Image(record);
				images.add(image);
			}
		} catch (Exception e) {
			Loggy.e(TAG, "queryImages", e);
		}
		return images;
	}

	public static long createImage(final User user, final Activity activity, final String imageURL) {
		try {
			if (user == null) throw new UserNotFoundException();
			if (activity == null) throw new ActivityNotFoundException();
			EasyPreparedStatementBuilder builderImage = new EasyPreparedStatementBuilder();
			String[] columns = {Image.URL, Image.META_ID, Image.META_TYPE, Image.GENERATED_TIME};
			Object[] values = {imageURL, activity.getId(), Image.TYPE_ACTIVITY, General.millisec()};
			return builderImage.insert(columns, values).into(Image.TABLE).execInsert();
		} catch (Exception e) {
			Loggy.e(TAG, "createImage", e);
		}
		return SQLHelper.INVALID;
	}

	protected static boolean deleteImageRecord(long imageId) {
		EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
		return builder.from(Image.TABLE).where(Image.ID, "=", imageId).execDelete();
	}

	public static boolean deleteImageRecordAndFile(Image image) {
		try {
			if (image == null) throw new ImageNotFoundException();

			String imageAbsolutePath = image.getAbsolutePath();
			File imageFile = new File(imageAbsolutePath);
			boolean isFileDeleted = (imageFile.exists() || imageFile.delete());

			EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
			boolean isRecordDeleted = builder.from(Image.TABLE)
											.where(Image.ID, "=", image.getId())
											.execDelete();
			if (!isFileDeleted || !isRecordDeleted) throw new NullPointerException();
			return true;
		} catch (Exception e) {
			Loggy.e(TAG, "deleteImageRecordAndFile", e);
		}
		return false;
	}

	public static boolean deleteImageRecordAndFile(Image image, long activityId) {
		try {
			if (image == null) throw new ImageNotFoundException();

			String imageAbsolutePath = image.getAbsolutePath();
			File imageFile = new File(imageAbsolutePath);
			boolean isFileDeleted = (imageFile.exists() || imageFile.delete());

			EasyPreparedStatementBuilder builderImage = new EasyPreparedStatementBuilder();
			boolean isRecordDeleted = builderImage.from(Image.TABLE)
												.where(Image.ID, "=", image.getId())
												.where(Image.META_ID, "=", activityId)
												.where(Image.META_TYPE, "=", Image.TYPE_ACTIVITY)
												.execDelete();

			if (!isFileDeleted || !isRecordDeleted) throw new NullPointerException();
			return true;
		} catch (Exception e) {
			Loggy.e(TAG, "deleteImageRecordAndFile", e);
		}
		return false;
	}

	public static long saveAvatar(FilePart imageFile, User user) {
        try {
			if (user == null) throw new UserNotFoundException();
			String fileName = imageFile.getFilename();
			File file = imageFile.getFile();
			if (!DataUtils.validateImage(imageFile)) throw new NullPointerException();
			String newImageName = DataUtils.generateUploadedImageName(fileName, user.getId());
			String imageURL = Image.getUrlPrefix() + newImageName;

			String imageAbsolutePath = Image.getFolderPath() + newImageName;

			long imageId = uploadAvatar(user, imageURL);
			if (imageId == SQLHelper.INVALID) throw new NullPointerException();

			try {
				// Save renamed file to server storage at the final step
				FileUtils.moveFile(file, new File(imageAbsolutePath));
			} catch (Exception err) {
				if(deleteImageRecord(imageId))	Loggy.i(TAG, "saveAvatar", newImageName + " has been reverted");
				File tmpFile = new File(imageAbsolutePath);
				if (tmpFile.exists()) tmpFile.delete();
				imageId = SQLHelper.INVALID;
			}
			return imageId;
		} catch (Exception e) {
			Loggy.e(TAG, "saveAvatar", e);
		}

		return SQLHelper.INVALID;
	}

	public static long saveImageOfActivity(FilePart imageFile, User user, Activity activity) {
		String fileName = imageFile.getFilename();
		File file = imageFile.getFile();
		try {
			if (user == null) throw new UserNotFoundException();
			if (activity == null) throw new ActivityNotFoundException();

			String newImageName = DataUtils.generateUploadedImageName(fileName, user.getId());
			String imageURL = Image.getUrlPrefix() + newImageName;

			String imageAbsolutePath = Image.getFolderPath() + newImageName;

			long imageId = createImage(user, activity, imageURL);
			if (imageId == SQLHelper.INVALID) throw new FileIOException();

			try {
				// Save renamed file to server storage at the final step
				FileUtils.moveFile(file, new File(imageAbsolutePath));
			} catch (Exception err) {
				if(deleteImageRecord(imageId))	Loggy.i(TAG, "saveImageOfActivity", newImageName + " has been reverted");
				File tmpFile = new File(imageAbsolutePath);
				if (tmpFile.exists()) tmpFile.delete();
				imageId = SQLHelper.INVALID;
			}

			return imageId;
		} catch (Exception e) {
			Loggy.e(TAG, "saveImageOfActivity", e);
		}

		return SQLHelper.INVALID;
	}

    public static boolean deleteComments(Long activityId) {
        try {
            if(activityId == null) throw new NullPointerException();
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.from(Comment.TABLE).where(Comment.ACTIVITY_ID, "=", activityId);
            if(!builder.execDelete()) throw new NullPointerException();
            return true;
        } catch (Exception e) {
            Loggy.e(TAG, "deleteComments", e);
        }
        return false;
    }

    public static boolean deleteAssessments(Long activityId) {
        try {
            if(activityId == null) throw new NullPointerException();
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.from(Assessment.TABLE).where(Assessment.ACTIVITY_ID, "=", activityId);
            if(!builder.execDelete()) throw new NullPointerException();
            return true;
        } catch (Exception e) {
            Loggy.e(TAG, "deleteAssessments", e);
        }
        return false;
    }
}
