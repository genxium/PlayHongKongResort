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
	public static String lastLoggedOutTimeKey="UserLastLoggedOutTime";
	public static String lastExitTimeKey="UserLastExitTime";
	public static String avatarKey="UserAvatar";

	public static String tokenKey="UserToken";
	
	private int m_userId=0;
	public int getUserId() {return m_userId;}
	public void setUserId(int userId) {m_userId=userId;}
	
	private String m_email=null;
	public String getEmail() {return m_email;}
	public void setEmail(String email) {m_email=email;}
	
	private String m_password=null;
	public String getPassword() {return m_password;}
	public void setPassword(String password) {m_password=password;}

	private String m_name=null;
	public String getName() {return m_name;}
	public void setName(String name) {m_name=name;}

	private int m_avatar=0;
	public int getAvatar() {return m_avatar;}
	public void setAvatar(int avatar) {m_avatar=avatar;}

	private UserGroup.GroupType m_userGroup=null;
	public UserGroup.GroupType getUserGroup() {return m_userGroup;}
	public void setUserGroup(UserGroup.GroupType userGroup) {m_userGroup=userGroup;}

	public BasicUser(int userId, String email, String password, String name, UserGroup.GroupType userGroup, int avatar){
		m_userId=userId;
		m_email=email;
		m_password=password;
		m_name=name;
		m_userGroup=userGroup;
		m_avatar=avatar;
	}

	public static BasicUser create(int userId, String email, String password, String name, UserGroup.GroupType userGroup, int avatar){
		BasicUser user=new BasicUser(userId, email, password, name, userGroup, avatar);
		return user;
	}

	public static BasicUser create(int userId, String email, String password, String name, UserGroup.GroupType userGroup){
		return create(userId, email, password, name, userGroup, 0);
	}

	public static BasicUser create(int userId, String email, String password, String name){
		return create(userId, email, password, name, UserGroup.GroupType.visitor);
	}

	public static BasicUser create(String email, String password, String name, UserGroup.GroupType userGroup){
		return create(0, email, password, name, userGroup);
	}
	
	public static BasicUser create(String email, String password, String name){
		return create(0, email, password, name);
	}
	
	public static BasicUser create(JSONObject userJson){
  		int id=(Integer)userJson.get(idKey);
  		String email=(String)userJson.get(emailKey);
		String password=(String)userJson.get(passwordKey);
  		String name=(String)userJson.get(nameKey);
  		int userGroupId=(Integer)userJson.get(groupIdKey);
  		UserGroup.GroupType userGroup=UserGroup.GroupType.getTypeForValue(userGroupId);
  		int avatar=(Integer)userJson.get(avatarKey);
  		return create(id, email, password, name, userGroup, avatar);
	}
}