package components;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.AbstractMessage;
import play.libs.Json;

public class StandardFailureResult {
    private static ObjectNode s_result = null;

    public static ObjectNode get() {
        if (s_result == null) {
            s_result = Json.newObject();
            s_result.put(AbstractMessage.RET, "1");
        }
        return s_result;
    }

    public static ObjectNode get(Integer errno) {
        ObjectNode ret = Json.newObject();
        ret.put(AbstractMessage.RET, String.valueOf(errno));
        return ret;
    }
}
