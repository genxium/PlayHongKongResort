$(document).ready(function(){
	g_userId=$('#userId').attr("value");

	initTopbar();
	g_callbackOnLoginSuccess=refreshOnLoggedIn;
	g_callbackOnLoginError=null;
	g_callbackOnEnter=refreshOnEnter
	initActivityEditor();

	g_sectionUploadAvatar=$("#idSectionUploadAvatar");

	g_activitiesFilter=$("#activitiesFilter");
	g_activitiesFilter.on("change", function(){
		queryActivities(0, g_numItemsPerPage, g_directionForward);
	});

	g_activitiesSorter=$("#activitiesSorter");

	g_sectionActivities=$("#idSectionActivities"); 
	g_sectionActivities.bind("scroll", onSectionActivitiesScrolled);
	g_sectionActivities.data(g_keyPageIndex, 0);

	g_btnUploadAvatar=$("#idBtnUploadAvatar");
	g_btnUploadAvatar.on("click", onBtnUploadAvatarClicked);
 	g_callbackOnActivityEditorRemoved=queryActivities;
 	queryActivities(0, g_numItemsPerPage, g_directionForward);

	g_btnPreviousPage=$("#idBtnPreviousPage");
 	g_btnPreviousPage.on("click", onBtnPreviousPageClicked);

	g_btnNextPage=$("idBtnNextPage");
	g_btnNextPage.on("click", onBtnNextPageClicked);

	checkLoginStatus();
});
