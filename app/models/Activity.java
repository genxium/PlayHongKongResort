package models;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.DBCommander;
import dao.SimpleMap;
import utilities.General;
import utilities.Loggy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class Activity extends AbstractSimpleMessage {

        public static final String TAG = Activity.class.getName();

        public static final Pattern TITLE_PATTERN = Pattern.compile(".{5,64}", Pattern.UNICODE_CHARACTER_CLASS);
        public static final Pattern ADDR_PATTERN = Pattern.compile(".{5,128}", Pattern.UNICODE_CHARACTER_CLASS);
        public static final Pattern CONTENT_PATTERN = Pattern.compile("[\\s\\S]{15,1024}", Pattern.UNICODE_CHARACTER_CLASS);

        public static final int CREATED = 0;
        public static final int PENDING = 1;
        public static final int REJECTED = 2;
        public static final int ACCEPTED = 3;

        public static final int CREATION_CRITICAL_NUMBER = 2;
        public static final int CREATION_CRITICAL_TIME_INTERVAL_MILLIS = 43200000; // 12 hours

        public static final String TABLE = "activity";
        public static final String TITLE = "title";
        public static final String ADDRESS = "address";
        public static final String CREATED_TIME = "created_time";
        public static final String BEGIN_TIME = "begin_time";
        public static final String DEADLINE = "application_deadline";
        public static final String CAPACITY = "capacity";
        public static final String NUM_APPLIED = "num_applied";
        public static final String NUM_SELECTED = "num_selected";
        public static final String STATUS = "status";
        public static final String HOST_ID = "host_id";
        public static final String HOST = "host";
        public static final String VIEWER = "viewer";

        public static final String LAST_ACCEPTED_TIME = "last_accepted_time";
        public static final String LAST_REJECTED_TIME = "last_rejected_time";

        public static final String PRIORITY = "priority";
        public static final String FILTER_MASK = "filter_mask";
        public static final String ORDER_MASK = "order_mask";

        public static String SELECTED_PARTICIPANTS = "selected_participants";

        public static String IMAGES = "images";

        public static final String ACTIVITIES = "activities";

        public static String[] QUERY_FIELDS = {ID, TITLE, ADDRESS, CONTENT, CREATED_TIME, BEGIN_TIME, DEADLINE, CAPACITY, NUM_APPLIED, NUM_SELECTED, STATUS, HOST_ID, PRIORITY, ORDER_MASK};
        public static final int MAX_APPLIED = 500;
        public static final int MAX_SELECTED = 250;

        // indexed fields
        public static HashMap<Integer, String> ORDER_MAP = new HashMap<>();

        static {
                ORDER_MAP.put(1, LAST_ACCEPTED_TIME);
                ORDER_MAP.put(2, BEGIN_TIME);
                ORDER_MAP.put(4, DEADLINE);
        }

        public static HashMap<String, Integer> REVERSE_ORDER_MAP = new HashMap<>();

        static {
                REVERSE_ORDER_MAP.put(LAST_ACCEPTED_TIME, 1);
                REVERSE_ORDER_MAP.put(BEGIN_TIME, 2);
                REVERSE_ORDER_MAP.put(DEADLINE, 4);
        }

        public static final int[] LAST_ACCEPTED_TIME_MASK_LIST = {1, (1 | 2), (1 | 4), (1 | 2 | 4)};
        public static final int[] BEGIN_TIME_MASK_LIST = {2, (1 | 2), (2 | 4), (1 | 2 | 4)};
        public static final int[] DEADLINE_MASK_LIST = {4, (1 | 4), (2 | 4), (1 | 2 | 4)};

        protected String title = null;

        public String getTitle() {
                return title;
        }

        public void setTitle(final String aTitle) {
                title = aTitle;
        }

        protected Long createdTime = null;

        public long getCreatedTime() {
                return createdTime;
        }

        public void setCreatedTime(final long aCreatedTime) {
                createdTime = aCreatedTime;
        }

        protected Long beginTime = null;

        public long getBeginTime() {
                return beginTime;
        }

        public void setBeginTime(final long aBeginTime) {
                beginTime = aBeginTime;
        }

        protected Long deadline = null;

        public long getDeadline() {
                return deadline;
        }

        public void setDeadline(final long aDeadline) {
                deadline = aDeadline;
        }

        protected Long lastAcceptedTime = null;

        public void setLastAcceptedTime(final long aTime) {
                lastAcceptedTime = aTime;
        }

        protected Long lastRejectedTime = null;

        public void setLastRejectedTime(final long aTime) {
                lastRejectedTime = aTime;
        }

        protected Integer capacity = 0;

        public int getCapacity() {
                return capacity;
        }

        protected Integer numApplied = 0;

        public int getNumApplied() {
                return numApplied;
        }

        public boolean exceededAppliedLimit() {
                return numApplied > MAX_APPLIED;
        }

        protected Integer numSelected = 0;

        public int getNumSelected() {
                return numSelected;
        }

        public boolean exceededSelectedLimit() {
                return numSelected > MAX_SELECTED;
        }

        protected Integer status = CREATED;

        public int getStatus() {
                return status;
        }

        public void setStatus(final int aStatus) {
                status = aStatus;
        }

        protected String address = null;

        public String getAddress() {
                return address;
        }

        public void setAddress(final String aAddress) {
                address = aAddress;
        }

        protected Long hostId = null;

        public Long getHostId() {
                return hostId;
        }

        protected Player host = null;

        public Player getHost() {
                return host;
        }

        public void setHost(final Player aHost) {
                host = aHost;
        }

        protected Player viewer = null;

        public Player getViewer() {
                return viewer;
        }

        public void setViewer(final Player aViewer) {
                viewer = aViewer;
        }

        protected List<Image> imageList = null;

        public void setImageList(final List<Image> aImageList) {
                imageList = aImageList;
        }

        public void addImage(final Image image) {
                if (imageList == null) imageList = new ArrayList<>();
                imageList.add(image);
        }

        protected Integer priority = 0;

        public int getPriority() {
                return priority;
        }

        protected Integer filterMask = 0;

        public int getFilterMask() {
                return filterMask;
        }

        protected Integer orderMask = 0;

        public int getOrderMask() {
                return orderMask;
        }

        public boolean isDeadlineExpired() {
                return General.millisec() > deadline;
        }

        public boolean hasBegun() {
                return General.millisec() > beginTime;
        }

        protected List<BasicPlayer> selectedParticipants = null;
        public void addSelectedParticipant(final BasicPlayer aPlayer) {
                if (selectedParticipants == null) selectedParticipants = new ArrayList<>();
                selectedParticipants.add(aPlayer);
        }

        public Activity() {
                super();
        }

        public Activity(final SimpleMap data) {
                super(data);
                title = data.getStr(TITLE);
                createdTime = data.getLong(CREATED_TIME);
                beginTime = data.getLong(BEGIN_TIME);
                deadline = data.getLong(DEADLINE);
                capacity = data.getInt(CAPACITY);
                numApplied = data.getInt(NUM_APPLIED);
                numSelected = data.getInt(NUM_SELECTED);
                status = data.getInt(STATUS);
                lastAcceptedTime = data.getLong(LAST_ACCEPTED_TIME);
                lastRejectedTime = data.getLong(LAST_REJECTED_TIME);
                address = data.getStr(ADDRESS);
                hostId = data.getLong(HOST_ID);
                priority = data.getInt(PRIORITY);
                filterMask = data.getInt(FILTER_MASK);
                orderMask = data.getInt(ORDER_MASK);
        }

        public ObjectNode toObjectNode(final Long viewerId) {
                final ObjectNode ret = super.toObjectNode();
                try {
                        ret.put(TITLE, title);
                        ret.put(ADDRESS, address);

                        ret.put(CREATED_TIME, String.valueOf(createdTime));
                        ret.put(BEGIN_TIME, String.valueOf(beginTime));
                        ret.put(DEADLINE, String.valueOf(deadline));

                        ret.put(CAPACITY, String.valueOf(capacity));
                        ret.put(NUM_APPLIED, String.valueOf(numApplied));
                        ret.put(NUM_SELECTED, String.valueOf(numSelected));
                        if (host != null) ret.put(HOST, host.toObjectNode(viewerId));

                        if (imageList != null && imageList.size() > 0) {
                                ArrayNode imagesNode = new ArrayNode(JsonNodeFactory.instance);
                                for (Image image : imageList) imagesNode.add(image.toObjectNode());
                                ret.put(ActivityDetail.IMAGES, imagesNode);
                        }

                        if (selectedParticipants != null && selectedParticipants.size() > 0) {
                                ArrayNode selectedParticipantsNode = new ArrayNode(JsonNodeFactory.instance);
                                for (BasicPlayer participant : selectedParticipants)
                                        selectedParticipantsNode.add(participant.toObjectNode(viewerId));
                                ret.put(SELECTED_PARTICIPANTS, selectedParticipantsNode);
                        }

                        ret.put(PRIORITY, String.valueOf(priority));

                        if (viewerId == null) return ret;
                        int relation = DBCommander.queryPlayerActivityRelation(viewerId, id);
                        if (relation != PlayerActivityRelation.INVALID) ret.put(PlayerActivityRelation.RELATION, relation);
                        if (viewerId.equals(host.getId())) ret.put(STATUS, String.valueOf(status));
                        if (viewer != null && viewer.getGroupId() == Player.ADMIN) {
                                ret.put(ORDER_MASK, String.valueOf(orderMask));
                                ret.put(STATUS, String.valueOf(status));
                        }

                } catch (Exception e) {
                        Loggy.e(TAG, "toObjectNode", e);
                }
                return ret;
        }
}
