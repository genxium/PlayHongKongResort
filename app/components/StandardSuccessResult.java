package components;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.AbstractMessage;
import play.libs.Json;

public class StandardSuccessResult {
    private static ObjectNode result = null;

    public static ObjectNode get() {
        if (result == null) {
            result = Json.newObject();
            result.put(AbstractMessage.RET, "0");
        }
        return result;
    }
}
