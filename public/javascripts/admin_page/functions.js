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
		        success: onQueryActivitiesSuccess,
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
                success: onQueryActivitiesSuccess,
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
