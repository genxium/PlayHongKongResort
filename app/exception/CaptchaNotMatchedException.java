package exception;

public class CaptchaNotMatchedException extends Exception {
    public CaptchaNotMatchedException() {
        super("Captcha not matched.");
    }
}
