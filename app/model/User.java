package model;

public class User {
	
	private int m_userId=0;
	public int getId() {return m_userId;}
	public void setId(int userId) {m_userId=userId;}
	
	private String m_email=null;
	public String getEmail() {return m_email;}
	public void setEmail(String email) {m_email=email;}

	private boolean m_emailIdentity=false;
	public boolean getEmailIdentity() {return m_emailIdentity;}
	public void setEmailIdentity(boolean emailIdentity) {m_emailIdentity=emailIdentity;}

	private boolean m_photoIdentity=false;	
	public boolean getPhotoIdentity() {return m_photoIdentity;}
	public void setPhotoIdentity(boolean photoIdentity) {m_photoIdentity=photoIdentity;}
	
};