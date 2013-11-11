/*
 * This is the base class of user model
 * */

package model;

import play.libs.Json;
import utilities.Converter;

import com.fasterxml.jackson.databind.node.ObjectNode;

import dao.SQLHelper;

public class BasicUser {
	
	private int m_userId=0;
	public int getUserId() {return m_userId;}
	public void setUserId(int userId) {m_userId=userId;}
	
	private String m_email=null;
	public String getEmail() {return m_email;}
	public void setEmail(String email) {m_email=email;}
	
	private String m_password=null;
	public String getPassword() {return m_password;}
	private void setPassword(String password) {m_password=Converter.md5(password);}

	private String m_name=null;
	public String getName() {return m_name;}
	public void setName(String name) {m_name=name;}

	private boolean m_emailIdentity=false;
	public boolean getEmailIdentity() {return m_emailIdentity;}
	public void setEmailIdentity(boolean emailIdentity) {m_emailIdentity=emailIdentity;}

	private boolean m_photoIdentity=false;	
	public boolean getPhotoIdentity() {return m_photoIdentity;}
	public void setPhotoIdentity(boolean photoIdentity) {m_photoIdentity=photoIdentity;}
	
	private boolean m_isAdmin=false;
	protected boolean getIsAdmin() {return m_isAdmin;}
	private void setIsAdmin(boolean isAdmin) {m_isAdmin=isAdmin;} 
	
	public BasicUser(int userId, String email, String password, String name, boolean emailIdentity, boolean photoIdentity, boolean isAdmin){
		setUserId(userId);
		setEmail(email);
		setPassword(password);
		setName(name);
		setEmailIdentity(emailIdentity);
		setPhotoIdentity(photoIdentity);
		setIsAdmin(isAdmin);
	}
}