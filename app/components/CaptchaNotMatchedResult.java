package components;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fixtures.Constants;
import models.AbstractMessage;
import play.libs.Json;

public class CaptchaNotMatchedResult {

    public static final String CAPTCHA_NOT_MATCHED = "captcha_not_matched";
    private static ObjectNode result = null;

    public static ObjectNode get() {
        if (result == null) {
            result = Json.newObject();
            result.put(CAPTCHA_NOT_MATCHED, true);
            result.put(AbstractMessage.RET, Constants.INFO_CAPTCHA_NOT_MATCHED);
        }
        return result;
    }

}
