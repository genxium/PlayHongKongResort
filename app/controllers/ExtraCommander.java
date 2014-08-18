package controllers;

import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import model.Activity;
import model.Image;
import model.User;
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
        do {
            try {
                EasyPreparedStatementBuilder builderRelation = new EasyPreparedStatementBuilder();
                builderRelation.from(UserActivityRelation.TABLE).where(UserActivityRelation.ACTIVITY_ID, "=", activityId);
                boolean resultRelationDeletion = SQLHelper.delete(builderRelation);

                if (resultRelationDeletion == false) break;

                List<Image> previousImages = queryImages(activityId);
                if (previousImages != null && previousImages.size() > 0) {
                    // delete previous images
                    Iterator<Image> itPreviousImage = previousImages.iterator();
                    while (itPreviousImage.hasNext()) {
                        Image previousImage = itPreviousImage.next();
                        boolean isDeleted = deleteImageRecordAndFile(previousImage, activityId);
                        if (isDeleted == false) break;
                    }
                }

                EasyPreparedStatementBuilder builderActivity = new EasyPreparedStatementBuilder();
                builderActivity.from(Activity.TABLE).where(Activity.ID, "=", activityId);
                ret = SQLHelper.delete(builderActivity);

            } catch (Exception e) {
                System.out.println("ExtraCommander.deleteActivity:" + e.getMessage());
            }
        } while (false);

        return ret;
    }

    public static boolean deleteImageRecordAndFile(Image image) {
        boolean ret = false;
        do {
            try {
                if (image == null) break;
                int imageId = image.getImageId();
                String imageAbsolutePath = image.getAbsolutePath();
                File imageFile = new File(imageAbsolutePath);
                boolean isFileDeleted = imageFile.delete();
                boolean isRecordDeleted = deleteImageRecord(imageId);
                if (isFileDeleted == false || isRecordDeleted == false) break;
                ret = true;
            } catch (Exception e) {
                System.out.println("ExtraCommander.deleteImageRecordAndFile: " + e.getMessage());
            }
        } while (false);
        return ret;
    }

    public static boolean deleteImageRecordAndFile(Image image, int activityId) {
        boolean ret = false;
        do {
            try {
                if (image == null) break;
                int imageId = image.getImageId();
                String imageAbsolutePath = image.getAbsolutePath();
                File imageFile = new File(imageAbsolutePath);
                boolean isFileDeleted = imageFile.delete();
                boolean isRecordDeleted = deleteImageRecord(imageId, activityId);
                if (isFileDeleted == false || isRecordDeleted == false) break;
                ret = true;
            } catch (Exception e) {
                System.out.println("ExtraCommander.deleteImageRecordAndFile: " + e.getMessage());
            }
        } while (false);
        return ret;
    }

    public static int saveAvatarFile(FilePart imageFile, User user) {
        int ret = INVALID;
        do {
            String fileName = imageFile.getFilename();
            File file = imageFile.getFile();
            try {
                if (DataUtils.validateImage(imageFile) == false) break;
                if (user == null) break;

                Integer userId = user.getId();
                String newImageName = DataUtils.generateUploadedImageName(fileName, userId);
                String imageURL = Image.getUrlPrefix() + newImageName;

                String imageAbsolutePath = Image.getFolderPath() + newImageName;

                int imageId = SQLCommander.uploadUserAvatar(user, imageURL);
                if (imageId == SQLCommander.INVALID) break;

                try {
                    // Save renamed file to server storage at the final step
                    FileUtils.moveFile(file, new File(imageAbsolutePath));
                } catch (Exception err) {
                    System.out.println("ExtraCommander.saveAvatarFile: " + newImageName + " could not be saved.");
                    boolean isRecovered = SQLCommander.deleteImageRecord(imageId);
                    if (isRecovered == true) {
                        System.out.println("ExtraCommander.saveAvatarFile: " + newImageName + " has been reverted");
                    }
                    break;
                }
                ret = imageId;
            } catch (Exception e) {
                System.out.println("ExtraCommander.saveAvatarFile: " + e.getMessage());
            }

        } while (false);
        return ret;
    }

    public static int saveImageOfActivity(FilePart imageFile, User user, Activity activity) {
        int ret = INVALID;
        do {
            String fileName = imageFile.getFilename();
            File file = imageFile.getFile();
            try {
                if (DataUtils.validateImage(imageFile) == false) break;
                if (user == null) break;
                if (activity == null) break;

                Integer userId = user.getId();

                String newImageName = DataUtils.generateUploadedImageName(fileName, userId);
                String imageURL = Image.getUrlPrefix() + newImageName;

                String imageAbsolutePath = Image.getFolderPath() + newImageName;

                int imageId = SQLCommander.uploadImage(user, activity, imageURL);
                if (imageId == SQLCommander.INVALID) break;

                try {
                    // Save renamed file to server storage at the final step
                    FileUtils.moveFile(file, new File(imageAbsolutePath));
                } catch (Exception err) {
                    System.out.println("ExtraCommander.saveImageOfActivity: " + newImageName + " could not be saved.");
                    boolean isRecovered = SQLCommander.deleteImageRecord(imageId);
                    if (isRecovered == true) {
                        System.out.println("ExtraCommander.saveImageOfActivity: " + newImageName + " has been reverted");
                    }
                    break;
                }

                ret = imageId;
            } catch (Exception e) {
                System.out.println("ExtraCommander.saveImageOfActivity: " + e.getMessage());
            }

        } while (false);
        return ret;
    }
}
