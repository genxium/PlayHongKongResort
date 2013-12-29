/*
 * This is the base class of user model
 * */

package model;

import org.json.simple.JSONObject;

import play.libs.Json;
import utilities.Converter;

import com.fasterxml.jackson.databind.node.ObjectNode;

import dao.SQLHelper;

public class BasicUser {
	
	public static String idKey="UserId";
	public static String emailKey="UserEmail";
	public static String nameKey="UserName";
	public static String passwordKey="UserPassword";
	public static String groupIdKey="UserGroupId";
	public static String authenticationStatusKey="UserAuthenticationStatus";
	public static String genderKey="UserGender";
	public static String lastLoggedInTimeKey="UserLastLoggedInTime";

	public static String tokenKey="UserToken";
	
	private int m_userId=0;
	public int getUserId() {return m_userId;}
	public void setUserId(int userId) {m_userId=userId;}
	
	private String m_email=null;
	public String getEmail() {return m_email;}
	public void setEmail(String email) {m_email=email;}
	
	private String m_password=null;
	public String getPassword() {return m_password;}
	private void setPassword(String password) {m_password=password;}

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
		m_userId=userId;
		m_email=email;
		m_password=password;
		m_name=name;
		m_emailIdentity=emailIdentity;
		m_photoIdentity=photoIdentity;
		m_isAdmin=isAdmin;
	}
	
	public static BasicUser create(String email, String password, String name){
		BasicUser user=new BasicUser(0, email, password, name, false, false, false);
		return user;
	}
	
	public static BasicUser create(JSONObject jsonObject){
  		int id=(Integer)jsonObject.get(idKey);
  		String email=(String)jsonObject.get(emailKey);
		String password=(String)jsonObject.get(passwordKey);
  		String name=(String)jsonObject.get(nameKey);
  		BasicUser user=new BasicUser(id, email, password, name, false, false, false);
  		return user;
	}
}