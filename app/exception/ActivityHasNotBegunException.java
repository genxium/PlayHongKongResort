package exception;

public class ActivityHasNotBegunException extends Exception {

	public ActivityHasNotBegunException() {
		super("Activity has not begun yet.");
	}
	
}
