function listActivitiesAndRefresh() {
	var page = 1;
	listActivities(page, onListActivitiesSuccess, onListActivitiesError);
}
	
function requestHome() {
	initRegisterWidget($("#section-register"));

	var selector = createSelector($("#pager-filters"), ["時間倒序", "時間順序"], [g_orderDescend, g_orderAscend], null, null, null, null);
	var filter = new PagerFilter(g_keyOrientation, selector);
	var filters = [filter];	

	var pagerCache = new PagerCache(5); 

	// initialize pager widgets
	g_pager = new Pager($("#pager-screen-activities"), $("#pager-bar-activities"), g_numItemsPerPage, "/activity/list", generateActivitiesListParams, pagerCache, filters, onListActivitiesSuccess, onListActivitiesError);

	g_onLoginSuccess = function(){
		hideRegisterSection();
		listActivitiesAndRefresh();
	};

	g_onLoginError = null;

	g_onEnter = function(){
		showRegisterSection();
		emptyRegisterFields();
		g_pager.screen.show();
		listActivitiesAndRefresh();
	};

	g_onRegisterSuccess = function(){
		alert("Registered successfully!");
		refreshOnEnter();
		listActivitiesAndRefresh();
	}

	g_onRegisterError = null;
	g_onActivitySaveSuccess = null;
	checkLoginStatus();
}
	
function displayHome() {

}

function requestProfile() {

}

$(document).ready(function(){

	initTopbar($("#topbar"));
	initActivityEditor($("#wrap"), null);
	
	requestHome();
});
