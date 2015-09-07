package utilities;

import play.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Loggy {

    public static void e(String tag, String method, Exception e) {
        if (e == null) return;
        StringWriter stktraces = new StringWriter();
        e.printStackTrace(new PrintWriter(stktraces));
        Logger.error(tag + "." + method + ",\n" + stktraces.toString());
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
