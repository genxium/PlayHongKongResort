/*
 * This is the base class of user model
 * */

package model;

import org.json.simple.JSONObject;

public class User extends BasicUser {
	
	public static String PASSWORD ="UserPassword";
	public static String GROUP_ID ="UserGroupId";
	public static String AUTHENTICATION_STATUS ="UserAuthenticationStatus";
	public static String GENDER ="UserGender";
	public static String LAST_LOGGED_IN_TIME ="UserLastLoggedInTime";
	public static String LAST_LOGGED_OUT_TIME ="UserLastLoggedOutTime";
	public static String LAST_EXIT_TIME ="UserLastExitTime";
	
	public static String TOKEN ="UserToken";
	public static String VERIFICATION_CODE="VerificationCode"; 
	
	protected String m_password=null;
	public String getPassword() {return m_password;}
	public void setPassword(String password) {m_password=password;}

	protected UserGroup.GroupType m_userGroup=null;
	public UserGroup.GroupType getUserGroup() {return m_userGroup;}
	public void setUserGroup(UserGroup.GroupType userGroup) {m_userGroup=userGroup;}

    protected String m_verificationCode=null;
    public String getVerificationCode() {return m_verificationCode;}
    public void setVerificationCode(String verificationCode) {m_verificationCode=verificationCode;}

	public User(int userId, String email, String password, String name, UserGroup.GroupType userGroup, int avatar){
		super(userId, email, name, avatar);
		m_password=password;
		m_userGroup=userGroup;
	}

	public User(JSONObject userJson){
		super(userJson);
		if(userJson.containsKey(PASSWORD)) m_password=(String)userJson.get(PASSWORD);
		if(userJson.containsKey(GROUP_ID)) m_userGroup=UserGroup.GroupType.getTypeForValue((Integer)userJson.get(GROUP_ID));
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
  		int id=(Integer)userJson.get(ID);
  		String name=(String)userJson.get(NAME);
  		String email=(String)userJson.get(EMAIL);
		String password=(String)userJson.get(PASSWORD);
  		int userGroupId=(Integer)userJson.get(GROUP_ID);
  		UserGroup.GroupType userGroup=UserGroup.GroupType.getTypeForValue(userGroupId);
  		int avatar=(Integer)userJson.get(AVATAR);
  		return create(id, email, password, name, userGroup, avatar);
	}

}
