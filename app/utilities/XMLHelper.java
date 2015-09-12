package utilities;

import dao.SQLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLHelper {

        public static final String TAG = XMLHelper.class.getName();

        public static Map<String, String> readDatabaseConfig(final String filepath) {

                Map<String, String> ret = null;

                try {
                        File fXmlFile = new File(filepath);
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.parse(fXmlFile);

                        Element root = doc.getDocumentElement();

                        ret = new HashMap<>();

                        ret.put(SQLHelper.DATABASE_NAME, root.getElementsByTagName(SQLHelper.DATABASE_NAME).item(0).getTextContent());
                        ret.put(SQLHelper.HOST, root.getElementsByTagName(SQLHelper.HOST).item(0).getTextContent());
                        ret.put(SQLHelper.PORT, root.getElementsByTagName(SQLHelper.PORT).item(0).getTextContent());
                        ret.put(SQLHelper.USER, root.getElementsByTagName(SQLHelper.USER).item(0).getTextContent());
                        ret.put(SQLHelper.PASSWORD, root.getElementsByTagName(SQLHelper.PASSWORD).item(0).getTextContent());

                } catch (Exception e) {
                        Loggy.e(TAG, "readDatabaseConfig", e);
                }
                return ret;
        }

        public static Map<String, String> readForeignPartyConfig(final String filepath) {

                try {
                        final File fXmlFile = new File(filepath);
                        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        final Document doc = dBuilder.parse(fXmlFile);

                        final Element root = doc.getDocumentElement();

                        final Map<String, String> ret = new HashMap<>();
                        ret.put(ForeignPartyHelper.APP_ID, root.getElementsByTagName(ForeignPartyHelper.APP_ID).item(0).getTextContent());
                        ret.put(ForeignPartyHelper.APP_KEY, root.getElementsByTagName(ForeignPartyHelper.APP_KEY).item(0).getTextContent());
                        return ret;
                } catch (Exception e) {
                        Loggy.e(TAG, "readForeignPartyConfig", e);
                }
                return null;
        }

        public static Map<String, Object> readCDNConfig(final String filepath) {
                try {
                        final File fXmlFile = new File(filepath);
                        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        final Document doc = dBuilder.parse(fXmlFile);

                        final Element root = doc.getDocumentElement();

                        final Map<String, Object> ret = new HashMap<>();
                        ret.put(CDNHelper.APP_ID, root.getElementsByTagName(CDNHelper.APP_ID).item(0).getTextContent());
                        ret.put(CDNHelper.APP_KEY, root.getElementsByTagName(CDNHelper.APP_KEY).item(0).getTextContent());

                        final List<CDNHelper.Bucket> bucketList = new ArrayList<>();
                        final Map<String, String> bucketMap = new HashMap<>();

                        final NodeList buckets = root.getElementsByTagName(CDNHelper.BUCKET);
                        int nBuckets = buckets.getLength();
                        for (int i = 0; i < nBuckets; ++i) {
                                Node node = buckets.item(i);
                                CDNHelper.Bucket bucket = new CDNHelper.Bucket(node);
                                bucketList.add(bucket);
                                bucketMap.put(bucket.name, bucket.domain);
                        }
                        ret.put(CDNHelper.BUCKET_LIST, bucketList);
                        ret.put(CDNHelper.BUCKET_MAP, bucketMap);
                        return ret;
                } catch (Exception e) {
                        Loggy.e(TAG, "readCDNConfig", e);
                }
                return null;
        }
}
