package model;

public class UserActivityRelation {
	
	public static String idKey="UserActivityRelationTableId";
	public static String userIdKey="UserId";
	public static String activityIdKey="ActivityId";
	public static String relationIdKey="UserActivityRelationId";
	public static String generatedTimeKey="GeneratedTime";
	
	public enum RelationType{
		host, 
		applied,
		selected,
		present,
		absent
	};
}
