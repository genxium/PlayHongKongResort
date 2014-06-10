$(document).ready(function(){
	// execute on page loaded
	g_sectionActivityMonitor=$("#idSectionActivityMonitor");
	g_btnPreviousPage=$("#idBtnPreviousPage");
	g_btnNextPage=$("#idBtnNextPage");
	g_selectFilter=$("#idSelectFilter");
	
	g_callbackOnActivityAccepted=queryActivities;
	g_callbackOnActivityRejected=queryActivities;
	g_callbackOnActivityDeleted=queryActivities;
    queryActivities(0, g_numItemsPerPage, g_directionForward);

	g_btnPreviousPage.on("click", onBtnPreviousPageClicked);
	g_btnNextPage.on("click", onBtnNextPageClicked);
    g_selectFilter.on("change", onSelectFilterChanged);
});
