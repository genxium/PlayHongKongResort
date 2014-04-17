package model;

import java.sql.Timestamp;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import play.libs.Json;

public class CommentOnActivity {
    public static final String ID="CommentAId";
    public static final String CONTENT="CommentAContent";
    public static final String COMMENTER_ID="CommenterId";
    public static final String ACTIVITY_ID="ActivityId";
    public static final String PREDECESSOR_ID="PredecessorId";
    public static final String COMMENT_TYPE="CommentType";
    public static final String GENERATED_TIME="GeneratedTime";

    protected Integer m_id=null;
    protected String m_content=null;
    protected Integer m_commenterId=null;
    protected Integer m_activityId=null;
    protected Integer m_predecessorId=null;
    protected Integer m_commentType=null;
    protected Timestamp m_generatedTime=null;

    public Integer getId() {return m_id;}
    public String getContent() {return m_content;}
    public Integer getCommenterId() {return m_commenterId;}
    public Integer getActivityId() {return m_activityId;}
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
            ret.put(ID, String.valueOf(m_id));
            ret.put(CONTENT, m_content);
            ret.put(GENERATED_TIME, m_generatedTime.toString());
        }while(false);
        return ret;
    }
}

