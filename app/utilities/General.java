package utilities;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;

public class General{
    public static boolean validateEmail(String email) {
        boolean result = true;
        try{
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException e) {
            result = false;
        }
        return result;
    }
}
