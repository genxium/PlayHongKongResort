package model;

import java.sql.Timestamp;
import java.util.Date;

public class Activity {
	
	private int m_activityId=0;
	public int getId() {return m_activityId;}
	public void setId(int activityId) {m_activityId=activityId;}

	private String m_name=null;
	public String getName() {return m_name;}
	public void setName(String name) {m_name=name;}
	
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
};