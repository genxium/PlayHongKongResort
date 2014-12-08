package utilities;

import models.User;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import java.sql.Timestamp;
import java.util.Map;

public class DataUtils {

    public static final String TAG = DataUtils.class.getName();
    public static final int CACHE_DURATION = 180;

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
        return isImage(imageFile);
    }

    public static String getUserToken(MultipartFormData data) {
        Map<String, String[]> formData = data.asFormUrlEncoded();
        String[] tokens = formData.get(User.TOKEN);
        return tokens[0];
    }

    public static boolean validateTitle(String title) {
        return title.length() >= 0;
    }

    public static boolean validateContent(String content) {
        return content.length() >= 0;
    }

    public static String generateUploadedImageName(String originalName, Long userId) {
        String ret = null;
        try {
            java.util.Date date = new java.util.Date();
            Timestamp currentTime = new Timestamp(date.getTime());
            Long epochTime = currentTime.getTime();
            String[] nameComponents = originalName.split("\\.(?=[^\\.]+$)");
            String base = nameComponents[0];
            String ext = nameComponents[1];
            ret = "UID" + userId.toString() + "_" + epochTime.toString() + "_" + Converter.md5(base) + "." + ext;
        } catch (Exception e) {
            System.out.println(DataUtils.class.getName() + ".generateUploadedImageName, " + e.getMessage());
        }
        return ret;
    }
	
    public static String encryptByTime(String seed) {
		try {
			java.util.Date date = new java.util.Date();
			Timestamp currentTime = new Timestamp(date.getTime());
			Long epochTime = currentTime.getTime();
			String tmp = Converter.md5(epochTime.toString() + seed);
			int length = tmp.length();
			return tmp.substring(0, (length >> 1));
		} catch (Exception e) {
			Loggy.e(TAG, "encryptByTime", e);
		}
		return null;
    } 

    public static String appendCacheKey(String cacheKey, String key, Object val) {
        return cacheKey + "|" + key + ":" + String.valueOf(val);
    }
}
