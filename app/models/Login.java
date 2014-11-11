package models;

import org.json.simple.JSONObject;
import utilities.Converter;

import java.sql.Timestamp;

public class Login {
    public static final String TAG = Login.class.getName();

    public static final String TABLE = "login";
    public static final String USER_ID = "user_id";
    public static final String TOKEN = "token";
    public static final String TIMESTAMP = "timestamp";

    protected Integer m_userId = null;
    protected String m_token = null;
    protected Timestamp m_timestamp = null;

    public Integer getUserId() {
        return m_userId;
    }

    public Login(JSONObject loginJson) {
        if(loginJson.containsKey(USER_ID)) m_userId = Converter.toInteger(loginJson.get(USER_ID));
        if(loginJson.containsKey(TOKEN)) m_token = (String)loginJson.get(TOKEN);
        if(loginJson.containsKey(TIMESTAMP)) m_timestamp = (Timestamp) loginJson.get(TIMESTAMP);
    }

    public boolean hasExpired() {
        /*
        * TODO: check expiry by current time and User.TOKEN_LIFE
        * TODO: if token has expired, delete the entry in table `login`
        * */
        return false;
    }
}
