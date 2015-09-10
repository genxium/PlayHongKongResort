package models;

import dao.SimpleMap;

public class PermForeignParty {

	public static final String TAG = PermForeignParty.class.getName();

	public static final String TABLE = "perm_foreign_party";

	public static final String ID = "id";
	public static final String PARTY = "party";
	public static final String PLAYER_ID = "player_id";

	public static final String[] QUERY_FIELDS = {ID, PARTY, PLAYER_ID};

	protected String id = null;
	public String getId() {
		return id;
	}

	protected Integer party = null;
	public Integer getParty() {
		return party;
	}
	public void setParty(final int data) {
		party = data;
	}

	protected Long playerId = null;

	public Long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(final Long data) {
		playerId = data;
	}

	public PermForeignParty(final SimpleMap data) {
		id = data.getStr(ID);
		party = data.getInt(PARTY);
		playerId = data.getLong(PLAYER_ID);
	}
}
