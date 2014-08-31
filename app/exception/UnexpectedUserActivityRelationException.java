package exception;

import java.lang.Exception;

public class UnexpectedUserActivityRelationException extends Exception {
	public UnexpectedUserActivityRelationException() {
		super("Unexpected user-activity-relation");
	}

	public UnexpectedUserActivityRelationException(String message) {
		super(message);
	}
}
