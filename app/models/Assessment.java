package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SQLCommander;
import org.json.simple.JSONObject;
import utilities.Loggy;

public class Assessment extends AbstractActivityMessage {

    public static final String TAG = Assessment.class.getName();

    public static final Integer MIN_CONTENT_LENGTH = 5;

    public static final String TABLE = "assessment";
    public static final String ASSESSMENT_ID = "assessment_id";

    public Assessment(JSONObject assessmentJson) {
        super(assessmentJson);
    }

    public ObjectNode toObjectNodeWithNames() {
        ObjectNode ret = toObjectNode();
        try {
            User fromUser = SQLCommander.queryUser(m_from);
            User toUser = SQLCommander.queryUser(m_to);
            ret.put(FROM_NAME, fromUser.getName());
            ret.put(TO_NAME, toUser.getName());
        } catch (Exception e) {
            Loggy.e(TAG, "toObjectNodeWithNames", e);
        }
        return ret;
    }
}

