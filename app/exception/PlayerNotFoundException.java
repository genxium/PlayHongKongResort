package exception;

public class PlayerNotFoundException extends Exception {
	
	public PlayerNotFoundException() {
		super("Player not found");
	}

}
