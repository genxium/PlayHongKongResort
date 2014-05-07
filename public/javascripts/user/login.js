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

function onBtnLoginClicked(evt){
    do{
        var email=g_loginUserHandle.val();
        var password=g_loginPassword.val();

        if( (email==null || email.length==0 || validateEmail(email)==false)
            || password==null || password.length==0 || validatePassword(password)==false) break;

        var params={};
        params[g_keyUserEmail]=email;
        params[g_keyUserPassword]=password;
        
        $.ajax({
            type: "POST",
            url: "/user/login",
            data: params,
            success: function(data, status, xhr){
                var jsonResponse=JSON.parse(data);
                g_userName=jsonResponse[g_keyUserEmail];
                g_userAvatarURL=jsonResponse[g_keyImageURL];
                // store token in cookie iff query succeeds
                $.cookie(g_keyLoginStatus.toString(), jsonResponse[g_keyUserToken]);
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
		var token = $.cookie(g_keyLoginStatus.toString());
		var params={};
		params[g_keyUserToken]=token;
		$.ajax({
			type: "POST",
			url: "/user/logout",
			data: params,
			success: function(data, status, xhr){
				$.removeCookie(g_keyLoginStatus.toString());
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
		var userProfileEditorPath="/show?page=user_profile_editor.html";
		var userProfileEditorPage=window.open(userProfileEditorPath);
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
	g_sectionActivityEditor.modal("show");
}

function generateLoginForm(){
	var ret=$('<p>');
	var span1=$('<span>').appendTo(ret);
	g_loginUserHandle=$('<input>', {
		placeHolder: "Email",
		type: "text",	
		style: "font-size: 15pt"
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
		style: "font-size: 15pt"
	}).appendTo(span2);

	g_loginPassword.keypress(function (evt) {
  		if (evt.which == 13) {
  			evt.preventDefault();
    		g_btnLogin.click();
  		}
	});

	var span3=$('<span>').appendTo(ret);
	g_btnLogin=$('<button>', {
		style: "font-size: 15pt; background-color: yellow",	
		text: "login"
	}).appendTo(span3);
	g_btnLogin.on("click", onBtnLoginClicked);

	return ret;	
}

function generateLoggedInMenu(){

	var ret=$('<table>');
	
	var row1=$('<tr>').appendTo(ret);
	var cell11=$('<td>').appendTo(row1);
	var cell12=$('<td>').appendTo(row1);

	var avatar=$('<img>',{
		src: g_userAvatarURL,
		style: "width: 50pt; height 50pt"	
	}).appendTo(cell11);

	var greeting=$('<plaintext>',{
		style: "font-size: 15pt; color: white",
		text: "Hello, "+g_userName	
	}).appendTo(cell12);

	var row2=$('<tr>').appendTo(ret);
	var cell21=$('<td>').appendTo(row2);
	var cell22=$('<td>').appendTo(row2);
	var cell23=$('<td>').appendTo(row2);
	g_btnLogout=$('<button>', {
		style: "font-size: 15pt",
		text: 'Logout'
	}).appendTo(cell22);
	g_btnLogout.on("click", onBtnLogoutClicked);

	g_btnProfile=$('<button>', {
		style: "font-size: 15pt",
		text: 'Profile'
	}).appendTo(cell22);
	g_btnProfile.on("click", onBtnProfileClicked);

	g_btnCreate=$('<button>', {
		style: "font-size: 15pt",
		text: "Create"	
	}).appendTo(cell23);
	g_btnCreate.on("click", onBtnCreateClicked);

	return ret;
}

function checkLoginStatus(evt){
	do{
		var token = $.cookie(g_keyLoginStatus.toString());
		if(token==null) {
			if(g_callbackOnEnter!=null){
				g_callbackOnEnter();
			}
			break;
		}
        var params={};
        params["token"]=token.toString();
		$.ajax({
            method: "GET",
            url: "/user/status",
            data: params,
            success: function(data, status, xhr){
                var userJson=JSON.parse(data);
                g_userName=userJson[g_keyUserEmail];
                g_userAvatarURL=userJson[g_keyImageURL];
                $.cookie(g_keyLoginStatus.toString(), userJson[g_keyUserToken]);
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
                $.removeCookie(g_keyLoginStatus.toString());
            	if(g_callbackOnEnter!=null){
					g_callbackOnEnter();		
				}
			}
        });

	} while(false);
}
