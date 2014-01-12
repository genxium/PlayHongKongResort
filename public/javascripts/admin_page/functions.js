function queryDefaultActivitiesByAdmin(pageIndex){
	do{
		var token = $.cookie(g_keyLoginStatus.toString());
		if(token==null) break;
		try{
			$.post("/queryDefaultActivitiesByAdmin",
				{
					UserToken: token.toString(),
					pageIndex: pageIndex.toString()
				},
				function(data, status, xhr){
	    				if(status=="success"){
	    					var jsonResponse=JSON.parse(data);
	    					if(jsonResponse!=null && Object.keys(jsonResponse).length>0){
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