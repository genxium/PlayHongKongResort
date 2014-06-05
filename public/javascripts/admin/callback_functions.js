function onQueryActivitiesSuccess(data, status, xhr){

    var targetSection=$("#"+g_idSectionActivityMonitor);
    // clean target section
    targetSection.empty();

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
