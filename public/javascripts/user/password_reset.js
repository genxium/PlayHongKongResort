var g_fieldPassword = null;
var g_fieldRetypePassword = null;
var g_spanHint = null;
var g_btnConfirm = null;

var g_email = null;
var g_code = null;

$(document).ready(function(){

	var params=extractParams(window.location.href);
	for(var i = 0; i < params.length; i++){
		var param=params[i];
		var pair=param.split("=");
		if(pair[0] == g_keyEmail)	g_email = pair[1];
		if(pair[0] == g_keyCode)	g_code = pair[1]; 
	}

	g_fieldPassword = $("#password-1");
	g_fieldRetypePassword = $("#password-2");
	g_spanHint = $("#hint");
	g_btnConfirm = $("#submit");

	g_fieldRetypePassword.on("input keyup paster", function(){
		var password2 = $(this).val();
		var password1 = g_fieldPassword.val();
		g_spanHint.empty();
		if(password1 == password2) return;
		g_spanHint.text("Doesn't match!");
	});
	
	g_btnConfirm.click(function(evt) {
		evt.preventDefault();
		var password1 = g_fieldPassword.val();
		var password2 = g_fieldRetypePassword.val();
		if(password1 != password2) {
			alert("Password doesn't match!");
			return;
		}	
		var params = {};
		params[g_keyEmail] = g_email;
		params[g_keyPasswordResetCode] = g_code;
		params[g_keyPassword] = password1;
		$.ajax({
			type: "POST",
			url: "/user/password/confirm",
			data: params,
			success: function(data, status, xhr) {
				alert("Password reset successfully!");
			},
			error: function(xhr, status, err) {
				alert("Password NOT reset");
			}
		});
	});
});
