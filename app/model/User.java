/*
 * This is the base class of user model
 * */

package model;

import org.json.simple.JSONObject;

import play.libs.Json;
import utilities.Converter;

import com.fasterxml.jackson.databind.node.ObjectNode;

import dao.SQLHelper;

public class User extends BasicUser {
	
	public static String passwordKey="UserPassword";
	public static String groupIdKey="UserGroupId";
	public static String authenticationStatusKey="UserAuthenticationStatus";
	public static String genderKey="UserGender";
	public static String lastLoggedInTimeKey="UserLastLoggedInTime";
	public static String lastLoggedOutTimeKey="UserLastLoggedOutTime";
	public static String lastExitTimeKey="UserLastExitTime";
	
	public static String tokenKey="UserToken";
	public static String VERIFICATION_CODE="VerificationCode"; 
	
	protected String m_password=null;
	public String getPassword() {return m_password;}
	public void setPassword(String password) {m_password=password;}

	protected UserGroup.GroupType m_userGroup=null;
	public UserGroup.GroupType getUserGroup() {return m_userGroup;}
	public void setUserGroup(UserGroup.GroupType userGroup) {m_userGroup=userGroup;}

	public User(int userId, String email, String password, String name, UserGroup.GroupType userGroup, int avatar){
		super(userId, email, name, avatar);
		m_password=password;
		m_userGroup=userGroup;
	}

	public static User create(int userId, String email, String password, String name, UserGroup.GroupType userGroup, int avatar){
		User user=new User(userId, email, password, name, userGroup, avatar);
		return user;
	}

	public static User create(int userId, String email, String password, String name, UserGroup.GroupType userGroup){
		return create(userId, email, password, name, userGroup, 0);
	}

	public static User create(int userId, String email, String password, String name){
		return create(userId, email, password, name, UserGroup.GroupType.visitor);
	}

	public static User create(String email, String password, String name, UserGroup.GroupType userGroup){
		return create(0, email, password, name, userGroup);
	}
	
	public static User create(String email, String password, String name){
		return create(0, email, password, name);
	}
	
	public static User create(JSONObject userJson){
  		int id=(Integer)userJson.get(idKey);
  		String name=(String)userJson.get(nameKey);
  		String email=(String)userJson.get(emailKey);
		String password=(String)userJson.get(passwordKey);
  		int userGroupId=(Integer)userJson.get(groupIdKey);
  		UserGroup.GroupType userGroup=UserGroup.GroupType.getTypeForValue(userGroupId);
  		int avatar=(Integer)userJson.get(avatarKey);
  		return create(id, email, password, name, userGroup, avatar);
	}
}
