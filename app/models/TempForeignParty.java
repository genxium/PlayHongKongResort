package models;

import dao.SimpleMap;

public class TempForeignParty {

	public static final String TAG = TempForeignParty.class.getName();

	public static final String TABLE = "temp_foreign_party";

	public static final String AUTHORIZATION_CODE = "authorization_code";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String PARTY = "party";
	public static final String PARTY_ID = "party_id";
	public static final String TIMESTAMP = "timestamp";

	public static final String[] QUERY_FIELDS = {ACCESS_TOKEN, PARTY, PARTY_ID, TIMESTAMP};

	protected String accessToken = null;

	public String getAccessToken() {
		return accessToken;
	}

	protected Integer party = null;

	public Integer getParty() {
		return party;
	}

	public void setParty(final int data) {
		party = data;
	}

	protected String partyId = null;

	public String getPartyId() {
		return partyId;
	}

	protected Long timestamp = null;

	public TempForeignParty(final SimpleMap data) {
		accessToken = data.getStr(ACCESS_TOKEN);
		party = data.getInt(PARTY);
		partyId = data.getStr(PARTY_ID);
		timestamp = data.getLong(TIMESTAMP);
	}
}
