package model;

import java.sql.Timestamp;

public class Activity {

	public static String idKey="ActivityId";
	public static String titleKey="ActivityTitle";
	public static String contentKey="ActivityContent";
	public static String createdTimeKey="ActivityCreatedTime";
	public static String beginDateKey="ActivityBeginDate";
	public static String endDateKey="ActivityEndDate";
	public static String capacityKey="ActivityCapacity";
	public static String statusKey="ActivityStatus";
	
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
	
	private int m_status=0;
	public int getStatus() {return m_status;}
	public void setStatus(int status) {m_status=status;}
	
	public Activity(int id, String title, String content, Timestamp createdTime, Timestamp beginDate, Timestamp endDate, int capacity, int status){
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
		Timestamp createdTime=new Timestamp(date.getTime());
		Timestamp beginDate=new Timestamp(date.getTime());
		Timestamp endDate=new Timestamp(date.getTime());
		int capacity=0;
		int status=0;
		Activity activity=new Activity(0, title, content, createdTime, beginDate, endDate, capacity, status);
		return activity;
	}
};