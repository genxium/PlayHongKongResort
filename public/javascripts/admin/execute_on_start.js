$(document).ready(function(){
	// execute on page loaded
	g_sectionActivityMonitor=$("#idSectionActivityMonitor");
	g_selectFilter=$("#idSelectFilter");
	
	g_callbackOnActivityAccepted=queryActivities;
	g_callbackOnActivityDeleted=queryActivities;
    queryActivities(0, g_numItemsPerPage, g_directionForward);

	$("#"+g_idBtnPreviousPage).bind("click", onBtnPreviousPageClicked);
	$("#"+g_idBtnNextPage).bind("click", onBtnNextPageClicked);
    g_selectFilter.bind("change", onSelectFilterChanged);
});
