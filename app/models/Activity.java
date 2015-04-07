package models;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.DBCommander;
import org.json.simple.JSONObject;
import utilities.Converter;
import utilities.General;
import utilities.Loggy;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Activity extends AbstractSimpleMessage {

	public static final String TAG = Activity.class.getName();

	public static final Pattern TITLE_PATTERN = Pattern.compile(".{5,64}", Pattern.UNICODE_CHARACTER_CLASS);
	public static final Pattern ADDR_PATTERN = Pattern.compile(".{5,128}", Pattern.UNICODE_CHARACTER_CLASS);
	public static final Pattern CONTENT_PATTERN = Pattern.compile("[\\s\\S]{15,1024}", Pattern.UNICODE_CHARACTER_CLASS);

	public static final int CREATED = 0;
	public static final int PENDING = 1;
	public static final int REJECTED = 2;
	public static final int ACCEPTED = 3;

	public static final int CREATION_CRITICAL_NUMBER = 2;
	public static final int CREATION_CRITICAL_TIME_INTERVAL_MILLIS = 43200000; // 12 hours 

	public static final String TABLE = "activity";
	public static final String TITLE = "title";
	public static final String ADDRESS = "address";
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

	public static String SELECTED_PARTICIPANTS = "selected_participants";

	public static String IMAGES = "images";

	public static final String ACTIVITIES = "activities";

	public static String[] QUERY_FIELDS = {Activity.ID, Activity.TITLE, Activity.ADDRESS, Activity.CONTENT, Activity.CREATED_TIME, Activity.BEGIN_TIME, Activity.DEADLINE, Activity.CAPACITY, Activity.NUM_APPLIED, NUM_SELECTED, Activity.STATUS, Activity.HOST_ID};
	public static final int MAX_APPLIED = 500;
	public static final int MAX_SELECTED = 250;

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
	public void setCreatedTime(final long createdTime) {
		m_createdTime = createdTime;
	}

	protected Long m_beginTime = null;

	public long getBeginTime() {
		return m_beginTime;
	}

	public void setBeginTime(final long beginTime) {
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

	public void setLastAcceptedTime(final long time) {
		m_lastAcceptedTime = time;
	}

	protected Long m_lastRejectedTime = null;

	public void setLastRejectedTime(final long time) {
		m_lastRejectedTime = time;
	}

	protected Integer m_capacity = 0;

	public int getCapacity() {
		return m_capacity;
	}

	protected Integer m_numApplied = 0;

	public int getNumApplied() {
		return m_numApplied;
	}

	public boolean exceededAppliedLimit() {
		return m_numApplied > MAX_APPLIED;
	}

	protected Integer m_numSelected = 0;

	public int getNumSelected() {
		return m_numSelected;
	} 

	public boolean exceededSelectedLimit() {
		return m_numSelected > MAX_SELECTED;	
	}

	protected Integer m_status = CREATED;

	public int getStatus() {
		return m_status;
	}

	public void setStatus(final int status) {
		m_status = status;
	}

	protected String m_address = null;	

	public String getAddress() {
		return m_address;
	}

	public void setAddress(final String address) {
		m_address = address;
	}

	protected Long m_hostId = null;
	public Long getHostId() {
		return m_hostId;
	}

	protected User m_host = null;

	public User getHost() {
		return m_host;
	}

	public void setHost(User host) {
		m_host = host;
	}

	protected User m_viewer = null;

	public User getViewer() {
		return m_viewer;
	}

	public void setViewer(final User viewer) {
		m_viewer = viewer;
	}

	protected List<Image> m_imageList = null;
	public void setImageList(final List<Image> imageList) {
		m_imageList = imageList;
	}
	public void addImage(final Image image) {
		if (m_imageList == null) m_imageList = new ArrayList<>();
		m_imageList.add(image);
	}

	public boolean isDeadlineExpired() {
		return General.millisec() > m_deadline;
	}

	public boolean hasBegun() {
		return General.millisec() > m_beginTime;
	}

	protected List<BasicUser> m_selectedParticipants = null;
	public void setSelectedParticipants(final List<BasicUser> selectedParticipants) {
		m_selectedParticipants = selectedParticipants;
	}
	public void addSelectedParticipant(final BasicUser user) {
		if (m_selectedParticipants == null) m_selectedParticipants = new ArrayList<>();
		m_selectedParticipants.add(user);
	}

	public Activity() {
		super();
	}

	public Activity(JSONObject activityJson) {
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

		if (activityJson.containsKey(HOST_ID))
			m_hostId = Converter.toLong(activityJson.get(HOST_ID));
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
			if (m_host != null) ret.put(HOST, m_host.toObjectNode(viewerId));

			if (m_imageList != null && m_imageList.size() > 0) {
				ArrayNode imagesNode = new ArrayNode(JsonNodeFactory.instance);
				for (Image image : m_imageList)	imagesNode.add(image.toObjectNode());
				ret.put(ActivityDetail.IMAGES, imagesNode);
			}

			if (m_selectedParticipants != null && m_selectedParticipants.size() > 0) {
				ArrayNode selectedParticipantsNode = new ArrayNode(JsonNodeFactory.instance);
				for (BasicUser participant : m_selectedParticipants)	selectedParticipantsNode.add(participant.toObjectNode(viewerId));
				ret.put(SELECTED_PARTICIPANTS, selectedParticipantsNode);
			}

			if (viewerId == null) return ret;
			int relation = DBCommander.queryUserActivityRelation(viewerId, m_id);
			if (relation != UserActivityRelation.INVALID)	ret.put(UserActivityRelation.RELATION, relation);
			if (viewerId.equals(m_host.getId()))	ret.put(STATUS, String.valueOf(m_status));
			if (m_viewer != null && m_viewer.getGroupId() == User.ADMIN)	ret.put(STATUS, String.valueOf(m_status));

		} catch (Exception e) {
			Loggy.e(TAG, "toObjectNode", e);
		}
		return ret;
	}
}
