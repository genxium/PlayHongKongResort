package utilities;

import models.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class General {

    protected static Calendar s_localCalendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));

    public static Calendar localCalendar() {
        return s_localCalendar;
    }

    public static boolean validateEmail(final String email) {
        try {
            Pattern pattern = Pattern.compile(User.EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
            return pattern.matcher(email).matches();
        } catch (Exception e) {
	        return false;
        }
    }
	
    public static boolean validateName(final String name) {
        try {
            Pattern pattern = Pattern.compile(User.NAME_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
            return pattern.matcher(name).matches();
        } catch (Exception e) {
	        return false;
        }
    }

    public static boolean validatePassword(final String password) {
        try {
            Pattern pattern = Pattern.compile(User.PASSWORD_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
            return pattern.matcher(password).matches();
        } catch (Exception e) {
	        return false;
        }
    }

    public static boolean validateAssessmentContent(final String content) {
        try {
            Pattern pattern = Pattern.compile(Assessment.CONTENT_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
            return pattern.matcher(content).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateCommentContent(final String content) {
        try {
            Pattern pattern = Pattern.compile(Comment.CONTENT_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
            return pattern.matcher(content).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateActivityTitle(final String title) {
        try {
            Pattern pattern = Pattern.compile(Activity.TITLE_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
            return pattern.matcher(title).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateActivityAddress(final String address) {
        try {
            Pattern pattern = Pattern.compile(Activity.ADDR_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
            return pattern.matcher(address).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateActivityContent(final String content) {
        try {
            Pattern pattern = Pattern.compile(Activity.CONTENT_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
            return pattern.matcher(content).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static long millisec() {
	    return System.currentTimeMillis();
    }

}
