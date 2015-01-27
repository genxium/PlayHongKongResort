package utilities;

import models.Activity;
import models.Assessment;
import models.Comment;
import models.User;

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
            return User.EMAIL_PATTERN.matcher(email).matches();
        } catch (Exception e) {
	        return false;
        }
    }
	
    public static boolean validateName(final String name) {
        try {
            return User.NAME_PATTERN.matcher(name).matches();
        } catch (Exception e) {
	        return false;
        }
    }

    public static boolean validatePassword(final String password) {
        try {
            return User.PASSWORD_PATTERN.matcher(password).matches();
        } catch (Exception e) {
	        return false;
        }
    }

    public static boolean validateAssessmentContent(final String content) {
        try {
            return Assessment.CONTENT_PATTERN.matcher(content).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateCommentContent(final String content) {
        try {
            return Comment.CONTENT_PATTERN.matcher(content).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateActivityTitle(final String title) {
        try {
            return Activity.TITLE_PATTERN.matcher(title).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateActivityAddress(final String address) {
        try {
            return Activity.ADDR_PATTERN.matcher(address).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateActivityContent(final String content) {
        try {
            return Activity.CONTENT_PATTERN.matcher(content).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static long millisec() {
	    return System.currentTimeMillis();
    }

}
