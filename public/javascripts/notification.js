var g_sectionNotifications = null;
var g_pagerNotifications = null;
var g_notificationTrash = null;

function emptySectionNotifications() {
	if (g_sectionNotifications == null) return;
	setDimensions(g_sectionNotifications, "100%", 0);
	g_sectionNotifications.empty();
}

function clearNotifications() {
	$("#tool-bar").empty();
	$("#pager-filters").empty();
	$("#pager-bar-notifications").empty();
	$("#pager-screen-notifications").empty();
	emptySectionNotifications();
}

function countNotifications() {
	var token = $.cookie(g_keyToken);
	if (token == null) return;
	var paramsBubble = {};
	paramsBubble[g_keyToken] = token;
	
	$.ajax({
		type: "GET",
		url: "/notification/count",
		data: paramsBubble,
		success: function(data, status, xhr) {
			if (g_loggedInUser == null) return;
			var jsonResponse = JSON.parse(data);
			var count = parseInt(jsonResponse[g_keyCount]);
			g_loggedInUser.unreadCount = count;
			g_postLoginMenu.bubble.update(count);
		}, 
		error: function(xhr, status, err) {

		}
	});
}
 
function NotificationTrash(btnDelete) {
	this.notificationIdSet = {};
	this.btnDelete = btnDelete;
	this.cells = [];
	this.isActive = false;

	this.activate = function() {
		this.isActive = true;
		for (var cell in this.cells) {
			cell.prependCheckbox();
		}
	};
	this.deactivate = function() {
		this.isActive = false;
		this.notificationIdSet = {};
		for (var cell in this.cells) {
			cell.removeCheckbox();
		}
		this.cells = [];
	};
	
	this.select = function(notificationId) {
		if (!this.isActive) return;
		this.notificationIdSet[notificationId] = 1;
	}; 

	this.unselect = function(notificationId) {
		if (!this.isActive) return;
		if (!this.notificationIdSet.hasOwnProperty(notificationId)) return;
		delete this.notificationIdSet.notificationId;
	};
	
	this.updateCells = function(cells) {
		this.cells = cells;	
	}

	this.btnDelete.click(this, function(evt){
		evt.preventDefault();

		var aTrash = evt.data;
		if (!aTrash.isActive) {
			aTrash.activate();
			return;
		} 		

		if (aTrash.notifications.length == 0) return;
	
		var notificationIdArray = [];
		for (var key in aTrash.notificationIdSet) {
			notificationIdArray.push(key);
		} 

		var params = {};
		var token = $.cookie(g_keyToken);
		if (token == null) return;
		params[g_keyToken] = token;
		params[g_keyBundle] = JSON.stringify(notificationIdArray) ;
			
		$.ajax({
			type: "POST",
			data: params,
			url: "/notification/delete",
			success: function(data, status, xhr) {
				listNotificationsAndRefresh();			
			},
			error: function(xhr, status, err) {
				alert("Error occurs!");
			}
		});
	});
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

	var notificationJsonList = jsonResponse[g_keyNotifications];
	var length = Object.keys(notificationJsonList).length;
	var pageSt = parseInt(jsonResponse[g_keyPageSt]);
	var pageEd = parseInt(jsonResponse[g_keyPageEd]);
	var page = pageSt;

	g_pagerNotifications.screen.empty();

	var notifications = [];
	var cells = [];
	for(var idx = 1; idx <= length; ++idx) {
		var notificationJson = notificationJsonList[idx - 1];
		var notification = new Notification(notificationJson);
		notifications.push(notification);
		if (page == g_pagerNotifications.page) {
			var cell = generateNotificationCell(g_pagerNotifications.screen, notification);
			cells.push(cell);
		}

		if (idx % g_pagerNotifications.nItems != 0) continue;
		g_pagerNotifications.cache.putPage(page, notifications);
		notifications = [];
		++page;	
	}
	g_notificationTrash.updateCells(cells);

	if (notifications != null && notifications.length > 0) {
		// for the last page
		g_pagerNotifications.cache.putPage(page, notifications);
	}
	g_pagerNotifications.refreshBar();

}

function onListNotificationsError(err){

}

function NotificationCell(container, notification, indicator) {

	this.container = container;
	this.notification = notification;
	this.indicator = indicator;
	this.isSelected = false;
	this.checkbox = null;

	this.select = function() {
		this.isSelected = true;	
		g_notificationTrash.select(aNotification.id);
	};

	this.unselect = function() {
		this.isSelected = false;
		g_notificationTrash.unselect(aNotification.id);
	};

	this.toggle = function() {
		if (this.isSelected) this.unselect();
		else this.select();
	};

	this.prependCheckbox = function() {
		var boxCell = $("<td>").prependTo(this.container);
		this.checkbox = $("<checkbox>").appendTo(boxCell);
		this.checkbox.change(this, function(evt) {
			evt.preventDefault();
			var aCell = evt.data;
			aCell.toggle();
		});	
		this.unselect();
	};

	this.removeCheckbox = function() {
		this.checkbox.parent().remove();
		this.checkbox.remove();
		this.checkbox = null;
		this.unselect();
	};

	this.container.click(this.notification, function(evt) {
		evt.preventDefault();
		var aNotification = evt.data;
		if (g_notificationTrash.isActive) {
			this.toggle();
			return;
		}
		window.location.hash = (g_keyActivityId + "=" + aNotification.activityId);
	});

	if (this.notification.isRead != 0) return;

	var unreadIndicator = $("<img>", {
		style: "position: relative; margin-left: 5pt;",
		src: "/assets/icons/notification.png"
	}).appendTo(this.indicator);
	unreadIndicator.width("50px");
	unreadIndicator.height("50px");

	this.container.click(this, function(evt) {
		evt.preventDefault();
		var aCell = evt.data;
		var aNotification = aCell.notification;
		if (g_notificationTrash.isActive) {
			aCell.toggle();
			return;
		}
		var token = $.cookie(g_keyToken);
		if (token == null) return;

		var params = {};
		params[g_keyToken] = token;
		params[g_keyId] = aNotification.id;
		params[g_keyIsRead] = 1;
		$.ajax({
			type: "POST",
			url: "/el/notification/mark",
			data: params,
			success: function(data, status, xhr) {
				window.location.hash = (g_keyActivityId + "=" + aNotification.activityId);
			},
			error: function(xhr, status, err) {

			}
		});	
	});

}

function generateNotificationCell(par, notification) {
	var container = $("<tr>", {
		style: "position: relative; height: 50px; border-bottom: 1px solid gray; cursor: pointer;"
	}).appendTo(par);
	var idColumn = $("<td>", {
		style: "postion: relative; margin-right: 5pt; font-size: 11pt; vertical-align: middle;",
		text: notification.id
	}).appendTo(container);
	var content = $("<td>", {
		style: "position: relative; font-size: 14pt; vertical-align: middle;",
		text: notification.content
	}).appendTo(container);
	var timestamp = $("<td>", {
		style: "position: relative; font-size: 11pt; margin-left: 10pt; color: blue; vertical-align: middle;",
		text: gmtMiilisecToLocalYmdhis(notification.generatedTime) 
	}).appendTo(container); 

	var indicator = $("<td>").appendTo(container); 
	return new NotificationCell(container, notification, indicator);
}

function requestNotifications() {
	clearHome();
	clearProfile();
	clearDetail();

	var toolbar = $("#toolbar"); 
	var btnDelete = $("<button>").appendTo(toolbar);
	btnDelete.width("25px");
	btnDelete.height("25px");

	setBackgroundImageDefault(btnDelete, "/assets/icons/delete.png");
	g_notificationTrash = new NotificationTrash(btnDelete);

	var selector = createSelector($("#pager-filters"), ["全部", "未讀", "已讀"], ["", 0, 1], null, null, null, null);
	var filter = new PagerFilter(g_keyIsRead, selector);
	var filters = [filter];	

	var pagerCache = new PagerCache(5); 

	// initialize pager widgets
	g_pagerNotifications = new Pager($("#pager-screen-notifications"), $("#pager-bar-notifications"), 10, "/notification/list", generateNotificationsListParams, pagerCache, filters, onListNotificationsSuccess, onListNotificationsError);

	var onLoginSuccess = function(data) {
		countNotifications();
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
