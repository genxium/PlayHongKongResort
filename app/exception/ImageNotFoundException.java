package exception;

import java.lang.Exception;

public class ImageNotFoundException extends Exception {
	
	public ImageNotFoundException() {
		super("Image not found.");
	}
} 
