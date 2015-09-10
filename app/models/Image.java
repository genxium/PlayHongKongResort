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

        protected String url = null;
        protected Long metaId = null;
        protected Integer metaType = null;
        protected String remoteName = null;
        protected String bucket = null;
        protected Integer cdnId = null;
        protected Long generatedTime = null;

        public String getUrl() {
                return url;
        }

        public Long getMetaId() {
                return metaId;
        }

        public Integer getMetaType() {
                return metaType;
        }

        public String getRemoteName() {
                return remoteName;
        }

        public String getBucket() {
                return bucket;
        }

        public Integer getCDNId() {
                return cdnId;
        }

        public Image(final SimpleMap data) {
                super(data);
                url = data.getStr(URL);
                metaId = data.getLong(META_ID);
                metaType = data.getInt(META_TYPE);
                cdnId = data.getInt(CDN_ID);
                remoteName = data.getStr(REMOTE_NAME);
                bucket = data.getStr(BUCKET);
                generatedTime = data.getLong(GENERATED_TIME);
        }

        public ObjectNode toObjectNode() {
                final ObjectNode ret = super.toObjectNode();
                ret.put(URL, url);
                return ret;
        }
}
