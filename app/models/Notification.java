package models;

public class Notification {

    public static int INVALID = (-1);

    public static String ID = "id";
    public static String IS_READ = "is_read";
    public static String FROM = "from";
    public static String TO = "to";
    public static String CONTENT = "content";
    public static String ACTIVITY_ID = "activity_id";

    protected String m_id = null;
    protected boolean m_isRead = false;
    protected int m_from = INVALID;
    protected int m_to = INVALID;
    protected String m_content = null;
    protected int m_activityId = INVALID;

}