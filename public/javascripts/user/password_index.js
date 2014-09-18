var g_spanHint = null;
var g_fieldEmail = null;
var g_btnSubmit = null;
var g_sectionResponse = null;

function onEmailInput(evt) {
	g_spanHint.empty();;
	var value = $(this).val();
	if(!validateEmail(value)) {
		g_spanHint.text("Not valid email format");
		return;
	}	
	
	var params={};
	params[g_keyEmail]=email;
	$.ajax({
		type: "GET",
		url: "/user/email/duplicate",
		data: params,
		success: function(data, status, xhr){
			g_spanHint.text(" Account not existing ");        
		},
		error: function(xhr, status, err){
			g_spanHint.text(" Accoutn exists ");        
		}
	});
}

function onEmailRequest(evt) {
	evt.preventDefault();
	var email = g_fieldEmail.val();
	var params = {};
	params[g_keyEmail] = email;
	$.ajax({
		type: "GET",
		url: "/usr/password/request",
		data: params,
		success: function(data, status, xhr) {
			g_sectionResponse.empty();	
			$("<span>", {
				text: "Instructions have been sent to "
			}).appendTo(g_sectionResponse);
			var linkSpan = $("<span>").appendTo(g_sectionResponse);
			$("<a>", {
				text: email,
				href: email
			}).appendTo(linkSpan);
		},
		error: function(xhr, status, err) {

		}
	});	
}

$(document).ready(function(){
	g_fieldEmail = $("#email");
	g_spanHint = $("#hint");	
	g_btnSubmit = $("#submit");	
	g_sectionResponse = $("#response");

	g_fieldEmail.on("input keyup paste", onEmailInput);
	g_btnSubmit.submit(onEmailRequest);
});
