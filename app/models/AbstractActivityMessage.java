package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import utilities.Converter;
import utilities.Loggy;

public class AbstractActivityMessage extends AbstractMessage {

    public static final String ACTIVITY_ID = "activity_id";

    protected Integer m_activityId = null;

    public Integer getActivityId() {
        return m_activityId;
    }

    public void setActivityId(int activityId) {
        m_activityId = activityId;
    }

    public AbstractActivityMessage() {
        super();
    }

    public AbstractActivityMessage(JSONObject json) {
        super(json);
        if (json.containsKey(ACTIVITY_ID)) m_activityId = Converter.toInteger(json.get(ACTIVITY_ID));
    }

    public ObjectNode toObjectNode() {
        ObjectNode ret = super.toObjectNode();
        try {
            ret.put(ACTIVITY_ID, String.valueOf(m_activityId));
        } catch (Exception e) {
            Loggy.e(this.getClass().getName(), "toObjectNode", e);
        }
        return ret;
    }
}