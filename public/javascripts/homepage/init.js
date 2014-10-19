function listActivitiesAndRefresh() {
	var page = 1;
	listActivities(page, onListActivitiesSuccess, onListActivitiesError);
}

function queryActivitiesAndRefresh() {
	var page = 1;
	queryActivities(page, onQueryActivitiesSuccess, onQueryActivitiesError);
}

function showRegisterSection(){
	if(g_sectionRegister == null)	return;
	g_sectionRegister.show();
}

function hideRegisterSection(){
	if(g_sectionRegister == null)	return;
	g_sectionRegister.hide();
}

function removeRegisterSection(){
	if(g_sectionRegister == null)	return;
	g_sectionRegister.remove();
}

function refreshOnEnter(){
	showRegisterSection();
	emptyRegisterFields();
	g_pager.screen.show();
}

function refreshOnLoggedIn(){
	hideRegisterSection();
}

$(document).ready(function(){

	// initialize local DOMs
	initTopbar();
	initRegisterWidget();

	var selector = createSelector($("#pager-filters"), ["時間倒序", "時間順序"], [g_orderDescend, g_orderAscend], null, null, null, null);
	var filter = new PagerFilter(g_keyOrientation, selector);
	var filters = [filter];	

	var pagerCache = new PagerCache(5); 

	// initialize pager widgets
	g_pager = new Pager($("#pager-screen-activities"), $("#pager-bar-activities"), g_numItemsPerPage, "/activity/list", generateActivitiesListParams, pagerCache, filters, onListActivitiesSuccess, onListActivitiesError);

	g_onLoginSuccess = function(){
		refreshOnLoggedIn();
		listActivitiesAndRefresh();
	};

	g_onLoginError = null;

	g_onEnter = function(){
		refreshOnEnter();
		listActivitiesAndRefresh();
	};

	g_onRegisterSuccess = function(){
		alert("Registered successfully!");
		refreshOnEnter();
		listActivitiesAndRefresh();
	}

	g_onRegisterError = null;

	initActivityEditor();

	// initialize callback functions
	g_onEditorRemoved = refreshOnLoggedIn;

	checkLoginStatus();
	
});
