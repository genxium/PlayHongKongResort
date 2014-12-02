package utilities;

import play.Logger;

public class Loggy {

    public static void e(String tag, String method, Exception e) {
        Logger.error(tag + "." + method + ", " + e.getMessage());
    }

}
