package models;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SQLCommander;
import dao.SQLHelper;
import org.json.simple.JSONObject;
import play.libs.Json;
import utilities.Converter;
import utilities.DataUtils;

import java.sql.Timestamp;
import java.util.List;

public class Comment {

    public static final String TAG = Comment.class.getName();

    public static final Integer MIN_CONTENT_LENGTH = 5;

    public static final String TABLE = "comment";
    public static final String ID = "id";
    public static final String COMMENT_ID = "comment_id";
    public static final String CONTENT = "content";
    public static final String FROM = "from";
    public static final String ACTIVITY_ID = "activity_id";
    public static final String PARENT_ID = "parent_id";
    public static final String PREDECESSOR_ID = "predecessor_id";
    public static final String GENERATED_TIME = "generated_time";
    public static final String NUM_CHILDREN = "num_children";

    public static final String FROM_NAME = "from_name";
    public static final String TO = "to";
    public static final String TO_NAME = "to_name";
    public static final String SUB_COMMENTS = "sub_comments";

    public static final String COUNT = "count";
    public static final String PAGE = "page";
    public static final String PAGE_ST = "page_st";
    public static final String PAGE_ED = "page_ed";
    public static final String COMMENTS = "comments";

    protected Integer m_id = null;
    protected String m_content = null;
    protected Integer m_from = null;
    protected Integer m_to = null;
    protected Integer m_activityId = null;
    protected Integer m_parentId = null;
    protected Integer m_predecessorId = null;
    protected Timestamp m_generatedTime = null;
    protected Integer m_numChildren = null;

    public Integer getId() {
        return m_id;
    }

    public String getContent() {
        return m_content;
    }

    public Integer getCommenterId() {
        return m_from;
    }

    public Integer getActivityId() {
        return m_activityId;
    }

    public Integer getParentId() {
        return m_parentId;
    }

    public Integer getPredecessorId() {
        return m_predecessorId;
    }

    public Timestamp getGeneratedTime() {
        return m_generatedTime;
    }

    public Integer getNumChildren() {
        return m_numChildren;
    }

    public Comment(JSONObject commentJson) {

        if (commentJson.containsKey(ID))    m_id = Converter.toInteger(commentJson.get(ID));
        if (commentJson.containsKey(CONTENT))   m_content = (String) commentJson.get(CONTENT);
        if (commentJson.containsKey(FROM))  m_from = Converter.toInteger(commentJson.get(FROM));
        if (commentJson.containsKey(TO))  m_to = Converter.toInteger(commentJson.get(TO));
        if (commentJson.containsKey(ACTIVITY_ID))   m_activityId = Converter.toInteger(commentJson.get(ACTIVITY_ID));
        if (commentJson.containsKey(PARENT_ID)) m_parentId = Converter.toInteger(commentJson.get(PARENT_ID));
        if (commentJson.containsKey(PREDECESSOR_ID))    m_predecessorId = Converter.toInteger(commentJson.get(PREDECESSOR_ID));
        if (commentJson.containsKey(GENERATED_TIME))    m_generatedTime = (Timestamp) commentJson.get(GENERATED_TIME);
        if (commentJson.containsKey(NUM_CHILDREN))  m_numChildren = Converter.toInteger(commentJson.get(NUM_CHILDREN));

    }

    public ObjectNode toSubCommentObjectNode() {
	    ObjectNode ret = Json.newObject();
	    try {
		    ret.put(ID, m_id);
		    ret.put(PARENT_ID, m_parentId);
		    ret.put(CONTENT, m_content);
		    ret.put(FROM, m_from);
		    ret.put(FROM_NAME, SQLCommander.queryUser(m_from).getName());
		    ret.put(TO, m_to);
		    ret.put(TO_NAME, SQLCommander.queryUser(m_to).getName());
		    ret.put(GENERATED_TIME, m_generatedTime.toString());
	    } catch (Exception e) {
		    DataUtils.log(TAG, "toSubCommentObjectNode", e);
	    }
	    return ret;
    }

    public ObjectNode toObjectNode(boolean single) {
	    ObjectNode ret = Json.newObject();
	    try {
		    ret.put(ID, m_id);
		    ret.put(PARENT_ID, m_parentId);
		    ret.put(CONTENT, m_content);
		    ret.put(FROM, m_from);
		    ret.put(FROM_NAME, SQLCommander.queryUser(m_from).getName());
		    ret.put(GENERATED_TIME, m_generatedTime.toString());
		    ret.put(NUM_CHILDREN, m_numChildren.toString());

		    if (single) return ret;
		    int limit = 3;
		    List<Comment> subComments = SQLCommander.querySubComments(m_id, SQLCommander.INITIAL_REF_INDEX, ID, SQLHelper.DESCEND, 3, SQLCommander.DIRECTION_FORWARD);

		    ArrayNode subCommentsNode = new ArrayNode(JsonNodeFactory.instance);
		    for (Comment subComment : subComments) {
			    subCommentsNode.add(subComment.toSubCommentObjectNode());
		    }
		    ret.put(SUB_COMMENTS, subCommentsNode);
	    } catch (Exception e) {
		    DataUtils.log(TAG, "toObjectNode", e);
	    }
	    return ret;
    }
}

