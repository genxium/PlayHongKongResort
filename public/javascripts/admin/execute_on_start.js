$(document).ready(function(){
    	// initialize local DOMs
	initTopbar();

	g_onLoginSuccess=null;
	g_onLoginError=null;
	g_onEnter=null;
	initActivityEditor();

	g_sectionActivityMonitor=$("#idSectionActivityMonitor");
	g_selectFilter=$("#idSelectFilter");
	g_selectFilter.on("change", onSelectFilterChanged);

	var status = g_selectFilter.val();
	queryActivities(0, g_numItemsPerPage, g_orderDescend, g_directionForward, null, null, status, onQueryActivitiesSuccess, onQueryActivitiesError);

	initWidgets(onBtnPreviousPageClicked, onBtnNextPageClicked);
	checkLoginStatus();
});
