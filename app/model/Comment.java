package model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SQLCommander;
import dao.SQLHelper;
import org.json.simple.JSONObject;
import play.libs.Json;

import java.sql.Timestamp;
import java.util.List;

public class Comment {

    public static final Integer MIN_CONTENT_LENGTH=5;

    public static final String TABLE="comment";
    public static final String ID="id";
    public static final String COMMENT_ID="comment_id";
    public static final String CONTENT="content";
    public static final String COMMENTER_ID="commenter_id";
    public static final String ACTIVITY_ID="activity_id";
    public static final String PARENT_ID="parent_id";
    public static final String PREDECESSOR_ID="predecessor_id";
    public static final String GENERATED_TIME="generated_time";

    public static final String COMMENTER_NAME="commenter_name";
    public static final String REPLYEE_ID="replyee_id";
    public static final String REPLYEE_NAME="replyee_name";
    public static final String SUB_COMMENTS="sub_comments";

    protected Integer m_id=null;
    protected String m_content=null;
    protected Integer m_commenterId=null;
    protected Integer m_activityId=null;
    protected Integer m_parentId=null;
    protected Integer m_predecessorId=null;
    protected Timestamp m_generatedTime=null;

    public Integer getId() {return m_id;}
    public String getContent() {return m_content;}
    public Integer getCommenterId() {return m_commenterId;}
    public Integer getActivityId() {return m_activityId;}
    public Integer getParentId() {return m_parentId;}
    public Integer getPredecessorId() {return m_predecessorId;}
    public Timestamp getGeneratedTime() {return m_generatedTime;}

    public Comment(JSONObject commentJson){
	    try{
		if(commentJson.containsKey(ID)){
		    m_id=(Integer)commentJson.get(ID);
		}
		if(commentJson.containsKey(CONTENT)){
		    m_content=(String)commentJson.get(CONTENT);
		}
		if(commentJson.containsKey(COMMENTER_ID)){
		    m_commenterId=(Integer)commentJson.get(COMMENTER_ID);
		}
		if(commentJson.containsKey(ACTIVITY_ID)){
		    m_activityId=(Integer)commentJson.get(ACTIVITY_ID);
		}
		if(commentJson.containsKey(PARENT_ID)){
		    m_parentId=(Integer)commentJson.get(PARENT_ID);
		}
		if(commentJson.containsKey(PREDECESSOR_ID)){
		    m_predecessorId=(Integer)commentJson.get(PREDECESSOR_ID);
		}
		if(commentJson.containsKey(GENERATED_TIME)){
		    m_generatedTime=(Timestamp)commentJson.get(GENERATED_TIME);
		}
	    }catch(Exception e){

	    }
    }

    public ObjectNode toObjectNode(){
        ObjectNode ret = Json.newObject();;
	try{
		ret.put(ID, m_id);
		ret.put(PARENT_ID, m_parentId);
		ret.put(CONTENT, m_content);
		ret.put(COMMENTER_NAME, SQLCommander.queryUser(m_commenterId).getName());
		ret.put(GENERATED_TIME, m_generatedTime.toString());
	} catch (Exception e){

	}
        return ret;
    }

    public ObjectNode toSubCommentObjectNode(){
        ObjectNode ret = Json.newObject();;
	do{
		try{
			ret.put(ID, m_id);
			ret.put(PARENT_ID, m_parentId);
			ret.put(CONTENT, m_content);
			ret.put(COMMENTER_ID, m_commenterId);
			ret.put(COMMENTER_NAME, SQLCommander.queryUser(m_commenterId).getName());
			ret.put(GENERATED_TIME, m_generatedTime.toString());
					
			Comment predecessorComment=SQLCommander.queryComment(m_predecessorId);
			if(predecessorComment==null) break;
			Integer replyeeId=predecessorComment.getCommenterId();
			String replyeeName=SQLCommander.queryUser(replyeeId).getName();
			ret.put(REPLYEE_ID, replyeeId);
			ret.put(REPLYEE_NAME, replyeeName);
		} catch (Exception e){

		}
	}while(false);
        return ret;
    }

    public ObjectNode toObjectNodeWithSubComments(){
        ObjectNode ret = Json.newObject();
	try{
		ret.put(ID, m_id);
		ret.put(PARENT_ID, m_parentId);
		ret.put(CONTENT, m_content);
		ret.put(COMMENTER_ID, m_commenterId);
		ret.put(COMMENTER_NAME, SQLCommander.queryUser(m_commenterId).getName());
		ret.put(GENERATED_TIME, m_generatedTime.toString());
		List<Comment> subComments=SQLCommander.querySubComments(m_id, SQLCommander.INITIAL_REF_INDEX, ID, SQLHelper.DESCEND, null, SQLCommander.DIRECTION_FORWARD);

		ArrayNode subCommentsNode=new ArrayNode(JsonNodeFactory.instance);
		for(Comment subComment : subComments){
		    subCommentsNode.add(subComment.toSubCommentObjectNode());
		}
		ret.put(SUB_COMMENTS, subCommentsNode);
	}catch(Exception e){

	}
        return ret;
    }
}
