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

	public static int CREATED=0;
	public static int PENDING=1;
	public static int REJECTED=2;
	public static int ACCEPTED=3;
	public static int EXPIRED=4;

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

	protected Timestamp m_beginTime=null;
	public Timestamp getBeginTime() {return m_beginTime;}
	public void setBeginTime(Timestamp beginTime) {m_beginTime=beginTime;}

	protected Timestamp m_deadline=null;
	public Timestamp getDeadline() {return m_deadline;}
	public void setDeadline(Timestamp deadline) {m_deadline=deadline;}

	protected int m_capacity=0;
	public int getCapacity() {return m_capacity;}

	protected int m_status=CREATED;
	public int getStatus() {return m_status;}
	public void setStatus(int status) {m_status=status;}

	protected int m_hostId=0;
	public int getHostId() {return m_hostId;}

	public boolean isDeadlineExpired(){
		java.util.Date date= new java.util.Date();
		return date.getTime()>m_deadline.getTime();
	}

	public boolean hasBegun(){
		java.util.Date date= new java.util.Date();
		return date.getTime()>m_beginTime.getTime();
	}

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
				m_status=(Integer)activityJson.get(STATUS);
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
				if(viewerId.equals(m_hostId)) ret.put(Activity.STATUS, String.valueOf(m_status));
				if(user!=null && user.getGroupId()==User.ADMIN) ret.put(Activity.STATUS, String.valueOf(m_status));
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
