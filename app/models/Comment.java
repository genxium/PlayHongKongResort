package models;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.DBCommander;
import dao.SimpleMap;
import utilities.Loggy;

import java.util.List;
import java.util.regex.Pattern;

public class Comment extends AbstractActivityMessage {

    public static final String TAG = Comment.class.getName();

    public static final Pattern CONTENT_PATTERN = Pattern.compile(".{5,128}", Pattern.UNICODE_CHARACTER_CLASS);

    public static final String TABLE = "comment";
    public static final String COMMENT_ID = "comment_id";
    public static final String PARENT_ID = "parent_id";
    public static final String PREDECESSOR_ID = "predecessor_id";
    public static final String NUM_CHILDREN = "num_children";

    public static final String SUB_COMMENTS = "sub_comments";
    public static final String COMMENTS = "comments";

    protected Integer parentId = null;
    protected Integer predecessorId = null;
    protected Integer numChildren = null;

    protected List<Comment> subCommentList = null;

    public void setSubCommentList(List<Comment> data) {
        subCommentList = data;
    }

    public Comment(final SimpleMap data) {
        super(data);
        parentId = data.getInt(PARENT_ID);
        predecessorId = data.getInt(PREDECESSOR_ID);
        numChildren = data.getInt(NUM_CHILDREN);
    }

    public ObjectNode toSubCommentObjectNode(final Long viewerId) {
        ObjectNode ret = super.toObjectNode();
        try {
            ret.put(PARENT_ID, parentId);
            ret.put(FROM_PLAYER, fromPlayer.toObjectNode(viewerId));
            ret.put(TO_PLAYER, toPlayer.toObjectNode(viewerId));
        } catch (Exception e) {
            Loggy.e(TAG, "toSubCommentObjectNode", e);
        }
        return ret;
    }

    public ObjectNode toObjectNode(final boolean single, final Long viewerId) {
        ObjectNode ret = super.toObjectNode();
        try {
            ret.put(PARENT_ID, parentId);
            Player fromPlayer = DBCommander.queryPlayer(from);
            if (fromPlayer == null) return null;
            ret.put(FROM_PLAYER, fromPlayer.toObjectNode(viewerId));
            ret.put(NUM_CHILDREN, numChildren.toString());

            if (single || subCommentList == null) return ret;
            ArrayNode subCommentsNode = new ArrayNode(JsonNodeFactory.instance);
            for (Comment subComment : subCommentList) {
                subCommentsNode.add(subComment.toSubCommentObjectNode(viewerId));
            }
            ret.put(SUB_COMMENTS, subCommentsNode);
        } catch (Exception e) {
            Loggy.e(TAG, "toObjectNode", e);
        }
        return ret;
    }
}

