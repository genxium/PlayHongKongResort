package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import play.libs.Json;
import utilities.Converter;
import utilities.Loggy;

public abstract class AbstractModel {

	public static final String TAG = AbstractModel.class.getName();

	public static int INVALID = (-1);

	public static final String COUNT = "count";
	public static final String PAGE = "page";
	public static final String PAGE_ST = "page_st";
	public static final String PAGE_ED = "page_ed";
	public static final String NUM_ITEMS = "num_items";
	public static final String ORDER = "order";
	public static final String ORIENTATION = "orientation";

	public static final String ID = "id";

	public AbstractModel() {

	}

	protected Long m_id = null;

	public Long getId() {
		return m_id;
	}

	public void setId(final long id) {
		m_id = id;
	}

	public AbstractModel(final JSONObject json) {
		if (json.containsKey(ID)) m_id = Converter.toLong(json.get(ID));
	}

	public ObjectNode toObjectNode() {
		ObjectNode ret = Json.newObject();
		try {
			ret.put(ID, m_id);
		} catch (Exception e) {
			Loggy.e(TAG, "toObjectNode", e);
		}
		return ret;
	}
}
