function onBtnPreviousPageClicked(evt){
	var targetSection=$("#"+g_idSectionActivityMonitor);
	var pageIndex=targetSection.data(g_pageIndexKey);
	queryPendingActivitiesByAdmin(pageIndex-1);
}

function onBtnNextPageClicked(evt){
	var targetSection=$("#"+g_idSectionActivityMonitor);
	var pageIndex=targetSection.data(g_pageIndexKey);
	queryPendingActivitiesByAdmin(pageIndex+1);
}

function onBtnSwitchMonitorClicked(evt){
    evt.preventDefault();
    switch (g_statusToQuery){
        case 0:
        {
            g_statusToQuery=1;    
        }
        case 1:
        {
            g_statusToQuery=0;
        }       
    }
    queryActivitiesByAdmin(0);    
}
