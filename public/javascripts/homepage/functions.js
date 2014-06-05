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

function emptyRegisterFields(){
    if(g_registerUsername){
        g_registerUsername.empty();
        g_registerUsername.val("");
    }
    if(g_registerEmail){
        g_registerEmail.empty();
        g_registerEmail.val("");
    }
    if(g_registerPassword){
        g_registerPassword.empty();
        g_registerPassword.val("");
    }
    if(g_spanCheckUsername){
        g_spanCheckUsername.empty();
    }
    if(g_spanCheckEmail){
        g_spanCheckEmail.empty();
    }
}

function showAccountSections(){
   if(g_sectionRegister){
       g_sectionRegister.show();
   }
}

function hideAccountSections(){
   if(g_sectionRegister){
       g_sectionRegister.hide();
   }
}

function removeAccountSections(){
    if(g_sectionRegister){
        g_sectionRegister.remove();
        g_sectionRegister=null;
    }
}

function refreshOnEnter(){
    showAccountSections();
    emptyRegisterFields();
	g_sectionDefaultActivities.show();
}

function refreshOnLoggedIn(){
    hideAccountSections();
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
            var activityId=activityJson[g_keyActivityId];
            if(idx==0){
                g_sectionDefaultActivities.data(g_keyStartingIndex, activityId);
            }
            if(idx==count-1){
                g_sectionDefaultActivities.data(g_keyEndingIndex, activityId);
            }
            var cell=null;
            var token = $.cookie(g_keyLoginStatus.toString());
            if(token==null){
                cell=generateActivityCell(activityJson, false, g_modeHomepage);
            } else{
                cell=generateActivityCell(activityJson, true, g_modeHomepage);
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

		var token = $.cookie(g_keyLoginStatus.toString());
		if(token!=null) params[g_keyToken]=token;

		try{
			$.ajax({
				method: "GET",
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
