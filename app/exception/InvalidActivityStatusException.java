package exception;

import java.lang.Exception;

public class InvalidActivityStatusException extends Exception {
	public InvalidActivityStatusException() {
		super("Invalid activity status.");
	}
}
