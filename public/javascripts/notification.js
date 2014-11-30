var g_sectionNotifications = null;
var g_pagerNotifications = null;

function emptySectionNotifications() {
	if (g_sectionNotifications == null) return;
	setDimensions(g_sectionNotifications, "100%", 0);
	g_sectionNotifications.empty();
}

function clearNotifications() {
	$("#pager-filters").empty();
	$("#pager-bar-notifications").empty();
	$("#pager-screen-notifications").empty();
	emptySectionNotifications();
}
 
function listNotificationsAndRefresh() {
	var page = 1;
	listNotifications(page, onListNotificationsSuccess, onListNotificationsError);
}

function listNotifications(page, onSuccess, onError){
	var params = generateNotificationsListParams(g_pagerNotifications, page);
	$.ajax({
		type: "GET",
		url: "/notification/list",
		data: params,
		success: function(data, status, xhr) {
			onSuccess(data);
		},
		error: function(xhr, status, err) {
			onError(err);
		}
	});
}

function generateNotificationsListParams(pager, page) {
	var params = {};
	if (g_loggedInUser == null) return null;
	var token = $.cookie(g_keyToken);
	params[g_keyToken] = token;
	var pageSt = page - 2;
	var pageEd = page + 2;
	var offset = pageSt < 1 ? (pageSt - 1) : 0;
	pageSt -= offset;
	pageEd -= offset;
	params[g_keyPageSt] = pageSt;
	params[g_keyPageEd] = pageEd;
	params[g_keyNumItems] = pager.nItems;
	params[g_keyOrientation] = g_orderDescend;

	if (pager.filters != null) {
		for (var i = 0; i < pager.filters.length; ++i) {
			var filter = pager.filters[i];
			if (filter.selector.val() == null || filter.selector.val() == "") continue;
			params[filter.key] = filter.selector.val();	
		}
	}
	return params;
}

// Tab Q & A a.k.a comments
function onListNotificationsSuccess(data){

	var jsonResponse = JSON.parse(data);
	if(jsonResponse == null) return;

	var notificationsJson = jsonResponse[g_keyNotifications];
	var length = Object.keys(notificationsJson).length;
	var pageSt = parseInt(jsonResponse[g_keyPageSt]);
	var pageEd = parseInt(jsonResponse[g_keyPageEd]);
	var page = pageSt;

	g_pagerNotifications.screen.empty();

	var notifications = [];
	for(var idx = 1; idx <= length; ++idx) {
		var notificationJson = notificationsJson[idx - 1];
		var notification = new Notification(notificationJson);
		notifications.push(notification);
		if (page == g_pagerNotifications.page) {
			generateNotificationCell(g_pagerNotifications.screen, notification);
		}

		if (idx % g_pagerNotifications.nItems != 0) continue;
		g_pagerNotifications.cache.putPage(page, notifications);
		notifications = [];
		++page;	
	}
	if (notifications != null && notifications.length > 0) {
		// for the last page
		g_pagerNotifications.cache.putPage(page, notifications);
	}
	g_pagerNotifications.refreshBar();

}

function onListNotificationsError(err){

}

function generateNotificationCell(par, notification) {
	var cell = $("<p>", {
		style: "border-bottom: thin solid gray;"
	}).appendTo(par);
	var idColumn = $("<span>", {
		text: notification.id
	}).appendTo(cell);
	var content = $("<span>", {
		style: "position: relative; font-size: 14pt; vertical-align: middle;",
		text: notification.content
	}).appendTo(cell);
	var timestamp = $("<span>", {
		style: "position: relative; font-size: 12pt; margin-left: 10pt; color: blue; vertical-align: bottom;",
		text: gmtMiilisecToLocalYmdhis(notification.generatedTime) 
	}).appendTo(cell); 
}

function requestNotifications() {
	clearHome();
	clearProfile();
	clearDetail();

	var selector = createSelector($("#pager-filters"), ["全部", "未讀", "已讀"], ["", 0, 1], null, null, null, null);
	var filter = new PagerFilter(g_keyIsRead, selector);
	var filters = [filter];	

	var pagerCache = new PagerCache(5); 

	// initialize pager widgets
	g_pagerNotifications = new Pager($("#pager-screen-notifications"), $("#pager-bar-notifications"), 10, "/notification/list", generateNotificationsListParams, pagerCache, filters, onListNotificationsSuccess, onListNotificationsError);

	var onLoginSuccess = function(data) {
		listNotificationsAndRefresh();
	};

	var onLoginError = function(err) {
		clearNotifications();
	};

	var onLogoutSuccess = function(data) {
		clearNotifications();
	}; 

	var onLogoutError = null;

	g_preLoginForm = generatePreLoginForm(g_sectionLogin, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError);
	g_onActivitySaveSuccess = null;
	checkLoginStatus();
}
