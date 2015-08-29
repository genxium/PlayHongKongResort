var g_sectionLogin = null;
var g_loggedInPlayer = null;
var g_preLoginForm = null;
var g_postLoginMenu = null;

function PreLoginForm(handle, psw, btn, forgot, registry, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError) {
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
	this.bubble = bubble;
	this.dropdownMenu = dropdownMenu;
	this.onLoginSuccess = onLoginSuccess;
	this.onLoginError = onLoginError;
	this.onLogoutSuccess = onLogoutSuccess;
	this.onLogoutError = onLogoutError;
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
	} else {
		/**
		* TODO: make it adaptable to all 3rd party login workflow
		**/
		
		var qqLoginEntry = $("<img>", {
			src: "/assets/icons/qq.png",
			"class": "foreign-party-logo"
		}).appendTo(row2);
		qqLoginEntry.click(function(evt) {
			evt.preventDefault();

			var currentHref = window.location.href;
			var currentBundle = extractTagAndParams(currentHref);
			if (currentBundle == null)	return;

			var currentTag = currentBundle["tag"];	
			var currentParams = currentBundle["params"];
			currentParams[g_keyParty] = g_partyQQ;

			var redirectUri = encodeURI(window.location.protocol + "//" + window.location.host + "#" + currentTag + "?" + currentParams.join('&'));

			var oauthTarget = 'https://graph.qq.com/oauth2.0/authorize?';
			var oauthParams = ['client_id=' + g_appIdQQ, 'redirect_uri=' + redirectURI, 'scope=get_user_info','response_type=token'];
        
			window.location.assign(oauthTarget + oauthParams.join('&'));
		});
	}

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

function showForeignPartyNameCompletion(par) {

}

function hideForeignPartyNameCompletion() {

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
				alert("Player not found!");
				checkLoginStatus(false);
				return;
			}
			if (isForeignPartyRegistrationRequired(data)) {
				alert("Name completion required");
				showForeignPartyNameCompletion();
				return;
			}
			if (isTempForeignPartyRecordNotFound(data)) {
				alert("Re-login required");
				checkLoginStatus(false);
				return;
			}
			if (isStandardFailure(data)) {
				alert("Unknown error!");
				checkLoginStatus(false);
				return;
			}

			var playerJson = data;
			$.removeCookie(g_keyAccessToken, {path: '/'});
			$.removeCookie(g_keyParty, {path: '/'});
			$.cookie(g_keyToken, playerJson[g_keyToken], {path: '/'});

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
			var playerJson = data;
			g_loggedInPlayer = new Player(playerJson);
			if (g_loggedInPlayer == null) return;
			$.cookie(g_keyToken, playerJson[g_keyToken], {path: '/'});
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
