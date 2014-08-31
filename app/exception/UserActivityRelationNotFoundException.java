package exception;

import java.lang.Exception;

public class UserActivityRelationNotFoundException extends Exception {

	public UserActivityRelationNotFoundException() {
		super("user-activity-relation not found.");
	}
	
	public UserActivityRelationNotFoundException(String message) {
		super(message);
	}
	
}
