package model;

public class UserActivityRelation {
	public static int invalid=0;
	public static int hosted=(1<<0);
	public static int applied=(1<<1);
	public static int selected=(1<<2);
	public static int present=(1<<3);
	public static int absent=(1<<4);

	public static String TABLE ="user_activity_relation";
	public static String ID ="id";
	public static String USER_ID ="user_id";
	public static String ACTIVITY_ID ="activity_id";
	public static String RELATION ="relation";
	public static String GENERATED_TIME ="generated_time";
	public static String LAST_APPLYING_TIME ="last_applying_time";
	public static String LAST_ACCEPTED_TIME ="last_accepted_time";
	public static String LAST_REJECTED_TIME ="last_rejected_time";
}
