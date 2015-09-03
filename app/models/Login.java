package models;

import dao.SimpleMap;

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

        public Login(final SimpleMap data) {
                m_playerId = data.getLong(PLAYER_ID);
                m_token = data.getStr(TOKEN);
                m_timestamp = data.getLong(TIMESTAMP);
        }

        public boolean hasExpired() {
        /*
        * TODO: check expiry by current time and Player.TOKEN_LIFE
        * TODO: if token has expired, delete the entry in table `login`
        * */
                return false;
        }
}
