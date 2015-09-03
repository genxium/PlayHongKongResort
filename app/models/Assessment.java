package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SimpleMap;
import utilities.Loggy;

import java.util.regex.Pattern;

public class Assessment extends AbstractActivityMessage {

    public static final String TAG = Assessment.class.getName();

    public static final Pattern CONTENT_PATTERN = Pattern.compile(".{0,64}", Pattern.UNICODE_CHARACTER_CLASS);

    public static final String TABLE = "assessment";
    public static final String ASSESSMENT_ID = "assessment_id";

    public Assessment(final SimpleMap data) {
        super(data);
    }

    public ObjectNode toObjectNode(final Long viewerId) {
        ObjectNode ret = toObjectNode();
        try {
            ret.put(FROM_PLAYER, m_fromPlayer.toObjectNode(viewerId));
            ret.put(TO_PLAYER, m_toPlayer.toObjectNode(viewerId));
        } catch (Exception e) {
            Loggy.e(TAG, "toObjectNodeWithNames", e);
        }
        return ret;
    }
}

