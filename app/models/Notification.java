package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SimpleMap;
import utilities.Loggy;

public class Notification extends AbstractActivityMessage {

        public static String TAG = Notification.class.getName();

        public static final String IS_READ = "is_read";
        public static final String COMMENT_ID = "comment_id";
        public static final String ASSESSMENT_ID = "assessment_id";
        public static final String CMD = "cmd";
        public static final String RELATION = "relation";
        public static final String STATUS = "status";

        public static final String TABLE = "notification";
        public static final String NOTIFICATIONS = "notifications";

        public static String[] QUERY_FIELDS = {ID, IS_READ, COMMENT_ID, ASSESSMENT_ID, CMD, RELATION, STATUS};

        protected Integer m_isRead = 0;
        protected Integer m_commentId = INVALID;
        protected Integer m_assessmentId = INVALID;
        protected Long m_cmd = (long) INVALID;
        protected Integer m_relation = PlayerActivityRelation.INVALID;
        protected Integer m_status = INVALID;

        public Notification(final SimpleMap data) {
                super(data);
                m_isRead = data.getInt(IS_READ);
                m_commentId = data.getInt(COMMENT_ID);
                m_assessmentId = data.getInt(ASSESSMENT_ID);
                m_cmd = data.getLong(CMD);
                m_relation = data.getInt(RELATION);
                m_status = data.getInt(STATUS);
        }

        public ObjectNode toObjectNode() {
                ObjectNode ret = super.toObjectNode();
                try {
                        ret.put(CMD, String.valueOf(m_cmd));
                        ret.put(IS_READ, String.valueOf(m_isRead));
                } catch (Exception e) {
                        Loggy.e(TAG, "toObjectNode", e);
                }
                return ret;
        }
}
