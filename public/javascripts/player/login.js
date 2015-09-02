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

		if ( email == null || email.length == 0 || !validateEmail(email) ) {
			alert(ALERTS["invalid_email_format"])
			return;
		}

		if ( password == null || password.length == 0 || !validatePassword(password) ) {
			alert(ALERTS["wrong_password"]);
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
					alert(ALERTS["player_not_existing"]);
					return;
				} 
				if (isPswErr(data)) {
					alert(ALERTS["wrong_password"]);
					return;
				}
				g_loggedInPlayer = new Player(data);
				if (g_loggedInPlayer == null) return;
				// store token in cookie iff query succeeds
				$.cookie(g_keyToken, data[g_keyToken], {path: '/'});
				wsConnect();	
				if(g_sectionLogin == null) return;
				g_postLoginMenu = generatePostLoginMenu(g_sectionLogin, form.onLoginSuccess, form.onLoginError, form.onLogoutSuccess, form.onLogoutError);
				if(form.onLoginSuccess == null) return;
				form.onLoginSuccess(data);
		    },
		    error: function(xhr, status, err){
				enableField(aButton);
				if(form.onLoginError == null) return;
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

	this.container = null;
	this.dialog = null;

	this.name = null;
	this.nameCheck = null;
	this.email = null;
	this.emailCheck = null;
	this.btnSubmit = null;
	this.btnCancel = null;
	this.onSuccess = null;
	this.onError = null;

	this.refresh = function() {
			this.content.empty();
		
			var title = $('<div>', {
				'class':	"title-alpha first-foreign-party-registration-title",
				text: TITLES['first_foreign_party_registration']
			}).appendTo(this.content);

			var registerBox = $('<div>', {
				id: "register-box"
			}).appendTo(this.content);
			var rowName = $('<div>', {
				"class": "register-name"
			}).appendTo(registerBox);
			this.name = $('<input>', {
				type: "text",
				placeHolder: HINTS["playername"],
			}).appendTo(rowName);
			this.nameCheck = $('<div>', {
				"class": "message"
			}).appendTo(rowName);

			var rowEmail = $('<div>', {
				"class": "register-email"
			}).appendTo(registerBox);
			this.email = $('<input>', {
				type: "text",
				placeHolder: HINTS["email"],
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
				if(nameVal == null || nameVal.length == 0) return;
				if(!validateName(nameVal)) {
					addWarningStyle(nameCheck);
					nameCheck.html("<p>" + MESSAGES["playername_requirement"] + "</p>");
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
							nameCheck.html("<p>" + MESSAGES["playername_valid"] + "</p>");
						}else{
							addWarningStyle(nameCheck);
							nameCheck.html("<p>" + MESSAGES["playername_invalid"] + "</p>");
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
				if(emailVal == null || emailVal.length == 0) return;
				if(!validateEmail(emailVal)) {
					addWarningStyle(emailCheck);
					emailCheck.html("<p>" + MESSAGES["email_requirement"] + "</p>");
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
							emailCheck.html("<p>" + MESSAGES["email_valid"] + "</p>");
						}else{
							addWarningStyle(emailCheck);
							emailCheck.html("<p>" + MESSAGES["email_invalid"] + "</p>");
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
			text: TITLES["submit"]
		}).appendTo(buttons).click(this, function(evt) {
		    var widget = evt.data;
		    var playername = widget.name.val();
		    var email = widget.email.val();

		    if (playername == null || playername.length == 0 || !validateName(playername)) return;
		    if (email != null && email.length > 0 && !validateEmail(email)) return;

		    var accessToken = $.cookie(g_keyAccessToken);
		    if (accessToken == null) return;

		    var party = $.cookie(g_keyParty);
		    if (party == null) return;

		    var btnSubmit = getTarget(evt);
		    disableField(btnSubmit);

		    var params = {};
		    params[g_keyName] = playername;
		    if (email != null && email.length > 0 && validateEmail(email)) params[g_keyEmail] = email;
		    params[g_keyAccessToken] = accessToken;
		    params[g_keyParty] = party;

		    $.ajax({
				type: "POST",
				url: "/player/foreign/login",
				data: params,
				success: function(data, status, xhr){
					enableField(btnSubmit);
					if (isPlayerNotFound(data)) {
						alert(ALERTS['player_not_existing']);
						widget.hide();
						return;
					}
					if (isForeignPartyRegistrationRequired(data)) {
						widget.refresh();
						return;
					}
					if (isTempForeignPartyRecordNotFound(data)) {
						alert(ALERTS['relogin_required']);
						widget.hide();
						return;
					}
					if (isStandardFailure(data)) {
						alert(ALERTS['unknown_error']);
						widget.refresh();
						return;
					}

					$.removeCookie(g_keyAccessToken, {path: '/'});
					$.removeCookie(g_keyParty, {path: '/'});

					g_loggedInPlayer = new Player(data); 
					$.cookie(g_keyToken, data[g_keyToken], {path: '/'});

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
			text: TITLES["cancel"]
		}).appendTo(buttons).click(this, function(evt) {
			$.removeCookie(g_keyAccessToken, {path: '/'});
			$.removeCookie(g_keyParty, {path: '/'});
			var widget = evt.data;
			widget.hide();
		});
	};

	this.appendTo = function(par) {
		// DOM elements
		this.container = $("<div class='modal fade activity-editor' data-keyboard='false' data-backdrop='static' tabindex='-1' role='dialog' aria-labelledby='create' aria-hidden='true'>").appendTo(par);
		this.dialog = $("<div class='modal-dialog modal-lg'>").appendTo(this.container);
		this.content= $("<div class='modal-content'>").appendTo(this.dialog);
	};

	this.show = function() {
		this.container.modal("show");
	};

	this.hide = function() {
		this.container.modal("hide");
	};

	this.remove = function() {
		this.container.remove();
	};
}

function initNameCompletionForm(par) {
	g_nameCompletionForm = new NameCompletionForm();
	g_nameCompletionForm.appendTo(par);
}

function appendForeignPartyLoginEntry(par, party) {
	if (par == null) return;
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
	if (par == null) return null;
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
		placeHolder: HINTS["email"],
		type: "text",
		"class": "login-email"
	}).appendTo(inputs);
	var psw = $('<input>', {
		placeHolder: HINTS["password"],
		type: "password",
		"class": "login-pw"
	}).appendTo(inputs);
	var btn = $('<button>',{
		text: TITLES["login"],
		"class": "login-btn right positive-button"
	}).appendTo(row1);
	var row2 = $("<div>", {
		"class": "accessory-row"
	}).appendTo(container);
	var forgot = $("<button>", {
		text: TITLES["forgot_password"],
		"class": "login-forgot faketext-button"
	}).appendTo(row2);	
	var registry = null;
	if (attachRegistry) {
		registry = $("<button>", {
			text: TITLES["register"],
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
	if (menu == null) return;

	var token = $.cookie(g_keyToken);
	var params = {};
	params[g_keyToken] = token;

	var aButton = (evt ? $(evt.srcElement ? evt.srcElement : evt.target) : null);
	disableField(aButton);
	$.ajax({
		type: "POST",
		url: "/player/logout",
		data: params,
		success: function(data, status, xhr){
			enableField(aButton);
			g_loggedInPlayer = null;
			$.removeCookie(g_keyToken, {path: '/'});
			wsDisconnect();
			if (g_sectionLogin == null) return;
			g_preLoginForm = generatePreLoginForm(g_sectionLogin, menu.onLoginSuccess, menu.onLoginError, menu.onLogoutSuccess, menu.onLogoutError);
			if (menu.onLogoutSuccess == null) return;
			menu.onLogoutSuccess(data);
		},
		error: function(xhr, status, err){
			// reload the whole page if exception occurs
			enableField(aButton);
			wsDisconnect();
			$.removeCookie(g_keyToken, {path: '/'});
			if (menu.onLogoutError == null) return;
			menu.onLogoutError(err);
		}	
	});
}

function generatePostLoginMenu(par, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError) {
	if (par == null) return null;
	if (g_loggedInPlayer == null) return null;
	par.empty();

	var createReact = function(evt){
		evt.preventDefault();
		if (g_loggedInPlayer == null) return;
		if (g_loggedInPlayer.isVisitor()) {
			alert(ALERTS["email_not_authenticated"]);
			return;
		}	
		if (g_activityEditor == null) return;
		g_activityEditor.refresh(null);
		g_activityEditor.show();
	};

	var profileReact = function(evt){
		evt.preventDefault();
		if (g_loggedInPlayer == null) return;
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
		text: TITLES["profile"]
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
		if (g_loggedInPlayer == null) return;
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
	var titles = [TITLES["create"], TITLES["profile"], TITLES["logout"]];
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
	if (currentBundle == null)	return;

	var currentTag = currentBundle[g_keyTag];	
	var currentParams = currentBundle[g_keyParams];
		
	var state = {};
	state[g_keyTag] = currentTag;
	for (var k in currentParams) state[k] = currentParams[k];

	var args = [];
	for (var k in argList)	args.push(argList[k]); 

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

	var accessToken = $.cookie(g_keyAccessToken);	
	var party = $.cookie(g_keyParty);	
	
	if (accessToken == null || party == null) {
		$.removeCookie(g_keyAccessToken, {path: '/'});
		$.removeCookie(g_keyParty, {path: '/'});
		checkLoginStatus(false);
		return;
	}

	var params={};
	params[g_keyAccessToken] = accessToken;
	params[g_keyParty] = party;
	$.ajax({
		type: "POST",
		url: "/player/foreign/login",
		data: params,
		success: function(data, status, xhr){
			if (isPlayerNotFound(data)) {
				alert(ALERTS['player_not_existing']);
				checkLoginStatus(false);
				return;
			}
			if (isForeignPartyRegistrationRequired(data)) {
				g_nameCompletionForm.refresh();
				g_nameCompletionForm.show();
				return;
			}
			if (isTempForeignPartyRecordNotFound(data)) {
				alert(ALERTS['relogin_required']);
				checkLoginStatus(false);
				return;
			}
			if (isStandardFailure(data)) {
				alert(ALERTS['unknown_error']);
				checkLoginStatus(false);
				return;
			}

			$.removeCookie(g_keyAccessToken, {path: '/'});
			$.removeCookie(g_keyParty, {path: '/'});
			g_loggedInPlayer = new Player(data);
			$.cookie(g_keyToken, data[g_keyToken], {path: '/'});

			checkLoginStatus(false);	
		},
		error: function(xhr, status, err){
			checkLoginStatus(false);	
		}
	});
}

function checkLoginStatus(willAttemptForeignPartyLogin) {
	
	willAttemptForeignPartyLogin = (willAttemptForeignPartyLogin == undefined ? true : willAttemptForeignPartyLogin);

	var token = $.cookie(g_keyToken);

	if(token == null) {		
		if (willAttemptForeignPartyLogin) {
			checkForeignPartyLoginStatus();
			return;
		}
		if (g_preLoginForm == null) return;
		g_preLoginForm.onLoginError(null);
		return;
	}

	if (g_loggedInPlayer != null) {
		if (g_preLoginForm == null) return;
		g_postLoginMenu = generatePostLoginMenu(g_sectionLogin, g_preLoginForm.onLoginSuccess, g_preLoginForm.onLoginError, g_preLoginForm.onLogoutSuccess, g_preLoginForm.onLogoutError);
		if (g_preLoginForm.onLoginSuccess == null)	return;
		g_preLoginForm.onLoginSuccess(null);
		return;
	}

	var params={};
	params[g_keyToken] = token;
	$.ajax({
		type: "GET",
		url: "/player/status",
		data: params,
		success: function(data, status, xhr){
			if (isStandardFailure(data)) {
				wsDisconnect();	
				$.removeCookie(g_keyToken, {path: '/'});
				if(g_preLoginForm.onLoginError == null) return;
				g_preLoginForm.onLoginError(null);		
				return;
			}
			g_loggedInPlayer = new Player(data);
			if (g_loggedInPlayer == null) return;
			$.cookie(g_keyToken, data[g_keyToken], {path: '/'});
			wsConnect();	
			if (g_preLoginForm == null) return;
			g_postLoginMenu = generatePostLoginMenu(g_sectionLogin, g_preLoginForm.onLoginSuccess, g_preLoginForm.onLoginError, g_preLoginForm.onLogoutSuccess, g_preLoginForm.onLogoutError);
			if (g_preLoginForm.onLoginSuccess == null)	return;
			g_preLoginForm.onLoginSuccess(data);
		},
		error: function(xhr, status, err){
		}
	});

}

function focusLogin() {

}
