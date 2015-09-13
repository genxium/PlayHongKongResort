package utilities;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.util.Auth;
import models.Image;
import org.apache.xerces.dom.DeferredElementImpl;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.Play;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class CDNHelper {

        public static final String TAG = CDNHelper.class.getName();

        public static final Integer QINIU = 1;

        public static final String UPTOKEN = "uptoken";
        public static final String REMOTE_NAME = "remote_name";

        public static final String APP_ID = "AppId";
        public static final String APP_KEY = "AppKey";
        public static final String BUCKET = "Bucket";
        public static final String NAME = "Name";
        public static final String DOMAIN = "Domain";

        public static final String BUCKET_LIST = "BucketList";
        public static final String BUCKET_MAP = "BucketMap";

        private static Random generator = null;
        private static Map<String, Object> qiniuMap = null;
        private static Auth qiniuAuth = null;

        public static class Bucket {
                public String name;
                public String domain;
                public Bucket(final Node node) {
                        final Node nameNode = ((DeferredElementImpl) node).getElementsByTagName(NAME).item(0);
                        this.name = nameNode.getTextContent();
                        final Node domainNode = ((DeferredElementImpl) node).getElementsByTagName(DOMAIN).item(0);
                        this.domain = domainNode.getTextContent();
                }
        }

        public static Random getGenerator() {
                if (generator == null) {
                        generator = new Random();
                }
                return generator;
        }

        public static Map<String, Object> getAttr(final int provider) {
                if (provider == QINIU) {
                        if (qiniuMap == null) {
                                String fullPath = Play.application().path() + "/conf/" + "qiniu_config.xml";
                                qiniuMap = XMLHelper.readCDNConfig(fullPath);
                        }
                        return qiniuMap;
                }
                return null;
        }

        public static String getDomain(final int provider, final String bucketName) {
                final Map<String, String> bucketMap = (Map<String, String>)getAttr(provider).get(CDNHelper.BUCKET_MAP);
                return bucketMap.get(bucketName);
        }

        public static Object getAuth(final int provider) {
                final Map<String, Object> attrs = getAttr(provider);
                if (attrs == null) return null;
                if (provider == QINIU) {
                        if (qiniuAuth == null) {
                                // TODO: maintain a singleton
                                final String appId = (String) attrs.get(CDNHelper.APP_ID);
                                final String appKey = (String) attrs.get(CDNHelper.APP_KEY);
                                qiniuAuth = Auth.create(appId, appKey);
                        }
                        return qiniuAuth;
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

        public static CDNHelper.Bucket pollSingleBucket(final int provider) {
                try {
                        if (provider == QINIU) {
                                        final Map<String, Object> attrs = getAttr(provider);
                                        final List<Bucket> bucketList = (List<Bucket>) attrs.get(CDNHelper.BUCKET_LIST);
                                        final int randomIndex = getGenerator().nextInt(bucketList.size());
                                        return bucketList.get(randomIndex);
                        }
                } catch (Exception e) {
                        Loggy.e(TAG, "pollSingleBucket", e);
                }
                return null;
        }
}
