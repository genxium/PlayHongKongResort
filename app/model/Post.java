package model;

import java.sql.Timestamp;
import java.util.Date;

public class Post {
	
	private int m_postId=0;
	public int getId() {return m_postId;}
	public void setId(int postId) {m_postId=postId;}
	
	private String m_content=null;
	public String getEmail() {return m_content;}
	public void setEmail(String email) {m_content=m_content;}

	private Timestamp m_postTime=null;
	public Timestamp getPostTime() {return m_postTime;}
	public void setPostTime(Timestamp postTime) {m_postTime=postTime;}

};