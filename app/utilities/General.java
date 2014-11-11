package utilities;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class General {

    protected static Calendar s_localCalendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));

    public static Calendar localCalendar() {
        return s_localCalendar;
    }

    public static boolean validateEmail(String email) {

        try {
            String regex = "/^((([a-z]|\\d|[!#\\$%&'\\*\\+\\-\\/=\\?\\^_`{\\|}~]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])+(\\.([a-z]|\\d|[!#\\$%&'\\*\\+\\-\\/=\\?\\^_`{\\|}~]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])+)*)|((\\x22)((((\\x20|\\x09)*(\\x0d\\x0a))?(\\x20|\\x09)+)?(([\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x7f]|\\x21|[\\x23-\\x5b]|[\\x5d-\\x7e]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(\\\\([\\x01-\\x09\\x0b\\x0c\\x0d-\\x7f]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]))))*(((\\x20|\\x09)*(\\x0d\\x0a))?(\\x20|\\x09)+)?(\\x22)))@((([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_|~|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))\\.)+(([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_|~|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))\\.?$/i"; // referred to https://jqueryui.com/resources/demos/dialog/modal-form.html;
            email.matches(regex);
	        return true;
        } catch (Exception e) {
	        return false;
        }

    }
	
    public static boolean validateName(String name) {

        try {
            String regex = "/^[0-9a-zA-Z]{6,20}$/"; // referred to https://jqueryui.com/resources/demos/dialog/modal-form.html;
            name.matches(regex);
    	    return true;
        } catch (Exception e) {
	        return false;
        }
    }

    public static boolean validatePassword(String password) {

        try {
            String regex = "/^[0-9a-zA-Z]{6,20}$/"; // referred to https://jqueryui.com/resources/demos/dialog/modal-form.html;
            password.matches(regex);
	        return true;
        } catch (Exception e) {
	        return false;
        }

    }

    public static long millisec() {
	    Calendar localCld = localCalendar();
	    return localCld.getTimeInMillis();
    }

    public static long localMillisec() {
	    Calendar localCld = localCalendar();
	    return (localCld.getTimeInMillis() + localCld.getTimeZone().getRawOffset());
    }
}
