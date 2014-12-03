package exception;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

public class CaptchaNotMatchedResult {

    public static final String CAPTCHA_NOT_MATCHED = "captcha_not_matched";
    protected static ObjectNode s_result = null;

    public static ObjectNode get() {
        if (s_result == null) {
            s_result = Json.newObject();
            s_result.put(CAPTCHA_NOT_MATCHED, true);
        }
        return s_result;
    }

}
