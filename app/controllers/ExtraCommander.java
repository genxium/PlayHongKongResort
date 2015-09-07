package controllers;

import dao.SQLBuilder;
import dao.SQLHelper;
import dao.SimpleMap;
import exception.ActivityNotFoundException;
import exception.ImageNotFoundException;
import models.*;
import utilities.Loggy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

public class ExtraCommander extends DBCommander {

        public static final String TAG = ExtraCommander.class.getName();

        public static ActivityDetail queryActivityDetail(Long activityId) {
                try {
                        String[] names = Activity.QUERY_FIELDS;
                        SQLBuilder builder = new SQLBuilder();
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
                        Connection connection = SQLHelper.getConnection();
                        if (connection == null) throw new NullPointerException();
                        try {
                                /**
                                 * begin SQL-transaction guard
                                 * */
                                SQLHelper.disableAutoCommit(connection);

                                SQLBuilder builderRelation = new SQLBuilder();
                                builderRelation.from(PlayerActivityRelation.TABLE).where(PlayerActivityRelation.ACTIVITY_ID, "=", activityId);
                                PreparedStatement statRelation = builderRelation.toDelete(connection);
                                SQLHelper.executeAndCloseStatement(statRelation);

                                // delete images
                                SQLBuilder builderImages = new SQLBuilder();
                                PreparedStatement statImages = builderImages.from(Image.TABLE)
                                        .where(Image.META_TYPE, "=", Image.TYPE_ACTIVITY)
                                        .where(Image.META_ID, "=", activityId).toDelete(connection);
                                SQLHelper.executeAndCloseStatement(statImages);

                                // delete comments
                                SQLBuilder builderComments = new SQLBuilder();
                                PreparedStatement statComments = builderComments.from(Comment.TABLE)
                                                                                .where(Comment.ACTIVITY_ID, "=", activityId)
                                                                                .toDelete(connection);
                                SQLHelper.executeAndCloseStatement(statComments);

                                // delete assessments
                                SQLBuilder builderAssessments = new SQLBuilder();
                                PreparedStatement statAssessments = builderAssessments.from(Comment.TABLE)
                                                                                .where(Comment.ACTIVITY_ID, "=", activityId)
                                                                                .toDelete(connection);
                                SQLHelper.executeAndCloseStatement(statAssessments);

                                SQLHelper.commit(connection);
                        } catch (Exception e) {
                                Loggy.e(TAG, "deleteActivity", e);
                                SQLHelper.rollback(connection);
                        } finally {
                                SQLHelper.enableAutoCommitAndClose(connection);
                        }
                        /**
                         * end SQL-transaction guard
                         * */
                } catch (Exception e) {
                        Loggy.e(TAG, "deleteActivity", e);
                }
                return false;
        }

        public static Image queryImage(final long imageId) {
                if (imageId == 0) return null;
                try {
                        final SQLBuilder builder = new SQLBuilder();
                        final List<SimpleMap> records = builder.select(Image.QUERY_FIELDS)
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

        public static Image queryImage(final Long metaId, final Integer metaType, final String remoteName) {
                if (remoteName == null || remoteName.isEmpty()) return null;
                try {
                        final SQLBuilder builder = new SQLBuilder();
                        final List<SimpleMap> records = builder.select(Image.QUERY_FIELDS)
                                .from(Image.TABLE)
                                .where(Image.META_TYPE, "=", metaType)
                                .where(Image.META_ID, "=", metaId)
                                .where(Image.REMOTE_NAME, "=", remoteName)
                                .execSelect();

                        if (records.size() != 1) throw new NullPointerException();
                        SimpleMap record = records.get(0);
                        return new Image(record);
                } catch (Exception e) {
                        Loggy.e(TAG, "queryImages", e);
                }
                return null;
        }

        public static List<Image> queryImages(final Long activityId) {
                final List<Image> images = new LinkedList<>();
                try {
                        final SQLBuilder builder = new SQLBuilder();
                        final List<SimpleMap> records = builder.select(Image.QUERY_FIELDS)
                                .from(Image.TABLE)
                                .where(Image.META_TYPE, "=", Image.TYPE_ACTIVITY)
                                .where(Image.META_ID, "=", activityId).execSelect();

                        for (final SimpleMap record : records) {
                                final Image image = new Image(record);
                                images.add(image);
                        }
                } catch (Exception e) {
                        Loggy.e(TAG, "queryImages", e);
                }
                return images;
        }

        public static List<Image> queryImages(final Long metaId, final Integer metaType, final List<String> remoteNameList) {
                final List<Image> images = new LinkedList<>();
                if (remoteNameList == null || remoteNameList.isEmpty()) return images;
                try {
                        final SQLBuilder builder = new SQLBuilder();
                        final List<SimpleMap> records = builder.select(Image.QUERY_FIELDS)
                                                        .from(Image.TABLE)
                                                        .where(Image.META_TYPE, "=", metaType)
                                                        .where(Image.META_ID, "=", metaId)
                                                        .where(Image.REMOTE_NAME, "IN", remoteNameList)
                                                        .execSelect();

                        for (SimpleMap record : records) {
                                final Image image = new Image(record);
                                images.add(image);
                        }
                } catch (Exception e) {
                        Loggy.e(TAG, "queryImages", e);
                }
                return images;
        }

        public static List<Image> queryImages(final List<Long> activityIdList) {
                List<Image> images = new LinkedList<>();
                if (activityIdList == null || activityIdList.isEmpty()) return images;
                try {
                        SQLBuilder builder = new SQLBuilder();
                        List<SimpleMap> records = builder.select(Image.QUERY_FIELDS)
                                                        .from(Image.TABLE)
                                                        .where(Image.META_TYPE, "=", Image.TYPE_ACTIVITY)
                                                        .where(Image.META_ID, "IN", activityIdList).execSelect();

                        for (SimpleMap record : records) {
                                final Image image = new Image(record);
                                images.add(image);
                        }
                } catch (Exception e) {
                        Loggy.e(TAG, "queryImages", e);
                }
                return images;
        }
}
