var g_registerWidget = null;
var g_modalRegister = null;
var g_sectionRegister = null;

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

		var btnRegister = $(this);
		disableField(btnRegister);
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
				enableField(btnRegister);
				if (isCaptchaNotMatched(data)) {
					alert(ALERTS["captcha_not_matched"]);
					if (widget.onError != null) widget.onError(data);
                    return;
				}
				widget.empty();
				if (widget.onSuccess == null) return;
				widget.onSuccess(data);
		    },
		    error: function(xhr, status, err){
				enableField(btnRegister);
				if (widget.onError == null) return;
				widget.onError(err);
		    }
		});
	});

	this.name.on("focusin focusout", this.nameCheck, function(evt){
		evt.preventDefault();
		var nameCheck = evt.data;
		nameCheck.empty();
		nameCheck.html("");
		nameCheck.removeClass("warn");
		var nameVal = $(this).val();
		if(nameVal == null || nameVal.length == 0) return;
		if(!validateName(nameVal)) {
			nameCheck.html("<p>" + MESSAGES["username_requirement"] + "</p>");
			nameCheck.addClass("warn");
			return;
		}

		var params={};
		params[g_keyName] = nameVal;
		$.ajax({
			type: "GET",
			url: "/user/name/duplicate",
			data: params,
			success: function(data, status, xhr){
				if (isStandardSuccess(data)){
					nameCheck.html("<p>" + MESSAGES["username_valid"] + "</p>");        
			    }else{
					nameCheck.addClass("warn");
					nameCheck.html("<p>" + MESSAGES["username_invalid"] + "</p>");
				}        
			},
			error: function(xhr, status, err){
			}
		});
	});	

	this.email.on("focusin focusout", this.emailCheck, function(evt){
		evt.preventDefault();
		var emailCheck = evt.data;
		emailCheck.empty();
		emailCheck.html("");
		emailCheck.removeClass("warn");
		var emailVal = $(this).val();
		if(emailVal == null || emailVal.length == 0) return;
		if(!validateEmail(emailVal)) {
			emailCheck.addClass("warn");
			emailCheck.html("<p>" + MESSAGES["email_requirement"] + "</p>");
			return;
		}

		var params = {};
		params[g_keyEmail] = emailVal;
		$.ajax({
			type: "GET",
			url: "/user/email/duplicate",
			data: params,
			success: function(data, status, xhr){
				if (isStandardSuccess(data)){
					emailCheck.html("<p>" + MESSAGES["email_valid"] + "</p>");        
				}else{
					emailCheck.addClass("warn");
					emailCheck.html("<p>" + MESSAGES["email_invalid"] + "</p>");
				}        
			},
			error: function(xhr, status, err){
			}
		});
	});	

	this.psw.on("input keyup paste", this.pswCheck, function(evt){
		evt.preventDefault();
		var pswCheck = evt.data;
		pswCheck.empty();
		pswCheck.html("");
		pswCheck.removeClass("warn");
		var pswVal = $(this).val();
		if(pswVal == null || pswVal.length ==0 ) return;
		if(validatePassword(pswVal))	return;
		pswCheck.addClass("warn");
		pswCheck.html("<p>" + MESSAGES["password_requirement"] + "</p>");
	});	
 
	this.pswConfirm.on("input keyup paste", {0: this.pswConfirmCheck, 1: this.psw}, function(evt){
		evt.preventDefault();
		var pswConfirmCheck = evt.data[0];
		var pswConfirmVal = $(this).val();
		var pswVal = evt.data[1].val();
		pswConfirmCheck.empty();
		pswConfirmCheck.html("");
		pswConfirmCheck.removeClass("warn");
		if(validatePasswordConfirm(pswVal, pswConfirmVal))	return;
		pswConfirmCheck.addClass("warn");
		pswConfirmCheck.html("<p>" + MESSAGES["password_confirm_requirement"] + "</p>");
	});	
}

function removeRegisterWidget() {
	if (g_sectionRegister == null) return;
	g_sectionRegister.empty();
	if (g_modalRegister == null) return;
	g_modalRegister.empty();
	g_modalRegister = null;
	g_sectionRegister.modal("hide");
}

function initRegisterWidget(par, isModal) {
	removeRegisterWidget();
	if (!isModal) {
		g_sectionRegister = $("<div>").appendTo(par);	
		return;
	}
	g_sectionRegister = $("<div class='modal fade activity-editor' tabindex='-1' role='dialog' aria-labelledby='create' aria-hidden='true'>").appendTo(par);
	var dialog = $("<div>", {
		"class": "modal-dialog modal-lg"
	}).appendTo(g_sectionRegister);
	g_modalRegister = $("<div>", {
		"class": "modal-content"
	}).appendTo(dialog);
}

function generateRegisterWidget(container, isModal, onSuccess, onError){	
	initRegisterWidget(container, isModal);
	var par = (isModal ? g_modalRegister : g_sectionRegister);	
	var registerBox = $('<div>', {
		id: "register-box"
	}).appendTo(par);
	var rowName = $('<div>', {
		"class": "register-name"
	}).appendTo(registerBox);
	var fieldName = $('<input>', {
		type: "text",
		placeHolder: HINTS["username"],	
	}).appendTo(rowName);
	var spName = $('<div>', {
		"class": "message"
	}).appendTo(rowName);

	var rowEmail = $('<div>', {
		"class": "register-email"
	}).appendTo(registerBox);
	var fieldEmail = $('<input>', {
		type: "text",
		placeHolder: HINTS["email"],
	}).appendTo(rowEmail);
	var spEmail = $('<div>', {
		"class": "message"
	}).appendTo(rowEmail);
	
	var rowPsw = $('<div>', {
		"class": "register-password"
	}).appendTo(registerBox);
	var fieldPsw = $('<input>', {
		type: "password",
		placeHolder: HINTS["password"]	
	}).appendTo(rowPsw);
	var spanPsw = $('<div>', {
		"class": "message"
	}).appendTo(rowPsw);

	var rowPswConfirm = $('<div>', {
		"class": "register-pswconfirm"
	}).appendTo(registerBox);
	var fieldPswConfirm = $('<input>', {
		type: "password",
		placeHolder: HINTS["confirm_password"]	
	}).appendTo(rowPswConfirm);
	var spPswConfirm = $('<div>', {
		"class": "message"
	}).appendTo(rowPswConfirm);

	var sid = generateUuid();
	var captcha = new Captcha(sid);
	captcha.appendTo(registerBox);

	var rowButton = $('<div>', {
		"class": "register-button"
	}).appendTo(registerBox);
	var btnRegister = $('<button>', {
		text: TITLES["register"],
		"class": "purple"
	}).appendTo(rowButton);

	return new RegisterWidget(fieldName, spName, fieldEmail, spEmail, fieldPsw, spanPsw, fieldPswConfirm, spPswConfirm, btnRegister, onSuccess, onError, captcha);
}

function validatePasswordConfirm(psw, pswConfirm){
	if(psw == null || pswConfirm == null) return false;
	if(psw != pswConfirm) return false;
	return true;
} 
