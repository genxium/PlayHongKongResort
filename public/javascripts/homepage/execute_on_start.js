$(document).ready(function(){

    	// initialize local DOMs
    	initTopbar();
	initRegisterWidget();

	g_callbackOnLoginSuccess=function(){
		refreshOnLoggedIn();
		queryActivities(0, g_numItemsPerPage, g_directionForward);
	};

	g_callbackOnLoginError=null;

	g_callbackOnEnter=function(){
		refreshOnEnter();
		queryActivities(0, g_numItemsPerPage, g_directionForward);
	};

	g_callbackOnRegisterSuccess=function(){
		refreshOnEnter();
		queryActivities(0, g_numItemsPerPage, g_directionForward);
	}

	g_callbackOnRegisterError=null;

	initActivityEditor();
	 
	// initialize callback functions
	g_callbackOnActivityEditorRemoved=refreshOnLoggedIn;
	g_callbackOnQueryActivitiesSuccess=onQueryActivitiesSuccess;

	// execute on page loaded
	g_sectionDefaultActivities=$("#idSectionDefaultActivities"); 
	g_sectionDefaultActivities.on("scroll", onSectionDefaultActivitiesScrolled);

	initWidgets(onBtnPreviousPageClicked, onBtnNextPageClicked);
	checkLoginStatus();
	
});
