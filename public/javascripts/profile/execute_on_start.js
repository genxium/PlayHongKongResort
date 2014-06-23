$(document).ready(function(){
    	// initialize local DOMs
	initLoginWidget();
	g_callbackOnLoginSuccess=refreshOnLoggedIn;
	g_callbackOnLoginError=null;
	g_callbackOnEnter=refreshOnEnter
	initActivityEditor();

	g_sectionUploadAvatar=$("#idSectionUploadAvatar");

	g_activitiesFilter=$("#activitiesFilter");
	g_activitiesSorter=$("#activitiesSorter");

	g_sectionActivities=$("#idSectionActivities"); 
	g_sectionActivities.bind("scroll", onSectionActivitiesScrolled);
	g_sectionActivities.data(g_keyPageIndex, 0);

	$("#"+g_idBtnUploadAvatar).bind("click", onBtnUploadAvatarClicked);
 	g_callbackOnActivityEditorRemoved=queryActivities;
 	queryActivities(0, g_numItemsPerPage, g_directionForward);

 	$("#"+g_idBtnPreviousPage).bind("click", onBtnPreviousPageClicked);
	$("#"+g_idBtnNextPage).bind("click", onBtnNextPageClicked);

	checkLoginStatus();
});
