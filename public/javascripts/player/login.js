var g_sectionLogin = null;
var g_loggedInPlayer = null;
var g_preLoginForm = null;
var g_postLoginMenu = null;
var g_nameCompletionForm = null;

function PreLoginForm(handle, psw, btn, forgot, registry, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError) {
	// TODO: refactor with container-dialog-appendTo-refresh pattern
	this.handle = handle;
	this.psw = psw;
	this.btn = btn;
	this.forgot = forgot;
	this.registry = registry;
	this.onLoginSuccess = onLoginSuccess;
	this.onLoginError = onLoginError;
	this.onLogoutSuccess = onLogoutSuccess;
	this.onLogoutError = onLogoutError;

	this.handle.keypress(this, function (evt) {
  		if (evt.which != 13) return;
		var form = evt.data;
		evt.preventDefault();
		form.btn.click();
	});
	
	this.psw.keypress(this, function (evt) {
  		if (evt.which != 13) return;
		var form = evt.data;
		evt.preventDefault();
    		form.btn.click();
	});

	this.btn.click(this, function(evt){
		var form = evt.data;
		var email = form.handle.val();
		var password = form.psw.val();

		if ( !email || email.length === 0 || !validateEmail(email) ) {
			alert(ALERTS.invalid_email_format);
			return;
		}

		if ( !password || password.length === 0 || !validatePassword(password) ) {
			alert(ALERTS.wrong_password);
			return;
		}

		var params={};
		params[g_keyEmail] = email;
		params[g_keyPassword] = password;

		var aButton = $(evt.srcElement ? evt.srcElement : evt.target);
		disableField(aButton);
		
		$.ajax({
		    type: "POST",
		    url: "/player/login",
		    data: params,
		    success: function(data, status, xhr){
				enableField(aButton);
				if (isPlayerNotFound(data)) {
					alert(ALERTS.player_not_existing);
					return;
				} 
				if (isPswErr(data)) {
					alert(ALERTS.wrong_password);
					return;
				}
				g_loggedInPlayer = new Player(data);
				if (!g_loggedInPlayer) return;
				// store token in cookie iff query succeeds
				saveToken(data.token);
				wsConnect();
				if(!g_sectionLogin) return;
				g_postLoginMenu = generatePostLoginMenu(g_sectionLogin, form.onLoginSuccess, form.onLoginError, form.onLogoutSuccess, form.onLogoutError);
				if(!form.onLoginSuccess) return;
				form.onLoginSuccess(data);
		    },
		    error: function(xhr, status, err){
				enableField(aButton);
				if(!form.onLoginError) return;
				form.onLoginError(err);
		    }
		});
	});

	this.forgot.click(function(evt){
		evt.preventDefault();
		window.open("/player/password/index");
	});

	if (this.registry) {
		this.registry.click(function(evt) {
			evt.preventDefault();
			/**
			 * TODO: replace `$("#content")` by a member variable
			 * */
			g_registerWidget = generateRegisterWidget($("#content"), true, null, null);
			g_sectionRegister.modal("show");
		});
	}
} 

function NotiBubble(num, view) {
	this.num = num;
	this.view = view;
	this.increase = function(n) {
		this.num += n;
		this.view.text(this.num.toString());
	};
	this.decrease = function(n) {
		this.num -= n;
		this.view.text(this.num.toString());
	};
	this.update = function(n) {
		this.num = n;
		this.view.text(this.num.toString());
	};
}

function PostLoginMenu(bubble, dropdownMenu, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError) {
	// TODO: refactor with container-dialog-appendTo-refresh pattern
	this.bubble = bubble;
	this.dropdownMenu = dropdownMenu;
	this.onLoginSuccess = onLoginSuccess;
	this.onLoginError = onLoginError;
	this.onLogoutSuccess = onLogoutSuccess;
	this.onLogoutError = onLogoutError;
}

function NameCompletionForm() {
	this.name = null;
	this.nameCheck = null;
	this.email = null;
	this.emailCheck = null;
	this.btnSubmit = null;
	this.btnCancel = null;
	this.onSuccess = null;
	this.onError = null;

	this.partyNickname = null;

	this.composeContent = function(data) {
		this.partyNickname = data;
		var party = getParty();
		var titleHtml = (party == g_partyQQ ? TITLES.first_foreign_party_registration_qq.format(this.partyNickname) : TITLES.first_foreign_party_registration);
                var title = $('<div>', {
                        'class':	"title-alpha first-foreign-party-registration-title",
                        html: titleHtml 
                }).appendTo(this.content);

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

                this.name.on("focusin focusout", this.nameCheck, function(evt) {
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
                                success: function(data, status, xhr){
                                        if (isStandardSuccess(data)){
                                                removeWarningStyle(nameCheck);
                                                nameCheck.html("<p>" + MESSAGES.playername_valid + "</p>");
                                        }else{
                                                addWarningStyle(nameCheck);
                                                nameCheck.html("<p>" + MESSAGES.playername_invalid + "</p>");
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
                                success: function(data, status, xhr){
                                        if (isStandardSuccess(data)){
                                                removeWarningStyle(emailCheck);
                                                emailCheck.html("<p>" + MESSAGES.email_valid + "</p>");
                                        }else{
                                                addWarningStyle(emailCheck);
                                                emailCheck.html("<p>" + MESSAGES.email_invalid + "</p>");
                                        }
                                },
                                error: function(xhr, status, err){
                                }
                        });
		});

		var buttons = $("<div>", {
			"class": "edit-button-rows"
		}).appendTo(registerBox);

		this.btnSubmit = $('<button>',{
			"class": "btn-submit positive-button",
			text: TITLES.submit
		}).appendTo(buttons).click(this, function(evt) {
		    var widget = evt.data;
		    var playername = widget.name.val();
		    var email = widget.email.val();

		    if (!playername || playername.length === 0 || !validateName(playername)) return;
		    if (!(!email) && email.length > 0 && !validateEmail(email)) return;

		    var accessToken = getAccessToken();
		    if (!accessToken) return;

		    var party = getParty();
		    if (!party) return;

		    var btnSubmit = getTarget(evt);
		    disableField(btnSubmit);

		    var params = {};
		    params[g_keyName] = playername;
		    if (!(!email) && email.length > 0 && validateEmail(email)) params[g_keyEmail] = email;
		    params[g_keyAccessToken] = accessToken;
		    params[g_keyParty] = party;

		    $.ajax({
				type: "POST",
				url: "/player/foreign/implicit/login",
				data: params,
				success: function(data, status, xhr){
					enableField(btnSubmit);
					if (isPlayerNotFound(data)) {
						alert(ALERTS.player_not_existing);
						clearAccessTokenAndParty();
						widget.hide();
						return;
					}
					if (isForeignPartyRegistrationRequired(data)) {
						widget.refresh(widget.partyNickname);
						return;
					}
					if (isTempForeignPartyRecordNotFound(data)) {
						alert(ALERTS.relogin_required);
						widget.hide();
						return;
					}
					if (isStandardFailure(data)) {
						alert(ALERTS.unknown_error);
						widget.refresh(widget.partyNickname);
						return;
					}

					clearAccessTokenAndParty();
					g_loggedInPlayer = new Player(data);
					saveToken(data.token);

					widget.hide();
					checkLoginStatus(false);
				},
				error: function(xhr, status, err){
					enableField(btnSubmit);
					widget.hide();
				}
		    });
		});

		this.btnCancel = $('<button>',{
			"class": "btn-cancel negative-button",
			text: TITLES.cancel
		}).appendTo(buttons).click(this, function(evt) {
			clearAccessTokenAndParty();
			var widget = evt.data;
			widget.hide();
		});
	};
}

NameCompletionForm.inherits(BaseModalWidget);

function initNameCompletionForm(par) {
	g_nameCompletionForm = new NameCompletionForm();
	g_nameCompletionForm.appendTo(par, true);
}

var g_qqWelcomePopup = null;
function QQWelcomePopup() {
	this.composeContent = function(data) {
		var nickname = data;
		var message = $('<div>', {
			html:MESSAGES.qq_welcome.format(nickname)
		}).appendTo(this.content);	
	};
}

QQWelcomePopup.inherits(BaseModalWidget);

function initQQWelcomePopup(par) {
	g_qqWelcomePopup = new QQWelcomePopup();
	g_qqWelcomePopup.appendTo(par);
}

function appendForeignPartyLoginEntry(par, party) {
	if (!par) return;
	if (party == g_partyQQ) {
		var qqLoginEntry = $("<img>", {
			src: "/assets/icons/qq.png",
			"class": "foreign-party-logo"
		}).appendTo(par);
		qqLoginEntry.click(function(evt) {
			evt.preventDefault();
			
			var rawBundle = encodeStateWithAction(g_partyQQ, checkLoginStatus, [false]);
			
			var redirectUri = (window.location.protocol + "//" + window.location.host);
			var oauthTarget = 'https://graph.qq.com/oauth2.0/authorize?';
			var oauthParams = ['client_id=' + g_appIdQQ, 'redirect_uri=' + redirectUri, 'scope=get_user_info','response_type=token', 'state=' + rawBundle];
        
			window.location.assign(oauthTarget + oauthParams.join('&'));
		});
	}
}

function generatePreLoginForm(par, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError, attachRegistry) {
	if (!par) return null;
	par.empty();
	var container = $('<div>', {
		id: "login-box"
	}).appendTo(par);
	var row1 = $('<div>', {
		"class": "login-row1 clearfix"
	}).appendTo(container);
	var inputs = $('<div>', {
		"class": "login-inputs left plain-input"
	}).appendTo(row1);
	var handle = $('<input>', {
		placeHolder: HINTS.email,
		type: "text",
		"class": "login-email"
	}).appendTo(inputs);
	var psw = $('<input>', {
		placeHolder: HINTS.password,
		type: "password",
		"class": "login-pw"
	}).appendTo(inputs);
	var btn = $('<button>',{
		text: TITLES.login,
		"class": "login-btn right positive-button"
	}).appendTo(row1);
	var row2 = $("<div>", {
		"class": "accessory-row"
	}).appendTo(container);
	var forgot = $("<button>", {
		text: TITLES.forgot_password,
		"class": "login-forgot faketext-button"
	}).appendTo(row2);	
	var registry = null;
	if (attachRegistry) {
		registry = $("<button>", {
			text: TITLES.register,
			"class": "login-registry faketext-button"
		}).appendTo(row2);	
	} 
	appendForeignPartyLoginEntry(row2, g_partyQQ);

	return new PreLoginForm(handle, psw, btn, forgot, registry, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError);
}

function logout(evt) {
	/**
	 * May be triggered by: 1. GUI click on the logout button 2. token expiry on operations
	 * */

	evt.preventDefault();
	var menu = ((evt && evt.data) ? evt.data : g_postLoginMenu);
	if (!menu) return;

	var params = {};
	params[g_keyToken] = getToken();

	var aButton = (evt ? $(evt.srcElement ? evt.srcElement : evt.target) : null);
	disableField(aButton);
	$.ajax({
		type: "POST",
		url: "/player/logout",
		data: params,
		success: function(data, status, xhr){
			enableField(aButton);
			g_loggedInPlayer = null;
			clearToken();
			wsDisconnect();
			if (!g_sectionLogin) return;
			g_preLoginForm = generatePreLoginForm(g_sectionLogin, menu.onLoginSuccess, menu.onLoginError, menu.onLogoutSuccess, menu.onLogoutError);
			if (!menu.onLogoutSuccess) return;
			menu.onLogoutSuccess(data);
		},
		error: function(xhr, status, err){
			// reload the whole page if exception occurs
			enableField(aButton);
			wsDisconnect();
			clearToken();
			if (!menu.onLogoutError) return;
			menu.onLogoutError(err);
		}	
	});
}

function generatePostLoginMenu(par, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError) {
	if (!par) return null;
	if (!g_loggedInPlayer) return null;
	par.empty();

	var createReact = function(evt){
		evt.preventDefault();
		if (!g_loggedInPlayer) return;
		if (g_loggedInPlayer.isVisitor()) {
			alert(ALERTS.email_not_authenticated);
			return;
		}	
		if (!g_activityEditor) return;
		g_activityEditor.refresh(null);
		g_activityEditor.show();
	};

	var profileReact = function(evt){
		evt.preventDefault();
		if (!g_loggedInPlayer) return;
		window.location.hash = ("profile?" + g_keyVieweeId + "=" + g_loggedInPlayer.id.toString());
	};
	
	var logoutReact = logout;
	
	var playerBox = $("<div>", {
		"class": "player-box clearfix"
	}).appendTo(par);
	var avatarContainer = $("<div>", {
		"class": "post-login-avatar left patch-block-alpha"
	}).appendTo(playerBox);
	var avatarImage = $("<img>", {
		src: g_loggedInPlayer.avatar
	}).appendTo(avatarContainer);

	var avatarSpan = $("<span>", {
		text: TITLES.profile
	}).appendTo(avatarContainer);
	avatarContainer.click(profileReact);
	
	var playerBoxLeft = $("<div>", {
		"class": "player-box-left left clearfix patch-block-gamma"
	}).appendTo(playerBox);
	var leftFirstRow = $("<div>", {
		"class": "left-first-row clearfix"
	}).appendTo(playerBoxLeft);
	var noti = $("<div>", {
		"class": "noti-container left"
	}).appendTo(leftFirstRow);

	noti.click(function(evt){
		evt.preventDefault();		
		if (!g_loggedInPlayer) return;
		window.location.hash = "notifications";
	}); 

	var spBubble = $("<span>", {
		"class": "bubble",
		text: "0"
	}).appendTo(noti);
	var bubble = new NotiBubble(0, spBubble);
	bubble.update(g_loggedInPlayer.unreadCount);

	var playerName = $("<div>", {
		"class": "playername left patch-block-delta",
		html: g_loggedInPlayer.name
	}).appendTo(leftFirstRow);

	var postLoginMenuContainer = $("<div>", {
		"class": "menu-action-row"
	}).appendTo(playerBoxLeft);

	var icons = ["/assets/icons/new_activity.png", "/assets/icons/profile.png", "/assets/icons/logout.png"];
	var actionNames = ["create", "profile", "logout"];
	var titles = [TITLES.create, TITLES.profile, TITLES.logout];
	var reactions = [createReact, profileReact, logoutReact]; 

	var dropdownMenu = createDropdownMenu(postLoginMenuContainer, "menu-post-login", g_loggedInPlayer.name, icons, actionNames, titles, reactions);
	var menu = new PostLoginMenu(bubble, dropdownMenu, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError);
	var params = [menu, menu, menu];
	dropdownMenu.setReactionParams(params);
	return menu;
}

function encodeStateWithAction(party, cbfunc, argList) {
	/**
         * this function returns a JSON-serialized and  URI-component-encoded string of 
         * {	 
	 * 	state: {
	 *		tag:	<tag_val>
	 *		param_name_1:	<param_val_1>,
	 *		param_name_2:	<param_val_2>,
	 *		...
	 * 	},
	 * 	party:	<party>,
	 *	cbfunc: <cbfunc.name>,	 
	 *	args:	[
	 *		<arg_val_1>,
	 *		<arg_val_2>,
	 *		...
	 *	]
	 * }
	 * where argList must be of list type and contain only integer or string variabls
	 * */ 
	var currentHref = window.location.href;
	var currentBundle = extractTagAndParams(currentHref);
	if (!currentBundle)	return;

	var currentTag = currentBundle[g_keyTag];	
	var currentParams = currentBundle[g_keyParams];
		
	var state = {};
	state[g_keyTag] = currentTag;
	for (var k in currentParams) state[k] = currentParams[k];

	var args = [];
	for (var u in argList)	args.push(argList[u]); 

	var ret = {};
	ret[g_keyState] = state;
	ret[g_keyParty] = party;

	if (cbfunc) ret[g_keyCbfunc] = cbfunc.name;	
	if (argList) ret[g_keyArgs] = args;

	ret = JSON.stringify(ret);	
	ret = encodeURIComponent(ret);

	return ret;
}

function decodeStateWithAction(rawBundle) {
	var ret = decodeURIComponent(rawBundle);
	ret = JSON.parse(ret);	
	return ret;
}

function checkForeignPartyLoginStatus() {

	var accessToken = getAccessToken();
	var party = getParty();
	
	if (!accessToken || !party) {
		clearAccessTokenAndParty();
		checkLoginStatus(false);
		return;
	}

	var params={};
	params[g_keyAccessToken] = accessToken;
	params[g_keyParty] = party;
	$.ajax({
		type: "POST",
		url: "/player/foreign/implicit/login",
		data: params,
		success: function(data, status, xhr){
			if (isPlayerNotFound(data)) {
				alert(ALERTS.player_not_existing);
				clearAccessTokenAndParty();
				checkLoginStatus(false);
				return;
			}
			if (isForeignPartyRegistrationRequired(data)) {
				var partyNickname = data[g_keyPartyNickname];
				g_nameCompletionForm.refresh(partyNickname);
				g_nameCompletionForm.show();
				return;
			}
			if (isTempForeignPartyRecordNotFound(data)) {
				alert(ALERTS.relogin_required);
				checkLoginStatus(false);
				return;
			}
			if (isStandardFailure(data)) {
				alert(ALERTS.unknown_error);
				checkLoginStatus(false);
				return;
			}

			clearAccessTokenAndParty();
			g_loggedInPlayer = new Player(data);
			saveToken(data.token);

			if (party == g_partyQQ && !(!data[g_keyPartyNickname])) {
				g_qqWelcomePopup.refresh(data[g_keyPartyNickname]);
				g_qqWelcomePopup.show();
			}

			checkLoginStatus(false);	
		},
		error: function(xhr, status, err){
			checkLoginStatus(false);	
		}
	});
}

function checkLoginStatus(willAttemptForeignPartyLogin) {
	
	willAttemptForeignPartyLogin = (willAttemptForeignPartyLogin === undefined ? true : willAttemptForeignPartyLogin);

	var token = getToken();

	if(!token) {		
		if (willAttemptForeignPartyLogin) {
			checkForeignPartyLoginStatus();
			return;
		}
		if (!g_preLoginForm) return;
		g_preLoginForm.onLoginError(null);
		return;
	}

	if (!(!g_loggedInPlayer)) {
		if (!g_preLoginForm) return;
		g_postLoginMenu = generatePostLoginMenu(g_sectionLogin, g_preLoginForm.onLoginSuccess, g_preLoginForm.onLoginError, g_preLoginForm.onLogoutSuccess, g_preLoginForm.onLogoutError);
		if (!g_preLoginForm.onLoginSuccess)	return;
		g_preLoginForm.onLoginSuccess(null);
		return;
	}

	var params={};
	params[g_keyToken] = token;
	$.ajax({
		type: "POST",
		url: "/player/status",
		data: params,
		success: function(data, status, xhr){
			if (isStandardFailure(data)) {
				wsDisconnect();	
				clearToken();
				if(!g_preLoginForm.onLoginError) return;
				g_preLoginForm.onLoginError(null);		
				return;
			}
			g_loggedInPlayer = new Player(data);
			if (!g_loggedInPlayer) return;
			saveToken(data.token);
			wsConnect();
			if (!g_preLoginForm) return;
			g_postLoginMenu = generatePostLoginMenu(g_sectionLogin, g_preLoginForm.onLoginSuccess, g_preLoginForm.onLoginError, g_preLoginForm.onLogoutSuccess, g_preLoginForm.onLogoutError);
			if (!g_preLoginForm.onLoginSuccess)	return;
			g_preLoginForm.onLoginSuccess(data);
		},
		error: function(xhr, status, err){
		}
	});

}

function focusLogin() {

}
