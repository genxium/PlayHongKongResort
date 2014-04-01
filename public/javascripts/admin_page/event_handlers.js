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

function onBtnSwitchMonitorClicked(evt){
    evt.preventDefault();
    switch (g_statusToQuery){
        case 0:
        {
            g_statusToQuery=1;    
        }
		break;
        case 1:
        {
            g_statusToQuery=0;
        }     
		break;  
    }
    queryActivitiesByAdmin(0, g_numItemsPerPage, g_directionForward);
}
