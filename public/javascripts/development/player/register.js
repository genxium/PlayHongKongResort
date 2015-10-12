var g_registerWidgetX = null;
var g_registerWidgetY = null;

function RegisterWidgetX(onSuccess, onError) {
	// prototypes: onSuccess(data), onError(err)		
	this.name = null;
	this.nameCheck = null;
	this.email = null;
	this.emailCheck = null;
	this.psw = null;
	this.pswCheck = null;
	this.pswConfirm = null;
	this.pswConfirmCheck = null;
	this.btn = null;
	this.captcha = null;
	this.onSuccess = onSuccess;
	this.onError = onError;
	this.composeContent = function(data) {
		var registerBox = $('<div>', {
			id: "register-box"
		}).appendTo(this.content);
		var rowName = $('<div>', {
			"class": "register-name"
		}).appendTo(registerBox);
		this.name = $('<input>', {
			type: "text",
			placeHolder: HINTS.playername,	
		}).appendTo(rowName);
		this.nameCheck = $('<div>', {
			"class": "message"
		}).appendTo(rowName);
		this.name.on("focusin focusout", this.nameCheck, function(evt){
			evt.preventDefault();
			var nameCheck = evt.data;
			nameCheck.empty();
			nameCheck.html("");
			var nameVal = $(this).val();
			if(!nameVal || nameVal.length === 0) return;
			if(!validateName(nameVal)) {
				addWarningStyle(nameCheck);
				nameCheck.html("<p>" + MESSAGES.playername_requirement + "</p>");
				return;
			}

			var params={};
			params[g_keyName] = nameVal;
			$.ajax({
				type: "GET",
				url: "/player/name/duplicate",
				data: params,
				success: function(data, status, xhr) {
					if (isStandardSuccess(data)) {
						removeWarningStyle(nameCheck);
						nameCheck.html("<p>" + MESSAGES.playername_valid + "</p>");
					} else {
						addWarningStyle(nameCheck);
						nameCheck.html("<p>" + MESSAGES.playername_invalid + "</p>");
					}
				}
			});
		});

		var rowEmail = $('<div>', {
			"class": "register-email"
		}).appendTo(registerBox);
		this.email = $('<input>', {
			type: "text",
			placeHolder: HINTS.email,
		}).appendTo(rowEmail);
		this.emailCheck = $('<div>', {
			"class": "message"
		}).appendTo(rowEmail);
		this.email.on("focusin focusout", this.emailCheck, function(evt){
			evt.preventDefault();
			var emailCheck = evt.data;
			emailCheck.empty();
			emailCheck.html("");
			var emailVal = $(this).val();
			if(!emailVal || emailVal.length === 0) return;
			if(!validateEmail(emailVal)) {
				addWarningStyle(emailCheck);
				emailCheck.html("<p>" + MESSAGES.email_requirement + "</p>");
				return;
			}

			var params = {};
			params[g_keyEmail] = emailVal;
			$.ajax({
				type: "GET",
				url: "/player/email/duplicate",
				data: params,
				success: function(data, status, xhr) {
					if (isStandardSuccess(data)){
						removeWarningStyle(emailCheck);
						emailCheck.html("<p>" + MESSAGES.email_valid + "</p>");
					} else {
						addWarningStyle(emailCheck);
						emailCheck.html("<p>" + MESSAGES.email_invalid + "</p>");
					}
				}
			});
		});
		
		var rowPsw = $('<div>', {
			"class": "register-password"
		}).appendTo(registerBox);
		this.psw = $('<input>', {
			type: "password",
			placeHolder: HINTS.password	
		}).appendTo(rowPsw);
		this.pswCheck = $('<div>', {
			"class": "message"
		}).appendTo(rowPsw);
		this.psw.on("input keyup paste", this.pswCheck, function(evt) {
			evt.preventDefault();
			var pswCheck = evt.data;
			pswCheck.empty();
			pswCheck.html("");
			var pswVal = $(this).val();
			if (!pswVal || pswVal.length === 0) return;
			if (validatePassword(pswVal))	return;
			addWarningStyle(pswCheck);
			pswCheck.html("<p>" + MESSAGES.password_requirement + "</p>");
		});

		var rowPswConfirm = $('<div>', {
			"class": "register-pswconfirm"
		}).appendTo(registerBox);
		this.pswConfirm = $('<input>', {
			type: "password",
			placeHolder: HINTS.confirm_password	
		}).appendTo(rowPswConfirm);
		this.pswConfirmCheck = $('<div>', {
			"class": "message"
		}).appendTo(rowPswConfirm);
		this.pswConfirm.on("input keyup paste", {0: this.pswConfirmCheck, 1: this.psw}, function(evt) {
			evt.preventDefault();
			var pswConfirmCheck = evt.data[0];
			var pswConfirmVal = $(this).val();
			var pswVal = evt.data[1].val();
			pswConfirmCheck.empty();
			pswConfirmCheck.html("");
			if(validatePasswordConfirm(pswVal, pswConfirmVal))	return;
			addWarningStyle(pswConfirmCheck);
			pswConfirmCheck.html("<p>" + MESSAGES.password_confirm_requirement + "</p>");
		});

		this.captcha = new Captcha(generateUuid());
		this.captcha.appendTo(registerBox);
		this.captcha.refresh();	

		var rowButton = $('<div>', {
			"class": "register-button"
		}).appendTo(registerBox);
		this.btn = $('<button>', {
			text: TITLES.register,
			"class": "positive-button"
		}).click(this, function(evt) {
			var widget = evt.data;
			var playername = widget.name.val();
			var email = widget.email.val();
			var password = widget.psw.val();
			var passwordConfirm = widget.pswConfirm.val();

			if (!playername || playername.length === 0 || !validateName(playername) || 
				!email || email.length === 0 || !validateEmail(email) ||
				!password || password.length === 0 || !validatePassword(password) || !validatePasswordConfirm(password, passwordConfirm)) return;

			var btnRegister = getTarget(evt);
			disableField(btnRegister);
			var params = {};
			params[g_keyName] = playername;
			params[g_keyEmail] = email;
			params[g_keyPassword] = password;
			params[g_keySid] = widget.captcha.sid;
			params[g_keyCaptcha] = widget.captcha.input.val();

			$.ajax({
			    type: "POST",
			    url: "/player/register",
			    data: params,
			    success: function(data, status, xhr){
					enableField(btnRegister);
					if (isCaptchaNotMatched(data)) {
						alert(ALERTS.captcha_not_matched);
						if (!(!widget.onError)) widget.onError(data);
			    return;
					}
					widget.refresh();	
					if (!widget.onSuccess) return;
					widget.onSuccess(data);
			    },
			    error: function(xhr, status, err){
					enableField(btnRegister);
					if (!widget.onError) return;
					widget.onError(err);
			    }
			});
		}).appendTo(rowButton);
	};
}
RegisterWidgetX.inherits(BaseWidget);

function RegisterWidgetY(onSuccess, onError) {
	this.onSuccess = onSuccess;
	this.onError = onError;

	this.container = null;
	this.dialog = null;
}
RegisterWidgetY.inherits(RegisterWidgetX);
RegisterWidgetY.swiss(BaseModalWidget, 'appendTo');
RegisterWidgetY.swiss(BaseModalWidget, 'show');
RegisterWidgetY.swiss(BaseModalWidget, 'hide');
RegisterWidgetY.swiss(BaseModalWidget, 'remove');

function initRegisterWidgetX(par, onSuccess, onError) {
	g_registerWidgetX = new RegisterWidgetX(onSuccess, onError);	
	g_registerWidgetX.appendTo(par);
}

function initRegisterWidgetY(par, onSuccess, onError) {
	g_registerWidgetY = new RegisterWidgetY(onSuccess, onError);
	g_registerWidgetY.appendTo(par);
}

function validatePasswordConfirm(psw, pswConfirm){
	if(!psw || !pswConfirm) return false;
	if(psw != pswConfirm) return false;
	return true;
} 
