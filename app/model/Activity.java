package model;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Activity {

	public static String idKey="ActivityId";
	public static String titleKey="ActivityTitle";
	public static String contentKey="ActivityContent";
	public static String createdTimeKey="ActivityCreatedTime";
	public static String beginDateKey="ActivityBeginDate";
	public static String endDateKey="ActivityEndDate";
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

	private Timestamp m_beginDate=null;
	public Timestamp getBeginDate() {return m_beginDate;}
	public void setBeginDate(Timestamp beginDate) {m_beginDate=beginDate;}

	private Timestamp m_endDate=null; 
	public Timestamp getEndDate() {return m_endDate;}
	public void setEndDate(Timestamp endDate) {m_endDate=endDate;}
	
	private int m_capacity=0;
	public int getCapacity() {return m_capacity;}
	public void setCapacity(int capacity) {m_capacity=capacity;}
	
	private StatusType m_status=StatusType.created;
	public StatusType getStatus() {return m_status;}
	public void setStatus(StatusType status) {m_status=status;}
	
	public Activity(int id, String title, String content, Timestamp createdTime, Timestamp beginDate, Timestamp endDate, int capacity, StatusType status){
		m_id=id;
		m_title=title;
		m_content=content;
		m_createdTime=createdTime;
		m_beginDate=beginDate;
		m_endDate=endDate;
		m_capacity=capacity;
		m_status=status;
	}
	
	public static Activity create(String title, String content){
		java.util.Date date= new java.util.Date();
		Timestamp currentTime=new Timestamp(date.getTime());
		Timestamp createdTime=currentTime;
		Timestamp beginDate=currentTime;
		Timestamp endDate=currentTime;
		int capacity=0;
		StatusType status=StatusType.created;
		Activity activity=new Activity(0, title, content, createdTime, beginDate, endDate, capacity, status);
		return activity;
	}

	public static Activity create(){
		return create("","");
	}
};