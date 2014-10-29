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
			g_sectionLogin.empty();
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

function PostLoginMenu(toggle, notification, create, profile, logout, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError) {
	this.toggle = toggle;
	this.notification = notification;
	this.create = create;
	this.profile = profile;
	this.logout = logout;
	this.onLoginSuccess = onLoginSuccess;
	this.onLoginError = onLoginError;
	this.onLogoutSuccess = onLogoutSuccess;
	this.onLogoutError = onLogoutError;

	this.notification.click(this, function(evt){
		
	});

	this.create.click(function(evt){
		evt.preventDefault();
		showActivityEditor(null);
	});

	this.profile.click(function(evt){
		evt.preventDefault();
		if (g_loggedInUser == null) return;
		requestProfile(g_loggedInUser.id);
	});
	
	this.logout.click(this, function(evt){
		var menu = this;
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
				g_sectionLogin.empty();
				g_preLoginForm = generatePreLoginForm(g_sectionLogin, menu.onLoginSuccess, menu.onLoginError, menu.onLogoutSuccess, menu.onLogoutError);
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
	});
}

function generatePreLoginForm(par, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError) {
	if (par == null) return null;
	var tbl = $('<table>', {
		style: "border-collapse:separate; border-spacing:5pt; margin: auto"
	}).appendTo(par);
	var row1 = $('<tr>').appendTo(tbl);
	var cell11 = $('<td>').appendTo(row1);
	var handle = $('<input>', {
		placeHolder: "Email",
		type: "text",	
		style: "font-family: Serif; font-size: 14pt; margin-left: 2pt"
	}).appendTo(cell11);

	var cell12 = $("<td>").appendTo(row1);
	var psw = $('<input>', {
                placeHolder: "Password",
                type: "password",
                style: "font-family: Serif; font-size: 14pt; margin-left: 2pt"
        }).appendTo(cell12);

	var row2 = $('<tr>').appendTo(tbl);
	var cell21 = $('<td>').appendTo(row2);
        var btn = $('<button>', {
                style: "font-size: 14pt; margin-left: 2pt; background-color: IndianRed; color: white",
                text: "Login"
        }).appendTo(cell21);

	var cell22 = $('<td>').appendTo(row2);
	var forgot = $("<button>", {
		text: "Forgot Password",
		style: "background: none; border: none; color: white; font-size: 12pt; vertical-align: middle; text-align: center; cursor: pointer; text-decoration: underline;"	
	}).appendTo(cell22);	
	
	return new PreLoginForm(handle, psw, btn, forgot, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError);
}

function generatePostLoginMenu(par, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError){
	if (g_loggedInUser == null) return null;
	var container = $("<div>", {
		style: "height: 100%"
	}).appendTo(par);
	
	var rightHalf = $('<div>',{
		style: "width: auto; height: 100%; margin-left: 5pt; float: right;"	
	}).appendTo(container);

	var greetingText = g_loggedInUser.name;
	var row1 = $("<p>", {
		style: "width: auto; height: 40%;"
	}).appendTo(rightHalf);
	var greeting = $("<span>" ,{
		style: "font-size: 13pt; color: white",
		text: greetingText	
	}).appendTo(row1);
	var logout = $('<button>', {
		style: "clear: both; margin-left: 10pt; font-size: 13pt; color: white; background-color: crimson",
		text: 'Logout'
	}).appendTo(row1);

	var row2 = $("<p>", {
		style: "width: auto; height: 40%;"
	}).appendTo(rightHalf);

	var notification = $("<button>", {
		style: "width: 30%; height: 90%;"
	}).appendTo(row2);
	setBackgroundImageDefault(notification, "/assets/icons/notification.png");

	var profile = $("<button>", {
		style: "width: 30%; height: 90%; margin-left: 5pt"
	}).appendTo(row2);
	setBackgroundImageDefault(profile, "/assets/icons/profile.png");

	var create = $("<button>", {
		style: "width: 30%; height: 90%; margin-left: 5pt"
	}).appendTo(row2);
	setBackgroundImageDefault(create, "/assets/icons/new_activity.png");

	var leftHalf = $("<div>", {
		style: "height: 100%; float: right;"
	}).appendTo(container);

	var avatar = $('<img>',{
		src: g_loggedInUser.avatar,
		style: "width: 50pt; height: auto;"	
	}).appendTo(leftHalf);

	return new PostLoginMenu(toggle, notification, create, profile, logout, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError); 

}

function checkLoginStatus(){
	var token = $.cookie(g_keyToken);
	if(token == null) {
		if(g_preLoginForm == null) return;
		g_preLoginForm.onLoginError(null);
		return;
	}
	var params={};
	params[g_keyToken] = token;
	$.ajax({
		method: "GET",
		url: "/user/status",
		data: params,
		success: function(data, status, xhr){
			var userJson = JSON.parse(data);
			g_loggedInUser = new User(userJson);
			if (g_loggedInUser == null) return;
			$.cookie(g_keyToken, userJson[g_keyToken], {path: '/'});
			wsConnect();	
			if(g_sectionLogin == null)	return;
			g_sectionLogin.empty();
			g_postLoginMenu = generatePostLoginMenu(g_sectionLogin, g_preLoginForm.onLoginSuccess, g_preLoginForm.onLoginError, g_preLoginForm.onLogoutSuccess, g_preLoginForm.onLogoutError);
			if(g_preLoginForm.onLoginSuccess == null)	return;
			g_preLoginForm.onLoginSuccess(data);
		},
		error: function(xhr, status, err){
			wsDisconnect();	
			$.removeCookie(g_keyToken, {path: '/'});
			if(g_preLoginForm.onLoginError == null) return;
			g_preLoginForm.onLoginError(err);		
		}
	});

}
