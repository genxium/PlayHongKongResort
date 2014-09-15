$(document).ready(function(){
    	// initialize local DOMs
	initTopbar();

	g_onLoginSuccess = queryActivitiesAndRefresh;
	g_onLoginError = null;
	g_onEnter = queryActivitiesAndRefresh;
	initActivityEditor();

	// initialize pager widgets
	g_pagerContainer = new PagerContainer($("#pager-screen-activities"), $("#pager-bar-activities"), g_keyActivityId, g_orderDescend, g_numItemsPerPage);		
	g_pagerContainer.status = g_statusPending;

	g_selectFilter = $("#select-filter");
	g_selectFilter.on("change", function() {
		g_pagerContainer.status = $(this).val();
		queryActivitiesAndRefresh();
	});

	checkLoginStatus();
});
