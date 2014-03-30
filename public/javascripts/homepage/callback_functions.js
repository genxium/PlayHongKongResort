function refreshOnEnter(){

	$("#"+g_idFieldEmail).empty();
	$("#"+g_idFieldEmail).val("");
	$("#"+g_idFieldPassword).empty();
	$("#"+g_idFieldPassword).val("");

	var sectionUserProfileEditor=$("#"+g_idSectionUserProfileEditor);
	sectionUserProfileEditor.empty();
	sectionUserProfileEditor.hide();

	$("#"+g_idSectionAccount).show();
	
	var sectionActivityEditor=$("#"+g_idSectionActivityEditor);
	sectionActivityEditor.empty();
	sectionActivityEditor.hide();

	var sectionUserInfo=$("#"+g_idSectionUserInfo);
	sectionUserInfo.empty();
	sectionUserInfo.hide();

	$("#"+g_idBtnCreate).hide();
	$("."+g_classActivityEditor).hide();

	var sectionDefaultActivities=$("#"+g_idSectionDefaultActivities);
	sectionDefaultActivities.show();
}

function onQueryActivitiesSuccess(data, status, xhr){
    var jsonResponse=JSON.parse(data);
    if(jsonResponse!=null && Object.keys(jsonResponse).length>0){
        var targetSection=$("#"+g_idSectionDefaultActivities);
        // clean target section
        targetSection.empty();
        var idx=0;
        var count=Object.keys(jsonResponse).length;
        // display contents
        for(var key in jsonResponse){
            var activityJson=jsonResponse[key];
            var activityId=activityJson[g_keyActivityId];
            if(idx==0){
                targetSection.data(g_keyStartingIndex, activityId);
            }
            if(idx==count-1){
                targetSection.data(g_keyEndingIndex, activityId);
            }
            var cell=null;
            var token = $.cookie(g_keyLoginStatus.toString());
            if(token==null){
                cell=generateActivityCell(activityJson, false, g_modeHomepage);
            } else{
                cell=generateActivityCell(activityJson, true, g_modeHomepage);
            }
            targetSection.append(cell);
            ++idx;
        }
    }
} 
