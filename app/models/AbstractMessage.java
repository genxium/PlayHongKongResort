package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SimpleMap;
import utilities.Loggy;

public abstract class AbstractMessage extends AbstractSimpleMessage {
        public static final String TAG = AbstractMessage.class.getName();

        public static final String BUNDLE = "bundle";
        public static final String RET = "ret";

        public static final String FROM = "from";
        public static final String TO = "to";

        public static final String FROM_PLAYER = "froplayer";
        public static final String TO_PLAYER = "to_player";

        public static final String GENERATED_TIME = "generated_time";

        protected Long from = null;
        protected Long to = null;
        protected Long generatedTime = null;

        public Long getFrom() {
                return from;
        }

        public void setFrom(final long data) {
                from = data;
        }

        public Long getTo() {
                return to;
        }

        protected Player fromPlayer = null;
        protected Player toPlayer = null;

        public Player getFromPlayer() {
                return fromPlayer;
        }

        public void setFromPlayer(final Player player) {
                fromPlayer = player;
        }

        public Player getToPlayer() {
                return toPlayer;
        }

        public void setToPlayer(final Player player) {
                toPlayer = player;
        }

        public AbstractMessage() {
                super();
        }

        public AbstractMessage(final SimpleMap data) {
                super(data);
                from = data.getLong(FROM);
                to = data.getLong(TO);
                generatedTime = data.getLong(GENERATED_TIME);
        }

        public ObjectNode toObjectNode() {
                ObjectNode ret = super.toObjectNode();
                try {
                        ret.put(FROM, from);
                        ret.put(TO, to);
                        ret.put(GENERATED_TIME, String.valueOf(generatedTime));
                } catch (Exception e) {
                        Loggy.e(TAG, "toObjectNode", e);
                }
                return ret;
        }
}
