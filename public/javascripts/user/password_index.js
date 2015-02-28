var g_spanHint = null;
var g_fieldEmail = null;
var g_btnSubmit = null;
var g_sectionResponse = null;

function onEmailInput(evt) {
	g_spanHint.empty();;
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
				g_spanHint.text(" Account not existing ");        
				return;
			}
			if (isStandardFailure(data)) {
				g_spanHint.text(" Account exists ");        
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
				text: "Instructions have been sent to "
			}).appendTo(g_sectionResponse);
			var linkSpan = $("<span>").appendTo(g_sectionResponse);
			$("<a>", {
				text: email
			}).appendTo(linkSpan);
		},
		error: function(xhr, status, err) {
			enableField(aButton);
			alert("Email request rejected!");
		}
	});	
}

$(document).ready(function(){
	g_fieldEmail = $("#email");
	g_spanHint = $("#hint");	
	g_btnSubmit = $("#submit");	
	g_sectionResponse = $("#response");

	g_fieldEmail.on("input keyup paste", onEmailInput);
	g_btnSubmit.click(onEmailRequest);
});
