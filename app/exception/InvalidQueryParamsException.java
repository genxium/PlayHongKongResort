package exception;

public class InvalidQueryParamsException extends Exception {
    public InvalidQueryParamsException() {
        super("Invalid query parameters.");
    }
}
