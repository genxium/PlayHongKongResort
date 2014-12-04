package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import utilities.Converter;
import utilities.Loggy;

public abstract class AbstractMessage extends AbstractSimpleMessage {

    public static final String FROM = "from";
    public static final String FROM_NAME = "from_name";
    public static final String TO = "to";
    public static final String TO_NAME = "to_name";

    public static final String GENERATED_TIME = "generated_time";

    protected Integer m_from = null;
    protected Integer m_to = null;
    protected Long m_generatedTime = null;

    public Integer getFrom() {
        return m_from;
    }

    public void setFrom(int from) {
        m_from = from;
    }

    public Integer getTo() {
        return m_to;
    }

    public AbstractMessage() {
        super();
    }

    public AbstractMessage(JSONObject json) {
        super(json);
        if (json.containsKey(FROM)) m_from = Converter.toInteger(json.get(FROM));
        if (json.containsKey(TO)) m_to = Converter.toInteger(json.get(TO));
        if (json.containsKey(GENERATED_TIME)) m_generatedTime = Converter.toLong(json.get(GENERATED_TIME));
    }

    public ObjectNode toObjectNode() {
        ObjectNode ret = super.toObjectNode();
        try {
            ret.put(FROM, m_from);
            ret.put(TO, m_to);
            ret.put(GENERATED_TIME, String.valueOf(m_generatedTime));
        } catch (Exception e) {
            Loggy.e(this.getClass().getName(), "toObjectNode", e);
        }
        return ret;
    }
}
