package model;

public class BasicUser {
	public static String idKey="UserId";
	public static String emailKey="UserEmail";
	public static String nameKey="UserName";
	public static String avatarKey="UserAvatar";
	
	protected int m_userId=0;
	public int getUserId() {return m_userId;}
	public void setUserId(int userId) {m_userId=userId;}
	
	protected String m_email=null;
	public String getEmail() {return m_email;}
	public void setEmail(String email) {m_email=email;}
	
	protected String m_name=null;
	public String getName() {return m_name;}
	public void setName(String name) {m_name=name;}

	protected int m_avatar=0;
	public int getAvatar() {return m_avatar;}
	public void setAvatar(int avatar) {m_avatar=avatar;}
	
	public BasicUser(int userId, String email, String name, int avatar){
		m_userId=userId;
		m_email=email;
		m_name=name;
		m_avatar=avatar;
	}
}
