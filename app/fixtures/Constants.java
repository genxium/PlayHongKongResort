package fixtures;

import java.util.HashMap;

public class Constants {
	public static final int INFO_NOT_LOGGED_IN = 1001;
	public static int INFO_USER_NOT_FOUND = 1003;
	public static int INFO_PSW_ERR = 1004;

	public static final int INFO_ACTIVITY_HAS_BEGUN = 3007;
	public static final int INFO_ACTIVITY_APPLIED_LIMIT = 3008;
	public static final int INFO_ACTIVITY_SELECTED_LIMIT = 3009;

	public static final int INFO_CAPTCHA_NOT_MATCHED = 4001;

	// language names
	public static final String EN_GB = "en_gb";
	public static final String EN_US = "en_us";
	public static final String ZH_HK = "zh_hk";
	public static final String ZH_CN = "zh_cn";

	// field names
	public static final String ADMIN_EMAIL = "admin@qiutongqu.com";
	public static final String HONGKONGRESORT_TEAM = "hongkongresort_team";
	public static final String WELCOME = "welcome";
	public static final String VERIFY_INSTRUCTION = "verify_instruction";
	public static final String RESET_PASSWORD_TITLE = "reset_password_title";
	public static final String RESET_PASSWORD_INSTRUCTION = "reset_password_instruction";

	// language map
	public static final HashMap<String, String> ZH_HK_MAP = new HashMap<>();
	static {
		ZH_HK_MAP.put(HONGKONGRESORT_TEAM, "求同去團隊");
		ZH_HK_MAP.put(WELCOME, "感謝註冊本站會員");
		ZH_HK_MAP.put(VERIFY_INSTRUCTION, "你好 %s, 請點擊以下鏈接以完成郵箱驗證: %s");
		ZH_HK_MAP.put(RESET_PASSWORD_TITLE, "密碼重置指引");
		ZH_HK_MAP.put(RESET_PASSWORD_INSTRUCTION, "你好 %s, 請點擊以下鏈接以完成密碼重置: %s");
	}

	public static final HashMap<String, HashMap<String, String>> LANG_MAP = new HashMap<>();
	static {
		LANG_MAP.put(ZH_HK, ZH_HK_MAP);
	}
}
