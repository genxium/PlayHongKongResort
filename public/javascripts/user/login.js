var g_sectionLogin = null;

var g_loginUserHandle = null;
var g_loginPassword = null;

var g_btnLogin = null;
var g_btnLogout = null;
var g_btnProfile = null;
var g_btnCreate = null;

var g_onLoginSuccess = null;
var g_onLoginError = null;
var g_onEnter = null;

var g_loggedInUser = null;

function initLoginWidget(){
	if(g_sectionLogin == null) return;
	var loginForm=generateLoginForm();
	g_sectionLogin.append(loginForm);
}

function onBtnLoginClicked(evt){
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
                // store token in cookie iff query succeeds
                $.cookie(g_keyToken, userJson[g_keyToken], {path: '/'});
                if(g_sectionLogin != null){
                    g_sectionLogin.empty();
                    g_sectionLogin.append(generateLoggedInMenu);
                }
                if(g_onLoginSuccess != null){
                    g_onLoginSuccess();
                }
	    },
	    error: function(xhr, status, err){
		if(g_onLoginError != null){
		    g_onLoginError();
		}
	    }
	});
}

function onBtnLogoutClicked(evt){
	try{
		var token = $.cookie(g_keyToken);
		var params={};
		params[g_keyToken]=token;
		$.ajax({
			type: "POST",
			url: "/user/logout",
			data: params,
			success: function(data, status, xhr){
				$.removeCookie(g_keyToken, {path: '/'});
				if(g_sectionLogin!=null){
					g_sectionLogin.empty();
					g_sectionLogin.append(generateLoginForm);
				}
				if(g_onEnter!=null){
					g_onEnter();	
				}
			},
			error: function(xhr, status, err){
				// reload the whole page if exception occurs
				location.reload();	
			}	
		});
	} catch(err){

	}
}

function onBtnProfileClicked(evt){
	try{
		var profilePath = "/user/profile/show?" + g_keyVieweeId + "=" + g_loggedInUser.id;
		var profilePage = window.open(profilePath);
	} catch (err){

	}
}

function onBtnCreateClicked(evt){
	evt.preventDefault();
	g_onEditorCancelled=function(){
		g_sectionActivityEditor.modal("hide");
	};
	g_activityEditor=generateActivityEditor(null);
	g_modalActivityEditor.empty();
	g_modalActivityEditor.append(g_activityEditor);
	g_sectionActivityEditor.modal({
		show: true
	});
}

function generateLoginForm(){
	var ret = $('<table>', {
		style: "border-collapse:separate; border-spacing:5pt; margin: auto"
	});
	var row1 = $('<tr>').appendTo(ret);
	var cell11 = $('<td>').appendTo(row1);
	g_loginUserHandle = $('<input>', {
		placeHolder: "Email",
		type: "text",	
		style: "font-size: 14pt; margin-left: 2pt"
	}).appendTo(cell11);

	g_loginUserHandle.keypress(function (evt) {
  		if (evt.which == 13) {
  			evt.preventDefault();
			g_btnLogin.click();
  		}
	});

	var row2 = $('<tr>').appendTo(ret);
	var cell21 = $('<td>').appendTo(row2);
	g_loginPassword = $('<input>', {
		placeHolder: "Password",
		type: "password",
		style: "font-family: Serif; font-size: 14pt; margin-left: 2pt"
	}).appendTo(cell21);

	g_loginPassword.keypress(function (evt) {
  		if (evt.which == 13) {
  			evt.preventDefault();
    		g_btnLogin.click();
  		}
	});

	var cell22=$('<td>').appendTo(row2);
	g_btnLogin=$('<button>', {
		style: "font-size: 14pt; margin-left: 2pt; background-color: IndianRed; color: white",	
		text: "Login"
	}).appendTo(cell22);
	g_btnLogin.on("click", onBtnLoginClicked);

	return ret;	
}

function generateLoggedInMenu(){

	var ret=$('<div>', {
		style: "height: auto"
	});
	
	var avatar=$('<img>',{
		src: g_loggedInUser.avatar,
		style: "width: 50pt; height: auto; float: left"	
	}).appendTo(ret);

	var rightHalf=$('<div>',{
		style: "width: auto; height: auto; margin-left: 10pt; float: left"	
	}).appendTo(ret);

	var greetingText = "Hello, "+g_loggedInUser.name;
	var greeting=$('<p>',{
		style: "font-size: 13pt; color: white",
		text: greetingText	
	}).appendTo(rightHalf);

	g_btnLogout=$('<button>', {
		style: "clear: both; font-size: 13pt; color: white; background-color: crimson",
		text: 'Logout'
	}).appendTo(rightHalf);
	g_btnLogout.on("click", onBtnLogoutClicked);

	g_btnProfile=$('<button>', {
		style: "font-size: 13pt; color: white; background-color: cornflowerblue",
		text: 'Profile'
	}).appendTo(rightHalf);
	g_btnProfile.on("click", onBtnProfileClicked);

	g_btnCreate=$('<button>', {
		style: "font-size: 13pt; margin-left: 10pt; background-color: chartreuse",
		text: "Create"	
	}).appendTo(rightHalf);
	g_btnCreate.on("click", onBtnCreateClicked);

	return ret;
}

function checkLoginStatus(){
	do{
		var token = $.cookie(g_keyToken);
		if(token==null) {
			if(g_onEnter!=null){
				g_onEnter();
			}
			break;
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
				$.cookie(g_keyToken, userJson[g_keyToken], {path: '/'});
				if(g_sectionLogin != null){
					g_sectionLogin.empty();
					g_sectionLogin.append(generateLoggedInMenu);
				}
				if(g_onLoginSuccess != null){
					g_onLoginSuccess();	
				}
            		},
			error: function(xhr, status, errorThrown){
				// refresh screen
				$.removeCookie(g_keyToken, {path: '/'});
				if(g_onEnter!=null){
					g_onEnter();		
				}
			}
		});

	} while(false);
}
