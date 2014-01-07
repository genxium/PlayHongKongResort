function queryDefaultActivitiesByAdmin(){
	do{
		var targetSection=$("#"+g_idSectionActivityMonitor);
		targetSection.empty();
		var token = $.cookie(g_keyLoginStatus.toString());
		if(token==null) break;
		try{
			$.post("/queryDefaultActivitiesByHost",
				{
					UserToken: token.toString()
				},
				function(data, status, xhr){
	    				if(status=="success"){
	    					var jsonResponse=JSON.parse(data);
	    					for(var key in jsonResponse){
	    						var jsonRecord=jsonResponse[key];
	    						var cell=generateActivityCellForAdmin(jsonRecord);
								targetSection.append(cell);
	    					}
	    				} else{
	    					
	    				}
				}
			);
		} catch(err){

		}
	}while(false);
}