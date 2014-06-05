function onBtnPreviousPageClicked(evt){
	var targetSection=$("#"+g_idSectionDefaultActivities);

	var pageIndex=targetSection.data(g_keyPageIndex);
    var startingIndex=targetSection.data(g_keyStartingIndex);
    var endingIndex=targetSection.data(g_keyEndingIndex);

    queryActivities(startingIndex, g_numItemsPerPage, g_directionBackward);
}

function onBtnNextPageClicked(evt){
    var targetSection=$("#"+g_idSectionActivityMonitor);

    var pageIndex=targetSection.data(g_keyPageIndex);
    var startingIndex=targetSection.data(g_keyStartingIndex);
    var endingIndex=targetSection.data(g_keyEndingIndex);

    queryActivities(endingIndex, g_numItemsPerPage, g_directionForward);
}

function onSelectFilterChanged(evt){
    evt.preventDefault();
    var selector=$(this);
    g_statusToQuery=parseInt(selector.val());
    queryActivities(0, g_numItemsPerPage, g_directionForward);
}

function onQueryActivitiesSuccess(data, status, xhr){

    // clean target section
	g_sectionActivityMonitor.empty();

    var jsonResponse=JSON.parse(data);
    if(jsonResponse!=null && Object.keys(jsonResponse).length>0){
        var idx=0;
        var count=Object.keys(jsonResponse).length;
        // display contents
        for(var key in jsonResponse){
            var activityJson=jsonResponse[key];
            var activityId=activityJson[g_keyActivityId];
            if(idx==0){
                targetSection.data(g_keyStartingIndex, activityId);
            }
            if(idx==count-1){
                targetSection.data(g_keyEndingIndex, activityId);
            }
            var cell=generateActivityCellForAdmin(activityJson);
            targetSection.append(cell);
            ++idx;
        }
    }
} 

function queryActivities(refIndex, numItems, direction){
	do{
		var token = $.cookie(g_keyLoginStatus.toString());
		if(refIndex==null || numItems==null || direction==null || token==null) break;

		var params={};
        params[g_keyRefIndex]=refIndex.toString();
        params[g_keyNumItems]=numItems.toString();
        params[g_keyDirection]=direction.toString();
        params[g_keyToken]=token;
		params[g_keyStatus]=g_statusToQuery;

		try{
		    $.ajax({
		        method: "GET",
		        url: "/activity/query",
		        data: params,
		        success: onQueryActivitiesSuccess,
                error: function(data, status, xhr){

                }
		    });
		} catch(err){

		}
	}while(false);
}
