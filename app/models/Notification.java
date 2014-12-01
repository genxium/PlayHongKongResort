package models;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import play.libs.Json;
import utilities.Converter;
import utilities.General;
import utilities.Loggy;

import java.util.List;

public class Notification {

	public static String TAG = Notification.class.getName();

    public static int INVALID = (-1);

    public static final String ID = "ID";
    public static final String IS_READ = "is_read";
    public static final String FROM = "from";
    public static final String TO = "to";
    public static final String CONTENT = "content";
    public static final String ACTIVITY_ID = "activity_id";
    public static final String COMMENT_ID = "comment_id";
    public static final String ASSESSMENT_ID = "assessment_id";
    public static final String CMD = "cmd";
    public static final String RELATION = "relation";
    public static final String STATUS = "status";
    public static final String GENERATED_TIME = "generated_time";

	public static final String TABLE = "notification";
	public static final String NOTIFICATIONS = "notifications";
    public static final String COUNT = "count"; 
	public static final String PAGE_ST = "page_st";
	public static final String PAGE_ED = "page_ed";

    protected long m_id = 0;
    protected boolean m_isRead = false;
    protected int m_from = INVALID;
    protected int m_to = INVALID;
    protected String m_content = null;
    protected int m_activityId = INVALID;
    protected int m_commentId = INVALID;
    protected int m_assessmentId = INVALID;
    protected int m_cmd = INVALID;
    protected int m_relation = UserActivityRelation.INVALID;
    protected int m_status = INVALID;
    protected long m_generatedTime;

    public Notification(JSONObject notificationJson) {
		if (notificationJson.containsKey(ID)) m_id = Converter.toLong(notificationJson.get(ID));
        if (notificationJson.containsKey(IS_READ)) m_isRead = (Boolean) notificationJson.get(IS_READ);
        if (notificationJson.containsKey(FROM)) m_from = Converter.toInteger(notificationJson.get(FROM));
        if (notificationJson.containsKey(TO)) m_to = Converter.toInteger(notificationJson.get(TO));
        if (notificationJson.containsKey(CONTENT)) m_content = (String) notificationJson.get(CONTENT);
        if (notificationJson.containsKey(ACTIVITY_ID)) m_activityId =  Converter.toInteger(notificationJson.get(ACTIVITY_ID));
        if (notificationJson.containsKey(COMMENT_ID)) m_commentId = Converter.toInteger(notificationJson.get(COMMENT_ID));
        if (notificationJson.containsKey(ASSESSMENT_ID)) m_assessmentId = Converter.toInteger(notificationJson.get(ASSESSMENT_ID));
        if (notificationJson.containsKey(CMD)) m_cmd = Converter.toInteger(notificationJson.get(CMD));
        if (notificationJson.containsKey(RELATION)) m_relation = Converter.toInteger(notificationJson.get(RELATION));
        if (notificationJson.containsKey(STATUS)) m_status = Converter.toInteger(notificationJson.get(STATUS));
		if (notificationJson.containsKey(GENERATED_TIME)) m_generatedTime = Converter.toLong(notificationJson.get(GENERATED_TIME));
    }
	
	public ObjectNode toObjectNode() {
	
        ObjectNode ret = Json.newObject();
        try {
            ret.put(ID, String.valueOf(m_id));
			ret.put(CMD, String.valueOf(m_cmd));
			ret.put(IS_READ, String.valueOf(m_isRead));
            ret.put(CONTENT, m_content);
			ret.put(GENERATED_TIME, String.valueOf(m_generatedTime));
        } catch (Exception e) {
            Loggy.e(TAG, "toObjectNode", e);
        }
        return ret;
	}
}
