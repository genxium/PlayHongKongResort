package utilities;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Converter {

    protected static SimpleDateFormat s_dateFormat = null;

    public static SimpleDateFormat getDateFormat() {
        if (s_dateFormat == null) {
            s_dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            s_dateFormat.setTimeZone(General.localCalendar().getTimeZone());
        }
        return s_dateFormat;
    }

    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte aDigest : digest) {
                buffer.append(Integer.toHexString((aDigest & 0xFF) | 0x100).substring(1, 3));
            }
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String generateToken(String playername, String password) {
        java.util.Date date = new java.util.Date();
        String dateStr = date.toString();
        String base = playername + dateStr + password;
        return md5(base);
    }

    public static Integer toInteger(Object obj) {
        if (obj instanceof Integer) return (Integer) obj;
        else if (obj instanceof Long) return ((Long) obj).intValue();
        else if (obj instanceof String) return Integer.valueOf((String) obj);
        else return null;
    }

    public static Long toLong(Object obj) {
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        else if (obj instanceof Long) return (Long) obj;
        else if (obj instanceof String) return Long.valueOf((String) obj);
        else if (obj instanceof BigInteger) return ((BigInteger) obj).longValue();
        else return null;
    }

    public static String gmtMillisecToLocalTime(long millisecs) {
        Timestamp ts = new Timestamp(millisecs);
        return getDateFormat().format(ts);
    }

    public static long localDateToGmtMillisec(String dateStr) throws ParseException {
        return getDateFormat().parse(dateStr).getTime();
    }
}
