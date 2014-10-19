package exception;

public class ActivityNotAcceptedException extends Exception {
    public ActivityNotAcceptedException() {
        super("Activity is not accepted yet.");
    }
}
