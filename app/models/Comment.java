package models;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SQLCommander;
import dao.SQLHelper;
import org.json.simple.JSONObject;
import utilities.Converter;
import utilities.Loggy;

import java.util.List;

public class Comment extends AbstractActivityMessage {

    public static final String TAG = Comment.class.getName();

    public static final String CONTENT_PATTERN = "/.{5,128}/u";

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

    public Comment(JSONObject commentJson) {
        super(commentJson);

        if (commentJson.containsKey(PARENT_ID)) m_parentId = Converter.toInteger(commentJson.get(PARENT_ID));
        if (commentJson.containsKey(PREDECESSOR_ID))    m_predecessorId = Converter.toInteger(commentJson.get(PREDECESSOR_ID));
        if (commentJson.containsKey(NUM_CHILDREN))  m_numChildren = Converter.toInteger(commentJson.get(NUM_CHILDREN));

    }

    public ObjectNode toSubCommentObjectNode() {
	    ObjectNode ret = super.toObjectNode();
	    try {
		    ret.put(PARENT_ID, m_parentId);
		    ret.put(FROM_NAME, SQLCommander.queryUser(m_from).getName());
		    ret.put(TO_NAME, SQLCommander.queryUser(m_to).getName());
	    } catch (Exception e) {
		    Loggy.e(TAG, "toSubCommentObjectNode", e);
	    }
	    return ret;
    }

    public ObjectNode toObjectNode(boolean single) {
	    ObjectNode ret = super.toObjectNode();
	    try {
		    ret.put(PARENT_ID, m_parentId);
		    ret.put(FROM_NAME, SQLCommander.queryUser(m_from).getName());
		    ret.put(NUM_CHILDREN, m_numChildren.toString());

		    if (single) return ret;
            List<Comment> subComments = SQLCommander.querySubComments(m_id, SQLCommander.INITIAL_REF_INDEX, ID, SQLHelper.DESCEND, 3, SQLCommander.DIRECTION_FORWARD);

		    ArrayNode subCommentsNode = new ArrayNode(JsonNodeFactory.instance);
		    for (Comment subComment : subComments) {
			    subCommentsNode.add(subComment.toSubCommentObjectNode());
		    }
		    ret.put(SUB_COMMENTS, subCommentsNode);
	    } catch (Exception e) {
		    Loggy.e(TAG, "toObjectNode", e);
	    }
	    return ret;
    }
}

