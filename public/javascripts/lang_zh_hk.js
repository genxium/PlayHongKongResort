// (prerequisite) Implement/Override String.format, reference: http://stackoverflow.com/questions/610406/javascript-equivalent-to-printf-string-format 
String.prototype.format = function() {
    var formatted = this;
    for (var i = 0; i < arguments.length; i++) {
        var regexp = new RegExp('\\{'+i+'\\}', 'gi');
        formatted = formatted.replace(regexp, arguments[i]);
    }
    return formatted;
};

// activity status names
var RELATION_NAMES = {
	applied: "已報名",
	selected: "被選上啦",
	present: "與席",
	absent: "缺席",
	assessed: "已評價",
	hosted: ""
};

var STATUS_NAMES = {
	created: "已創建",
	pending: "等待審核",
	rejected: "未通過審核",
	accepted: "已通過審核"
}

// constant titles
var TITLES = {
	create: "創建",
	profile: "個人",
	register: "註冊帳號",
	login: "登入",
	logout: "登出",	
	forgot_password: "忘記密碼",
	upload: "上傳",
	choose_picture: "選擇圖片",
	edit: "編輯",
	'delete': "刪除",
	update: "更新",
	save: "保存",
	submit: "提交",
	cancel: "取消",
	detail: "詳情",
	view: "詳細",
	join: "報名",
	begin_time: "開始時間",
	deadline: "報名截止",
	selected: "選中",
	applied: "報名",
	yes: "是",
	no: "否",
	submit_comment_question: "提交問題",
	submit_comment_reply: "提交",
	collapse: "收起",
	reply: "回覆",
	replied_to: "對@{0}: ",	
	time_ascendant: "時間順序",
	time_descendant: "時間倒序",
	joined_activities: "參與的活動",
	hosted_activities: "發起的活動",
	question: "Q & A",
	participant: "參與者",
	assessment: "評價",
	view_assessment: "查看Ta收到的評價 >",
	assessment_disabled: "不可用",
	present: "已出席",
	absent: "未出席",
	from: "來自",
	content: "內容",
	by_host: "由 @{0} 發起",
	view_all_replies: "查看全部回覆({0})",
	about_us: "關於我們",
	contact_us: "聯絡我們",
	privacy_policy: "隱私聲明",
	resend_email_verification: "重發驗證郵件",
	submit_participant_selection: "提交",
	new_password: "新密碼",
	confirm_new_password: "確認新密碼",
	input_to_reset_password: "請輸入新密碼並提交以完成修改",
	add_image: "+",
	send: "發送",
	check_all: "全選",
	uncheck_all: "清空已選",
	ascendant: "順序",
	descendant: "倒序",
	automatic: "自動",	
	time: "時間",
	last_accepted_time: "審核完成時間"
};

var MESSAGES = {
	uploading: "正在上傳...",
	uploaded: "上傳成功",
	upload_failed: "上傳失敗",
	delete_activity_confirmation: "確定要刪除此活動嗎？",
	username_requirement: "用戶名需為6-32位由英文字母， 數字或符號'_'組成",
	username_valid: "此用戶名可以使用",
	username_invalid: "此用戶名已被佔用",
	email_valid: "此電郵地址可以使用",
	email_invalid: "此電郵地址已被佔用",
	email_requirement: "請填寫有效的電郵地址",
	password_requirement: "密碼需為6~32位由英文字母，數字或符號'#'，'_'及'!'組成",	
	password_confirm_requirement: "密碼不匹配",	
	comment_reply_not_submitted: "回覆未提交",
	assessment_requirement: "請填寫－～64個字",
	comment_disabled_activity_has_begun: "Q&A時間已經結束",
	comment_disabled_activity_not_accepted: "活動尚未通過審核",
	email_verification_success: "Hi @{0}， 你的電郵地址{1}已完成驗證， {2}秒後將為你跳轉到首頁",
	email_verification_failure: "Hi @{0}， 很遺憾你的電郵地址{1}未能通過驗證， 如需幫助請與hongkongresort@126.com聯繫",
	about_us: "關於我們\n\r\n\r仲未寫",
	privacy_policy: "隱私聲明\n\r\n\r仲未寫",
	please_wait: "請稍後...",
	email_verification_sent: "驗證郵件已發送到你的註冊郵箱{0}, 請查收",
	email_verification_not_sent: "未能成功發送驗證郵件, 請重試",
	image_selection_requirement: "請選擇不多於3張圖片輔助活動說明, 每張圖片應不超過2MB(2048KB), 如需編輯或壓縮圖片可以使用<a target='_blank' href='http://www.pixlr.com'>Pixlr</a>",
	activity_created: "活動已創建, 你可以在個人主頁查看相關信息",
	activity_saved: "更新已保存",
	activity_not_saved: "保存不成功",
	activity_saving: "正在保存...",
	instructions_sent_to: "密碼重設郵件已發送至{0}, 請查收",
	instructions_not_sent: "郵件發送失敗， 請重試",
	password_reset_tips: "請輸入你的註冊電郵地址",
	notice: "請注意",
	password_reset_notice_1: "若電郵地址未在本站註冊， 你將無法收到所需郵件",
	password_reset_notice_2: "若電郵地址不存在， 你將無法收到所需郵件",
	password_reset_notice_3: "電郵地址所關聯帳號在申請密碼重設期間不會失效， 如需凍結帳號請通過<a href=\"mailto:admin@qiutongqu.com?subject='account suspension'\">admin@qiutongqu.com</a>聯繫我們"
};

var HINTS = {
	username: "用戶名",
	email: "電郵地址",
	password: "密碼",
	confirm_password: "確認密碼",
	title: "標題",
	address: "地址",
	content: "內容",
	activity_title: "標題",
	activity_address: "活動地點",
	activity_content: "活動內容",
	captcha: "驗證碼",
	reply: "對@{0}說: "
};

var ALERTS = {
	captcha_not_matched: "驗證碼錯誤",
	creation_limit_exceeded: "創建活動太頻繁囉， 請稍後片刻",
	applicant_num_exceeded: "報名人數已達到上限",
	selected_num_exceeded: "已選人數已達到上限",
	deadline_expired: "報名已截止",
	image_selection_limit_exceeded: "請選擇{0}張以內圖片",
	invalid_email_format: "無效的電郵地址",
	wrong_password: "密碼錯誤",
	user_not_existing: "用戶不存在",
	comment_requirement: "請填寫5～128個字",
	comment_question_not_submitted: "問題未提交",
	registered: "註冊成功",
	not_registered: "註冊不成功",
	not_permitted_to_view_detail: "你無權瀏覽此頁",
	activity_not_begun: "活動尚未開始",
	assessment_submitted: "評價已成功提交",
	assessment_not_submitted: "評價未提交",
	no_assessment: "Oops! 暫時沒有人給Ta留下評價喔",
	choose_one_image: "一次只能選擇一張圖片",
	please_log_in: "請先登入",
	image_selection_requirement: "請選擇不多於3張圖片輔助活動說明, 每張圖片應不超過2MB(2048KB)",
	deadline_behind_begin_time: "報名截止時間應在活動開始時間之前",
	please_follow_activity_field_instructions: "請根據字數要求填寫各項",
	not_updated: "更新不成功"
};
