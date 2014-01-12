function onBtnPreviousPageClicked(evt){
	var targetSection=$("#"+g_idSectionActivityMonitor);
	var pageIndex=targetSection.data(g_pageIndexKey);
	queryDefaultActivitiesByAdmin(pageIndex-1);
}

function onBtnNextPageClicked(evt){
	var targetSection=$("#"+g_idSectionActivityMonitor);
	var pageIndex=targetSection.data(g_pageIndexKey);
	queryDefaultActivitiesByAdmin(pageIndex+1);
}