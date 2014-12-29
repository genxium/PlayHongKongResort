package dao;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class XMLHelper {

    public static Map<String, String> readDatabaseConfig(String fileName) {

        Map<String, String> ret = null;

        try {

            File fXmlFile = new File(fileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            Element root = doc.getDocumentElement();

            ret = new HashMap<String, String>();

            ret.put(SQLHelper.DATABASE_NAME, root.getElementsByTagName(SQLHelper.DATABASE_NAME).item(0).getTextContent());
            ret.put(SQLHelper.HOST, root.getElementsByTagName(SQLHelper.HOST).item(0).getTextContent());
            ret.put(SQLHelper.PORT, root.getElementsByTagName(SQLHelper.PORT).item(0).getTextContent());
            ret.put(SQLHelper.USER, root.getElementsByTagName(SQLHelper.USER).item(0).getTextContent());
            ret.put(SQLHelper.PASSWORD, root.getElementsByTagName(SQLHelper.PASSWORD).item(0).getTextContent());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
};