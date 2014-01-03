function validateImage(file){
	var fileName = file.value;
	var ext = fileName.split('.').pop().toLowerCase();
	if($.inArray(ext, ['gif','png','jpg','jpeg']) == -1) {
	    alert('invalid extension!');
	    return false;
	}
	return true;
}


function queryActivitiesHostedByUser(){
	var targetSection=$("#"+g_idSectionOwnedActivities);
	targetSection.empty();
	var token = $.cookie(g_keyLoginStatus.toString());
	try{
		$.post("/queryActivitiesHostedByUser", 
			{
				UserToken: token.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					var jsonResponse=JSON.parse(data);
    					for(var key in jsonResponse){
    						var jsonActivity=jsonResponse[key];
    						var cell=generateActivityCell(jsonActivity);
    						var text=cell.html();
							targetSection.append(cell);
    					}
    				} else{
    					
    				}
			}
		);
	} catch(err){

	}
}