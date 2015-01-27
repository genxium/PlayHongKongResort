package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SQLCommander;
import org.json.simple.JSONObject;
import utilities.Loggy;

public class Assessment extends AbstractActivityMessage {

    public static final String TAG = Assessment.class.getName();

    public static final String CONTENT_PATTERN = ".{0,64}";

    public static final String TABLE = "assessment";
    public static final String ASSESSMENT_ID = "assessment_id";

    public Assessment(JSONObject assessmentJson) {
        super(assessmentJson);
    }

    public ObjectNode toObjectNode(Long viewerId) {
        ObjectNode ret = toObjectNode();
        try {
            BasicUser fromUser = SQLCommander.queryUser(m_from);
            BasicUser toUser = SQLCommander.queryUser(m_to);
            ret.put(FROM_USER, fromUser.toObjectNode(viewerId));
            ret.put(TO_USER, toUser.toObjectNode(viewerId));
        } catch (Exception e) {
            Loggy.e(TAG, "toObjectNodeWithNames", e);
        }
        return ret;
    }
}

