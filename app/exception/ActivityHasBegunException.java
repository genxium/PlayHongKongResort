package exception;

public class ActivityHasBegunException extends Exception {
	public ActivityHasBegunException() {
		super("Activity has begun, operation denied.");
	} 
}
