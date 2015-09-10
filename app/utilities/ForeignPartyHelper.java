package utilities;

import controllers.ForeignPartyController;
import play.Play;

import java.util.Map;

public class ForeignPartyHelper {

        public static final String APP_ID = "AppId";
        public static final String APP_KEY = "AppKey";

        public static final int PARTY_NONE = 0;
        public static final int PARTY_QQ = 1;

        private static Map<String, String> qqMap = null;

        public static Map<String, String> getAttr(final int party) {
                if (party == PARTY_QQ) {
                        if (qqMap == null) {
                                String fullPath = Play.application().path() + "/conf/" + "qq_config.xml";
                                qqMap = XMLHelper.readForeignPartyConfig(fullPath);
                        }
                        return qqMap;
                }
                return null;
        }
}
