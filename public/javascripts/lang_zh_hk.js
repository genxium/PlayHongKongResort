// (prerequisite) implement String.format if not existing, reference: http://stackoverflow.com/questions/610406/javascript-equivalent-to-printf-string-format 
if (!String.prototype.format) {
  String.prototype.format = function(format) {
    var args = Array.prototype.slice.call(arguments, 1);
    return format.replace(/{(\d+)}/g, function(match, number) { 
      return typeof args[number] != 'undefined'
        ? args[number] 
        : match
      ;
    });
  };
}

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
	delete: "刪除",
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
	replied_to: "對{0}: ",	
	time_ascendant: "時間順序",
	time_descendant: "時間倒序",
	joined_activities: "參與的活動",
	posted_activities: "發起的活動",
	question: "Q & A",
	participant: "參與者",
	assessment: "評價",
	view_assessment: "查看Ta收到的評價 >",
	assessment_disabled: "未可用",
	present: "已出席",
	absent: "未出席",
	from: "來自",
	content: "內容",
	by_host: "由 {0} 發起"
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
	comment_disabled_activity_not_accepted: "活動尚未通過審核"
};

var HINTS = {
	username: "用戶名",
	email: "電郵地址",
	password: "密碼",
	confirm_password: "確認密碼",
	title: "標題",
	address: "地址",
	content: "內容",
	captcha: "驗證碼",
	reply: "對@{0}說: "
};

var ALERTS = {
	applicant_num_exceeded: "報名人數已達到上限",
	deadline_expired: "報名已截止",
	image_selection_limit_exceeded: "請選擇{0}張以內圖片",
	invalid_email_format: "無效的電郵地址",
	wrong_password: "密碼錯誤",
	user_not_existing_or_wrong_password: "用戶不存在或密碼錯誤",	
	comment_requirement: "請填寫5～128個字",
	comment_question_not_submitted: "問題未提交",
	registered: "註冊成功",
	not_registered: "註冊不成功",
	not_permitted_to_view_detail: "你無權瀏覽此頁",
	activity_not_begun: "活動尚未開始",
	assessment_submitted: "評價已成功提交",
	assessment_not_submitted: "評價未提交",
	choose_one_image: "一次只能選擇一張圖片"
};
