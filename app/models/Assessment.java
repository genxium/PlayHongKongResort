package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import utilities.Loggy;

import java.util.regex.Pattern;

public class Assessment extends AbstractActivityMessage {

    public static final String TAG = Assessment.class.getName();

    public static final Pattern CONTENT_PATTERN = Pattern.compile(".{0,64}", Pattern.UNICODE_CHARACTER_CLASS);

    public static final String TABLE = "assessment";
    public static final String ASSESSMENT_ID = "assessment_id";

    public Assessment(JSONObject assessmentJson) {
        super(assessmentJson);
    }

    public ObjectNode toObjectNode(Long viewerId) {
        ObjectNode ret = toObjectNode();
        try {
            ret.put(FROM_USER, m_fromUser.toObjectNode(viewerId));
            ret.put(TO_USER, m_toUser.toObjectNode(viewerId));
        } catch (Exception e) {
            Loggy.e(TAG, "toObjectNodeWithNames", e);
        }
        return ret;
    }
}

