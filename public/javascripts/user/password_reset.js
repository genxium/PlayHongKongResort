var g_fieldPassword = null;
var g_fieldRetypePassword = null;
var g_spanHint = null;
var g_spanRetypeHint = null;
var g_btnConfirm = null;

var g_email = null;
var g_code = null;

$(document).ready(function(){

	var bundle = extractTagAndParams(window.location.href);
	var params = bundle["params"];
	g_email = params["email"]; 
	g_keyCode = params["code"];

	g_fieldPassword = $("#password-1");
	g_fieldRetypePassword = $("#password-2");
	g_spanHint = $("#hint");
	g_spanRetypeHint = $("retype-hint");
	g_btnConfirm = $("#submit");

	g_fieldPassword.on("input keyup paste", function(evt){
		evt.preventDefault();
		g_spanHint.empty();
		g_spanHint.text("");
		var password = $(this).val();
		if(password == null || password.length ==0 ) return;
		if(validatePassword(password)) return;
		g_spanHint.text(" Password can only contain 6~20 alphabet letters and numbers");
	});	

	g_fieldRetypePassword.on("input keyup paster", function(){
		var password2 = $(this).val();
		var password1 = g_fieldPassword.val();
		g_spanRetypeHint.empty();
		g_spanRetypeHint.text("");
		if(validatePasswordConfirm()) return;
		g_spanRetypeHint.text(" Doesn't match!");
	});
	
	g_btnConfirm.click(function(evt) {
		evt.preventDefault();
		var password = g_fieldPassword.val();
		if(!validatePassword(password)) {
			alert("Password can only contain 6~20 alphabet letters and numbers!");
			return;
		}
		if(!validatePasswordConfirm()) {
			alert("Password doesn't match!");
			return;
		}	
		var params = {};
		params[g_keyEmail] = g_email;
		params[g_keyPasswordResetCode] = g_code;
		params[g_keyPassword] = password;
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

	function validatePasswordConfirm(){
		var password = g_fieldPassword.val();		
		var passwordConfirm = g_fieldRetypePassword.val();
		if(password == null || passwordConfirm == null) return false;
		if(password != passwordConfirm) return false;
		return true;
	} 
});
