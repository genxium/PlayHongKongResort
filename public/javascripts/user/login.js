var g_sectionLogin = null;

var g_loginUserHandle = null;
var g_loginPassword = null;

var g_btnLogin = null;
var g_btnLogout = null;
var g_btnNotification = null;
var g_btnProfile = null;
var g_btnCreate = null;
var g_btnResetPassword = null;

var g_onLoginSuccess = null;
var g_onLoginError = null;
var g_onEnter = null;

var g_loggedInUser = null;

function initLoginWidget() {
	if(g_sectionLogin == null) return;
	generateLoginForm(g_sectionLogin);
}

function onBtnLoginClicked(evt) {
        var email = g_loginUserHandle.val();
        var password = g_loginPassword.val();

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
		generateLoggedInMenu(g_sectionLogin);
                if(g_onLoginSuccess == null) return;
		g_onLoginSuccess();
	    },
	    error: function(xhr, status, err){
		if(g_onLoginError == null) return;
		g_onLoginError();
	    }
	});
}

function onBtnLogoutClicked(evt){
	var token = $.cookie(g_keyToken);
	var params={};
	params[g_keyToken]=token;
	$.ajax({
		type: "POST",
		url: "/user/logout",
		data: params,
		success: function(data, status, xhr){
			g_loggedInUser = null;
			$.removeCookie(g_keyToken, {path: '/'});
			wsDisconnect();
			if(g_sectionLogin == null) return;
			g_sectionLogin.empty();
			generateLoginForm(g_sectionLogin);
			if(g_onEnter == null) return;
			g_onEnter();
		},
		error: function(xhr, status, err){
			// reload the whole page if exception occurs
			wsDisconnect();
			$.removeCookie(g_keyToken, {path: '/'});
			location.reload();	
		}	
	});
}

function onBtnProfileClicked(evt) {
	requestProfile(g_loggedInUser.id);
}

function onBtnCreateClicked(evt) {
	evt.preventDefault();
	showActivityEditor(null);
}

function onBtnNotificationClicked(evt) {

}

function generateLoginForm(par){
	var ret = $('<table>', {
		style: "border-collapse:separate; border-spacing:5pt; margin: auto"
	}).appendTo(par);
	var row1 = $('<tr>').appendTo(ret);
	var cell11 = $('<td>').appendTo(row1);
	g_loginUserHandle = $('<input>', {
		placeHolder: "Email",
		type: "text",	
		style: "font-family: Serif; font-size: 14pt; margin-left: 2pt"
	}).appendTo(cell11);

	g_loginUserHandle.keypress(function (evt) {
  		if (evt.which != 13) return;
		evt.preventDefault();
		g_btnLogin.click();
	});
	
	var cell12 = $("<td>").appendTo(row1);
	g_loginPassword = $('<input>', {
                placeHolder: "Password",
                type: "password",
                style: "font-family: Serif; font-size: 14pt; margin-left: 2pt"
        }).appendTo(cell12);

	g_loginPassword.keypress(function (evt) {
  		if (evt.which != 13) return;
		evt.preventDefault();
    		g_btnLogin.click();
	});

	var row2 = $('<tr>').appendTo(ret);
	var cell21 = $('<td>').appendTo(row2);
        g_btnLogin=$('<button>', {
                style: "font-size: 14pt; margin-left: 2pt; background-color: IndianRed; color: white",
                text: "Login"
        }).appendTo(cell21);

        g_btnLogin.on("click", onBtnLoginClicked);

	var cell22=$('<td>').appendTo(row2);
	g_btnResetPassword = $("<button>", {
		text: "Forgot Password",
		style: "background: none; border: none; color: white; font-size: 12pt; vertical-align: middle; text-align: center; cursor: pointer; text-decoration: underline;"	
	}).appendTo(cell22);	
	g_btnResetPassword.click( function(evt) {
		evt.preventDefault();
		window.open("/user/password/index");
	});
}

function generateLoggedInMenu(par){

	var ret = $("<div>", {
		style: "height: 100%"
	}).appendTo(par);
	
	var rightHalf = $('<div>',{
		style: "width: auto; height: 100%; margin-left: 5pt; float: right;"	
	}).appendTo(ret);

	var greetingText = g_loggedInUser.name;
	var row1 = $("<p>", {
		style: "width: auto; height: 40%;"
	}).appendTo(rightHalf);
	var greeting = $("<span>" ,{
		style: "font-size: 13pt; color: white",
		text: greetingText	
	}).appendTo(row1);
	g_btnLogout = $('<button>', {
		style: "clear: both; margin-left: 10pt; font-size: 13pt; color: white; background-color: crimson",
		text: 'Logout'
	}).appendTo(row1);

	g_btnLogout.click(onBtnLogoutClicked);

	var row2 = $("<p>", {
		style: "width: auto; height: 40%;"
	}).appendTo(rightHalf);

	g_btnNotification = $("<button>", {
		style: "width: 30%; height: 90%;"
	}).appendTo(row2);
	setBackgroundImageDefault(g_btnNotification, "/assets/icons/notification.png");
	g_btnNotification.click(onBtnNotificationClicked);

	g_btnProfile = $("<button>", {
		style: "width: 30%; height: 90%; margin-left: 5pt"
	}).appendTo(row2);
	setBackgroundImageDefault(g_btnProfile, "/assets/icons/profile.png");
	g_btnProfile.click(onBtnProfileClicked);

	g_btnCreate = $("<button>", {
		style: "width: 30%; height: 90%; margin-left: 5pt"
	}).appendTo(row2);
	setBackgroundImageDefault(g_btnCreate, "/assets/icons/new_activity.png");
	g_btnCreate.click(onBtnCreateClicked);

	var leftHalf = $("<div>", {
		style: "height: 100%; float: right;"
	}).appendTo(ret);
	var avatar = $('<img>',{
		src: g_loggedInUser.avatar,
		style: "width: 50pt; height: auto;"	
	}).appendTo(leftHalf);

}

function checkLoginStatus(){
	var token = $.cookie(g_keyToken);
	if(token == null) {
		if(g_onEnter == null) return;
		g_onEnter();
		return;
	}
	var params={};
	params[g_keyToken]=token.toString();
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
			generateLoggedInMenu(g_sectionLogin);
			if(g_onLoginSuccess == null)	return;
			g_onLoginSuccess();	
		},
		error: function(xhr, status, errorThrown){
			wsDisconnect();	
			$.removeCookie(g_keyToken, {path: '/'});
			if(g_onEnter ==null) return;
			g_onEnter();		
		}
	});

}
