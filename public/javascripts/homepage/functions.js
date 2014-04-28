// Assistant Functions
function queryDefaultActivities(refIndex, numItems, direction){
	var params={};
	params[g_keyRefIndex]=refIndex.toString();
	params[g_keyNumItems]=numItems.toString();
	params[g_keyDirection]=direction.toString();

	var token = $.cookie(g_keyLoginStatus.toString());
    if(token!=null) params[g_keyToken]=token;

	try{
		$.ajax({
            method: "GET",
            url: "/activity/query",
			data: params,
			success: g_callbackOnQueryActivitiesSuccess,
            error: function(data, status, xhr){

            }
        });
	} catch(err){

	}
}

// Assistant Handlers
function onBtnLogoutClicked(evt){
	var token = $.cookie(g_keyLoginStatus.toString());
	try{
		$.post("/user/logout",
			{
				UserToken: token.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					$.removeCookie(g_keyLoginStatus.toString());
    					refreshOnEnter();
    					queryDefaultActivities(0, g_numItemsPerPage, g_directionForward);
    				} else{

    				}
			}
		);
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

// Generators
function generateLoggedInUserMenu(){

	var ret=$('<div>',
			{
				class: g_classLoggedInUserMenu
			});

	var btnLogout=$('<button>',
			{
				class: g_classBtnLogout,
				text: 'Logout'
			});
	btnLogout.on("click", onBtnLogoutClicked);
	ret.append(btnLogout);
	ret.data(g_indexBtnLogout, btnLogout);

	var btnProfile=$('<button>',
			{
				class: g_classBtnProfile,
				text: 'Profile'
			});

	btnProfile.on("click", onBtnProfileClicked);
	ret.append(btnProfile);
	ret.data(g_indexBtnProfile, btnProfile);

	return ret;
}

function checkLoginStatus(evt){

	do{
		var token = $.cookie(g_keyLoginStatus.toString());
		if(token==null) {
			// refresh screen
			refreshOnEnter();
			queryDefaultActivities(0, g_numItemsPerPage, g_directionForward);
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
                // store token in cookie iff query succeeds
                $.cookie(g_keyLoginStatus.toString(), userJson[g_keyUserToken]);
                // refresh screen
                refreshOnLoggedIn();
                queryDefaultActivities(0, g_numItemsPerPage, g_directionForward);
            },
            error: function(xhr, status, errorThrown){
                // refresh screen
                $.removeCookie(g_keyLoginStatus.toString());
                refreshOnEnter();
                queryDefaultActivities(0, g_numItemsPerPage, g_directionForward);
            }
        });

	} while(false);
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

    hideAccountSections();

	var sectionActivityEditor=$("#"+g_idSectionActivityEditor);
	sectionActivityEditor.empty();
	sectionActivityEditor.show();

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
	greetingMessage.append(menu);
	sectionUserInfo.data(g_indexLoggedInUserMenu, menu);
	menu.hide();

	sectionUserInfo.on("mouseenter", onMouseEnterSectionUserInfo);
	sectionUserInfo.on("mouseleave", onMouseLeaveSectionUserInfo);

	$("#"+g_idBtnCreate).show();
}
