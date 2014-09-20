package exception;

public class InvalidPasswordException extends Exception {
	public InvalidPasswordException() {
		super("Invalid password.");
	}
}
