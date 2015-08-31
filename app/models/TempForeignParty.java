package models;

import org.json.simple.JSONObject;
import utilities.Converter;

import java.sql.Timestamp;

public class TempForeignParty {

    public static final String TAG = TempForeignParty.class.getName();

    public static final String TABLE = "temp_foreign_party";

    public static final String ACCESS_TOKEN = "access_token";
    public static final String PARTY = "party";
    public static final String PARTY_ID = "party_id";
    public static final String TIMESTAMP = "timestamp";

    public static String[] QUERY_FIELDS = {ACCESS_TOKEN, PARTY, PARTY_ID, TIMESTAMP};

    protected String m_accessToken = null;

    public String getAccessToken() {
        return m_accessToken;
    }

    protected Integer m_party = null;

    public Integer getParty() {
        return m_party;
    }

    public void setParty(final Integer party) {
        m_party = party;
    }

    protected String m_partyId = null;

    public String getPartyId() {
        return m_partyId;
    }

    protected Long m_timestamp = null;

    public TempForeignParty(final JSONObject json) {
        if (json.containsKey(ACCESS_TOKEN)) m_accessToken = (String) (json.get(ACCESS_TOKEN));
        if (json.containsKey(PARTY)) m_party = Converter.toInteger(json.get(PARTY));
        if (json.containsKey(PARTY_ID)) m_partyId = (String) json.get(PARTY_ID);
        if (json.containsKey(TIMESTAMP)) m_timestamp = Converter.toLong(json.get(TIMESTAMP));
    }
}
