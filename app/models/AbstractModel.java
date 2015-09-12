package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SimpleMap;
import play.libs.Json;
import utilities.Loggy;

public abstract class AbstractModel {

        public static final String TAG = AbstractModel.class.getName();

        public static int INVALID = (-1);

        public static final String COUNT = "count";
        public static final String PAGE = "page";
        public static final String PAGE_ST = "page_st";
        public static final String PAGE_ED = "page_ed";
        public static final String NUM_ITEMS = "nuitems";
        public static final String ORDER = "order";
        public static final String ORIENTATION = "orientation";

        public static final String ID = "id";

        public AbstractModel() {

        }

        protected Long id = null;

        public Long getId() {
                return id;
        }

        public void setId(final long data) {
                id = data;
        }

        public AbstractModel(final SimpleMap data) {
                id = data.getLong(ID);
        }

        public ObjectNode toObjectNode() {
                final ObjectNode ret = Json.newObject();
                try {
                        ret.put(ID, id);
                } catch (Exception e) {
                        Loggy.e(TAG, "toObjectNode", e);
                }
                return ret;
        }
}
