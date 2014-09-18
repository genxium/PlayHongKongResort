package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SQLCommander;
import org.json.simple.JSONObject;
import play.libs.Json;
import utilities.Converter;

import java.sql.Timestamp;

public class Assessment {

    public static final Integer MIN_CONTENT_LENGTH = 5;

    public static final String TABLE = "assessment";
    public static final String ID = "id";
    public static final String ASSESSMENT_ID = "assessment_id";
    public static final String CONTENT = "content";
    public static final String FROM = "from";
    public static final String ACTIVITY_ID = "activity_id";
    public static final String TO = "to";
    public static final String GENERATED_TIME = "generated_time";

    public static final String FROM_NAME = "from_name";
    public static final String TO_NAME = "to_name";

    protected Integer m_id = null;
    protected String m_content = null;
    protected Integer m_from = null;
    protected Integer m_to = null;
    protected Integer m_activityId = null;
    protected Timestamp m_generatedTime = null;

    public Integer getId() {
        return m_id;
    }

    public String getContent() {
        return m_content;
    }

    public Integer getFrom() {
        return m_from;
    }

    public Integer getActivityId() {
        return m_activityId;
    }

    public Integer getTo() {
        return m_to;
    }

    public Timestamp getGeneratedTime() {
        return m_generatedTime;
    }

    public void setActivityId(int activityId) {
        m_activityId = activityId;
    }

    public void setFrom(int from) {
        m_from = from;
    }

    public Assessment(JSONObject assessmentJson) {
        if (assessmentJson.containsKey(ID)) {
            m_id = Converter.toInteger(assessmentJson.get(ID));
        }
        if (assessmentJson.containsKey(CONTENT)) {
            m_content = (String) assessmentJson.get(CONTENT);
        }
        if (assessmentJson.containsKey(FROM)) {
            m_from = Converter.toInteger(assessmentJson.get(FROM));
        }
        if (assessmentJson.containsKey(TO)) {
            m_to = Converter.toInteger(assessmentJson.get(TO));
        }
        if (assessmentJson.containsKey(ACTIVITY_ID)) {
            m_activityId = Converter.toInteger(assessmentJson.get(ACTIVITY_ID));
        }
        if (assessmentJson.containsKey(GENERATED_TIME)) {
            m_generatedTime = (Timestamp) assessmentJson.get(GENERATED_TIME);
        }
    }

    public ObjectNode toObjectNode() {
        ObjectNode ret = Json.newObject();
        try {
            ret.put(ID, m_id);
            ret.put(FROM, m_from);
            ret.put(TO, m_to);
            ret.put(CONTENT, m_content);
            ret.put(GENERATED_TIME, m_generatedTime.toString());
        } catch (Exception e) {
            System.out.println(Assessment.class.getName() + ".toObjectNode, " + e.getMessage());
        }
        return ret;
    }

    public ObjectNode toObjectNodeWithNames() {
        ObjectNode ret = toObjectNode();
        try {
            User fromUser = SQLCommander.queryUser(m_from);
            User toUser = SQLCommander.queryUser(m_to);
            ret.put(FROM_NAME, fromUser.getName());
            ret.put(TO_NAME, toUser.getName());
        } catch (Exception e) {
            System.out.println(Assessment.class.getName() + ".toObjectNodeWithNames, " + e.getMessage());
        }
        return ret;
    }
}

