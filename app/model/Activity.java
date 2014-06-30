package model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SQLCommander;
import org.json.simple.JSONObject;
import play.libs.Json;

import java.sql.Timestamp;
import java.util.*;

public class Activity {

	public static String TABLE ="activity";
	public static String ID ="id";
	public static String TITLE="title";
	public static String CONTENT ="content";
	public static String CREATED_TIME ="created_time";
	public static String BEGIN_TIME ="begin_time";
	public static String DEADLINE ="application_deadline";
	public static String CAPACITY ="capacity";
	public static String STATUS ="status";
	public static String HOST_ID = "host_id";

	public enum StatusType{
		created(0),
		pending(1),
		rejected(2),
		accepted(3),
		expired(4);

		private static final Map<Integer, StatusType> statusLookUpMap = new HashMap<Integer, StatusType>();

	    static {
	        for (StatusType type : StatusType.values()) {
	            statusLookUpMap.put(type.value, type);
	        }
	    }

	    private final int value;

	    private StatusType(int value) {
	        this.value = value;
	    }

	    public static StatusType getTypeForValue(int value) {
	        return statusLookUpMap.get(value);
	    }
	};

	protected int m_id=0;
	public int getId() {return m_id;}
	public void setId(int id) {m_id=id;}

	protected String m_title=null;
	public String getTitle() {return m_title;}
	public void setTitle(String title) {m_title=title;}

	protected String m_content=null;
	public String getContent() {return m_content;}
	public void setContent(String content) {m_content=content;}

	protected Timestamp m_createdTime=null;
	public Timestamp getCreatedTime() {return m_createdTime;}
	public void setCreatedTime(Timestamp createdTime) {m_createdTime=createdTime;}

	protected Timestamp m_beginTime=null;
	public Timestamp getBeginTime() {return m_beginTime;}
	public void setBeginTime(Timestamp beginTime) {m_beginTime=beginTime;}

	protected Timestamp m_deadline=null;
	public Timestamp getDeadline() {return m_deadline;}
	public void setDeadline(Timestamp deadline) {m_deadline=deadline;}

	protected int m_capacity=0;
	public int getCapacity() {return m_capacity;}
	public void setCapacity(int capacity) {m_capacity=capacity;}

	protected StatusType m_status=StatusType.created;
	public StatusType getStatus() {return m_status;}
	public void setStatus(StatusType status) {m_status=status;}

	protected int m_hostId=0;
	public int getHostId() {return m_hostId;}
	public void setHostId(int hostId) {m_hostId=hostId;}

	protected Activity(){

	}

	public Activity(JSONObject activityJson){
		try{
			if(activityJson.containsKey(ID))
				m_id=(Integer)activityJson.get(ID);
			if(activityJson.containsKey(TITLE))
				m_title=(String)activityJson.get(TITLE);
			if(activityJson.containsKey(CONTENT))
				m_content=(String)activityJson.get(CONTENT);
			if(activityJson.containsKey(CREATED_TIME))
				m_createdTime=(Timestamp)activityJson.get(CREATED_TIME);
			if(activityJson.containsKey(BEGIN_TIME))
				m_beginTime=(Timestamp)activityJson.get(BEGIN_TIME);
			if(activityJson.containsKey(DEADLINE))
				m_deadline=(Timestamp)activityJson.get(DEADLINE);
			if(activityJson.containsKey(CAPACITY))
				m_capacity=(Integer)activityJson.get(CAPACITY);
			if(activityJson.containsKey(STATUS))
				m_status=StatusType.getTypeForValue((Integer)activityJson.get(STATUS));
			if(activityJson.containsKey(HOST_ID))
				m_hostId=(Integer)activityJson.get(HOST_ID);
		}catch(Exception e){
			System.out.println("Activity, "+e.getMessage());
		}
	}

	public ObjectNode toObjectNode(Integer viewerId){
		ObjectNode ret = Json.newObject();;
		try{
			ret.put(Activity.ID, String.valueOf(m_id));
			ret.put(Activity.TITLE, m_title);
			ret.put(Activity.CONTENT, m_content);
			ret.put(Activity.CREATED_TIME, m_createdTime.toString());
			ret.put(Activity.BEGIN_TIME, m_beginTime.toString());
			ret.put(Activity.DEADLINE, m_deadline.toString());
			ret.put(Activity.CAPACITY, String.valueOf(m_capacity));
			ret.put(Activity.HOST_ID, String.valueOf(m_hostId));
			if(viewerId!=null) { 
				int relation=SQLCommander.queryUserActivityRelation(viewerId, m_id);
				if(relation!= UserActivityRelation.invalid)	ret.put(UserActivityRelation.RELATION, relation);
				User user=SQLCommander.queryUser(viewerId);
				if(viewerId.equals(m_hostId)) ret.put(Activity.STATUS, String.valueOf(m_status.ordinal()));	
				if(user!=null && user.getGroupId()==User.ADMIN) ret.put(Activity.STATUS, String.valueOf(m_status.ordinal())); // 3 is temporary hard-coded 
			}
		} catch (Exception e){
			System.out.println("Activity.toObjectNode, "+e.getMessage());
		}
		return ret;
	}

	public ObjectNode toObjectNodeWithImages(Integer viewerId){
		ObjectNode ret = toObjectNode(viewerId);
		do{	
			try{
				List<Image> images=SQLCommander.queryImages(m_id);
				if(images==null || images.size()<=0) break;
				ArrayNode imagesNode=new ArrayNode(JsonNodeFactory.instance);
				for(Image image : images){
					imagesNode.add(image.toObjectNode());
				}
				ret.put(ActivityDetail.IMAGES, imagesNode);
			} catch (Exception e){
				System.out.println("Activity.toObjectNodeWithImages, "+e.getMessage());	
			}
		}while(false);
		return ret;
	}
};
