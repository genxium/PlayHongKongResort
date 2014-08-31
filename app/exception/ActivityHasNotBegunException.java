package exception;

import java.lang.Exception;

public class ActivityHasNotBegunException extends Exception {

	public ActivityHasNotBegunException() {
		super("Activity has not begun yet.");
	}
	
	public ActivityHasNotBegunException(String message) {
		super(message);
	}
	
}
