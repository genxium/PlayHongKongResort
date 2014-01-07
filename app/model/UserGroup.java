package model;

import java.util.HashMap;
import java.util.Map;

public class UserGroup{
	public enum GroupType{
		visitor(0), 
		user(1),
		manager(2),
		admin(3);

		private static final Map<Integer, GroupType> userGroupLookUpMap = new HashMap<Integer, GroupType>();

	    static {
	        for (GroupType type : GroupType.values()) {
	            userGroupLookUpMap.put(type.value, type);
	        }
	    }

	    private final int value;

	    private GroupType(int value) {
	        this.value = value;
	    }

	    public static GroupType getTypeForValue(int value) {
	        return userGroupLookUpMap.get(value);
	    }
	};		
}