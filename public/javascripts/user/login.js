var g_sectionLogin=null;

var g_loginUserHandle=null;
var g_loginPassword=null;

var g_btnLogin=null;
var g_btnLogout=null;
var g_btnProfile=null;
var g_btnCreate=null;

var g_callbackOnLoginSuccess=null;
var g_callbackOnLoginError=null;
var g_callbackOnEnter=null;

function initLoginWidget(){
	g_sectionLogin=$("#idSectionLogin");
	var loginForm=generateLoginForm();
	g_sectionLogin.append(loginForm);
}

function onBtnLoginClicked(evt){
    do{
        var email=g_loginUserHandle.val();
        var password=g_loginPassword.val();

        if( (email==null || email.length==0 || validateEmail(email)==false)
            || password==null || password.length==0 || validatePassword(password)==false) break;

        var params={};
        params[g_keyEmail]=email;
        params[g_keyPassword]=password;
        
        $.ajax({
            type: "POST",
            url: "/user/login",
            data: params,
            success: function(data, status, xhr){
                var userJson=JSON.parse(data);
                g_username=userJson[g_keyName];
                g_userAvatarURL=userJson[g_keyURL];
                // store token in cookie iff query succeeds
                $.cookie(g_keyToken, userJson[g_keyToken], {path: '/'});
		$.cookie(g_keyUid, userJson[g_keyId], {path: '/'});
		if(g_sectionLogin!=null){
			g_sectionLogin.empty();
			g_sectionLogin.append(generateLoggedInMenu);
		}
		if(g_callbackOnLoginSuccess!=null){
			g_callbackOnLoginSuccess();	
		}
            },
            error: function(xhr, status, err){
                if(g_callbackOnLoginError!=null){
			g_callbackOnLoginError();	
		} 
            }
        });
    }while(false);
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
				$.removeCookie(g_keyUid, {path: '/'});
				if(g_sectionLogin!=null){
					g_sectionLogin.empty();
					g_sectionLogin.append(generateLoginForm);
				}
				if(g_callbackOnEnter!=null){
					g_callbackOnEnter();	
				}
			},
			error: function(xhr, status, err){
				
			}	
		});
	} catch(err){

	}
}

function onBtnProfileClicked(evt){
	try{
		var target=$.cookie(g_keyUid);
		var profilePath="/user/profile?"+g_keyTarget+"="+target;
		var profilePage=window.open(profilePath);
	} catch (err){

	}
}

function onBtnCreateClicked(evt){
	evt.preventDefault();
	g_callbackOnEditorCancelled=function(){
		g_sectionActivityEditor.modal("hide");
	};
	g_activityEditor=generateActivityEditorByJson(null);	
	g_modalActivityEditor.empty();
	g_modalActivityEditor.append(g_activityEditor);
	g_sectionActivityEditor.css("height", "80%");
	g_sectionActivityEditor.css("padding", "5pt");
	g_sectionActivityEditor.modal({
		show: true
	});
}

function generateLoginForm(){
	var ret=$('<p>', {
		class: "margin-top: 2pt; margin-bottom: 2pt"
	});
	var span1=$('<span>').appendTo(ret);
	g_loginUserHandle=$('<input>', {
		placeHolder: "Email",
		type: "text",	
		style: "font-size: 14pt; margin-right: 2pt"
	}).appendTo(span1);

	g_loginUserHandle.keypress(function (evt) {
  		if (evt.which == 13) {
  			evt.preventDefault();
    		g_btnLogin.click();
  		}
	});

	var span2=$('<span>').appendTo(ret);
	g_loginPassword=$('<input>', {
		placeHolder: "Password",
		type: "password",
		style: "font-size: 14pt; margin-left: 2pt"
	}).appendTo(span2);

	g_loginPassword.keypress(function (evt) {
  		if (evt.which == 13) {
  			evt.preventDefault();
    		g_btnLogin.click();
  		}
	});

	var span3=$('<span>').appendTo(ret);
	g_btnLogin=$('<button>', {
		style: "font-size: 14pt; margin-left: 2pt; background-color: yellow",	
		text: "login"
	}).appendTo(span3);
	g_btnLogin.on("click", onBtnLoginClicked);

	return ret;	
}

function generateLoggedInMenu(){

	var ret=$('<div>');
	
	var avatar=$('<img>',{
		src: g_userAvatarURL,
		style: "width: 50pt; height 50pt; float: left"	
	}).appendTo(ret);

	var rightHalf=$('<div>',{
		style: "width: 200pt; height 50pt; margin-left: 10pt; float: left"	
	}).appendTo(ret);

	var greeting=$('<p>',{
		style: "font-size: 15pt; color: white",
		text: "Hello, "+g_username	
	}).appendTo(rightHalf);

	g_btnLogout=$('<button>', {
		style: "clear: both; font-size: 15pt; color: white; background-color: crimson",
		text: 'Logout'
	}).appendTo(rightHalf);
	g_btnLogout.on("click", onBtnLogoutClicked);

	g_btnProfile=$('<button>', {
		style: "font-size: 15pt; color: white; background-color: cornflowerblue",
		text: 'Profile'
	}).appendTo(rightHalf);
	g_btnProfile.on("click", onBtnProfileClicked);

	g_btnCreate=$('<button>', {
		style: "font-size: 15pt; margin-left: 10pt; background-color: chartreuse",
		text: "Create"	
	}).appendTo(rightHalf);
	g_btnCreate.on("click", onBtnCreateClicked);

	return ret;
}

function checkLoginStatus(){
	do{
		var token = $.cookie(g_keyToken);
		if(token==null) {
			if(g_callbackOnEnter!=null){
				g_callbackOnEnter();
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
				var userJson=JSON.parse(data);
				g_username=userJson[g_keyName];
				g_userAvatarURL=userJson[g_keyURL];
				$.cookie(g_keyToken, userJson[g_keyToken], {path: '/'});
				$.cookie(g_keyUid, userJson[g_keyId], {path: '/'});
				if(g_sectionLogin!=null){
					g_sectionLogin.empty();
					g_sectionLogin.append(generateLoggedInMenu);
				}
				if(g_callbackOnLoginSuccess!=null){
					g_callbackOnLoginSuccess();	
				}
            		},
			error: function(xhr, status, errorThrown){
				// refresh screen
				$.removeCookie(g_keyToken, {path: '/'});
				$.removeCookie(g_keyUid, {path: '/'});
				if(g_callbackOnEnter!=null){
					g_callbackOnEnter();		
				}
			}
		});

	} while(false);
}
