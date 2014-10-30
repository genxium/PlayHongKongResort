var g_registerWidget = null;

function emptySectionRegister() {
	if (g_sectionRegister == null) return;
	setDimensions(g_sectionRegister, "100%", 0);
	g_sectionRegister.empty();
}

function initSectionRegister() {
	if (g_sectionRegister == null) return;
	setDimensions(g_sectionRegister, "100%", "auto");
}

function clearHome() {
	$("#pager-filters").empty();
	$("#pager-screen-activities").empty();
	$("#pager-bar-activities").empty();
	emptySectionRegister();
}

function requestHome() {
	clearProfile();
	clearDetail();
	window.location.hash = "";

	initSectionRegister();
	g_sectionUser = $("#section-user");
	g_sectionRegister = $("#section-register");
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

	var onLoginSuccess = function(data) {
		emptySectionRegister();
		g_registerWidget.hide();
		listActivitiesAndRefresh();
	};

	var onLoginError = function(err) {
		initSectionRegister();
		g_registerWidget.show();
		g_pager.screen.show();
		listActivitiesAndRefresh();
	};

	var onLogoutSuccess = function(data) {
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

function routeByHash() {
	var hash = window.location.hash;
	if (hash == null || hash == "") {
		requestHome();
		return;
	}
	var parts = hash.split("=");		
	if (parts[0] == ("#" + g_keyVieweeId)) requestProfile(parseInt(parts[1]));
	else requestActivityDetail(parseInt(parts[1]));
}
	
$(document).ready(function(){

	initTopbar($("#topbar"));
	initActivityEditor($("#wrap"), null);

	$(window).on("hashchange", function(evt) {
		routeByHash();
	});
	routeByHash();
});
