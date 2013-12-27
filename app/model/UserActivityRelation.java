package model;

import java.util.HashMap;
import java.util.Map;

public class UserActivityRelation {
	
	public enum RelationType{
		host(0), 
		applied(1),
		selected(2),
		present(3),
		absent(4);

		private static final Map<Integer, RelationType> relationLookUpMap = new HashMap<Integer, RelationType>();

	    static {
	        for (RelationType type : RelationType.values()) {
	            relationLookUpMap.put(type.value, type);
	        }
	    }

	    private final int value;

	    private RelationType(int value) {
	        this.value = value;
	    }

	    public static RelationType getTypeForValue(int value) {
	        return relationLookUpMap.get(value);
	    }
	};
}
