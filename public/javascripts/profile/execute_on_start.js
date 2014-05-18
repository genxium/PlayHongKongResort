$(document).ready(function(){
    // initialize local DOMs
	g_sectionLogin=$("#idSectionLogin");
	var loginForm=generateLoginForm();
    g_sectionLogin.append(loginForm);

	g_sectionUploadAvatar=$("#idSectionUploadAvatar");

	g_callbackOnLoginSuccess=function(){
		refreshOnLoggedIn();
	};

	g_callbackOnLoginError=null;

	g_callbackOnEnter=function(){
		refreshOnEnter();
	};

	initActivityEditor();

	var sectionOwnedActivities=$("#"+g_idSectionOwnedActivities); 
	sectionOwnedActivities.bind("scroll", onSectionOwnedActivitiesScrolled);
	sectionOwnedActivities.data(g_keyPageIndex, 0);

	$("#"+g_idBtnUploadAvatar).bind("click", onBtnUploadAvatarClicked);
 	g_callbackOnActivityEditorRemoved=queryActivitiesHostedByUser;
 	queryActivitiesHostedByUser(0, g_numItemsPerPage, g_directionForward);

 	$("#"+g_idBtnPreviousPage).bind("click", onBtnPreviousPageClicked);
	$("#"+g_idBtnNextPage).bind("click", onBtnNextPageClicked);

	checkLoginStatus();
});
