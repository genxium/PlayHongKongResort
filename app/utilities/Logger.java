package utilities;

public class Logger {

    public static void e(String tag, String method, Exception e) {
        System.out.println(tag + "." + method + ", " + e.getMessage());
    }

}
