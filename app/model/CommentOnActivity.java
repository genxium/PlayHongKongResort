package model;

import java.sql.Timestamp;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SQLCommander;
import org.json.simple.JSONObject;
import play.libs.Json;

public class CommentOnActivity {

    public static final Integer TYPE_QA=0;
    public static final Integer TYPE_ASSESSMENT=1;

    public static final String ID="CommentAId";
    public static final String CONTENT="CommentAContent";
    public static final String COMMENTER_ID="CommenterId";
    public static final String ACTIVITY_ID="ActivityId";
    public static final String PARENT_ID="ParentId";
    public static final String PREDECESSOR_ID="PredecessorId";
    public static final String COMMENT_TYPE="CommentType";
    public static final String GENERATED_TIME="GeneratedTime";

    public static final String COMMENTER_NAME="CommenterName";

    protected Integer m_id=null;
    protected String m_content=null;
    protected Integer m_commenterId=null;
    protected Integer m_activityId=null;
    protected Integer m_parentId=null;
    protected Integer m_predecessorId=null;
    protected Integer m_commentType=null;
    protected Timestamp m_generatedTime=null;

    public Integer getId() {return m_id;}
    public String getContent() {return m_content;}
    public Integer getCommenterId() {return m_commenterId;}
    public Integer getActivityId() {return m_activityId;}
    public Integer getParentId() {return m_parentId;}
    public Integer getPredecessorId() {return m_predecessorId;}
    public Integer getCommentType() {return m_commentType;}
    public Timestamp getGeneratedTime() {return m_generatedTime;}


    public CommentOnActivity(JSONObject commentJson){
        do{
            try{
                m_id=(Integer)commentJson.get(ID);
                m_content=(String)commentJson.get(CONTENT);
                m_commenterId=(Integer)commentJson.get(COMMENTER_ID);
                m_activityId=(Integer)commentJson.get(ACTIVITY_ID);
                m_parentId=(Integer)commentJson.get(PARENT_ID);
                m_predecessorId=(Integer)commentJson.get(PREDECESSOR_ID);
                m_commentType=(Integer)commentJson.get(COMMENT_TYPE);
                m_generatedTime=(Timestamp)commentJson.get(GENERATED_TIME);
            }catch(Exception e){

            }
        }while(false);
    }

    public ObjectNode toObjectNode(){
        ObjectNode ret = Json.newObject();;
        do{
            try{
                ret.put(ID, m_id);
                ret.put(PARENT_ID, m_parentId);
                ret.put(CONTENT, m_content);
                ret.put(COMMENTER_NAME, SQLCommander.queryUser(m_commenterId).getName());
                ret.put(GENERATED_TIME, m_generatedTime.toString());
            } catch (Exception e){

            }
        }while(false);
        return ret;
    }
}

