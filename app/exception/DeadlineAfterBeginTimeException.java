package exception;

public class DeadlineAfterBeginTimeException extends Exception {
    public DeadlineAfterBeginTimeException() {
        super("Deadline can not be set after the begin time of an activity.");
    }
}
