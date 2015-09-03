package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.ForeignPartyController;
import dao.SimpleMap;
import fixtures.Constants;
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

	protected String m_password = null;

	public String getPassword() {
		return m_password;
	}

	public void setPassword(String password) {
		m_password = password;
	}

	protected String m_salt = null;

	public String getSalt() {
		return (m_salt == null) ? "" : m_salt;
	}

	public void setSalt(String salt) {
		m_salt = salt;
	}

	protected Integer m_groupId = VISITOR;

	public int getGroupId() {
		return m_groupId;
	}

	public void setGroupId(final Integer groupId) {
		m_groupId = groupId;
	}

	protected Integer m_authenticationStatus = NOT_AUTHENTICATED; 
	public int getAuthenticationStatus() {
		return m_authenticationStatus;
	}

	protected String m_verificationCode = null;

	public String getVerificationCode() {
		return m_verificationCode;
	}

	public void setVerificationCode(String code) {
		m_verificationCode = code;
	}

	protected Integer m_unreadCount = 0;

	public int getUnreadCount() {
		return m_unreadCount;
	}

	protected Integer m_unassessedCount = 0;

	public int getUnassessedCount() {
		return m_unassessedCount;
	}

	protected Integer m_party = ForeignPartyController.PARTY_NONE;

	public Integer getParty() {
		return m_party;
	}

	public void setParty(final Integer party) {
		m_party = party;
	}

	protected String m_lang = null;

	public String getLang() {
		if (m_lang == null) return Constants.ZH_HK; // default to traditional Chinese
		return m_lang;
	}

	protected String m_gender = "";

	public void setGender(final String gender) {
		m_gender = gender;
	}

	public String getGender() {
		return m_gender;
	}

	protected String m_age = "";

	public void setAge(final String age) {
		m_age = age;
	}

	public String getAge() {
		return m_age;
	}

	protected String m_mood = "";

	public void setMood(final String mood) {
		m_mood = mood;
	}

	public String getMood() {
		return m_mood;
	}

	public Player(final String email, final String name) {
		super(email, name);
		m_groupId = VISITOR;
		m_password = "";
	}

	public Player(final SimpleMap data) {
		super(data);
		m_password = data.getStr(PASSWORD);
		m_salt = data.getStr(SALT);
		m_groupId = data.getInt(GROUP_ID);
		m_authenticationStatus = data.getInt(AUTHENTICATION_STATUS);
		m_unreadCount = data.getInt(UNREAD_COUNT);
		m_unassessedCount = data.getInt(UNASSESSED_COUNT);
		m_lang = data.getStr(LANG);
		m_gender = data.getStr(GENDER);
		m_age = data.getStr(AGE);
		m_mood = data.getStr(MOOD);
		m_party = data.getInt(PARTY);
	}

	public ObjectNode toObjectNode(final Long viewerId) {
		ObjectNode ret = super.toObjectNode(viewerId);
		try {
			ret.put(GENDER, String.valueOf(m_gender));
			ret.put(AGE, String.valueOf(m_age));
			ret.put(MOOD, String.valueOf(m_mood));
			ret.put(UNREAD_COUNT, String.valueOf(m_unreadCount));
			ret.put(UNASSESSED_COUNT, String.valueOf(m_unassessedCount));
			ret.put(GROUP_ID, String.valueOf(m_groupId));
			ret.put(AUTHENTICATION_STATUS, String.valueOf(m_authenticationStatus));
			ret.put(PARTY, String.valueOf(m_party));
		} catch (Exception e) {
			Loggy.e(TAG, "toObjectNode", e);
		}
		return ret;
	}
}
