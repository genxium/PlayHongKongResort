var g_spanHint = null;
var g_fieldEmail = null;
var g_btnSubmit = null;
var g_sectionResponse = null;

function onEmailInput(evt) {
	evt.preventDefault();
	g_spanHint.empty();
	var email = $(this).val();
	if(email == null || email.length == 0) return;
	if(!validateEmail(email)) {
		g_spanHint.text(MESSAGES["email_requirement"]);
		return;
	}	
	
	var params={};
	params[g_keyEmail] = email;
	$.ajax({
		type: "GET",
		url: "/user/email/duplicate",
		data: params,
		success: function(data, status, xhr){
			if (isStandardSuccess(data)) {
				g_spanHint.text(ALERTS["user_not_existing"]);        
				return;
			}
			if (isStandardFailure(data)) {
				g_spanHint.text("");        
				return;
			}
		},
		error: function(xhr, status, err){
		}
	});
}

function onEmailRequest(evt) {
	evt.preventDefault();
	var email = g_fieldEmail.val();
	var params = {};
	params[g_keyEmail] = email;
	var aButton = $(evt.srcElement ? evt.srcElement : evt.target);
	disableField(aButton);
	$.ajax({
		type: "GET",
		url: "/user/password/request",
		data: params,
		success: function(data, status, xhr) {
			enableField(aButton);
			g_sectionResponse.empty();	
			$("<span>", {
				text: MESSAGES["instructions_sent_to"].format(email)
			}).appendTo(g_sectionResponse);
		},
		error: function(xhr, status, err) {
			enableField(aButton);
			alert(MESSAGES["instructions_not_sent"]);
		}
	});	
}

$(document).ready(function(){
	
	initTopbar($("#topbar"));
	initFooter($("#footer-content"));

	$("#reset-tips").html(MESSAGES["password_reset_tips"]);
	$("#notice").html(MESSAGES["notice"]);
	var noticeList = $("#notice-list");
	$("<li>", {
		html: MESSAGES["password_reset_notice_1"]
	}).appendTo(noticeList);
	$("<li>", {
		html: MESSAGES["password_reset_notice_2"]
	}).appendTo(noticeList);
	$("<li>", {
		html: MESSAGES["password_reset_notice_3"]
	}).appendTo(noticeList);

	g_fieldEmail = $("#email");
	g_spanHint = $("#hint");	
	g_btnSubmit = $("#submit");	
	g_btnSubmit.text(TITLES["send"]);
	g_sectionResponse = $("#response");

	g_fieldEmail.on("input keyup paste", onEmailInput);
	g_btnSubmit.click(onEmailRequest);
});
