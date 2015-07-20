package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.AccessDeniedException;
import org.json.simple.JSONObject;
import utilities.Converter;
import utilities.Loggy;

public class PermForeignParty extends AbstractModel {

    public static final String TAG = PermForeignParty.class.getName();

    public static final String TABLE = "perm_foreign_party";

    public static final String PARTY = "party";
    public static final String USER_ID = "user_id";

    public static String[] QUERY_FIELDS = {ID, PARTY, USER_ID};

    protected Integer m_party = null;

    public Integer getParty() {
        return m_party;
    }

    public void setParty(final Integer party) {
        m_party = party;
    }

    protected Long m_userId = null;

    public Long getUserId() {
        return m_userId;
    }

    public void setUserId(final Long userId) {
        m_userId = userId;
    }

    public PermForeignParty(final JSONObject json) {
        super(json);

        if (json.containsKey(PARTY))
            m_party = Converter.toInteger(json.get(PARTY));

        if (json.containsKey(USER_ID))
            m_userId = Converter.toLong(json.get(USER_ID));
    }

    public ObjectNode toObjectNode(final Long viewerId) {
        ObjectNode ret = super.toObjectNode();
        try {
            if (viewerId == null || !viewerId.equals(m_userId)) throw new AccessDeniedException();
            ret.put(PARTY, String.valueOf(m_party));
            ret.put(USER_ID, String.valueOf(m_userId));
        } catch (Exception e) {
            Loggy.e(TAG, "toObjectNode", e);
        }
        return ret;
    }
}