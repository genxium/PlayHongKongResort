var g_registerWidget = null;

function emptySectionRegister() {
	setDimensions(g_sectionRegister, "100%", 0);
}

function initSectionRegister() {
	setDimensions(g_sectionRegister, "100%", "auto");
}

function clearHome() {
	$("#pager-filters").empty();
	$("#pager-screen-activities").empty();
	$("#pager-bar-activities").empty();
	setDimensions(g_sectionRegister, "100%", 0);
	g_sectionRegister.empty();
}

function requestHome() {
	clearProfile();
	initSectionRegister();
	g_registerWidget = generateRegisterWidget($("#section-register"), 
			function(data) {
				alert("Registered successfully!");
				listActivitiesAndRefresh();
			},
			function(err) {
				
			});

	var selector = createSelector($("#pager-filters"), ["時間倒序", "時間順序"], [g_orderDescend, g_orderAscend], null, null, null, null);
	var filter = new PagerFilter(g_keyOrientation, selector);
	var filters = [filter];	

	var pagerCache = new PagerCache(5); 

	// initialize pager widgets
	g_pager = new Pager($("#pager-screen-activities"), $("#pager-bar-activities"), g_numItemsPerPage, "/activity/list", generateActivitiesListParams, pagerCache, filters, onListActivitiesSuccess, onListActivitiesError);

	var onLoginSuccess = function(){
		emptySectionRegister();
		g_registerWidget.hide();
		listActivitiesAndRefresh();
	};

	var onLoginError = null;

	var onLogoutSuccess = function(){
		initSectionRegister();
		g_registerWidget.show();
		g_pager.screen.show();
		listActivitiesAndRefresh();
	};

	var onLogoutError = null;

	g_preLoginForm = generatePreLoginForm(g_sectionLogin, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError);
	g_onActivitySaveSuccess = null;
	checkLoginStatus();
}
	
$(document).ready(function(){

	initTopbar($("#topbar"));
	initActivityEditor($("#wrap"), null);
	g_sectionUser = $("#section-user");
	g_sectionRegister = $("#section-register");
	
	requestHome();
});
