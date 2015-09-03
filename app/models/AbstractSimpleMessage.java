package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SimpleMap;
import utilities.Loggy;

public abstract class AbstractSimpleMessage extends AbstractModel {

        public static final String CONTENT = "content";

        protected String m_content = null;

        public String getContent() {
                return m_content;
        }

        public void setContent(final String content) {
                m_content = content;
        }

        public AbstractSimpleMessage() {
                super();
        }

        public AbstractSimpleMessage(final SimpleMap data) {
                super(data);
                m_content = data.getStr(CONTENT);
        }

        public ObjectNode toObjectNode() {
                ObjectNode ret = super.toObjectNode();
                try {
                        ret.put(CONTENT, m_content);
                } catch (Exception e) {
                        Loggy.e(this.getClass().getName(), "toObjectNode", e);
                }
                return ret;
        }
}
