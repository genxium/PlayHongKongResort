package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SimpleMap;
import utilities.Loggy;

public abstract class AbstractSimpleMessage extends AbstractModel {

        public static final String CONTENT = "content";

        protected String content = null;

        public String getContent() {
                return content;
        }

        public void setContent(final String data) {
                content = data;
        }

        public AbstractSimpleMessage() {
                super();
        }

        public AbstractSimpleMessage(final SimpleMap data) {
                super(data);
                content = data.getStr(CONTENT);
        }

        public ObjectNode toObjectNode() {
                ObjectNode ret = super.toObjectNode();
                try {
                        ret.put(CONTENT, content);
                } catch (Exception e) {
                        Loggy.e(this.getClass().getName(), "toObjectNode", e);
                }
                return ret;
        }
}
