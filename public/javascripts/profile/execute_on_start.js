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

	g_onQueryActivitiesSuccess = onQueryActivitiesSuccess;
	g_onQueryActivitiesError = onQueryActivitiesError;
	
	g_onActivitySaveSuccess = function() {
		queryActivities(0, g_numItemsPerPage, order, g_directionForward, g_userId, relation, null, onQueryActivitiesSuccess, onQueryActivitiesError);
	}

	g_onLoginSuccess = function(){
		refreshOnLoggedIn();
		var relation = g_activitiesFilter.val();
		var order = g_activitiesSorter.val();
		queryActivities(0, g_numItemsPerPage, order, g_directionForward, g_userId, relation, null, onQueryActivitiesSuccess, onQueryActivitiesError);
	};
	g_onLoginError = null;
	g_onEnter = refreshOnEnter;
	initActivityEditor();

	g_formAvatar = $("#form-avatar");
	g_sectionResponse=$("#section-response");

	g_activitiesFilter = $("#activities-filter");
	g_activitiesFilter.on("change", function(){
		var relation = g_activitiesFilter.val();
		var order = g_activitiesSorter.val();
		queryActivities(0, g_numItemsPerPage, order, g_directionForward, g_userId, relation, null, onQueryActivitiesSuccess, onQueryActivitiesError);
	});

	g_activitiesSorter = $("#activities-sorter");
	g_activitiesSorter.on("change", function(){
		var relation = g_activitiesFilter.val();
		var order = g_activitiesSorter.val();
		queryActivities(0, g_numItemsPerPage, order, g_directionForward, g_userId, relation, null, onQueryActivitiesSuccess, onQueryActivitiesError);
	});

	g_sectionActivities = $("#section-activities"); 
	g_sectionActivities.bind("scroll", onSectionActivitiesScrolled);
	g_sectionActivities.data(g_keyPageIndex, 0);

	g_sectionUser=$("#section-user");

	g_btnUploadAvatar=$("#btn-upload-avatar");
	g_btnUploadAvatar.on("click", onBtnUploadAvatarClicked);
 	g_onEditorRemoved = queryActivities;

	var relation = g_activitiesFilter.val();
	var order = g_activitiesSorter.val();
	queryActivities(0, g_numItemsPerPage, order, g_directionForward, g_userId, relation, null, onQueryActivitiesSuccess, onQueryActivitiesError);

	initWidgets(onBtnPreviousPageClicked, onBtnNextPageClicked);
	checkLoginStatus();
});
