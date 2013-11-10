/*
 * This is the base class of user model, please note that 
 * 1. password is not needed to be stored in data model
 * 2. getting/setting password could be done by DAO+Controller
 * 3. m_isAdmin flag should not be set by anyone except this basic class
 *
 * */

package model;

public class BasicUser {
	
	private int m_userId=0;
	public int getUserId() {return m_userId;}
	public void setUserId(int userId) {m_userId=userId;}
	
	private String m_email=null;
	public String getEmail() {return m_email;}
	public void setEmail(String email) {m_email=email;}

	private boolean m_emailIdentity=false;
	public boolean getEmailIdentity() {return m_emailIdentity;}
	public void setEmailIdentity(boolean emailIdentity) {m_emailIdentity=emailIdentity;}

	private boolean m_photoIdentity=false;	
	public boolean getPhotoIdentity() {return m_photoIdentity;}
	public void setPhotoIdentity(boolean photoIdentity) {m_photoIdentity=photoIdentity;}
	
	private boolean m_isAdmin=false;
	protected boolean getIsAdmin() {return m_isAdmin;}
	private void setIsAdmin(boolean isAdmin) {m_isAdmin=isAdmin;} 
	
	public BasicUser(int userId, String email, boolean emailIdentity, boolean photoIdentity, boolean isAdmin){
		setUserId(userId);
		setEmail(email);
		setEmailIdentity(emailIdentity);
		setPhotoIdentity(photoIdentity);
		setIsAdmin(isAdmin);
	}
}
