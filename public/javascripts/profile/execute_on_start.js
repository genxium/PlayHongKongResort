$(document).ready(function(){

	var params=extractParams(window.location.href);
	for(var i=0;i<params.length;i++){
		var param=params[i];
		var pair=param.split("=");
		if(pair[0] == g_keyVieweeId){
			g_vieweeId = pair[1];
			break;
		}
	}
	
	initTopbar();
	
	// initialize pager widgets
	g_pagerContainer = new PagerContainer($("#pager-screen-activities"), $("#pager-bar-activities"), g_keyActivityId, g_orderDescend, g_numItemsPerPage);		
	g_pagerContainer.relation = hosted;
	g_pagerContainer.orientation = g_orderDescend;

	g_onQueryActivitiesSuccess = onQueryActivitiesSuccess;
	g_onQueryActivitiesError = onQueryActivitiesError;
	
	g_onLoginSuccess = function(){
		refreshOnLoggedIn();
		queryActivitiesAndRefresh();
	};
	g_onLoginError = null;
	g_onEnter = refreshOnEnter;
	initActivityEditor();

	g_formAvatar = $("#form-avatar");
	g_sectionResponse = $("#section-response");

	g_activitiesFilter = $("#activities-filter");
	g_activitiesFilter.on("change", function(){
		g_pagerContainer.relation = $(this).val();
		queryActivitiesAndRefresh();	 
	});

	g_activitiesSorter = $("#activities-sorter");
	g_activitiesSorter.on("change", function(){
		g_pagerContainer.orientation = $(this).val();
		queryActivitiesAndRefresh();
	});


	g_sectionUser = $("#section-user");

	g_btnUploadAvatar = $("#btn-upload-avatar");
	g_btnUploadAvatar.on("click", onBtnUploadAvatarClicked);
 	g_onEditorRemoved = queryActivitiesAndRefresh;

	g_onActivitySaveSuccess = queryActivitiesAndRefresh;

	checkLoginStatus();
});
