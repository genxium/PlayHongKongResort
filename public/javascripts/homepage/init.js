function clearHome() {
	$("#pager-filters").empty();
	$("#pager-bar-activities").empty();
	$("#pager-screen-activities").empty();
	removeRegisterWidget();
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
	var keySelector = createSelector($("#pager-filters"), [TITLES["default"], TITLES["begin_time"], TITLES["deadline"]], ["", g_keyBeginTime, g_keyDeadline], null, null, null, null);
	var orientationSelector = createSelector($("#pager-filters"), [TITLES["descendant"], TITLES["ascendant"]], [g_orderDescend, g_orderAscend], null, null, null, null);
	var keyFilter = new PagerFilter(g_keyOrderKey, keySelector);
	var orientationFilter = new PagerFilter(g_keyOrientation, orientationSelector); 
	var filters = [keyFilter, orientationFilter];	
	var pagerCache = new PagerCache(5);

	// initialize pager widgets
	g_pager = new Pager($("#pager-screen-activities"), $("#pager-bar-activities"), g_numItemsPerPage, "/activity/list", generateActivitiesListParams, null, pagerCache, filters, onListActivitiesSuccess, onListActivitiesError);

	var onLoginSuccess = function(data) {
		if (g_registerWidget != null) g_registerWidget.hide();
		listActivitiesAndRefresh();
	};

	var onLoginError = function(err) {
		if (g_registerWidget != null) g_registerWidget.show();
		g_pager.screen.show();
		listActivitiesAndRefresh();
	};

	var onLogoutSuccess = function(data) {
		if (g_registerWidget != null) g_registerWidget.show();
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
		window.location.hash = "home";
		return;
	}

	var tag = bundle["tag"];	
	var params = bundle["params"];

	if (params.hasOwnProperty(g_keyParty) && params.hasOwnProperty(g_keyAccessToken)) {
		alert("access token is " +  params[g_keyAccessToken]);
		$.cookie(g_keyParty, params[g_keyParty], {path: '/'});
		$.cookie(g_keyAccessToken, params[g_keyAccessToken], {path: '/'});
		// will be used when invoking `checkForeignPartyLoginStatus`
	}

	if (tag == "home") {
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

	initActivityEditor($("#content"));
	initAvatarEditor($("#content"));

	$(window).on("hashchange", function(evt) {
		routeByHash();
	});

	routeByHash();
});
