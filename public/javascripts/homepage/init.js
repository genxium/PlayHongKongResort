function queryActivitiesAndRefresh() {
	queryActivities(0, 0, g_pager.nItems, g_pager.orientation, g_directionForward, g_vieweeId, g_pager.relation, g_pager.status, onQueryActivitiesSuccess, onQueryActivitiesError);
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

	var filtersBar = $("#pager-filters");
	var selector = createSelector(["時間倒序", "時間順序"], [g_orderDescend, g_orderAscend], null, null, null, null));
	var filter = new PagerFilter("orientation", selector);
	var filters = [filter];	

	var pagerCache = new PagerCache(5); 

	// initialize pager widgets
	g_pager = new Pager($("#pager-screen-activities"), $("#pager-bar-activities"),
		g_keyActivityId, g_orderDescend, g_numItemsPerPage,
		"/activity/query", generateActivitiesQueryParams, pagerCache, filters);
	g_pager.status = g_statusAccepted;	

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
