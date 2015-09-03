package models;

import dao.SimpleMap;

public class PermForeignParty {

	public static final String TAG = PermForeignParty.class.getName();

	public static final String TABLE = "perm_foreign_party";

	public static final String ID = "id";
	public static final String PARTY = "party";
	public static final String PLAYER_ID = "player_id";

	public static String[] QUERY_FIELDS = {ID, PARTY, PLAYER_ID};

	protected String m_id = null;
	public String getId() {
		return m_id;
	}

	protected Integer m_party = null;
	public Integer getParty() {
		return m_party;
	}
	public void setParty(final Integer party) {
		m_party = party;
	}

	protected Long m_playerId = null;

	public Long getPlayerId() {
		return m_playerId;
	}

	public void setPlayerId(final Long playerId) {
		m_playerId = playerId;
	}

	public PermForeignParty(final SimpleMap data) {
		m_id = data.getStr(ID);
		m_party = data.getInt(PARTY);
		m_playerId = data.getLong(PLAYER_ID);
	}
}
