// activity status names
var STATUS_NAMES = {
	applied: "已報名",
	selected: "被選上啦",
	present: "與席",
	absent: "缺席",
	assessed: "已評價",
	hosted: ""
};

// constant titles
var TITLES = {
	create: "創建",
	profile: "個人資料",
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
	no: "否"
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
};

var HINTS = {
	username: "用戶名",
	email: "電郵地址",
	password: "密碼",
	confirm_password: "確認密碼",
	title: "標題",
	address: "地址",
	content: "內容"
};

var ALERTS = {
	applicant_num_exceeded: "報名人數已達到上限",
	deadline_expired: "報名已截止",
	image_selection_limit_exceeded: "請選擇%d張以內圖片",
	invalid_email_format: "無效的電郵地址",
	wrong_password: "密碼錯誤",
	user_not_existing_or_wrong_password: "用戶不存在或密碼錯誤"	
};
