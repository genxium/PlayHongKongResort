package exception;

/**
 * Created by memoriki on 15/9/14.
 */
public class InvalidLoginParamsException extends Exception {
    public InvalidLoginParamsException() {
        super("Invalid login params.");
    }
}
