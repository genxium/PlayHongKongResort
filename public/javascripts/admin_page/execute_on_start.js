$(document).ready(function(){
	// execute on page loaded
	g_callbackOnActivityAccepted=queryPendingActivitiesByAdmin;
	queryPendingActivitiesByAdmin(0);

	$("#"+g_idBtnPreviousPage).bind("click", onBtnPreviousPageClicked);
	$("#"+g_idBtnNextPage).bind("click", onBtnNextPageClicked);
});