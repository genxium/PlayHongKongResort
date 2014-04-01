function queryPendingActivitiesByAdmin(refIndex, numItems, direction){
	do{
		var token = $.cookie(g_keyLoginStatus.toString());
		if(token==null) break;

		var params={};
        params[g_keyRefIndex]=refIndex.toString();
        params[g_keyNumItems]=numItems.toString();
        params[g_keyDirection]=direction.toString();
        params[g_keyToken]=token;

		try{
		    $.ajax({
		        method: "GET",
		        url: "/queryPendingActivitiesByAdmin",
		        data: params,
		        success: function(data, status, xhr){
                        var jsonResponse=JSON.parse(data);
                        if(jsonResponse!=null){
                            var targetSection=$("#"+g_idSectionActivityMonitor);
                            // clean target section
                            targetSection.empty();
                            // update page index of the target section
                            targetSection.data(g_pageIndexKey, pageIndex);
                            // display contents
                            for(var key in jsonResponse){
                                var jsonRecord=jsonResponse[key];
                                var cell=generateActivityCellForAdmin(jsonRecord);
                                targetSection.append(cell);
                            }
                        }
                },
                error: function(data, status, xhr){

                }
		    });
		} catch(err){

		}
	}while(false);
}

function queryAcceptedActivitiesByAdmin(refIndex, numItems, direction){
    do{
        var token = $.cookie(g_keyLoginStatus.toString());
        if(token==null) break;

        var params={};
        params[g_keyRefIndex]=refIndex.toString();
        params[g_keyNumItems]=numItems.toString();
        params[g_keyDirection]=direction.toString();
        params[g_keyToken]=token;

        try{
            $.ajax({
                method: "GET",
                url: "/queryAcceptedActivitiesByAdmin",
                data: params,
                success: function(data, status, xhr){
                        var jsonResponse=JSON.parse(data);
                        if(jsonResponse!=null){
                            var targetSection=$("#"+g_idSectionActivityMonitor);
                            // clean target section
                            targetSection.empty();
                            // update page index of the target section
                            targetSection.data(g_pageIndexKey, pageIndex);
                            // display contents
                            for(var key in jsonResponse){
                                var jsonRecord=jsonResponse[key];
                                var cell=generateActivityCellForAdmin(jsonRecord);
                                targetSection.append(cell);
                            }
                        }
                },
                error: function(data, status, xhr){

                }
            });
        } catch(err){

        }
    }while(false);
}

function queryActivitiesByAdmin(refIndex, numItems, direction){
    switch (g_statusToQuery){
        case 0:
        {
            queryPendingActivitiesByAdmin(refIndex, numItems, direction);
        }
        break;
        case 1:
        {
            queryAcceptedActivitiesByAdmin(refIndex, numItems, direction);
        }
        break;
    }
}
