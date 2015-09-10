package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SimpleMap;
import utilities.Loggy;

public class Notification extends AbstractActivityMessage {

        public static String TAG = Notification.class.getName();

        public static final String IS_READ = "iread";
        public static final String COMMENT_ID = "comment_id";
        public static final String ASSESSMENT_ID = "assessment_id";
        public static final String CMD = "cmd";
        public static final String RELATION = "relation";
        public static final String STATUS = "status";

        public static final String TABLE = "notification";
        public static final String NOTIFICATIONS = "notifications";

        public static final String[] QUERY_FIELDS = {ID, IS_READ, COMMENT_ID, ASSESSMENT_ID, CMD, RELATION, STATUS};

        protected Integer isRead = 0;
        protected Integer commentId = INVALID;
        protected Integer assessmentId = INVALID;
        protected Long cmd = (long) INVALID;
        protected Integer relation = PlayerActivityRelation.INVALID;
        protected Integer status = INVALID;

        public Notification(final SimpleMap data) {
                super(data);
                isRead = data.getInt(IS_READ);
                commentId = data.getInt(COMMENT_ID);
                assessmentId = data.getInt(ASSESSMENT_ID);
                cmd = data.getLong(CMD);
                relation = data.getInt(RELATION);
                status = data.getInt(STATUS);
        }

        public ObjectNode toObjectNode() {
                final ObjectNode ret = super.toObjectNode();
                try {
                        ret.put(CMD, String.valueOf(cmd));
                        ret.put(IS_READ, String.valueOf(isRead));
                } catch (Exception e) {
                        Loggy.e(TAG, "toObjectNode", e);
                }
                return ret;
        }
}
