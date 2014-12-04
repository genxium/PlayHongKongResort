/*
 * This is the base class of image model
 * */

package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import play.Play;

public class Image extends AbstractModel {

    public static String PROD_URL_PREFIX = "/images/";
    public static String DEV_URL_PREFIX = "/assets/images/";
    public static String PROD_FOLDER_PATH = "/var/www/html/images/";
    public static String DEV_FOLDER_PATH = Play.application().path().getAbsolutePath() + "/public/images/";

    public static String TABLE = "image";
    public static String URL = "url";

    private String m_url = null;

    public String getUrl() {
        return m_url;
    }

    public static String getUrlPrefix() {
        if (Play.application().isProd()) return PROD_URL_PREFIX;
        else return DEV_URL_PREFIX;
    }

    public static String getFolderPath() {
        if (Play.application().isProd()) return PROD_FOLDER_PATH;
        else return DEV_FOLDER_PATH;
    }

    public String getAbsolutePath() {
        String baseName = FilenameUtils.getBaseName(m_url);
        String extension = FilenameUtils.getExtension(m_url);
        return getFolderPath() + baseName + "." + extension;
    }

    public Image(JSONObject imageJson) {
        super(imageJson);
        m_url = (String) imageJson.get(Image.URL);
    }

    public ObjectNode toObjectNode() {
        ObjectNode ret = super.toObjectNode();
        ret.put(URL, m_url);
        return ret;
    }
}
