package utilities;

import play.Play;

import java.util.Map;

public class CDNHelper {

        public static final Integer QINIU = 1;

        public static String UPTOKEN = "uptoken";

        public static String APP_ID = "AppId";
        public static String APP_KEY = "AppKey";
        public static String BUCKET = "Bucket";

        private static Map<String, String> s_qiniuMap = null;

        public static Map<String, String> getAttr(final int provider) {
                if (provider == QINIU) {
                        if (s_qiniuMap == null) {
                                String fullPath = Play.application().path() + "/conf/" + "qiniu_config.xml";
                                s_qiniuMap = XMLHelper.readCdnConfig(fullPath);
                        }
                        return s_qiniuMap;
                }
                return null;
        }

}
