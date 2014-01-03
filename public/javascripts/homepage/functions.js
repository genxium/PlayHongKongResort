function checkLoginStatus(evt){
	var token = $.cookie(g_keyLoginStatus.toString());
	if(token!=null){
		validateToken(token);
	}
}

function validateToken(token){
	$.post("/checkLoginStatus",
			{
				UserToken: token.toString(),
			},
			// post response callback function
			function(data, status, xhr){
				if(status=="success"){
					var userJson=JSON.parse(data);
    				g_userName=userJson[g_keyUserEmail];
    				g_userAvatarURL=userJson[g_keyImageURL];
    				// store token in cookie iff query succeeds
    				$.cookie(g_keyLoginStatus.toString(), userJson[g_keyUserToken]);
    				// refresh screen
    				refreshOnLoggedIn();
	    		} else{

	    		}
		    }
	);
}

function validateEmail(){
	var x=document.getElementById(g_idFieldEmail).value;
	var atpos=x.indexOf("@");
	var dotpos=x.lastIndexOf(".");
	if (atpos<1 || dotpos<atpos+2 || dotpos+1>=x.length) {
	  alert("Not a valid e-mail address");
	  return false;
	}
	return true;
}

function queryDefaultActivitiesByUser(){
	var targetSection=$("#"+g_idSectionDefaultActivities);
	targetSection.empty();
	var token = $.cookie(g_keyLoginStatus.toString());
	try{
		$.post("/queryDefaultActivitiesByUser", 
			{
				UserToken: token.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					var jsonResponse=JSON.parse(data);
    					for(var key in jsonResponse){
    						var jsonRecord=jsonResponse[key];
    						var cell=generateDefaultActivityCell(jsonRecord);
						targetSection.append(cell);
    					}
    				} else{
    					
    				}
			}
		);
	} catch(err){

	}
}

function onMouseEnterSectionUserInfo(evt){
	var menu=jQuery.data(this, g_indexLoggedInUserMenu);
	menu.show();	
}

function onMouseLeaveSectionUserInfo(evt){
	var menu=jQuery.data(this, g_indexLoggedInUserMenu);
	menu.hide();		
}

function refreshOnLoggedIn(){
	$("#"+g_idSectionAccount).hide();

	var sectionActivityEditor=$("#"+g_idSectionActivityEditor);
	sectionActivityEditor.empty();
	sectionActivityEditor.show();

	var sectionUserProfileEditor=$("#"+g_idSectionUserProfileEditor);
	sectionUserProfileEditor.empty();
	sectionUserProfileEditor.show();

	// bind menu to sectionUserInfo
	var sectionUserInfo=$("#"+g_idSectionUserInfo);
	sectionUserInfo.empty();
	sectionUserInfo.show();

	var greetingMessage=$('<div>',
						{
							class: g_classSectionGreetingMessage,
							html: "Hello, "+g_userName.toString() 
						});
	sectionUserInfo.append(greetingMessage);

	var userAvatar=$('<img>',
						{
							class: g_classSectionUserAvatar,
							src: g_userAvatarURL
						});
	sectionUserInfo.append(userAvatar);

	var menu=generateLoggedInUserMenu();
	sectionUserInfo.append(menu);
	sectionUserInfo.data(g_indexLoggedInUserMenu, menu);
	menu.hide();

	sectionUserInfo.bind("mouseenter", onMouseEnterSectionUserInfo);
	sectionUserInfo.bind("mouseleave", onMouseLeaveSectionUserInfo);

	$("."+g_classBtnCreate).show();
}