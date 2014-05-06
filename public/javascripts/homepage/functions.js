// Assistant Functions
function queryDefaultActivities(refIndex, numItems, direction){
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
}

function refreshOnLoggedIn(){

    hideAccountSections();

	var sectionActivityEditor=$("#"+g_idSectionActivityEditor);
	sectionActivityEditor.empty();
	sectionActivityEditor.show();

	$("#"+g_idBtnCreate).show();
}
