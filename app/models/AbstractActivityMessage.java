package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SimpleMap;
import utilities.Loggy;

public class AbstractActivityMessage extends AbstractMessage {

        public static final String ACTIVITY_ID = "activity_id";

        protected Long activityId = null;

        public Long getActivityId() {
                return activityId;
        }

        public void setActivityId(final long data) {
                activityId = data;
        }

        public AbstractActivityMessage() {
                super();
        }

        public AbstractActivityMessage(SimpleMap data) {
                super(data);
                activityId = data.getLong(ACTIVITY_ID);
        }

        public ObjectNode toObjectNode() {
                ObjectNode ret = super.toObjectNode();
                try {
                        ret.put(ACTIVITY_ID, String.valueOf(activityId));
                } catch (Exception e) {
                        Loggy.e(this.getClass().getName(), "toObjectNode", e);
                }
                return ret;
        }
}
