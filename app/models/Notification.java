package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import utilities.Converter;
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
	protected Long m_cmd = (long)INVALID;
	protected Integer m_relation = UserActivityRelation.INVALID;
	protected Integer m_status = INVALID;

	public Notification(JSONObject notificationJson) {
		super(notificationJson);
		if (notificationJson.containsKey(IS_READ)) m_isRead = Converter.toInteger(notificationJson.get(IS_READ));
		if (notificationJson.containsKey(COMMENT_ID)) m_commentId = Converter.toInteger(notificationJson.get(COMMENT_ID));
		if (notificationJson.containsKey(ASSESSMENT_ID)) m_assessmentId = Converter.toInteger(notificationJson.get(ASSESSMENT_ID));
		if (notificationJson.containsKey(CMD)) m_cmd = Converter.toLong(notificationJson.get(CMD));
		if (notificationJson.containsKey(RELATION)) m_relation = Converter.toInteger(notificationJson.get(RELATION));
		if (notificationJson.containsKey(STATUS)) m_status = Converter.toInteger(notificationJson.get(STATUS));
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
