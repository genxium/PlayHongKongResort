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
    protected Long m_timestamp = null;

    public Long getPlayerId() {
        return m_playerId;
    }

    public Login(final JSONObject json) {
        if (json.containsKey(PLAYER_ID)) m_playerId = Converter.toLong(json.get(PLAYER_ID));
        if (json.containsKey(TOKEN)) m_token = (String) json.get(TOKEN);
        if (json.containsKey(TIMESTAMP)) m_timestamp = Converter.toLong(json.get(TIMESTAMP));
    }

    public boolean hasExpired() {
        /*
        * TODO: check expiry by current time and Player.TOKEN_LIFE
        * TODO: if token has expired, delete the entry in table `login`
        * */
        return false;
    }
}
