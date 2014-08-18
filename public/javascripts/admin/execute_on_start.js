$(document).ready(function(){
    	// initialize local DOMs
	initTopbar();

	g_onLoginSuccess=null;
	g_onLoginError=null;
	g_onEnter=null;
	initActivityEditor();

	g_sectionActivityMonitor=$("#idSectionActivityMonitor");
	g_selectFilter=$("#idSelectFilter");
	
	g_onActivityAccepted=queryActivities;
	g_onActivityRejected=queryActivities;
	g_onActivityDeleted=queryActivities;
	queryActivities(0, g_numItemsPerPage, g_directionForward);

	g_selectFilter.on("change", onSelectFilterChanged);

	initWidgets(onBtnPreviousPageClicked, onBtnNextPageClicked);
	checkLoginStatus();
});
