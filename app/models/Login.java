package models;

import dao.SimpleMap;

public class Login {
        public static final String TAG = Login.class.getName();

        public static final String TABLE = "login";
        public static final String PLAYER_ID = "player_id";
        public static final String TOKEN = "token";
        public static final String TIMESTAMP = "timestamp";

        protected Long playerId = null;
        protected String token = null;
        protected Long timestamp = null;

        public Long getPlayerId() {
                return playerId;
        }

        public Login(final SimpleMap data) {
                playerId = data.getLong(PLAYER_ID);
                token = data.getStr(TOKEN);
                timestamp = data.getLong(TIMESTAMP);
        }

        public boolean hasExpired() {
        /*
        * TODO: check expiry by current time and Player.TOKEN_LIFE
        * TODO: if token has expired, delete the entry in table `login`
        * */
                return false;
        }
}
