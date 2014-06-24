function refreshOnEnter(){
	g_sectionUploadAvatar.hide();
}

function refreshOnLoggedIn(){
	g_sectionUploadAvatar.show();
}

function validateImage(file){
	var fileName = file.value;
	var ext = fileName.split('.').pop().toLowerCase();
	if($.inArray(ext, ['gif','png','jpg','jpeg']) == -1) {
	    alert('invalid extension!');
	    return false;
	}
	return true;
}

function queryActivities(refIndex, numItems, direction){
	var params={};
	params[g_keyRefIndex]=refIndex.toString();
	params[g_keyNumItems]=numItems.toString();
	params[g_keyDirection]=direction.toString();

	var token=$.cookie(g_keyLoginStatus.toString());
	params[g_keyToken]=token;

	var relation=g_activitiesFilter.val();
	params[g_keyRelation]=relation;

	try{
		$.ajax({
			type: "GET",
			url: "/activity/query",
			data: params,
			success: function(data, status, xhr){
					var jsonResponse=JSON.parse(data);
					var count=Object.keys(jsonResponse).length;
					if(jsonResponse!=null){
						// clean target section
						g_sectionActivities.empty();
						var idx=0;
						// display contents
						for(var key in jsonResponse){
							var activityJson=jsonResponse[key];
							var activityId=activityJson[g_keyActivityId];
							if(idx==0){
								g_sectionActivities.data(g_keyStartingIndex, activityId);
							}
							if(idx==count-1){
							    g_sectionActivities.data(g_keyEndingIndex, activityId);
							}
							var cell=generateActivityCell(activityJson);
								g_sectionActivities.append(cell);
								++idx;
						}
					}
			},
			error: function(xhr, status, err){
			}
		});
	} catch(err){

	}
}
