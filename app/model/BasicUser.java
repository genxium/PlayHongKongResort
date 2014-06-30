package model;

import org.json.simple.JSONObject;

public class BasicUser {
	public static String ID ="id";
	public static String EMAIL ="email";
	public static String NAME ="name";
	public static String AVATAR ="avatar";
	
	protected int m_Id =0;
	public int getId() {return m_Id;}
	public void setId(int id) {m_Id =id;}
	
	protected String m_email=null;
	public String getEmail() {return m_email;}
	public void setEmail(String email) {m_email=email;}
	
	protected String m_name=null;
	public String getName() {return m_name;}
	public void setName(String name) {m_name=name;}

	protected int m_avatar=0;
	public int getAvatar() {return m_avatar;}
	public void setAvatar(int avatar) {m_avatar=avatar;}
	
	public BasicUser(String email, String name){
		m_email=email;
		m_name=name;
	}

	public BasicUser(JSONObject userJson){
		if(userJson.containsKey(ID)) m_Id =(Integer)userJson.get(ID);
		if(userJson.containsKey(NAME)) m_name=(String)userJson.get(NAME);
		if(userJson.containsKey(EMAIL)) m_email=(String)userJson.get(EMAIL);
		if(userJson.containsKey(AVATAR)) m_avatar=(Integer)userJson.get(AVATAR);
	}
}
