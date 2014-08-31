package exception;

import java.lang.Exception;

public class AccessDeniedException extends Exception {
	
	public AccessDeniedException() {
		super("Access Denied");
	}

	public AccessDeniedException(String message) {
		super(message);
	}	
} 
