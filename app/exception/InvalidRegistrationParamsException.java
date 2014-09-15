package exception;

public class InvalidRegistrationParamsException extends Exception {
    public InvalidRegistrationParamsException() {
        super("Invalid registration parameters.");
    }
}
