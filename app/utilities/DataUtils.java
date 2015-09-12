package utilities;

import models.Image;
import play.mvc.Http.MultipartFormData.FilePart;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Map;

public class DataUtils {

        public static final String TAG = DataUtils.class.getName();
        public static final int CACHE_DURATION = 60;

        public static String getFileExt(String fileName) {
                int dotPos = fileName.lastIndexOf('.');
                return fileName.substring(dotPos + 1, fileName.length() - 1);
        }

        public static boolean isImage(FilePart imageFile) {
                String contentType = imageFile.getContentType();
                int slashPos = contentType.indexOf('/');
                String typePrefix = contentType.substring(0, slashPos);
                return (typePrefix.compareTo("image") == 0);
        }

        public static boolean validateImage(FilePart imageFile) {
                boolean isImage = isImage(imageFile);
                File file = imageFile.getFile();
                return (isImage && file.length() <= Image.SINGLE_FILE_SIZE_LIMIT);
        }

        public static String encryptByTime(String seed) {
                try {
                        java.util.Date date = new java.util.Date();
                        Timestamp currentTime = new Timestamp(date.getTime());
                        Long epochTime = currentTime.getTime();
                        String tmp = Converter.md5(epochTime.toString() + seed);
                        if (tmp == null) throw new NullPointerException();
                        int length = tmp.length();
                        return tmp.substring(0, (length >> 1));
                } catch (Exception e) {
                        Loggy.e(TAG, "encryptByTime", e);
                }
                return null;
        }

        public static String appendCacheKey(final String cacheKey, final String key, final Object val) {
                return cacheKey + "|" + key + ":" + String.valueOf(val);
        }

        public static String encodeUtf8(final String toEncode) {
                try {
                        return URLEncoder.encode(toEncode, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                        return null;
                }
        }

        public static String toUrlParams(final Map<String, Object> map) {
                String ret = "";
                boolean first = true;
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                        String val = encodeUtf8(String.valueOf(entry.getValue()));
                        if (!first) ret += String.format("&%s=%s", entry.getKey(), val);
                        else ret += String.format("%s=%s", entry.getKey(), val);
                        first = false;
                }
                return ret;
        }
}
