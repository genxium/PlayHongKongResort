// Assistant Functions
function queryDefaultActivities(refIndex, numItems, direction){
	var params={};
	params[g_keyRefIndex]=refIndex.toString();
	params[g_keyNumItems]=numItems.toString();
	params[g_keyDirection]=direction.toString();
	
	try{
		$.get("/queryDefaultActivities", 
			params,
			function(data, status, xhr){
    				if(status=="success"){
    					var jsonResponse=JSON.parse(data);
    					if(jsonResponse!=null && Object.keys(jsonResponse).length>0){
							var targetSection=$("#"+g_idSectionDefaultActivities);
    						// clean target section
	    					targetSection.empty();

	    					var count=Object.keys(jsonResponse).length;
	    					// display contents
	    					for(var key in jsonResponse){
	    						var activityJson=jsonResponse[key];
	    						var activityId=activityJson[g_keyActivityId];
	    						if(key==0){
	    						    targetSection.data(g_keyStartingIndex, activityId);
	    						}
	    						if(key==count-1){
	    						    targetSection.data(g_keyEndingIndex, activityId);
	    						}
	    						var cell=generateActivityCell(activityJson, false, 0);
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

function queryDefaultActivitiesByUser(refIndex, numItems, direction){
	var params={};
	params[g_keyRefIndex]=refIndex.toString();
	params[g_keyNumItems]=numItems.toString();
	params[g_keyDirection]=direction.toString();
	
	var token = $.cookie(g_keyLoginStatus.toString());
	params[g_keyUserToken]=token;
	
	try{
		$.get("/queryDefaultActivitiesByUser", 
			params,
			function(data, status, xhr){
    				if(status=="success"){
    					var jsonResponse=JSON.parse(data);
    					if(jsonResponse!=null && Object.keys(jsonResponse).length>0){
							var targetSection=$("#"+g_idSectionDefaultActivities);
    						// clean target section
	    					targetSection.empty();

	    					var count=Object.keys(jsonResponse).length;
	    					// display contents
	    					for(var key in jsonResponse){
	    						var activityJson=jsonResponse[key];
	    						if(key==0){
                                    targetSection.data(g_keyStartingIndex, activityId);
                                }
                                if(key==count-1){
                                    targetSection.data(g_keyEndingIndex, activityId);
                                }
	    						var cell=generateActivityCell(activityJson, false, 0);
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
// Assistant Handlers
function onBtnLogoutClicked(evt){
	var token = $.cookie(g_keyLoginStatus.toString());
	try{
		$.post("/logout", 
			{
				UserToken: token.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					$.removeCookie(g_keyLoginStatus.toString());
    					refreshOnEnter();
    					queryDefaultActivities(0, g_numItemsPerPage, g_directionForward);
    				} else{

    				}
			}
		);
	} catch(err){

	}
}

function onBtnProfileClicked(evt){
	try{
		var userProfileEditorPath="/show?page=user_profile_editor.html";
		var userProfileEditorPage=window.open(userProfileEditorPath);
	} catch (err){

	}
}


// Generators
function generateLoggedInUserMenu(){

	var ret=$('<div>',
			{
				class: g_classLoggedInUserMenu
			});

	var btnLogout=$('<button>',
			{
				class: g_classBtnLogout,
				text: 'Logout'		
			});
	btnLogout.bind("click", onBtnLogoutClicked);
	ret.append(btnLogout);
	ret.data(g_indexBtnLogout, btnLogout);

	var btnProfile=$('<button>',
			{
				class: g_classBtnProfile,
				text: 'Profile'
			});

	btnProfile.bind("click", onBtnProfileClicked);
	ret.append(btnProfile);
	ret.data(g_indexBtnProfile, btnProfile);

	return ret;
}
