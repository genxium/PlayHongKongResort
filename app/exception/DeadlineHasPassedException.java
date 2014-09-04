package exception;

public class DeadlineHasPassedException extends Exception {
	public DeadlineHasPassedException() {
		super("Deadline has passed.");
	}
}
