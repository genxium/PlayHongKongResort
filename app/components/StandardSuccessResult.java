package components;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.AbstractMessage;
import play.libs.Json;

public class StandardSuccessResult {
    private static ObjectNode s_result = null;

    public static ObjectNode get() {
        if (s_result == null) {
            s_result = Json.newObject();
            s_result.put(AbstractMessage.RET, "0");
        }
        return s_result;
    }
}
