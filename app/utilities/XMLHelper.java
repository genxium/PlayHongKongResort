package utilities;

import controllers.ForeignPartyController;
import dao.SQLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
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

                Map<String, String> ret = null;

                try {
                        File fXmlFile = new File(filepath);
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.parse(fXmlFile);

                        Element root = doc.getDocumentElement();

                        ret = new HashMap<>();
                        ret.put(ForeignPartyController.APP_ID, root.getElementsByTagName(ForeignPartyController.APP_ID).item(0).getTextContent());
                        ret.put(ForeignPartyController.APP_KEY, root.getElementsByTagName(ForeignPartyController.APP_KEY).item(0).getTextContent());
                } catch (Exception e) {
                        Loggy.e(TAG, "readForeignPartyConfig", e);
                }
                return ret;
        }

        public static Map<String, String> readCdnConfig(final String filepath) {
                Map<String, String> ret = null;
                try {
                        File fXmlFile = new File(filepath);
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.parse(fXmlFile);

                        Element root = doc.getDocumentElement();

                        ret = new HashMap<>();
                        ret.put(CDNHelper.APP_ID, root.getElementsByTagName(ForeignPartyController.APP_ID).item(0).getTextContent());
                        ret.put(CDNHelper.APP_KEY, root.getElementsByTagName(ForeignPartyController.APP_KEY).item(0).getTextContent());
                        ret.put(CDNHelper.BUCKET, root.getElementsByTagName(ForeignPartyController.APP_KEY).item(0).getTextContent());
                } catch (Exception e) {
                        Loggy.e(TAG, "readForeignPartyConfig", e);
                }
                return ret;
        }
}
