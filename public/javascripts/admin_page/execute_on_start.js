$(document).ready(function(){
	// execute on page loaded
	g_callbackOnActivityAccepted=queryDefaultActivitiesByAdmin;
	queryDefaultActivitiesByAdmin(0);

	$("#"+g_idBtnPreviousPage).bind("click", onBtnPreviousPageClicked);
	$("#"+g_idBtnNextPage).bind("click", onBtnNextPageClicked);
});
