package models;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SQLCommander;
import org.json.simple.JSONObject;
import play.libs.Json;
import utilities.General;
import utilities.Converter;
import utilities.Logger;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class Activity {

    public static final String TAG = Activity.class.getName();

    public static final int CREATED = 0;
    public static final int PENDING = 1;
    public static final int REJECTED = 2;
    public static final int ACCEPTED = 3;

    public static final String TABLE = "activity";
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String CREATED_TIME = "created_time";
    public static final String BEGIN_TIME = "begin_time";
    public static final String DEADLINE = "application_deadline";
    public static final String CAPACITY = "capacity";
    public static final String NUM_APPLIED = "num_applied";
    public static final String NUM_SELECTED = "num_selected";
    public static final String STATUS = "status";
    public static final String HOST_ID = "host_id";
    public static final String HOST = "host";
    public static final String VIEWER = "viewer";

    public static final String LAST_ACCEPTED_TIME = "last_accepted_time";
    public static final String LAST_REJECTED_TIME = "last_rejected_time";

    public static final String COUNT = "count";
    public static final String PAGE = "page";
    public static final String PAGE_ST = "page_st";
    public static final String PAGE_ED = "page_ed";
    public static final String ACTIVITIES = "activities";

    protected int m_id = 0;

    public int getId() {
	    return m_id;
    }

    public void setId(int id) {
	    m_id = id;
    }

    protected String m_title = null;

    public String getTitle() {
	    return m_title;
    }

    public void setTitle(String title) {
	    m_title = title;
    }

    protected String m_content = null;

    public String getContent() {
	    return m_content;
    }

    public void setContent(String content) {
	    m_content = content;
    }

    protected Timestamp m_createdTime = null;

    public Timestamp getCreatedTime() {
	    return m_createdTime;
    }

    protected Timestamp m_beginTime = null;

    public Timestamp getBeginTime() {
	    return m_beginTime;
    }

    public void setBeginTime(Timestamp beginTime) {
	    m_beginTime = beginTime;
    }

    protected Timestamp m_deadline = null;

    public Timestamp getDeadline() {
	    return m_deadline;
    }

    public void setDeadline(Timestamp deadline) {
	    m_deadline = deadline;
    }

    protected Timestamp m_lastAcceptedTime = null;

    public Timestamp getLastAcceptedTime() {
        return m_lastAcceptedTime;
    }

    protected Timestamp m_lastRejectedTime = null;

    public Timestamp getLastRejectedTime() {
        return m_lastRejectedTime;
    }

    protected int m_capacity = 0;

    public int getCapacity() {
	    return m_capacity;
    }
	
    protected int m_numApplied = 0;

    public int getNumApplied() {
	return m_numApplied;
    }

    protected int m_numSelected = 0;
	
    public int getNumSelected() {
	return m_numSelected;
    } 

    protected int m_status = CREATED;

    public int getStatus() {
	    return m_status;
    }

    public void setStatus(int status) {
	    m_status = status;
    }

    protected User m_host = null;

    public User getHost() {
	    return m_host;
    }

    public void setHost(User host) {
	    m_host = host;
    }

    protected User m_viewer = null;

    public boolean isDeadlineExpired() {
	    long milisecs = General.localCalendar().getTimeInMillis();
	    return  milisecs > m_deadline.getTime();
    }

    public boolean hasBegun() {
	    long milisecs = General.localCalendar().getTimeInMillis();
	    return milisecs > m_beginTime.getTime();
    }

    protected Activity() {

    }

    public Activity(JSONObject activityJson, User host) {
	    if (activityJson.containsKey(ID))
		    m_id = Converter.toInteger(activityJson.get(ID));
	    if (activityJson.containsKey(TITLE))
		    m_title = (String) activityJson.get(TITLE);
	    if (activityJson.containsKey(CONTENT))
		    m_content = (String) activityJson.get(CONTENT);
	    if (activityJson.containsKey(CREATED_TIME))
		    m_createdTime = (Timestamp) activityJson.get(CREATED_TIME);
	    if (activityJson.containsKey(BEGIN_TIME))
		    m_beginTime = (Timestamp) activityJson.get(BEGIN_TIME);
	    if (activityJson.containsKey(DEADLINE))
		    m_deadline = (Timestamp) activityJson.get(DEADLINE);
	    if (activityJson.containsKey(CAPACITY))
		    m_capacity = Converter.toInteger(activityJson.get(CAPACITY));
	    if (activityJson.containsKey(NUM_APPLIED))
		    m_numApplied = Converter.toInteger(activityJson.get(NUM_APPLIED));
	    if (activityJson.containsKey(NUM_SELECTED))
	        m_numSelected = Converter.toInteger(activityJson.get(NUM_SELECTED));
	    if (activityJson.containsKey(STATUS))
		    m_status = Converter.toInteger(activityJson.get(STATUS));
        if (activityJson.containsKey(LAST_ACCEPTED_TIME)) {
            m_lastAcceptedTime = (Timestamp) activityJson.get(LAST_ACCEPTED_TIME);
        }
        if (activityJson.containsKey(LAST_REJECTED_TIME)) {
            m_lastRejectedTime = (Timestamp) activityJson.get(LAST_REJECTED_TIME);
        }
	    if (host != null)
		    m_host = host;
    }

    public ObjectNode toObjectNode(Integer viewerId) {
        ObjectNode ret = Json.newObject();
        try {
            ret.put(ID, String.valueOf(m_id));
            ret.put(TITLE, m_title);
            ret.put(CONTENT, m_content);
            SimpleDateFormat splfmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ret.put(CREATED_TIME, splfmt.format(m_createdTime));
            ret.put(BEGIN_TIME, splfmt.format(m_beginTime));
            ret.put(DEADLINE, splfmt.format(m_deadline));
            ret.put(CAPACITY, String.valueOf(m_capacity));
	    ret.put(NUM_APPLIED, String.valueOf(m_numApplied));
	    ret.put(NUM_SELECTED, String.valueOf(m_numSelected));
            ret.put(HOST, m_host.toObjectNode(viewerId));
            
            if (viewerId == null) return ret;
	    int relation = SQLCommander.queryUserActivityRelation(viewerId, m_id);
	    if (relation != UserActivityRelation.INVALID)	ret.put(UserActivityRelation.RELATION, relation);
	    m_viewer = SQLCommander.queryUser(viewerId);
	    if (viewerId.equals(m_host.getId()))	ret.put(STATUS, String.valueOf(m_status));
	    if (m_viewer != null && m_viewer.getGroupId() == User.ADMIN)	ret.put(STATUS, String.valueOf(m_status));
        } catch (Exception e) {
            Logger.e(TAG, "toObjectNode", e);
        }
        return ret;
    }

    public ObjectNode toObjectNodeWithImages(Integer viewerId) {
	    ObjectNode ret = toObjectNode(viewerId);
	    try {
		    List<Image> images = SQLCommander.queryImages(m_id);
		    if (images == null || images.size() <= 0) return ret;
		    ArrayNode imagesNode = new ArrayNode(JsonNodeFactory.instance);
		    for (Image image : images)	imagesNode.add(image.toObjectNode());
		    ret.put(ActivityDetail.IMAGES, imagesNode);
	    } catch (Exception e) {
		    Logger.e(TAG, "toObjectNodeWithImages", e);
	    }
	    return ret;
    }
};
