package exception;

public class ActivityCreationLimitException extends Exception {
    public ActivityCreationLimitException() {
        super("Activity creation reaches frequency limit.");
    }
}
