$(document).ready(function(){
	var params=extractParams(window.location.href);
	for(var i=0;i<params.length;i++){
		var param=params[i];
		var pair=param.split("=");
		if(pair[0]==g_keyUserId){
			g_userId=pair[1];
			break;
		}
	}
	
	initTopbar();
	g_callbackOnLoginSuccess=refreshOnLoggedIn;
	g_callbackOnLoginError=null;
	g_callbackOnEnter=refreshOnEnter;
	initActivityEditor();

	g_formAvatar=$("#form_avatar");
	g_sectionResponse=$("#section_response");

	g_activitiesFilter=$("#activitiesFilter");
	g_activitiesFilter.on("change", function(){
		queryActivities(0, g_numItemsPerPage, g_directionForward);
	});

	g_activitiesSorter=$("#activitiesSorter");

	g_sectionActivities=$("#section_activities"); 
	g_sectionActivities.bind("scroll", onSectionActivitiesScrolled);
	g_sectionActivities.data(g_keyPageIndex, 0);

	g_sectionUser=$("#section_user");

	g_btnUploadAvatar=$("#btn_upload_avatar");
	g_btnUploadAvatar.on("click", onBtnUploadAvatarClicked);
 	g_callbackOnActivityEditorRemoved=queryActivities;
 	queryActivities(0, g_numItemsPerPage, g_directionForward);

	initWidgets(onBtnPreviousPageClicked, onBtnNextPageClicked);
	checkLoginStatus();
});
