package components;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.AbstractMessage;
import play.libs.Json;

public class StandardFailureResult {
    private static ObjectNode result = null;

    public static ObjectNode get() {
        if (result == null) {
            result = Json.newObject();
            result.put(AbstractMessage.RET, "1");
        }
        return result;
    }

    public static ObjectNode get(Integer errno) {
        ObjectNode ret = Json.newObject();
        ret.put(AbstractMessage.RET, String.valueOf(errno));
        return ret;
    }
}
