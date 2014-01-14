package model;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Activity {

	public static String idKey="ActivityId";
	public static String titleKey="ActivityTitle";
	public static String contentKey="ActivityContent";
	public static String createdTimeKey="ActivityCreatedTime";
	public static String beginTimeKey="ActivityBeginTime";
	public static String deadlineKey="ActivityApplicationDeadline";
	public static String capacityKey="ActivityCapacity";
	public static String statusKey="ActivityStatus";

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
	
	private int m_id=0;
	public int getId() {return m_id;}
	public void setId(int id) {m_id=id;}

	private String m_title=null;
	public String getTitle() {return m_title;}
	public void setTitle(String title) {m_title=title;}
	
	private String m_content=null;
	public String getContent() {return m_content;}
	public void setContent(String content) {m_content=content;}

	private Timestamp m_createdTime=null;
	public Timestamp getCreatedTime() {return m_createdTime;}
	public void setCreatedTime(Timestamp createdTime) {m_createdTime=createdTime;}

	private Timestamp m_beginTime=null;
	public Timestamp getBeginTime() {return m_beginTime;}
	public void setBeginTime(Timestamp beginTime) {m_beginTime=beginTime;}

	private Timestamp m_deadline=null; 
	public Timestamp getDeadline() {return m_deadline;}
	public void setDeadline(Timestamp deadline) {m_deadline=deadline;}
	
	private int m_capacity=0;
	public int getCapacity() {return m_capacity;}
	public void setCapacity(int capacity) {m_capacity=capacity;}
	
	private StatusType m_status=StatusType.created;
	public StatusType getStatus() {return m_status;}
	public void setStatus(StatusType status) {m_status=status;}
	
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
	
	public static Activity create(String title, String content){
		java.util.Date date= new java.util.Date();
		Timestamp currentTime=new Timestamp(date.getTime());
		Timestamp createdTime=currentTime;
		Timestamp beginTime=currentTime;
		Timestamp deadline=currentTime;
		int capacity=0;
		StatusType status=StatusType.created;
		Activity activity=new Activity(0, title, content, createdTime, beginTime, deadline, capacity, status);
		return activity;
	}

	public static Activity create(){
		return create("","");
	}
};