package models;

import org.json.simple.JSONObject;
import utilities.Converter;

import java.sql.Timestamp;

public class Login {
    public static final String TAG = Login.class.getName();

    public static final String TABLE = "login";
    public static final String PLAYER_ID = "player_id";
    public static final String TOKEN = "token";
    public static final String TIMESTAMP = "timestamp";

    protected Long m_playerId = null;
    protected String m_token = null;
    protected Timestamp m_timestamp = null;

    public Long getPlayerId() {
        return m_playerId;
    }

    public Login(JSONObject loginJson) {
        if (loginJson.containsKey(PLAYER_ID)) m_playerId = Converter.toLong(loginJson.get(PLAYER_ID));
        if (loginJson.containsKey(TOKEN)) m_token = (String)loginJson.get(TOKEN);
        if (loginJson.containsKey(TIMESTAMP)) m_timestamp = (Timestamp) loginJson.get(TIMESTAMP);
    }

    public boolean hasExpired() {
        /*
        * TODO: check expiry by current time and Player.TOKEN_LIFE
        * TODO: if token has expired, delete the entry in table `login`
        * */
        return false;
    }
}
