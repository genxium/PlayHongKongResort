var g_sectionLogin = null;
var g_loggedInUser = null;
var g_preLoginForm = null;
var g_postLoginMenu = null;

function PreLoginForm(handle, psw, btn, forgot, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError) {
	this.handle = handle;
	this.psw = psw;
	this.btn = btn;
	this.forgot = forgot;
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
		    url: "/user/login",
		    data: params,
		    success: function(data, status, xhr){
				enableField(aButton);
				g_loggedInUser = new User(data);
				if (g_loggedInUser == null) return;
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
				if (isUserNotFound(err)) {
					alert(ALERTS["user_not_existing"]);
				} 
				if (isPswErr(err)) {
					alert(ALERTS["wrong_password"]);
				}
				if(form.onLoginError == null) return;
				form.onLoginError(err);
		    }
		});
	});

	this.forgot.click(function(evt){
		evt.preventDefault();
		window.open("/user/password/index");
	});
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

function generatePreLoginForm(par, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError) {
	if (par == null) return null;
	par.empty();
	var container = $('<div>', {
		id: "login-box"
	}).appendTo(par);
	var row1 = $('<div>', {
		class: "login-row1 clearfix"
	}).appendTo(container);
	var inputs = $('<div>', {
		class: "login-inputs left"
	}).appendTo(row1);
	var handle = $('<input>', {
		placeHolder: HINTS["email"],
		type: "text",
		class: "login-email"
	}).appendTo(inputs);
	var psw = $('<input>', {
        placeHolder: HINTS["password"],
        type: "password",
		class: "login-pw"
    }).appendTo(inputs);
    var btn = $('<button>',{
        text: TITLES["login"],
		class: "login-btn right purple"
    }).appendTo(row1);
	var forgot = $("<button>", {
		text: TITLES["forgot_password"],
		class: "login-forgot"
	}).appendTo(container);	
	
	return new PreLoginForm(handle, psw, btn, forgot, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError);
}

function logout(evt) {
	/**
	 * May be triggered by: 1. GUI click on the logout button 2. token expiry on operations
	 * */

	var menu = ((evt && evt.data) ? evt.data : g_postLoginMenu);
	if (menu == null) return;

	var token = $.cookie(g_keyToken);
	var params = {};
	params[g_keyToken] = token;

	var aButton = (evt ? $(evt.srcElement ? evt.srcElement : evt.target) : null);
	disableField(aButton);
	$.ajax({
		type: "POST",
		url: "/user/logout",
		data: params,
		success: function(data, status, xhr){
			enableField(aButton);
			g_loggedInUser = null;
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
	if (g_loggedInUser == null) return null;
	par.empty();

	var createReact = function(evt){
		evt.preventDefault();
		showActivityEditor(null);
	};

	var profileReact = function(evt){
		evt.preventDefault();
		if (g_loggedInUser == null) return;
		window.location.hash = ("profile?" + g_keyVieweeId + "=" + g_loggedInUser.id.toString());
	};
	
	var logoutReact = logout;
	
	var userBox = $("<div>", {
		class: "user-box clearfix"
	}).appendTo(par);
	var avatarContainer = $("<div>", {
		class: "post-login-avatar left"
	}).appendTo(userBox);
	var avatarImage = $("<img>", {
		src: g_loggedInUser.avatar
	}).appendTo(avatarContainer);
	var avatarSpan = $("<span>", {
		text: TITLES["edit"]
	}).appendTo(avatarContainer);
	avatarContainer.click(function(evt){
		evt.preventDefault();
		if (g_loggedInUser == null) return;
		showAvatarEditor(g_loggedInUser);
	});
	
	var userBoxLeft = $("<div>", {
		class: "user-box-left left clearfix"
	}).appendTo(userBox);
	var leftFirstRow = $("<div>", {
		class: "left-first-row clearfix"
	}).appendTo(userBoxLeft);
	var noti = $("<div>", {
		class: "noti-container left"
	}).appendTo(leftFirstRow);

	noti.click(function(evt){
		evt.preventDefault();		
		if (g_loggedInUser == null) return;
		window.location.hash = "notifications";
	}); 

	var spBubble = $("<span>", {
		class: "noti-bubble",
		text: "0"
	}).appendTo(noti);
	var bubble = new NotiBubble(0, spBubble);
	bubble.update(g_loggedInUser.unreadCount);

	var userName = $("<div>", {
		class: "username left",
		html: g_loggedInUser.name
	}).appendTo(leftFirstRow);

	var postLoginMenuContainer = $("<div>", {
		class: "menu-action-row"
	}).appendTo(userBoxLeft);

	var icons = ["/assets/icons/new_activity.png", "/assets/icons/profile.png", "/assets/icons/logout.png"];
	var actionNames = ["create", "profile", "logout"];
	var titles = [TITLES["create"], TITLES["profile"], TITLES["logout"]];
	var reactions = [createReact, profileReact, logoutReact]; 

	var dropdownMenu = createDropdownMenu(postLoginMenuContainer, "menu-post-login", g_loggedInUser.name, icons, actionNames, titles, reactions);
	var menu = new PostLoginMenu(bubble, dropdownMenu, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError);
	var params = [menu, menu, menu];
	dropdownMenu.setReactionParams(params);
	return menu;

}

function checkLoginStatus(){

	var token = $.cookie(g_keyToken);
	if(token == null) {
		if(g_preLoginForm == null) return;
		g_preLoginForm.onLoginError(null);
		return;
	}

	if (g_loggedInUser != null) {
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
		url: "/user/status",
		data: params,
		success: function(data, status, xhr){
			if (isStandardFailure(data)) {
				wsDisconnect();	
				$.removeCookie(g_keyToken, {path: '/'});
				if(g_preLoginForm.onLoginError == null) return;
				g_preLoginForm.onLoginError(null);		
				return;
			}
			var userJson = JSON.parse(data);
			g_loggedInUser = new User(userJson);
			if (g_loggedInUser == null) return;
			$.cookie(g_keyToken, userJson[g_keyToken], {path: '/'});
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
