package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SimpleMap;

import java.util.regex.Pattern;

public class Image extends AbstractModel {

        public static final String TABLE = "image";
        public static final String URL = "url";
        public static final String META_ID = "meta_id";
        public static final String META_TYPE = "meta_type";

        public static final String REMOTE_NAME = "remote_name";
        public static final String BUCKET = "bucket";
        public static final String CDN_ID = "cdn_id";
        public static final String GENERATED_TIME = "generated_time";

        public static final int SINGLE_FILE_SIZE_LIMIT = (1 << 21);

        public static final String[] QUERY_FIELDS = {ID, URL, META_ID, META_TYPE, REMOTE_NAME, BUCKET, CDN_ID};

        public static final int TYPE_OWNER = 0;
        public static final int TYPE_PLAYER = 1;
        public static final int TYPE_ACTIVITY = 2;

        public static final Pattern REMOTE_NAME_PATTERN = Pattern.compile("^([\\d]+)_([\\d]+)$", Pattern.UNICODE_CHARACTER_CLASS);

        protected String m_url = null;
        protected Long m_metaId = null;
        protected Integer m_metaType = null;
        protected String m_remoteName = null;
        protected String m_bucket = null;
        protected Integer m_cdnId = null;
        protected Long m_generatedTime = null;

        public String getUrl() {
                return m_url;
        }

        public Long getMetaId() {
                return m_metaId;
        }

        public Integer getMetaType() {
                return m_metaType;
        }

        public String getRemoteName() {
                return m_remoteName;
        }

        public String getBucket() {
                return m_bucket;
        }

        public Integer getCDNId() {
                return m_cdnId;
        }

        public Image(final SimpleMap data) {
                super(data);
                m_url = data.getStr(URL);
                m_metaId = data.getLong(META_ID);
                m_metaType = data.getInt(META_TYPE);
                m_cdnId = data.getInt(CDN_ID);
                m_remoteName = data.getStr(REMOTE_NAME);
                m_bucket = data.getStr(BUCKET);
                m_generatedTime = data.getLong(GENERATED_TIME);
        }

        public ObjectNode toObjectNode() {
                final ObjectNode ret = super.toObjectNode();
                ret.put(URL, m_url);
                return ret;
        }
}
