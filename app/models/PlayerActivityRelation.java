package models;

public class PlayerActivityRelation extends AbstractModel {

    public static final int FULLMASK = ((1 << 16) - 1);
    public static final int INVALID = 0;
    public static final int APPLIED = (1 << 0);
    public static final int SELECTED = (1 << 1);
    public static final int PRESENT = (1 << 2);
    public static final int ABSENT = (1 << 3);
    public static final int ASSESSED = (1 << 4);
    public static final int HOSTED = (1 << 5);

    public static final int[] PRESENT_STATES = {SELECTED | PRESENT, SELECTED | PRESENT | ASSESSED};
    public static final int[] ABSENT_STATES = {SELECTED | ABSENT, SELECTED | ABSENT | ASSESSED};
    public static final int[] APPLIED_STATES = {APPLIED};
    public static final int[] SELECTED_STATES = {SELECTED, SELECTED | PRESENT, SELECTED | ABSENT, SELECTED | PRESENT | ASSESSED, SELECTED | ABSENT | ASSESSED};

    // (PLAYER_ID, ACTIVITY_ID) is UNIQUE
    // RELATION is indexed
    public static String TABLE = "player_activity_relation";
    public static String PLAYER_ID = "player_id";
    public static String ACTIVITY_ID = "activity_id";
    public static String RELATION = "relation";
    public static String LAST_APPLYING_TIME = "last_applying_time";
    public static String LAST_SELECTED_TIME = "last_selected_time";
    public static String GENERATED_TIME = "generated_time";

    public static String VIEWEE_ID = "viewee_id";

    public static String SID = "sid";
    public static String CAPTCHA = "captcha";

    public static int maskRelation(final int relation, final Integer originalRelation) {
        int ret = INVALID;
        if (originalRelation != null) ret = originalRelation;
        switch (relation) {
            case SELECTED:
                if ((ret & APPLIED) > 0) ret &= (FULLMASK ^ APPLIED);
                ret |= SELECTED;
                break;
            case APPLIED:
                if ((ret & SELECTED) > 0) ret &= (FULLMASK ^ SELECTED);
                ret |= APPLIED;
                break;
            case PRESENT:
                if ((ret & ABSENT) > 0) ret &= (FULLMASK ^ ABSENT);
                ret |= PRESENT;
                break;
            case ABSENT:
                if ((ret & PRESENT) > 0) ret &= (FULLMASK ^ PRESENT);
                ret |= ABSENT;
                break;
            case ASSESSED:
                ret |= ASSESSED;
                break;
            default:
                break;
        }
        return ret;
    }
}
