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

		if( (email == null || email.length == 0 || !validateEmail(email))
		    || password == null || password.length == 0 || !validatePassword(password)) return;

		var params={};
		params[g_keyEmail] = email;
		params[g_keyPassword] = password;
		
		$.ajax({
		    type: "POST",
		    url: "/user/login",
		    data: params,
		    success: function(data, status, xhr){
				var userJson = JSON.parse(data);
				g_loggedInUser = new User(userJson);
				if (g_loggedInUser == null) return;
				// store token in cookie iff query succeeds
				$.cookie(g_keyToken, userJson[g_keyToken], {path: '/'});
				wsConnect();	
				if(g_sectionLogin == null) return;
				g_postLoginMenu = generatePostLoginMenu(g_sectionLogin, form.onLoginSuccess, form.onLoginError, form.onLogoutSuccess, form.onLogoutError);
				if(form.onLoginSuccess == null) return;
				form.onLoginSuccess(data);
		    },
		    error: function(xhr, status, err){
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
		class: "login-row1"
	}).appendTo(container);
	var inputs = $('<div>', {
		class: "login-inputs"
	}).appendTo(row1);
	var handle = $('<input>', {
		placeHolder: "Email",
		type: "text",
		class: "login-email"
	}).appendTo(inputs);
	var psw = $('<input>', {
        placeHolder: "Password",
        type: "password",
		class: "login-pw"
    }).appendTo(inputs);
    var btn = $('<button>',{
        text: "Login",
		class: "login-btn"
    }).appendTo(row1);
	var forgot = $("<button>", {
		text: "Forgot Password",
		class: "login-fotgot"
	}).appendTo(container);	
	
	return new PreLoginForm(handle, psw, btn, forgot, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError);
}

function generatePostLoginMenu(par, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError){
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
		window.location.hash = (g_keyVieweeId + "=" + g_loggedInUser.id.toString());
	};
	
	var logoutReact = function(evt){
		evt.preventDefault();
		var menu = evt.data;
		if (menu == null) return;
		var token = $.cookie(g_keyToken);
		var params = {};
		params[g_keyToken] = token;
		$.ajax({
			type: "POST",
			url: "/user/logout",
			data: params,
			success: function(data, status, xhr){
				g_loggedInUser = null;
				$.removeCookie(g_keyToken, {path: '/'});
				wsDisconnect();
				if (g_sectionLogin == null) return;
				g_preLoginForm = generatePreLoginForm(par, menu.onLoginSuccess, menu.onLoginError, menu.onLogoutSuccess, menu.onLogoutError);
				if (menu.onLogoutSuccess == null) return;
				menu.onLogoutSuccess(data);
			},
			error: function(xhr, status, err){
				// reload the whole page if exception occurs
				wsDisconnect();
				$.removeCookie(g_keyToken, {path: '/'});
				if (menu.onLogoutError == null) return;
				menu.onLogoutError(error);
			}	
		});
	};

	var noti = $("<span>", {
		style: "cursor: pointer; position: absolute; width: 20%; height: 80%; left: 45%; top: 20px;"
	}).appendTo(par);

	noti.click(function(evt){
		evt.preventDefault();		
		if (g_loggedInUser == null) return;
		window.location.hash = "notifications";
	}); 

	setBackgroundImage(noti, "/assets/icons/notification.png", "contain", "no-repeat", "center");
	var spBubble = $("<span>", {
		style: "position: absolute; width: 20px; height: 20px; left: 70%; top: 10%; border-radius: 50%; background-color: indianred; text-align: center; vertical-align: middle; font-size: auto; color: white",
		text: "0"
	}).appendTo(noti);
	var bubble = new NotiBubble(0, spBubble);
	bubble.update(g_loggedInUser.unreadCount);

	var spDropdown = $("<span>", {
		style: "position: absolute; width: 30%; height: 80%; left: 70%; top: 20px;"
	}).appendTo(par);

	var icons = ["/assets/icons/new_activity.png", "/assets/icons/profile.png", "/assets/icons/logout.png"];
	var titles = ["create", "profile", "logout"];
	var reactions = [createReact, profileReact, logoutReact]; 

	var dropdownMenu = createDropdownMenu(spDropdown, "menu-post-login", g_loggedInUser.name, icons, titles, reactions);
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
				g_preLoginForm.onLoginError(err);		
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
