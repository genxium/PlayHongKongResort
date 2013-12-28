package dao;
import javax.xml.parsers.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLHelper{

	public static Map<String, String> readDatabaseConfig(String fileName){
		
		Map<String, String> ret=null;
		
		try {
			
			File fXmlFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
		
			Element root = doc.getDocumentElement();
			
			ret=new HashMap<String, String>();
			
			ret.put(SQLHelper.databaseNameKey, root.getElementsByTagName(SQLHelper.databaseNameKey).item(0).getTextContent());
			ret.put(SQLHelper.hostKey, root.getElementsByTagName(SQLHelper.hostKey).item(0).getTextContent());
			ret.put(SQLHelper.portKey, root.getElementsByTagName(SQLHelper.portKey).item(0).getTextContent());
			ret.put(SQLHelper.userKey, root.getElementsByTagName(SQLHelper.userKey).item(0).getTextContent());
			ret.put(SQLHelper.passwordKey, root.getElementsByTagName(SQLHelper.passwordKey).item(0).getTextContent());
			
	    } catch (Exception e) {
			e.printStackTrace();
	    }
		return ret;
	}
};