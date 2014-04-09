function onBtnPreviousPageClicked(evt){
	var targetSection=$("#"+g_idSectionDefaultActivities);

	var pageIndex=targetSection.data(g_keyPageIndex);
    var startingIndex=targetSection.data(g_keyStartingIndex);
    var endingIndex=targetSection.data(g_keyEndingIndex);

    queryDefaultActivitiesByAdmin(startingIndex, g_numItemsPerPage, g_directionBackward);
}

function onBtnNextPageClicked(evt){
    var targetSection=$("#"+g_idSectionActivityMonitor);

    var pageIndex=targetSection.data(g_keyPageIndex);
    var startingIndex=targetSection.data(g_keyStartingIndex);
    var endingIndex=targetSection.data(g_keyEndingIndex);

    queryActivitiesByAdmin(endingIndex, g_numItemsPerPage, g_directionForward);
}

function onSelectFilterChanged(evt){
    evt.preventDefault();
    var selector=$(this);
    g_statusToQuery=parseInt(selector.val());
    queryActivitiesByAdmin(0, g_numItemsPerPage, g_directionForward);
}
