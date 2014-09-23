package models;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SQLCommander;
import org.json.simple.JSONObject;
import play.libs.Json;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.List;

public class Activity {

    public static final int CREATED = 0;
    public static final int PENDING = 1;
    public static final int REJECTED = 2;
    public static final int ACCEPTED = 3;
    public static final int EXPIRED = 4;

    public static final String TABLE = "activity";
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String CREATED_TIME = "created_time";
    public static final String BEGIN_TIME = "begin_time";
    public static final String DEADLINE = "application_deadline";
    public static final String CAPACITY = "capacity";
    public static final String STATUS = "status";
    public static final String HOST_ID = "host_id";
    public static final String HOST = "host";
    public static final String VIEWER = "viewer";

    public static final String COUNT = "count";
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

    protected int m_capacity = 0;

    public int getCapacity() {
	    return m_capacity;
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
	    Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8")); // should match mysql setting `/etc/my.cnf`
        long milisecs = calendar.getTimeInMillis();
	    return  milisecs > m_deadline.getTime();
    }

    public boolean hasBegun() {
	    Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8")); // should match mysql setting `/etc/my.cnf`
        long milisecs = calendar.getTimeInMillis();
	    return milisecs > m_beginTime.getTime();
    }

    protected Activity() {

    }

    public Activity(JSONObject activityJson, User host) {
	    if (activityJson.containsKey(ID))
		    m_id = (Integer) activityJson.get(ID);
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
		    m_capacity = (Integer) activityJson.get(CAPACITY);
	    if (activityJson.containsKey(STATUS))
		    m_status = (Integer) activityJson.get(STATUS);
	    if (host != null)
		    m_host = host;
    }

    public ObjectNode toObjectNode(Integer viewerId) {
        ObjectNode ret = Json.newObject();
        try {
            ret.put(Activity.ID, String.valueOf(m_id));
            ret.put(Activity.TITLE, m_title);
            ret.put(Activity.CONTENT, m_content);
            SimpleDateFormat splfmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ret.put(Activity.CREATED_TIME, splfmt.format(m_createdTime));
            ret.put(Activity.BEGIN_TIME, splfmt.format(m_beginTime));
            ret.put(Activity.DEADLINE, splfmt.format(m_deadline));
            ret.put(Activity.CAPACITY, String.valueOf(m_capacity));
            ret.put(Activity.HOST, m_host.toObjectNode(viewerId));
            if (viewerId == null) return ret;
	    int relation = SQLCommander.queryUserActivityRelation(viewerId, m_id);
	    if (relation != UserActivityRelation.invalid)	ret.put(UserActivityRelation.RELATION, relation);
	    m_viewer = SQLCommander.queryUser(viewerId);
	    if (viewerId.equals(m_host.getId()))	ret.put(Activity.STATUS, String.valueOf(m_status));
	    if (m_viewer != null && m_viewer.getGroupId() == User.ADMIN)	ret.put(Activity.STATUS, String.valueOf(m_status));
        } catch (Exception e) {
            System.out.println(Activity.class.getName() + ".toObjectNode, " + e.getMessage());
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
		    System.out.println(Activity.class.getName() + ".toObjectNodeWithImages, " + e.getMessage());
	    }
	    return ret;
    }
};
