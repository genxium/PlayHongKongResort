package controllers;

import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import dao.SimpleMap;
import exception.ActivityNotFoundException;
import exception.FileIOException;
import exception.ImageNotFoundException;
import exception.PlayerNotFoundException;
import models.*;
import org.apache.commons.io.FileUtils;
import play.mvc.Http.MultipartFormData.FilePart;
import utilities.Converter;
import utilities.DataUtils;
import utilities.General;
import utilities.Loggy;

import java.io.File;
import java.util.*;

public class ExtraCommander extends DBCommander {

    public static final String TAG = ExtraCommander.class.getName();

    public static ActivityDetail queryActivityDetail(Long activityId) {
        try {
            String[] names = Activity.QUERY_FIELDS;
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<SimpleMap> results = builder.select(names).from(Activity.TABLE).where(Activity.ID, "=", activityId).execSelect();
            if (results == null || results.size() != 1) throw new ActivityNotFoundException();
            SimpleMap data = results.get(0);
            Player host = queryPlayer(data.getLong(Activity.HOST_ID));
            ActivityDetail activityDetail = new ActivityDetail(data);
            activityDetail.setHost(host);

            List<Image> images = queryImages(activityId);
            activityDetail.setImageList(images);

            List<BasicPlayer> appliedParticipants = queryAppliedParticipants(activityId);
            List<BasicPlayer> selectedParticipants = querySelectedParticipants(activityId);
            List<BasicPlayer> presentParticipants = new LinkedList<>(); // not used
            activityDetail.setAppliedParticipants(appliedParticipants);
            activityDetail.setPresentParticipants(presentParticipants);
            activityDetail.setSelectedParticipants(selectedParticipants);

            return activityDetail;
        } catch (Exception e) {
            Loggy.e(TAG, "queryActivityDetail", e);
        }
        return null;
    }

    public static boolean appendParticipantInfoForActivityDetail(final List<ActivityDetail> activityList) {
        Map<Long, ActivityDetail> tmp = new HashMap<>();
        List<Long> activityIdList = new ArrayList<>();
        for (ActivityDetail activity : activityList) {
            tmp.put(activity.getId(), activity);
            activityIdList.add(activity.getId());
        }
        List<Integer> appliedStates = new LinkedList<>();
        for (Integer state : PlayerActivityRelation.APPLIED_STATES) appliedStates.add(state);
        List<Integer> selectedStates = new LinkedList<>();
        for (Integer state : PlayerActivityRelation.SELECTED_STATES) selectedStates.add(state);
        List<Integer> presentStates = new LinkedList<>();
        for (Integer state : PlayerActivityRelation.PRESENT_STATES) presentStates.add(state);

        List<SpecialPlayerRecord> appliedList = queryPlayers(activityIdList, appliedStates);
        for (SpecialPlayerRecord record : appliedList) {
            ActivityDetail activity = tmp.get(record.activityId);
            activity.addAppliedParticipant(record.player);
        }

        List<SpecialPlayerRecord> selectedList = queryPlayers(activityIdList, selectedStates);
        for (SpecialPlayerRecord record : selectedList) {
            ActivityDetail activity = tmp.get(record.activityId);
            activity.addSelectedParticipant(record.player);
        }

        List<SpecialPlayerRecord> presentList = queryPlayers(activityIdList, presentStates);
        for (SpecialPlayerRecord record : presentList) {
            ActivityDetail activity = tmp.get(record.activityId);
            activity.addPresentParticipant(record.player);
        }

        return true;
    }

    public static boolean deleteActivity(long activityId) {
        try {
            // delete records in tables activity_image_relation and image,
            // as well as corresponding image files
            EasyPreparedStatementBuilder builderRelation = new EasyPreparedStatementBuilder();
            builderRelation.from(PlayerActivityRelation.TABLE).where(PlayerActivityRelation.ACTIVITY_ID, "=", activityId);
            if (!builderRelation.execDelete()) throw new NullPointerException();

            List<Image> previousImages = queryImages(activityId);
            if (previousImages != null && previousImages.size() > 0) {
                // delete previous images
                for (Image previousImage : previousImages) deleteImageRecordAndFile(previousImage, activityId);
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

    public static long uploadAvatar(Player player, String imageURL) {
        try {
            EasyPreparedStatementBuilder builderImage = new EasyPreparedStatementBuilder();
            String[] names = {Image.URL, Image.META_ID, Image.META_TYPE, Image.GENERATED_TIME};
            Object[] vals = {imageURL, player.getId(), Image.TYPE_PLAYER, General.millisec()};
            long lastImageId = builderImage.insert(names, vals)
                    .into(Image.TABLE)
                    .execInsert();
            if (lastImageId == SQLHelper.INVALID) throw new ImageNotFoundException();

            EasyPreparedStatementBuilder builderPlayer = new EasyPreparedStatementBuilder();
            builderPlayer.update(Player.TABLE)
                    .set(Player.AVATAR, lastImageId)
                    .where(Player.ID, "=", player.getId());
            if (!builderPlayer.execUpdate()) {
                deleteImageRecord(lastImageId);
                throw new NullPointerException();
            }
            return lastImageId;
        } catch (Exception e) {
            Loggy.e(TAG, "uploadAvatar", e);
        }
        return SQLHelper.INVALID;
    }

    public static Image queryImage(long imageId) {
        if (imageId == 0) return null;
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] names = {Image.ID, Image.URL};
            List<SimpleMap> records = builder.select(names)
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
            List<SimpleMap> records = builder.select(Image.QUERY_FIELDS)
                    .from(Image.TABLE)
                    .where(Image.META_TYPE, "=", Image.TYPE_ACTIVITY)
                    .where(Image.META_ID, "=", activityId).execSelect();

            for (SimpleMap record : records) {
                Image image = new Image(record);
                images.add(image);
            }
        } catch (Exception e) {
            Loggy.e(TAG, "queryImages", e);
        }
        return images;
    }

    public static List<Image> queryImages(List<Long> activityIdList) {
        List<Image> images = new LinkedList<>();
        if (activityIdList == null || activityIdList.isEmpty()) return images;
        try {
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            List<SimpleMap> records = builder.select(Image.QUERY_FIELDS)
                    .from(Image.TABLE)
                    .where(Image.META_TYPE, "=", Image.TYPE_ACTIVITY)
                    .where(Image.META_ID, "IN", activityIdList).execSelect();

            for (SimpleMap record : records) {
                Image image = new Image(record);
                images.add(image);
            }
        } catch (Exception e) {
            Loggy.e(TAG, "queryImages", e);
        }
        return images;
    }

    public static long createImage(final Player player, final Activity activity, final String imageURL) {
        try {
            if (player == null) throw new PlayerNotFoundException();
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

    public static long saveAvatar(FilePart imageFile, Player player) {
        try {
            if (player == null) throw new PlayerNotFoundException();
            String fileName = imageFile.getFilename();
            File file = imageFile.getFile();
            if (!DataUtils.validateImage(imageFile)) throw new NullPointerException();
            String newImageName = DataUtils.generateUploadedImageName(fileName, player.getId());
            String imageURL = Image.getUrlPrefix() + newImageName;

            String imageAbsolutePath = Image.getFolderPath() + newImageName;

            long imageId = uploadAvatar(player, imageURL);
            if (imageId == SQLHelper.INVALID) throw new NullPointerException();

            try {
                // Save renamed file to server storage at the final step
                FileUtils.moveFile(file, new File(imageAbsolutePath));
            } catch (Exception err) {
                if (deleteImageRecord(imageId)) Loggy.d(TAG, "saveAvatar", newImageName + " has been reverted");
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

    public static long saveImageOfActivity(FilePart imageFile, Player player, Activity activity) {
        String fileName = imageFile.getFilename();
        File file = imageFile.getFile();
        try {
            if (player == null) throw new PlayerNotFoundException();
            if (activity == null) throw new ActivityNotFoundException();

            String newImageName = DataUtils.generateUploadedImageName(fileName, player.getId());
            String imageURL = Image.getUrlPrefix() + newImageName;

            String imageAbsolutePath = Image.getFolderPath() + newImageName;

            long imageId = createImage(player, activity, imageURL);
            if (imageId == SQLHelper.INVALID) throw new FileIOException();

            try {
                // Save renamed file to server storage at the final step
                FileUtils.moveFile(file, new File(imageAbsolutePath));
            } catch (Exception err) {
                if (deleteImageRecord(imageId))
                    Loggy.i(TAG, "saveImageOfActivity", newImageName + " has been reverted");
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
            if (activityId == null) throw new NullPointerException();
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.from(Comment.TABLE).where(Comment.ACTIVITY_ID, "=", activityId);
            if (!builder.execDelete()) throw new NullPointerException();
            return true;
        } catch (Exception e) {
            Loggy.e(TAG, "deleteComments", e);
        }
        return false;
    }

    public static boolean deleteAssessments(Long activityId) {
        try {
            if (activityId == null) throw new NullPointerException();
            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            builder.from(Assessment.TABLE).where(Assessment.ACTIVITY_ID, "=", activityId);
            if (!builder.execDelete()) throw new NullPointerException();
            return true;
        } catch (Exception e) {
            Loggy.e(TAG, "deleteAssessments", e);
        }
        return false;
    }
}
