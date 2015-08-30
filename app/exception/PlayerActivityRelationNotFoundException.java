package exception;

public class PlayerActivityRelationNotFoundException extends Exception {

    public PlayerActivityRelationNotFoundException() {
        super("player-activity-relation not found.");
    }

}
