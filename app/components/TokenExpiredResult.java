package components;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fixtures.Constants;
import models.AbstractMessage;
import play.libs.Json;

public class TokenExpiredResult {

    public static final String TOKEN_TIMED_OUT = "token_timed_out";
    private static ObjectNode result = null;

    public static ObjectNode get() {
        if (result == null) {
            result = Json.newObject();
            result.put(TOKEN_TIMED_OUT, true);
            result.put(AbstractMessage.RET, Constants.INFO_NOT_LOGGED_IN);
        }
        return result;
    }

}
