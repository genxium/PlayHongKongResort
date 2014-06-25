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

	public static String TABLE ="Activity";
	public static String ID ="ActivityId";
	public static String TITLE="ActivityTitle";
	public static String CONTENT ="ActivityContent";
	public static String CREATED_TIME ="ActivityCreatedTime";
	public static String BEGIN_TIME ="ActivityBeginTime";
	public static String DEADLINE ="ActivityApplicationDeadline";
	public static String CAPACITY ="ActivityCapacity";
	public static String STATUS ="ActivityStatus";
	public static String HOST_ID = "HostId";

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
		m_id=(Integer)activityJson.get(ID);
		m_title=(String)activityJson.get(TITLE);
		m_content=(String)activityJson.get(CONTENT);
		m_createdTime=(Timestamp)activityJson.get(CREATED_TIME);
		m_beginTime=(Timestamp)activityJson.get(BEGIN_TIME);
		m_deadline=(Timestamp)activityJson.get(DEADLINE);
		m_capacity=(Integer)activityJson.get(CAPACITY);
		m_status=StatusType.getTypeForValue((Integer)activityJson.get(STATUS));
		m_hostId=(Integer)activityJson.get(HOST_ID);
	}catch(Exception e){

	}
    }

    public ObjectNode toObjectNode(Integer viewerId){
        ObjectNode ret = Json.newObject();;
	ret.put(Activity.ID, String.valueOf(m_id));
	ret.put(Activity.TITLE, m_title);
	ret.put(Activity.CONTENT, m_content);
	ret.put(Activity.CREATED_TIME, m_createdTime.toString());
	ret.put(Activity.BEGIN_TIME, m_beginTime.toString());
	ret.put(Activity.DEADLINE, m_deadline.toString());
	ret.put(Activity.CAPACITY, String.valueOf(m_capacity));
	ret.put(Activity.HOST_ID, String.valueOf(m_hostId));
	if(viewerId!=null && viewerId.equals(m_hostId)) { 
		// only host can view the status of an activity
		ret.put(Activity.STATUS, String.valueOf(m_status.ordinal()));		
	}
        return ret;
    }

    public ObjectNode toObjectNodeWithImages(Integer viewerId){
        ObjectNode ret = toObjectNode(viewerId);
        do{
            List<Image> images=SQLCommander.queryImages(m_id);
            if(images==null || images.size()<=0) break;
            ArrayNode imagesNode=new ArrayNode(JsonNodeFactory.instance);
            for(Image image : images){
                imagesNode.add(image.toObjectNode());
            }
            ret.put(ActivityDetail.IMAGES, imagesNode);
        }while(false);
        return ret;
    }

    public ObjectNode toObjectNodeWithImagesAndRelation(Integer viewerId){
	/*
		Note that when the activity is queried with a valid user token
		1. status is only shown when the user is the owner of the activity
		2. relation is only shown when relation is specified
 	*/
        ObjectNode ret=toObjectNodeWithImages(viewerId);
        do{
		int relation=SQLCommander.queryUserActivityRelation(viewerId, m_id);
		if(relation==UserActivityRelationTable.invalid) break;
		ret.put(UserActivityRelationTable.RELATION, relation);
        }while(false);
        return ret;
    }
};
