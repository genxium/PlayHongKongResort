package utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.TempForeignParty;
import play.Play;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class ForeignPartyHelper {

        public static final String TAG = ForeignPartyHelper.class.getName();

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

        public static String queryQQNickname(final String accessToken, final String openId) {
                try {
                        final Map<String, String> qqAttr = ForeignPartyHelper.getAttr(ForeignPartyHelper.PARTY_QQ);
                        if (qqAttr == null) return null;
                        final Map<String, Object> params = new HashMap<>();
                        params.put("oauth_consumer_key", qqAttr.get(ForeignPartyHelper.APP_ID));
                        params.put(TempForeignParty.ACCESS_TOKEN, accessToken);
                        params.put("openid", openId);
                        params.put("format", "json");
                        final String url = "https://graph.qq.com/user/get_user_info?" + DataUtils.toUrlParams(params);
                        Loggy.e(TAG, "queryQQNickname", "url is " + url);
                        final URLConnection conn = new URL(url).openConnection();
                        final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String line = "";
                        String tmp;
                        while ((tmp = in.readLine()) != null) {
                                line += tmp;
                        }
                        in.close();
                        Loggy.e(TAG, "queryQQNickname", line);
                        final ObjectMapper mapper = new ObjectMapper();
                        final Map<String, String> parsedData = mapper.readValue(line, mapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
                        return parsedData.get("nickname");
                } catch (Exception e) {
                        Loggy.e(TAG, "queryQQNickname", e);
                }
                return null;
        }

}
