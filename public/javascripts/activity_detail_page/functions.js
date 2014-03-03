function queryActivityDetail(activityId){
    try{
		$.get("/queryActivityDetail", 
			{
				activityId: activityId.toString(),
			},
			function(data, status, xhr){
				if(status=="success"){
                    var activityDetailJson=JSON.parse(data);
				    displayActivityDetail(activityDetailJson);	
				} else{

				}
			}
		);
    }catch(err){
    
    }
}

function displayActivityDetail(activityDetailJson){
    var activityTitle=activityDetailJson[g_keyActivityTitle];
    var activityContent=activityDetailJson[g_keyActivityContent];
    var activityImages=activityDetailJson[g_keyActivityImages];

    var body=$("body");
    var detailView=generateActivityDetailViewByJson(activityDetailJson);
    body.append(detailView);
}
