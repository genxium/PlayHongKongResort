package model;

import java.sql.Timestamp;

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
}

