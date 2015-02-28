package components;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fixtures.Constants;
import models.AbstractMessage;
import play.libs.Json;

public class CaptchaNotMatchedResult {

    public static final String CAPTCHA_NOT_MATCHED = "captcha_not_matched";
    private static ObjectNode s_result = null;

    public static ObjectNode get() {
        if (s_result == null) {
            s_result = Json.newObject();
            s_result.put(CAPTCHA_NOT_MATCHED, true);
            s_result.put(AbstractMessage.RET, Constants.INFO_CAPTCHA_NOT_MATCHED);
        }
        return s_result;
    }

}
