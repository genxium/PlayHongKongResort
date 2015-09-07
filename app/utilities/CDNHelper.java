package utilities;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.util.Auth;
import models.Image;
import play.Play;

import java.util.List;
import java.util.Map;

public class CDNHelper {

        public static final String TAG = CDNHelper.class.getName();

        public static final Integer QINIU = 1;

        public static String UPTOKEN = "uptoken";
        public static String REMOTE_NAME = "remote_name";

        public static String APP_ID = "AppId";
        public static String APP_KEY = "AppKey";
        public static String BUCKET = "Bucket";
        public static String DOMAIN = "Domain";

        private static Map<String, String> s_qiniuMap = null;
        private static Auth s_qiniuAuth = null;

        public static Map<String, String> getAttr(final int provider) {
                if (provider == QINIU) {
                        if (s_qiniuMap == null) {
                                String fullPath = Play.application().path() + "/conf/" + "qiniu_config.xml";
                                s_qiniuMap = XMLHelper.readCDNConfig(fullPath);
                        }
                        return s_qiniuMap;
                }
                return null;
        }

        public static Object getAuth(final int provider) {
                final Map<String, String> attrs = getAttr(provider);
                if (attrs == null) return null;
                if (provider == QINIU) {
                        if (s_qiniuAuth == null) {
                                // TODO: maintain a singleton
                                return Auth.create(attrs.get(CDNHelper.APP_ID), attrs.get(CDNHelper.APP_KEY));
                        }
                }
                return null;
        }


        public static boolean deleteRemoteImages(final int provider, final List<Image> imageList) throws QiniuException {
                if (provider == QINIU) {
                        final Auth auth = (Auth)getAuth(provider);
                        // Note that remote_name owns player_id and GMT timestamp info by design
                        final BucketManager bucketManager = new BucketManager(auth);
                        for (Image image : imageList) {
                                final String bucket = image.getBucket();
                                final String remoteName = image.getRemoteName();
                                bucketManager.delete(bucket, remoteName);
                        }
                        return true;
                }
                return false;
        }
}
