/*
 * This is the base class of user model
 * */

package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import utilities.Converter;
import utilities.Loggy;

import java.util.regex.Pattern;

public class User extends BasicUser {

    public static final int TOKEN_LIFE = 7889230; // in seconds, this equals 3 months

    public static int VISITOR = 0;
    public static int USER = 1;
    public static int MANAGER = 2;
    public static int ADMIN = 3;

    public static final String TABLE = "user";

    public static final String PASSWORD = "password";
    public static final String SALT = "salt";
    public static final String GROUP_ID = "group_id";
    public static final String AUTHENTICATION_STATUS = "authentication_status";
    public static final String GENDER = "gender";

    public static final String TOKEN = "token";
    public static final String VERIFICATION_CODE = "verification_code";
    public static final String PASSWORD_RESET_CODE = "password_reset_code";  

	public static final String UNREAD_COUNT = "unread_count";
	public static final String UNASSESSED_COUNT = "unassessed_count";

    public static final String[] QUERY_FILEDS = {ID, EMAIL, NAME, AVATAR, PASSWORD, PASSWORD_RESET_CODE, UNREAD_COUNT, UNASSESSED_COUNT, SALT, GROUP_ID, AUTHENTICATION_STATUS, GENDER, VERIFICATION_CODE};

    public static final Pattern PASSWORD_PATTERN = Pattern.compile("^[0-9a-zA-Z_#\\!]{6,32}$", Pattern.UNICODE_CHARACTER_CLASS);

    protected String m_password = null;

    public String getPassword() {
        return m_password;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    protected String m_salt = null;

    public String getSalt() {
        return (m_salt == null) ? "" : m_salt;
    }

    public void setSalt(String salt) {
        m_salt = salt;
    }

    protected Integer m_groupId = VISITOR;

    public int getGroupId() {
        return m_groupId;
    }

    protected String m_verificationCode = null;

    public String getVerificationCode() {
        return m_verificationCode;
    }

    public void setVerificationCode(String code) {
        m_verificationCode = code;
    }

	protected Integer m_unreadCount = 0;

	public int getUnreadCount() {
		return m_unreadCount;
	}

	protected Integer m_unassessedCount = 0;
	
	public int getUnassessedCount() {
		return m_unassessedCount;
	}	

    public User(String email, String password, String name) {
        super(email, name);
        m_password = password;
        m_groupId = VISITOR;
    }

    public User(JSONObject userJson) {
        super(userJson);
        if (userJson.containsKey(PASSWORD)) m_password = (String) userJson.get(PASSWORD);
        if (userJson.containsKey(SALT)) m_salt = (String) userJson.get(SALT);
        if (userJson.containsKey(GROUP_ID)) m_groupId = Converter.toInteger(userJson.get(GROUP_ID));
		if (userJson.containsKey(UNREAD_COUNT)) m_unreadCount = Converter.toInteger(userJson.get(UNREAD_COUNT));
		if (userJson.containsKey(UNASSESSED_COUNT)) m_unassessedCount = Converter.toInteger(userJson.get(UNASSESSED_COUNT));
    }

    public ObjectNode toObjectNode(Long viewerId) {
        ObjectNode ret = super.toObjectNode(viewerId);
        try {
            ret.put(UNREAD_COUNT, String.valueOf(m_unreadCount));
			ret.put(UNASSESSED_COUNT, String.valueOf(m_unassessedCount));
            ret.put(GROUP_ID, String.valueOf(m_groupId));
        } catch (Exception e) {
            Loggy.e(TAG, "toObjectNode", e);
        }
        return ret;
    }
}
