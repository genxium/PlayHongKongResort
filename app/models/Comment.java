package models;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.DBCommander;
import org.json.simple.JSONObject;
import utilities.Converter;
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

    protected Integer m_parentId = null;
    protected Integer m_predecessorId = null;
    protected Integer m_numChildren = null;

	protected List<Comment> m_subCommentList = null;
	public void setSubCommentList(List<Comment> subCommentList) {
		m_subCommentList = subCommentList;
	}

    public Comment(JSONObject commentJson) {
        super(commentJson);
        if (commentJson.containsKey(PARENT_ID)) m_parentId = Converter.toInteger(commentJson.get(PARENT_ID));
        if (commentJson.containsKey(PREDECESSOR_ID))    m_predecessorId = Converter.toInteger(commentJson.get(PREDECESSOR_ID));
        if (commentJson.containsKey(NUM_CHILDREN))  m_numChildren = Converter.toInteger(commentJson.get(NUM_CHILDREN));

    }

    public ObjectNode toSubCommentObjectNode(final Long viewerId) {
	    ObjectNode ret = super.toObjectNode();
	    try {
		    ret.put(PARENT_ID, m_parentId);
		    ret.put(FROM_PLAYER, m_fromPlayer.toObjectNode(viewerId));
		    ret.put(TO_PLAYER, m_toPlayer.toObjectNode(viewerId));
	    } catch (Exception e) {
		    Loggy.e(TAG, "toSubCommentObjectNode", e);
	    }
	    return ret;
    }

    public ObjectNode toObjectNode(final boolean single, final Long viewerId) {
	    ObjectNode ret = super.toObjectNode();
	    try {
		    ret.put(PARENT_ID, m_parentId);
			Player fromPlayer = DBCommander.queryPlayer(m_from);
			if (fromPlayer == null) return null;
		    ret.put(FROM_PLAYER, fromPlayer.toObjectNode(viewerId));
		    ret.put(NUM_CHILDREN, m_numChildren.toString());

		    if (single || m_subCommentList == null) return ret;
            ArrayNode subCommentsNode = new ArrayNode(JsonNodeFactory.instance);
		    for (Comment subComment : m_subCommentList) {
			    subCommentsNode.add(subComment.toSubCommentObjectNode(viewerId));
		    }
		    ret.put(SUB_COMMENTS, subCommentsNode);
	    } catch (Exception e) {
		    Loggy.e(TAG, "toObjectNode", e);
	    }
	    return ret;
    }
}

