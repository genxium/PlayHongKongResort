$(document).ready(function(){

    	// initialize local DOMs
    	initTopbar();
	initRegisterWidget();

	g_onLoginSuccess=function(){
		refreshOnLoggedIn();
		queryActivities(0, g_numItemsPerPage, g_orderDescend, g_directionForward, null, null, g_statusAccepted, onQueryActivitiesSuccess, onQueryActivitiesError);
	};

	g_onLoginError=null;

	g_onEnter=function(){
		refreshOnEnter();
		queryActivities(0, g_numItemsPerPage, g_orderDescend, g_directionForward, null, null, g_statusAccepted, onQueryActivitiesSuccess, onQueryActivitiesError);
	};

	g_onRegisterSuccess=function(){
		alert("Registered successfully!");
		refreshOnEnter();
		queryActivities(0, g_numItemsPerPage, g_orderDescend, g_directionForward, null, null, g_statusAccepted, onQueryActivitiesSuccess, onQueryActivitiesError);
	}

	g_onRegisterError=null;

	initActivityEditor();
	 
	// initialize callback functions
	g_onEditorRemoved=refreshOnLoggedIn;
	g_onQueryActivitiesSuccess=onQueryActivitiesSuccess;

	// execute on page loaded
	g_sectionActivities=$("#section-activities"); 
	g_sectionActivities.on("scroll", onSectionDefaultActivitiesScrolled);

	initWidgets(onBtnPreviousPageClicked, onBtnNextPageClicked);

	g_pagerContainer = new PagerScreen(g_sectionActivities, 0, g_orderDescend, 0, 0, g_numItemsPerPage); 
	checkLoginStatus();
	
});
