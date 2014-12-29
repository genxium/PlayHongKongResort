package exception;

public class InvalidActivityStatusException extends Exception {
	public InvalidActivityStatusException() {
		super("Invalid activity status.");
	}
}
