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
							alert(ALERTS.registered);
						},
						function(err) {
							alert(ALERTS.not_registered);
						});
	var keySelector = createSelector($("#pager-filters"), [TITLES.defaulted, TITLES.begin_time, TITLES.deadline], ["", g_keyBeginTime, g_keyDeadline], null, null, null, null);
	var orientationSelector = createSelector($("#pager-filters"), [TITLES.descendant, TITLES.ascendant], [g_orderDescend, g_orderAscend], null, null, null, null);
	var keyFilter = new PagerFilter(g_keyOrderKey, keySelector);
	var orientationFilter = new PagerFilter(g_keyOrientation, orientationSelector); 
	var filters = [keyFilter, orientationFilter];	
	var pagerCache = new PagerCache(5);

	// initialize pager widgets
	g_pager = new Pager($("#pager-screen-activities"), $("#pager-bar-activities"), g_numItemsPerPage, "/activity/list", generateActivitiesListParams, null, pagerCache, filters, onListActivitiesSuccess, onListActivitiesError);

	var onLoginSuccess = function(data) {
		if (!(!g_registerWidget)) g_registerWidget.hide();
		listActivitiesAndRefresh();
	};

	var onLoginError = function(err) {
		if (!(!g_registerWidget)) g_registerWidget.show();
		g_pager.screen.show();
		listActivitiesAndRefresh();
	};

	var onLogoutSuccess = function(data) {
		if (!(!g_registerWidget)) g_registerWidget.show();
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
	if (!bundle) {
		window.location.hash = "home";
		return;
	}

	var tag = bundle[g_keyTag];	
	var params = bundle[g_keyParams];

	var cbfuncName = null;
	var args = null;

	if (typeof tag == "object") {
		// for QQ only
		var stateWithAction = decodeStateWithAction(params[g_keyState]);
		if (stateWithAction.hasOwnProperty(g_keyCbfunc)) cbfuncName = stateWithAction[g_keyCbfunc];
		if (stateWithAction.hasOwnProperty(g_keyArgs)) args = stateWithAction[g_keyArgs];

		var party = stateWithAction[g_keyParty];
		var accessToken = tag[g_keyAccessToken]; 	

                saveAccessTokenAndParty(accessToken, party);

		var state = stateWithAction[g_keyState];
		tag = state[g_keyTag];			
		var toRecoverHash = tag;
		 
		if (Object.keys(state).length > 1) {
			var toRecoverParamList = [];
			for (var k in state) {
				if (k == g_keyTag) continue;
				toRecoverParamList.push(k + "=" + state[k]);
			}
			toRecoverHash += ("?" + toRecoverParamList.join('&'));
		}

		// TODO: proceed accordingly with `cbfuncName` and `args` 
		window.location.hash = toRecoverHash;
		return;
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
		requestActivityDetail(parseInt(params.activity_id));
		return;
	}
	if (tag == "profile") {
		requestProfile(parseInt(params.viewee_id));
		return;
	}
}
	
$(document).ready(function(){

	initTopbar($("#topbar"));
	initFooter($("#footer-content"));

	var homepageContent = $("#content");
	initActivityEditor(homepageContent);
	initNameCompletionForm(homepageContent);
	initQQWelcomePopup(homepageContent);

	$(window).on("hashchange", function(evt) {
		routeByHash();
	});

	routeByHash();
});
