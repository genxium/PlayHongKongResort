function onBtnPreviousPageClicked(evt){
	var pageIndex=g_sectionDefaultActivities.data(g_keyPageIndex);
    var startingIndex=g_sectionDefaultActivities.data(g_keyStartingIndex);
    var endingIndex=g_sectionDefaultActivities.data(g_keyEndingIndex);

    queryActivities(startingIndex, g_numItemsPerPage, g_directionBackward);
}

function onBtnNextPageClicked(evt){
    var pageIndex=g_sectionDefaultActivities.data(g_keyPageIndex);
    var startingIndex=g_sectionDefaultActivities.data(g_keyStartingIndex);
    var endingIndex=g_sectionDefaultActivities.data(g_keyEndingIndex);

    queryActivities(endingIndex, g_numItemsPerPage, g_directionForward);
}

function onSectionDefaultActivitiesScrolled(evt){
	if( $(this).scrollTop() + $(this).height() >= $(document).height() ){
		evt.preventDefault();
		alert("Bottom!");
	}
}

function showRegisterSection(){
	if(g_sectionRegister){
		g_sectionRegister.show();
	}
}

function hideRegisterSection(){
	if(g_sectionRegister){
		g_sectionRegister.hide();
	}
}

function removeRegisterSection(){
	if(g_sectionRegister){
		g_sectionRegister.remove();
		g_sectionRegister=null;
	}
}

function refreshOnEnter(){
	showRegisterSection();
	emptyRegisterFields();
	g_sectionDefaultActivities.show();
}

function refreshOnLoggedIn(){
	hideRegisterSection();
}

function onQueryActivitiesSuccess(data, status, xhr){
    var jsonResponse=JSON.parse(data);
    if(jsonResponse!=null && Object.keys(jsonResponse).length>0){
        g_sectionDefaultActivities.empty();
        var idx=0;
        var count=Object.keys(jsonResponse).length;
        // display contents
        for(var key in jsonResponse){
            var activityJson=jsonResponse[key];
            var activityId=activityJson[g_keyId];
            if(idx==0){
                g_sectionDefaultActivities.data(g_keyStartingIndex, activityId);
            }
            if(idx==count-1){
                g_sectionDefaultActivities.data(g_keyEndingIndex, activityId);
            }
            var cell=null;
            var token = $.cookie(g_keyToken);
            if(token==null){
                cell=generateActivityCell(activityJson);
            } else{
                cell=generateActivityCell(activityJson);
            }
            g_sectionDefaultActivities.append(cell);
            ++idx;
        }
    }
} 
function queryActivities(refIndex, numItems, direction){
	do{	
		if(refIndex==null || numItems==null || direction==null) break;

		var params={};
		params[g_keyRefIndex]=refIndex.toString();
		params[g_keyNumItems]=numItems.toString();
		params[g_keyDirection]=direction.toString();
		params[g_keyStatus]=g_statusAccepted;

		var token = $.cookie(g_keyToken);
		if(token!=null) params[g_keyToken]=token;

		try{
			$.ajax({
				type: "GET",
				url: "/activity/query",
				data: params,
				success: g_callbackOnQueryActivitiesSuccess,
				error: function(data, status, xhr){

				}
			});
		} catch(err){

		}
	}while(false);
}
