package model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.tools.corba.se.idl.constExpr.Times;
import org.json.simple.JSONObject;
import play.libs.Json;

import java.sql.Timestamp;
import java.util.*;

public class Activity {

	public static String idKey="ActivityId";
	public static String titleKey="ActivityTitle";
	public static String contentKey="ActivityContent";
	public static String createdTimeKey="ActivityCreatedTime";
	public static String beginTimeKey="ActivityBeginTime";
	public static String deadlineKey="ActivityApplicationDeadline";
	public static String capacityKey="ActivityCapacity";
	public static String statusKey="ActivityStatus";
    public static String imagesKey="ActivityImages";

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

    protected List<Image> m_images=null;
    public List<Image> getImages() {return m_images;}
    public void setImages(List<Image> images){
        if(m_images!=null){
            m_images.clear();
        } else{
            m_images=new ArrayList<Image>();
        }
        Iterator<Image> it=images.iterator();
        while(it.hasNext()){
            Image image=it.next();
            m_images.add(image);
        }
    }

    public Activity(JSONObject activityJson){
        do{
            try{
                m_id=(Integer)activityJson.get(idKey);
                m_title=(String)activityJson.get(titleKey);
                m_content=(String)activityJson.get(contentKey);
                m_createdTime=(Timestamp)activityJson.get(createdTimeKey);
                m_beginTime=(Timestamp)activityJson.get(beginTimeKey);
                m_deadline=(Timestamp)activityJson.get(deadlineKey);
                m_capacity=(Integer)activityJson.get(capacityKey);
                m_status=StatusType.getTypeForValue((Integer)activityJson.get(statusKey));
            }catch(Exception e){

            }
        }while(false);
    }

	public Activity(int id, String title, String content, Timestamp createdTime, Timestamp beginTime, Timestamp deadline, int capacity, StatusType status){
		m_id=id;
		m_title=title;
		m_content=content;
		m_createdTime=createdTime;
		m_beginTime=beginTime;
		m_deadline=deadline;
		m_capacity=capacity;
		m_status=status;
	}

	public static Activity create(String title, String content, Timestamp createdTime, Timestamp beginTime, Timestamp deadline, int capacity){
		StatusType status=StatusType.created;
		Activity activity=new Activity(0, title, content, createdTime, beginTime, deadline, capacity, status);
		return activity;
	}

	public static Activity create(){
		java.util.Date date= new java.util.Date();
		Timestamp currentTime=new Timestamp(date.getTime());
		int capacity=0;
		return create("", "", currentTime, currentTime, currentTime, capacity);
	}

    public ObjectNode toObjectNode(){
        ObjectNode ret = null;
        do{
            ret= Json.newObject();

            ret.put(Activity.idKey, String.valueOf(m_id));
            ret.put(Activity.titleKey, m_title);
            ret.put(Activity.contentKey, m_content);
            ret.put(Activity.createdTimeKey, m_createdTime.toString());
            ret.put(Activity.beginTimeKey, m_beginTime.toString());
            ret.put(Activity.deadlineKey, m_deadline.toString());
            ret.put(Activity.capacityKey, String.valueOf(m_capacity));
            ret.put(Activity.statusKey, String.valueOf(m_status));

            if(m_images!=null && m_images.size()>0){
                ArrayNode imagesNode=new ArrayNode(JsonNodeFactory.instance);
                Iterator<Image> itImage=m_images.iterator();
                while(itImage.hasNext()){
                    ObjectNode singleImageNode=Json.newObject();
                    Image image=itImage.next();
                    Integer imageId=image.getImageId();
                    String imageURL=image.getImageURL();
                    singleImageNode.put(Image.idKey, imageId);
                    singleImageNode.put(Image.urlKey, imageURL);
                    imagesNode.add(singleImageNode);
                }
                ret.put(ActivityDetail.imagesKey, imagesNode);
            }
        }while(false);
        return ret;
    }
};