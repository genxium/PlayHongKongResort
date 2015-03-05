function clearHome() {
	$("#pager-filters").empty();
	$("#pager-bar-activities").empty();
	$("#pager-screen-activities").empty();
	if (g_sectionRegister == null) return;
	g_sectionRegister.empty();
}

function requestHome() {
	clearProfile();
	clearDetail();
	clearNotifications();
	g_registerWidget = generateRegisterWidget($("#section-register"), false, 
						function(data) {
							alert(ALERTS["registered"]);
						},
						function(err) {
							alert(ALERTS["not_registered"]);
						});

	var selector = createSelector($("#pager-filters"), [TITLES["time_descendant"], TITLES["time_ascendant"]], [g_orderDescend, g_orderAscend], null, null, null, null);
	var filter = new PagerFilter(g_keyOrientation, selector);
	var filters = [filter];	

	var pagerCache = new PagerCache(5); 

	// initialize pager widgets
	g_pager = new Pager($("#pager-screen-activities"), $("#pager-bar-activities"), g_numItemsPerPage, "/activity/list", generateActivitiesListParams, null, pagerCache, filters, onListActivitiesSuccess, onListActivitiesError);

	var onLoginSuccess = function(data) {
		emptySectionRegister();
		g_sectionRegister.hide();
		listActivitiesAndRefresh();
	};

	var onLoginError = function(err) {
		initSectionRegister();
		g_sectionRegister.show();
		g_pager.screen.show();
		listActivitiesAndRefresh();
	};

	var onLogoutSuccess = function(data) {
		initSectionRegister();
		g_sectionRegister.show();
		g_pager.screen.show();
		listActivitiesAndRefresh();
	}; 

	var onLogoutError = null;

	g_preLoginForm = generatePreLoginForm(g_sectionLogin, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError, false);
	g_onActivitySaveSuccess = null;
	checkLoginStatus();
}

function routeByHash() {
	var href = window.location.href;
	var bundle = extractTagAndParams(href);
	if (bundle == null) {
		requestHome();	
		return;
	}
	var tag = bundle["tag"];	
	var params = bundle["params"];

	if (tag == null || tag == "" || tag == "home") {
		requestHome();
		return;
	}
	if (tag == "notifications") {
		requestNotifications();
		return;
	}
	if (tag == "detail") {
		requestActivityDetail(parseInt(params["activity_id"]));
		return;
	}
	if (tag == "profile") {
		requestProfile(parseInt(params["viewee_id"]));
		return;
	}
}
	
$(document).ready(function(){

	initTopbar($("#topbar"));
	initFooter($("#footer-content"));
	initActivityEditor($("#content"), listActivitiesAndRefresh);
	initAvatarEditor($("#content"));

	$(window).on("hashchange", function(evt) {
		routeByHash();
	});
	routeByHash();
});
