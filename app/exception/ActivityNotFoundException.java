package exception;

import java.lang.Exception;

public class ActivityNotFoundException extends Exception{
	
	public ActivityNotFoundException() {
		super("Activity not found");
	}	

}
