var g_fieldPassword = null;
var g_fieldRetypePassword = null;
var g_spanHint = null;
var g_spanRetypeHint = null;
var g_btnSubmit = null;

var g_email = null;
var g_code = null;

$(document).ready(function(){

	$("#reset-hint").text(TITLES["input_to_reset_password"]);
	$("#new-password").text(TITLES["new_password"]);
	$("#confirm-new-password").text(TITLES["confirm_new_password"]);

	var bundle = extractTagAndParams(window.location.href);
	var params = bundle["params"];
	g_email = params["email"]; 
	g_code = params["code"];

	g_fieldPassword = $("#password-1");
	g_fieldRetypePassword = $("#password-2");
	g_spanHint = $("#hint");
	g_spanRetypeHint = $("retype-hint");
	g_btnSubmit = $("#submit");

	g_btnSubmit.text(TITLES["submit"]);

	g_fieldPassword.on("input keyup paste", function(evt){
		evt.preventDefault();
		g_spanHint.empty();
		g_spanHint.text("");
		var password = $(this).val();
		if(password == null || password.length ==0 ) return;
		if(validatePassword(password)) return;
		g_spanHint.text(MESSAGES["password_requirement"]);
	});	

	g_fieldRetypePassword.on("input keyup paster", function(){
		var password2 = $(this).val();
		var password1 = g_fieldPassword.val();
		g_spanRetypeHint.empty();
		g_spanRetypeHint.text("");
		if(validatePasswordConfirm()) return;
		g_spanRetypeHint.text(MESSAGES["password_confirm_requirement"]);
	});
	
	g_btnSubmit.click(function(evt) {
		evt.preventDefault();
		var password = g_fieldPassword.val();
		if(!validatePassword(password)) {
			alert(MESSAGES["password_requirement"]);
			return;
		}
		if(!validatePasswordConfirm()) {
			alert(MESSAGES["password_confirm_requirement"]);
			return;
		}	
		var params = {};
		params[g_keyEmail] = g_email;
		params[g_keyPasswordResetCode] = g_code;
		params[g_keyPassword] = password;

		var aButton = $(evt.srcElement ? evt.srcElement : evt.target); 
		disableField(aButton);
		$.ajax({
			type: "POST",
			url: "/user/password/confirm",
			data: params,
			success: function(data, status, xhr) {
				enableField(aButton);
				alert("Password reset successfully!");
			},
			error: function(xhr, status, err) {
				enableField(aButton);
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
