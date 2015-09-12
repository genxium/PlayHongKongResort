package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SimpleMap;
import fixtures.Constants;
import utilities.ForeignPartyHelper;
import utilities.Loggy;

import java.util.regex.Pattern;

public class Player extends BasicPlayer {

	public static final int TOKEN_LIFE = 7889230; // in seconds, this equals 3 months

	public static int VISITOR = 0;
	public static int USER = 1;
	public static int MANAGER = 2;
	public static int ADMIN = 3;

	public static final String TABLE = "player";

	public static final String PASSWORD = "password";
	public static final String SALT = "salt";
	public static final String GROUP_ID = "group_id";
	public static final String AUTHENTICATION_STATUS = "authentication_status";
	public static final String GENDER = "gender";
	public static final String AGE = "age";
	public static final String MOOD = "mood";

	public static final String TOKEN = "token";
	public static final String VERIFICATION_CODE = "verification_code";
	public static final String PASSWORD_RESET_CODE = "password_reset_code";

	public static final String UNREAD_COUNT = "unread_count";
	public static final String UNASSESSED_COUNT = "unassessed_count";

	public static final String PARTY = "party";

	public static final String LANG = "lang";

	public static final String[] QUERY_FILEDS = {ID, EMAIL, NAME, AVATAR, PASSWORD, PASSWORD_RESET_CODE, UNREAD_COUNT, UNASSESSED_COUNT, SALT, GROUP_ID, AUTHENTICATION_STATUS, GENDER, AGE, MOOD, VERIFICATION_CODE, PARTY};

	public static final Pattern PASSWORD_PATTERN = Pattern.compile("^[0-9a-zA-Z_#\\!]{6,32}$", Pattern.UNICODE_CHARACTER_CLASS);
	public static final Pattern AGE_PATTERN = Pattern.compile(".{0,16}", Pattern.UNICODE_CHARACTER_CLASS);
	public static final Pattern GENDER_PATTERN = Pattern.compile(".{0,16}", Pattern.UNICODE_CHARACTER_CLASS);
	public static final Pattern MOOD_PATTERN = Pattern.compile(".{0,64}", Pattern.UNICODE_CHARACTER_CLASS);

	public static final int NOT_AUTHENTICATED = 0;
	public static final int EMAIL_AUTHENTICATED = (1 << 0);
	public static final int PHONE_AUTHENTICATED = (1 << 1);

	protected String password = null;

	public String getPassword() {
		return password;
	}

	public void setPassword(final String data) {
		password = data;
	}

	protected String salt = null;

	public String getSalt() {
		return (salt == null) ? "" : salt;
	}

	public void setSalt(final String data) {
		salt = data;
	}

	protected Integer groupId = VISITOR;

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(final Integer data) {
		groupId = data;
	}

	protected Integer authenticationStatus = NOT_AUTHENTICATED; 
	public int getAuthenticationStatus() {
		return authenticationStatus;
	}

	protected String verificationCode = null;

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(final String data) {
		verificationCode = data;
	}

	protected Integer unreadCount = 0;

	public int getUnreadCount() {
		return unreadCount;
	}

	protected Integer unassessedCount = 0;

	public int getUnassessedCount() {
		return unassessedCount;
	}

	protected Integer party = ForeignPartyHelper.PARTY_NONE;

	public Integer getParty() {
		return party;
	}

	public void setParty(final Integer data) {
		party = data;
	}

	protected String lang = null;

	public String getLang() {
		if (lang == null) return Constants.ZH_HK; // default to traditional Chinese
		return lang;
	}

	protected String gender = "";

	public void setGender(final String data) {
		gender = data;
	}

	public String getGender() {
		return gender;
	}

	protected String age = "";

	public void setAge(final String data) {
		age = data;
	}

	public String getAge() {
		return age;
	}

	protected String mood = "";

	public void setMood(final String data) {
		mood = data;
	}

	public String getMood() {
		return mood;
	}

	public Player(final String email, final String name) {
		super(email, name);
		groupId = VISITOR;
		password = "";
	}

	public Player(final SimpleMap data) {
		super(data);
		password = data.getStr(PASSWORD);
		salt = data.getStr(SALT);
		groupId = data.getInt(GROUP_ID);
		authenticationStatus = data.getInt(AUTHENTICATION_STATUS);
		unreadCount = data.getInt(UNREAD_COUNT);
		unassessedCount = data.getInt(UNASSESSED_COUNT);
		lang = data.getStr(LANG);
		gender = data.getStr(GENDER);
		age = data.getStr(AGE);
		mood = data.getStr(MOOD);
		party = data.getInt(PARTY);
	}

	public ObjectNode toObjectNode(final Long viewerId) {
		ObjectNode ret = super.toObjectNode(viewerId);
		try {
			ret.put(GENDER, String.valueOf(gender));
			ret.put(AGE, String.valueOf(age));
			ret.put(MOOD, String.valueOf(mood));
			ret.put(UNREAD_COUNT, String.valueOf(unreadCount));
			ret.put(UNASSESSED_COUNT, String.valueOf(unassessedCount));
			ret.put(GROUP_ID, String.valueOf(groupId));
			ret.put(AUTHENTICATION_STATUS, String.valueOf(authenticationStatus));
			ret.put(PARTY, String.valueOf(party));
		} catch (Exception e) {
			Loggy.e(TAG, "toObjectNode", e);
		}
		return ret;
	}
}
