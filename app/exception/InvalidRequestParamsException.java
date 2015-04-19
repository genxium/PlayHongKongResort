package exception;

public class InvalidRequestParamsException extends Exception {
    public InvalidRequestParamsException() {
        super("Invalid query parameters.");
    }
}
