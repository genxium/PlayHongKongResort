var g_keyHomeActivityPagerPos = "home_activity_pager_pos";

function saveHomeActivityPagerPos() {
	if (!g_pagerActivity) return;
	var pageNumber = g_pagerActivity.page;	
	setCookie(g_keyHomeActivityPagerPos, pageNumber);	
}

function loadHomeActivityPagerPos() {
	var pageNumber = parseInt(getCookie(g_keyHomeActivityPagerPos));	
	if (!pageNumber) return 1;
	removeCookie(g_keyHomeActivityPagerPos);
	return pageNumber;
}

function HomeActivityPager(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError) {
	this.init(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError);

	this.updateScreen = function(data) {
		if (!data) return;
		var pageSt = parseInt(data[g_keyPageSt]);
		var pageEd = parseInt(data[g_keyPageEd]);
		var page = pageSt;

		var activitiesJson = data[g_keyActivities];
		var length = Object.keys(activitiesJson).length;

		var activities = [];
		for(var idx = 1; idx <= length; ++idx) {
			var activityJson = activitiesJson[idx - 1];
			var activity = new Activity(activityJson);
			activities.push(activity);
			if (page == this.page)	generateActivityCell(this.screen, activity);
			if (idx % this.nItems != 0) continue;
			this.cache.putPage(page, activities);
			activities = [];
			++page;	
		}
		if (activities != null && activities.length > 0) {
			// for the last page
			this.cache.putPage(page, activities);
		}
	};
}

HomeActivityPager.inherits(Pager);

function clearHome() {
	saveHomeActivityPagerPos();
	$("#pager-activities").empty();
	g_registerWidgetY.hide();
	g_registerWidgetX.hide();
}

function requestHome() {
	clearProfile();
	clearDetail();
	clearNotifications();

	// initialize pager 
	var filterMap = {};
	filterMap[g_keyOrderKey] = [[TITLES.defaulted, TITLES.begin_time, TITLES.deadline], ["", g_keyBeginTime, g_keyDeadline]];
	filterMap[g_keyOrientation] = [[TITLES.descendant, TITLES.ascendant], [g_orderDescend, g_orderAscend]];

	g_pagerActivity = new HomeActivityPager(g_numItemsPerPage, "/activity/list", generateActivitiesListParams, null, 5, filterMap, onListActivitiesSuccess, onListActivitiesError);
	g_pagerActivity.appendTo("#pager-activities");
	g_pagerActivity.refresh();

	var savedPageNumber = loadHomeActivityPagerPos();
	g_pagerActivity.goToPage(savedPageNumber);

	var onLoginSuccess = function(data) {
		if (!(!g_registerWidgetX)) g_registerWidgetX.hide();
		listActivitiesAndRefresh();
	};

	var onLoginError = function(err) {
		if (!(!g_registerWidgetX)) g_registerWidgetX.show();
		listActivitiesAndRefresh();
	};

	var onLogoutSuccess = function(data) {
		if (!(!g_registerWidgetX)) g_registerWidgetX.show();
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

	var onRegisterSuccess = function(data) {
		alert(ALERTS.registered);
	};
	var onRegisterError = function(err) {
		alert(ALERTS.not_registered);
	};
	initRegisterWidgetX($("#section-register"), onRegisterSuccess, onRegisterError);
	initRegisterWidgetY(homepageContent, onRegisterSuccess, onRegisterError);
	g_registerWidgetX.refresh();
	g_registerWidgetY.refresh();

	$(window).on("hashchange", function(evt) {
		routeByHash();
	});

	routeByHash();
});
