package models;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SQLCommander;
import org.json.simple.JSONObject;
import utilities.Converter;
import utilities.General;
import utilities.Loggy;

import java.util.List;

public class Activity extends AbstractSimpleMessage {

    public static final String TAG = Activity.class.getName();

    public static final int CREATED = 0;
    public static final int PENDING = 1;
    public static final int REJECTED = 2;
    public static final int ACCEPTED = 3;

    public static final String TABLE = "activity";
    public static final String TITLE = "title";
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

	public static final String ADDRESS = "address";

    public static final String ACTIVITIES = "activities";

    protected String m_title = null;

    public String getTitle() {
	    return m_title;
    }

    public void setTitle(final String title) {
	    m_title = title;
    }
	
    protected Long m_createdTime = null;

    public long getCreatedTime() {
	    return m_createdTime;
    }

    protected Long m_beginTime = null;

    public long getBeginTime() {
	    return m_beginTime;
    }

    public void setBeginTime(long beginTime) {
	    m_beginTime = beginTime;
    }

    protected Long m_deadline = null;

    public long getDeadline() {
	    return m_deadline;
    }

    public void setDeadline(long deadline) {
	    m_deadline = deadline;
    }

    protected Long m_lastAcceptedTime = null;

    public void setLastAcceptedTime(long time) {
        m_lastAcceptedTime = time;
    }

    protected Long m_lastRejectedTime = null;

    public void setLastRejectedTime(long time) {
        m_lastRejectedTime = time;
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

	protected String m_address = null;	
	public String getAddress() {
		return m_address;
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
	    return General.millisec() > m_deadline;
    }

    public boolean hasBegun() {
	    return General.millisec() > m_beginTime;
    }

    public Activity() {
        super();
    }

    public Activity(JSONObject activityJson, User host) {
        super(activityJson);

        if (activityJson.containsKey(TITLE))
		    m_title = (String) activityJson.get(TITLE);

        if (activityJson.containsKey(CREATED_TIME))
		    m_createdTime = Converter.toLong(activityJson.get(CREATED_TIME));

        if (activityJson.containsKey(BEGIN_TIME))
		    m_beginTime = Converter.toLong(activityJson.get(BEGIN_TIME));

        if (activityJson.containsKey(DEADLINE))
		    m_deadline = Converter.toLong(activityJson.get(DEADLINE));

        if (activityJson.containsKey(CAPACITY))
		    m_capacity = Converter.toInteger(activityJson.get(CAPACITY));

        if (activityJson.containsKey(NUM_APPLIED))
		    m_numApplied = Converter.toInteger(activityJson.get(NUM_APPLIED));

        if (activityJson.containsKey(NUM_SELECTED))
	        m_numSelected = Converter.toInteger(activityJson.get(NUM_SELECTED));

	    if (activityJson.containsKey(STATUS))
		    m_status = Converter.toInteger(activityJson.get(STATUS));

        if (activityJson.containsKey(LAST_ACCEPTED_TIME))
            m_lastAcceptedTime = Converter.toLong(activityJson.get(LAST_ACCEPTED_TIME));

        if (activityJson.containsKey(LAST_REJECTED_TIME))
            m_lastRejectedTime = Converter.toLong(activityJson.get(LAST_REJECTED_TIME));

		if (activityJson.containsKey(ADDRESS)) 
			m_address = (String) activityJson.get(ADDRESS);

	    if (host != null)
		    m_host = host;

    }

    public ObjectNode toObjectNode(Long viewerId) {
        ObjectNode ret = super.toObjectNode();
        try {
            ret.put(TITLE, m_title);
			ret.put(ADDRESS, m_address);

            ret.put(CREATED_TIME, String.valueOf(m_createdTime));
            ret.put(BEGIN_TIME, String.valueOf(m_beginTime));
            ret.put(DEADLINE, String.valueOf(m_deadline));

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
            Loggy.e(TAG, "toObjectNode", e);
        }
        return ret;
    }

    public ObjectNode toObjectNodeWithImages(Long viewerId) {
	    ObjectNode ret = this.toObjectNode(viewerId);
	    try {
		    List<Image> images = SQLCommander.queryImages(m_id);
		    if (images == null || images.size() <= 0) return ret;
		    ArrayNode imagesNode = new ArrayNode(JsonNodeFactory.instance);
		    for (Image image : images)	imagesNode.add(image.toObjectNode());
		    ret.put(ActivityDetail.IMAGES, imagesNode);
	    } catch (Exception e) {
		    Loggy.e(TAG, "toObjectNodeWithImages", e);
	    }
	    return ret;
    }
}
