/*
 * This is the base class of image model
 * */

package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Play;
import play.libs.Json;
import org.json.simple.JSONObject;
import org.apache.commons.io.FilenameUtils;

public class Image {

    public static String PROD_URL_PREFIX = "/images/";
    public static String DEV_URL_PREFIX = "/assets/images/";
    public static String PROD_FOLDER_PATH = "/var/www/html/images/";
    public static String DEV_FOLDER_PATH = Play.application().path().getAbsolutePath() + "/public/images/";

    public static String TABLE = "image";
    public static String ID = "id";
    public static String URL = "url";

    private int m_imageId = 0;

    public int getImageId() {
        return m_imageId;
    }

    public void setImageId(int imageId) {
        m_imageId = imageId;
    }

    private String m_imageURL = null;

    public String getImageURL() {
        return m_imageURL;
    }

    public void setImageURL(String imageURL) {
        m_imageURL = imageURL;
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
        String baseName = FilenameUtils.getBaseName(m_imageURL);
        String extension = FilenameUtils.getExtension(m_imageURL);
        return getFolderPath() + baseName + "." + extension;
    }

    public Image(JSONObject imageJson) {
        try {
            if (imageJson.containsKey(ID)) {
                m_imageId = (Integer) imageJson.get(Image.ID);
            }
            m_imageURL = (String) imageJson.get(Image.URL);
        } catch (Exception e) {

        }
    }

    public ObjectNode toObjectNode() {
        ObjectNode ret = Json.newObject();
        try {
            ret.put(ID, m_imageId);
            ret.put(URL, m_imageURL);
        } catch (Exception e) {

        }
        return ret;
    }
}
