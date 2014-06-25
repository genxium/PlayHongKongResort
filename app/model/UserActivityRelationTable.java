package model;

public class UserActivityRelationTable {
	public static int invalid=0;
	public static int hosted=(1<<0);
	public static int applied=(1<<1);
	public static int selected=(1<<2);
	public static int present=(1<<3);
	public static int absent=(1<<4);

	public static String TABLE ="UserActivityRelationTable";
	public static String ID ="UserActivityRelationTableId";
	public static String USER_ID ="UserId";
	public static String ACTIVITY_ID ="ActivityId";
	public static String RELATION ="Relation";
	public static String GENERATED_TIME ="GeneratedTime";
	public static String LAST_APPLYING_TIME ="LastApplyingTime";
	public static String LAST_ACCEPTED_TIME ="LastAcceptedTime";
	public static String LAST_REJECTED_TIME ="LastRejectedTime";
}
