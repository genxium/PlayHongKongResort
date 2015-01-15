/*
 * This is the base class of image model
 * */

package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import play.Play;
import utilities.Converter;

public class Image extends AbstractModel {

    public static final String PROD_URL_PREFIX = "/images/";
    public static final String DEV_URL_PREFIX = "/assets/images/";
    public static final String PROD_FOLDER_PATH = "/var/www/html/images/";
    public static final String DEV_FOLDER_PATH = Play.application().path().getAbsolutePath() + "/public/images/";

    public static final String TABLE = "image";
    public static final String URL = "url";
    public static final String META_ID = "meta_id";
    public static final String META_TYPE = "meta_type";
    public static final String GENERATED_TIME = "generated_time";

    public static final int SINGLE_FILE_SIZE_LIMIT = (1 << 21);

    public static final String[] QUERY_FIELDS = {ID, URL, META_ID};

    public static final int TYPE_USER = 1;
    public static final int TYPE_ACTIVITY = 2;

    private String m_url = null;
    private Long m_metaId = null;
    private Integer m_metaType = null;
    protected Long m_generatedTime = null;

    public String getUrl() {
        return m_url;
    }

    public Long getMetaId() {
        return m_metaId;
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
        if (imageJson.containsKey(URL)) m_url = (String) imageJson.get(URL);
        if (imageJson.containsKey(META_ID)) m_metaId = Converter.toLong(imageJson.get(META_ID));
        if (imageJson.containsKey(META_TYPE)) m_metaType = Converter.toInteger(imageJson.get(META_TYPE));
        if (imageJson.containsKey(GENERATED_TIME)) m_generatedTime = Converter.toLong(imageJson.get(GENERATED_TIME));
    }

    public ObjectNode toObjectNode() {
        ObjectNode ret = super.toObjectNode();
        ret.put(URL, m_url);
        return ret;
    }
}
