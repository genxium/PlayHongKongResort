package utilities;

import models.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class General {

    protected static Calendar s_localCalendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));

    public static Calendar localCalendar() {
        return s_localCalendar;
    }

    public static boolean validateEmail(final String email) {
        try {
            return email.matches(BasicUser.EMAIL_PATTERN);
        } catch (Exception e) {
	        return false;
        }
    }
	
    public static boolean validateName(final String name) {
        try {
            return name.matches(BasicUser.NAME_PATTERN);
        } catch (Exception e) {
	        return false;
        }
    }

    public static boolean validatePassword(final String password) {
        try {
            return password.matches(User.PASSWORD_PATTERN);
        } catch (Exception e) {
	        return false;
        }
    }

    public static boolean validateAssessmentContent(final String content) {
        try {
            return content.matches(Assessment.CONTENT_PATTERN);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateCommentContent(final String content) {
        try {
            return content.matches(Comment.CONTENT_PATTERN);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateActivityTitle(final String title) {
        try {
            return title.matches(Activity.TITLE_PATTERN);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateActivityAddress(final String address) {
        try {
            return address.matches(Activity.ADDR_PATTERN);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateActivityContent(final String content) {
        try {
            return content.matches(Activity.CONTENT_PATTERN);
        } catch (Exception e) {
            return false;
        }
    }

    public static long millisec() {
	    return System.currentTimeMillis();
    }

}
