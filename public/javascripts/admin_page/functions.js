function queryPendingActivitiesByAdmin(pageIndex){
	do{
		var token = $.cookie(g_keyLoginStatus.toString());
		if(token==null) break;
		try{
			$.post("/queryPendingActivitiesByAdmin",
				{
					UserToken: token.toString(),
					pageIndex: pageIndex.toString()
				},
				function(data, status, xhr){
	    				if(status=="success"){
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
	    				} else{
	    					
	    				}
				}
			);
		} catch(err){

		}
	}while(false);
}

function queryAcceptedActivitiesByAdmin(pageIndex){
	do{
		var token = $.cookie(g_keyLoginStatus.toString());
		if(token==null) break;
		try{
			$.post("/queryAcceptedActivitiesByAdmin",
				{
					UserToken: token.toString(),
					pageIndex: pageIndex.toString()
				},
				function(data, status, xhr){
	    				if(status=="success"){
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
	    				} else{
	    					
	    				}
				}
			);
		} catch(err){

		}
	}while(false);
}

function queryActivitiesByAdmin(pageIndex){
    switch (g_statusToQuery){
        case 0:
        {
            queryPendingActivitiesByAdmin(pageIndex);
        }
        break;
        case 1:
        {
            queryAcceptedActivitiesByAdmin(pageIndex);
        }
        break;
    }
}
