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
		url: "/el/notification/query",
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
	if (g_loggedInUser != null)	params[g_keyUserId] = g_loggedInUser.id;
        params[g_keyPage] = page;
	params[g_keyNumItems] = pager.nItems;
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
        var page = parseInt(jsonResponse[g_keyPage]);

	g_pagerNotifications.screen.empty();

        for(var idx = 1; idx <= length; ++idx) {
		var notificationJson = notificationsJson[idx - 1];
		var notification = new Notification(notificationJson);
		if (page != g_pagerNotifications.page)	continue;
		generateNotificationCell(g_pagerNotifications.screen, notification);
        }
        g_pagerNotifications.refreshBar();

}

function onListNotificationsError(err){

}

function generateNotificationCell(par, notification) {
	var cell = $("<p>").appendTo(par);
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

	var pagerCache = new PagerCache(1); 

	// initialize pager widgets
	g_pagerNotifications = new Pager($("#pager-screen-notifications"), $("#pager-bar-notifications"), 10, "/el/notification/query", generateActivitiesListParams, pagerCache, filters, onListActivitiesSuccess, onListActivitiesError);

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
