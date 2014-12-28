function RegisterWidget(name, nameCheck, email, emailCheck, psw, pswCheck, pswConfirm, pswConfirmCheck, btn, onSuccess, onError, captcha) {
	// prototypes: onSuccess(data), onError(err)		
	this.name = name;
	this.nameCheck = nameCheck;
	this.email = email;
	this.emailCheck = emailCheck;
	this.psw = psw;
	this.pswCheck = pswCheck;
	this.pswConfirm = pswConfirm;
	this.pswConfirmCheck = pswConfirmCheck;
	this.btn = btn;
	this.onSuccess = onSuccess;
	this.onError = onError;
	this.captcha = captcha;
	this.hide = function() {
		this.name.hide();	
		this.nameCheck.hide();
		this.email.hide();
		this.emailCheck.hide();
		this.psw.hide();
		this.pswCheck.hide();
		this.pswConfirm.hide();
		this.pswConfirmCheck.hide();
		this.btn.hide();
		this.captcha.hide();
	};
	this.show = function() {
		this.name.show();	
		this.nameCheck.show();
		this.email.show();
		this.emailCheck.show();
		this.psw.show();
		this.pswCheck.show();
		this.pswConfirm.show();
		this.pswConfirmCheck.show();
		this.btn.show();
		this.captcha.show();
	}; 
	this.empty = function() {
		this.name.val("");	
		this.nameCheck.text("");
		this.email.val("");
		this.emailCheck.text("");
		this.psw.val("");
		this.pswCheck.text("");
		this.pswConfirm.val("");
		this.pswConfirmCheck.text("");
		this.captcha.input.val("");
		this.captcha.img.attr("src", "/captcha?" + g_keySid + "=" + this.captcha.sid + "&ts=" + new Date().getTime());
	};

	this.btn.click(this, function(evt) {
		var widget = evt.data;
		var username = widget.name.val();
		var email = widget.email.val();
		var password = widget.psw.val();
		var passwordConfirm = widget.pswConfirm.val();

		if(username == null || username.length == 0 || !validateName(username)
		    || email == null || email.length == 0 || !validateEmail(email)
		    || password == null || password.length == 0 || !validatePassword(password) || !validatePasswordConfirm(password, passwordConfirm)) return;

		var params = {};
		params[g_keyName] = username;
		params[g_keyEmail] = email;
		params[g_keyPassword] = password;
		params[g_keySid] = widget.captcha.sid;
		params[g_keyCaptcha] = widget.captcha.input.val();

		$.ajax({
		    type: "POST",
		    url: "/user/register",
		    data: params,
		    success: function(data, status, xhr){
				widget.empty();
				if (widget.onSuccess == null) return;
				widget.onSuccess(data);
		    },
		    error: function(xhr, status, err){
				alert("Oops! Not registered...");
				if (widget.onError == null) return;
				widget.onError(err);
		    }
		});
	});

	this.name.on("focusin focusout", this.nameCheck, function(evt){
		evt.preventDefault();
		var nameCheck = evt.data;
		nameCheck.empty();
		nameCheck.text("");
		var nameVal = $(this).val();
		if(nameVal == null || nameVal.length == 0) return;
		if(!validateName(nameVal)) {
			nameCheck.text(" Username can only contain 6~20 alphabet letters and numbers");
			return;
		}

		var params={};
		params[g_keyName] = nameVal;
		$.ajax({
			type: "GET",
			url: "/user/name/duplicate",
			data: params,
			success: function(data, status, xhr){
			    nameCheck.text(" This username can be used :)");        
			},
			error: function(xhr, status, err){
			    nameCheck.text(" This username cannot be used :(");        
			}
		});
	});	

	this.email.on("focusin focusout", this.emailCheck, function(evt){
		evt.preventDefault();
		var emailCheck = evt.data;
		emailCheck.empty();
		emailCheck.text("");
		var emailVal = $(this).val();
		if(emailVal == null || emailVal.length == 0) return;
		if(!validateEmail(emailVal)) {
			 emailCheck.text(" Not valid email format");
			 return;
		}
		var params = {};
		params[g_keyEmail] = emailVal;
		$.ajax({
			type: "GET",
			url: "/user/email/duplicate",
			data: params,
			success: function(data, status, xhr){
				emailCheck.text(" This email can be used :)");        
			},
			error: function(xhr, status, err){
				emailCheck.text(" This email cannot be used :(");        
			}
		});
	});	

	this.psw.on("input keyup paste", this.pswCheck, function(evt){
		evt.preventDefault();
		var pswCheck = evt.data;
		pswCheck.empty();
		pswCheck.text("");
		var pswVal = $(this).val();
		if(pswVal == null || pswVal.length ==0 ) return;
		if(validatePassword(pswVal))	return;
		pswCheck.text(" Password can only contain 6~20 alphabet letters and numbers");
	});	
 
	this.pswConfirm.on("input keyup paste", {0: this.pswConfirmCheck, 1: this.psw}, function(evt){
		evt.preventDefault();
		var pswConfirmCheck = evt.data[0];
		var pswConfirmVal = $(this).val();
		var pswVal = evt.data[1].val();
		pswConfirmCheck.empty();
		pswConfirmCheck.text("");
		if(validatePasswordConfirm(pswVal, pswConfirmVal))	return;
		pswConfirmCheck.text(" Doesn't match! ");
	});	
}

function generateRegisterWidget(par, onSuccess, onError){	
	par.empty();
	var tbl = $('<table>', {
		style: "border-collapse:separate; border-spacing:5pt; margin-bottom: 2pt"
	}).appendTo(par);

	var row1 = $('<tr>').appendTo(tbl);
	var cell11=$('<td>').appendTo(row1);
	var fieldName = $('<input>', {
		type: "text",
		style: "font-size: 15pt",
		placeHolder: "Username"	
	}).appendTo(cell11);

	var cell12=$('<td>').appendTo(row1);
	var spName = $('<span>').appendTo(cell12);

	var row2 = $('<tr>').appendTo(tbl);
	var cell21 = $('<td>').appendTo(row2);
	var fieldEmail = $('<input>', {
		type: "text",
		style: "font-size: 15pt",
		placeHolder: "Email"
	}).appendTo(cell21);
	var cell22=$('<td>').appendTo(row2);
	var spEmail = $('<span>').appendTo(cell22);

	var row3=$('<tr>').appendTo(tbl);
	var cell31=$('<td>').appendTo(row3);
	var cell32=$('<td>').appendTo(row3);
	var fieldPsw = $('<input>', {
		type: "password",
		style: "font-size: 15pt",
		placeHolder: "Password"	
	}).appendTo(cell31);
	var spanPsw = $('<span>').appendTo(cell32);

	var row4 = $('<tr>').appendTo(tbl);
	var cell41 = $('<td>').appendTo(row4);
	var cell42 = $('<td>').appendTo(row4);
	var fieldPswConfirm = $('<input>', {
		type: "password",
		style: "font-size: 15pt",
		placeHolder: "Confirm Password"	
	}).appendTo(cell41);
	var spPswConfirm = $('<span>').appendTo(cell42);

	var sid = generateUuid();
	var captcha = new Captcha(sid);
	captcha.appendCaptcha(par);

	var btnRow = $('<p>').appendTo(par);
	var btnRegister = $('<button>', {
		style: "font-family: Serif; font-size: 15pt; background-color: Teal; color: white",
		text: "Register"	
	}).appendTo(btnRow);

	return new RegisterWidget(fieldName, spName, fieldEmail, spEmail, fieldPsw, spanPsw, fieldPswConfirm, spPswConfirm, btnRegister, onSuccess, onError, captcha);
}

function validatePasswordConfirm(psw, pswConfirm){
	if(psw == null || pswConfirm == null) return false;
	if(psw != pswConfirm) return false;
	return true;
} 
