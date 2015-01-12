package utilities;

import play.Logger;

public class Loggy {

    public static void e(String tag, String method, Exception e) {
        if (e == null || e.getMessage() == null) return;
        Logger.error(tag + "." + method + ", " + e.getMessage());
    }

    public static void e(String tag, String method, String message) {
        Logger.error(tag + "." + method + ", " + message);
    }

    public static void i(String tag, String method, String message) {
        Logger.info(tag + "." + method + ", " + message);
    }

    public static void d(String tag, String method, String message) {
        Logger.debug(tag + "." + method + ", " + message);
    }
}
