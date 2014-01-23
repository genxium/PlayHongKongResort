function validateImage(file){
	var fileName = file.value;
	var ext = fileName.split('.').pop().toLowerCase();
	if($.inArray(ext, ['gif','png','jpg','jpeg']) == -1) {
	    alert('invalid extension!');
	    return false;
	}
	return true;
}

function queryActivitiesHostedByUser(pageIndex){
	var token = $.cookie(g_keyLoginStatus.toString());
	try{
		$.post("/queryActivitiesHostedByUser", 
			{
				UserToken: token.toString(),
				pageIndex: pageIndex.toString() 
			},
			function(data, status, xhr){
    				if(status=="success"){
    					var jsonResponse=JSON.parse(data);
    					if(jsonResponse!=null){
    						var targetSection=$("#"+g_idSectionOwnedActivities);
    						// clean target section
	    					targetSection.empty();
    						// update page index of the target section
    						targetSection.data(g_pageIndexKey, pageIndex);
	    					// display contents
	    					for(var key in jsonResponse){
	    						var jsonActivity=jsonResponse[key];
	    						var cell=generateActivityCell(jsonActivity);
	    						var text=cell.html();
								targetSection.append(cell);
	    					}
    					}	
    				} else{
    					
    				}
			}
		);
	} catch(err){

	}
}
