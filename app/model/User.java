/*
 * This is the base class of user model
 * */

package model;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.json.simple.JSONObject;

public class User extends BasicUser {

    public static int VISITOR = 0;
    public static int USER = 1;
    public static int MANAGER = 2;
    public static int ADMIN = 3;

    public static final String TABLE = "user";
    public static final String PASSWORD = "password";
    public static final String GROUP_ID = "group_id";
    public static final String AUTHENTICATION_STATUS = "authentication_status";
    public static final String GENDER = "gender";
    public static final String LAST_LOGGED_IN_TIME = "last_logged_in_time";
    public static final String LAST_LOGGED_OUT_TIME = "last_logged_out_time";
    public static final String LAST_EXIT_TIME = "last_exit_time";

    public static final String TOKEN = "token";
    public static final String VERIFICATION_CODE = "verification_code";
    public static final String PASSWORD_RESET_CODE = "password_reset_code";  

    protected String m_password = null;

    public String getPassword() {
        return m_password;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    protected int m_groupId = 0;

    public int getGroupId() {
        return m_groupId;
    }

    public void setGroupId(int groupId) {
        m_groupId = groupId;
    }

    protected String m_verificationCode = null;

    public String getVerificationCode() {
        return m_verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        m_verificationCode = verificationCode;
    }

    public User(String email, String password, String name) {
        super(email, name);
        m_password = password;
        m_groupId = VISITOR;
    }

    public User(JSONObject userJson) {
        super(userJson);
        if (userJson.containsKey(PASSWORD)) m_password = (String) userJson.get(PASSWORD);
        if (userJson.containsKey(GROUP_ID)) m_groupId = (Integer) userJson.get(GROUP_ID);
    }

    public ObjectNode toObjectNode(Integer viewerId) {
        ObjectNode ret = super.toObjectNode(viewerId);
        return ret;
    }
}
