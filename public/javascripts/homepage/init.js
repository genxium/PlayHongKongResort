$(document).ready(function(){

    	// initialize local DOMs
    	initTopbar();
	initRegisterWidget();
	
	// initialize pager widgets
	g_pagerContainer = new PagerContainer($("#pager-screen-activities"), $("#pager-bar-activities"), g_keyActivityId, g_orderDescend, g_numItemsPerPage);		
	g_pagerContainer.status = g_statusAccepted;	

	g_onLoginSuccess = function(){
		refreshOnLoggedIn();
		queryActivitiesAndRefresh();
	};

	g_onLoginError = null;

	g_onEnter = function(){
		refreshOnEnter();
		queryActivitiesAndRefresh();
	};

	g_onRegisterSuccess = function(){
		alert("Registered successfully!");
		refreshOnEnter();
		queryActivitiesAndRefresh();
	}

	g_onRegisterError = null;

	initActivityEditor();
	 
	// initialize callback functions
	g_onEditorRemoved = refreshOnLoggedIn;
	g_onQueryActivitiesSuccess = onQueryActivitiesSuccess;

	checkLoginStatus();
	
});
