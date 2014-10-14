package exception;

public class TokenExpiredException extends Exception {
    public TokenExpiredException() {
        super("Token expired.");
    }

    public TokenExpiredException(String controllerName) {
        super("Token expired in " + controllerName + ".");
    }
}
