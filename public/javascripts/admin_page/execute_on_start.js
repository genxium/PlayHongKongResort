$(document).ready(function(){
	// execute on page loaded
	g_callbackOnActivityAccepted=queryActivitiesByAdmin;
	g_callbackOnActivityDeleted=queryActivitiesByAdmin;
    queryActivitiesByAdmin(0);

	$("#"+g_idBtnPreviousPage).bind("click", onBtnPreviousPageClicked);
	$("#"+g_idBtnNextPage).bind("click", onBtnNextPageClicked);
    $("#"+g_idBtnSwitchMonitor).bind("click", onBtnSwitchMonitorClicked);
});
