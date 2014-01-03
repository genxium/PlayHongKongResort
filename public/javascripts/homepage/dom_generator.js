// Assistant functions
function refreshOnEnter(){

	$("#"+g_idFieldEmail).empty();
	$("#"+g_idFieldEmail).val("");
	$("#"+g_idFieldPassword).empty();
	$("#"+g_idFieldPassword).val("");

	var sectionUserProfileEditor=$("#"+g_idSectionUserProfileEditor);
	sectionUserProfileEditor.empty();
	sectionUserProfileEditor.hide();

	$("#"+g_idSectionAccount).show();
	
	var sectionActivityEditor=$("#"+g_idSectionActivityEditor);
	sectionActivityEditor.empty();
	sectionActivityEditor.hide();

	var sectionUserInfo=$("#"+g_idSectionUserInfo);
	sectionUserInfo.empty();
	sectionUserInfo.hide();

	$("#"+g_classBtnCreate).hide();
	$("."+g_classActivityEditor).hide();

	var sectionDefaultActivities=$("#"+g_idSectionDefaultActivities);
	sectionDefaultActivities.show();
	queryDefaultActivities();
}

function queryDefaultActivities(){
	var targetSection=$("#"+g_idSectionDefaultActivities);
	targetSection.empty();
	try{
		$.post("/queryDefaultActivities", 
			{

			},
			function(data, status, xhr){
    				if(status=="success"){
    					var jsonResponse=JSON.parse(data);
    					for(var key in jsonResponse){
    						var jsonActivity=jsonResponse[key];
    						var cell=generateDefaultActivityCell(jsonActivity);
							targetSection.append(cell);
    					}
    				} else{
    					
    				}
			}
		);
	} catch(err){

	}
}



function onBtnLogoutClicked(evt){
	var token = $.cookie(g_keyLoginStatus.toString());
	try{
		$.post("/logout", 
			{
				UserToken: token.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					$.removeCookie(g_keyLoginStatus.toString());
    					refreshOnEnter();
    				} else{

    				}
			}
		);
	} catch(err){

	}
}

function onBtnProfileClicked(evt){
	try{
		var userProfileEditorPath="/user_profile_editor.html";
		var userProfileEditorPage=window.open(userProfileEditorPath);
		/*
		var profileEditorPage=$('<iframe>',
								{
									class: g_classIFrameUserProfileEditor,
									src: userProfileEditorPath
								});

		var sectionUserProfileEditor=$("#"+g_idSectionUserProfileEditor);
		sectionUserProfileEditor.empty();
		sectionUserProfileEditor.append(profileEditorPage);
		*/
	} catch (err){

	}
}


// generators
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
	btnLogout.bind("click", onBtnLogoutClicked);
	ret.append(btnLogout);
	ret.data(g_indexBtnLogout, btnLogout);

	var btnProfile=$('<button>',
			{
				class: g_classBtnProfile,
				text: 'Profile'
			});

	btnProfile.bind("click", onBtnProfileClicked);
	ret.append(btnProfile);
	ret.data(g_indexBtnProfile, btnProfile);

	return ret;
}
