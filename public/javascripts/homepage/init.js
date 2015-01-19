var g_registerWidget = null;

function emptySectionRegister() {
	if (g_sectionRegister == null) return;
	//setDimensions(g_sectionRegister, "100%", 0);
}

function initSectionRegister() {
	if (g_sectionRegister == null) return;
	//setDimensions(g_sectionRegister, "100%", "auto");
}

function clearHome() {
	$("#pager-filters").empty();
	$("#pager-bar-activities").empty();
	$("#pager-screen-activities").empty();
	emptySectionRegister();
	if (g_sectionRegister == null) return;
	g_sectionRegister.empty();
}

function requestHome() {
	clearProfile();
	clearDetail();
	clearNotifications();

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
				alert("Oops! Not registered...");
			});

	var selector = createSelector($("#pager-filters"), ["時間倒序", "時間順序"], [g_orderDescend, g_orderAscend], null, null, null, null);
	var filter = new PagerFilter(g_keyOrientation, selector);
	var filters = [filter];	

	var pagerCache = new PagerCache(5); 

	// initialize pager widgets
	g_pager = new Pager($("#pager-screen-activities"), $("#pager-bar-activities"), g_numItemsPerPage, "/activity/list", generateActivitiesListParams, pagerCache, filters, onListActivitiesSuccess, onListActivitiesError);

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

	g_preLoginForm = generatePreLoginForm(g_sectionLogin, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError);
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
