var g_pagerNotifications = null;

function NotificationPager(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError) {
	this.init(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError);

	this.updateScreen = function(data) {
		if (!data) return;
			
		var notificationJsonList = data[g_keyNotifications];
		var length = Object.keys(notificationJsonList).length;
		var pageSt = parseInt(data[g_keyPageSt]);
		var pageEd = parseInt(data[g_keyPageEd]);
		var page = pageSt;

		var tbl = $("<table class='notifications-viewer'>").appendTo(this.screen);
		var notifications = [];
		var cells = [];
		for(var idx = 1; idx <= length; ++idx) {
			var notificationJson = notificationJsonList[idx - 1];
			var notification = new Notification(notificationJson);
			notifications.push(notification);
			if (page == this.page) {
				var cell = generateNotificationCell(tbl, notification);
				cells.push(cell);
			}

			if (idx % this.nItems !== 0) continue;
			this.cache.putPage(page, notifications);
			notifications = [];
			++page;	
		}
		g_notificationTrash.updateCells(cells);

		if (!(!notifications) && notifications.length > 0) {
			// for the last page
			this.cache.putPage(page, notifications);
		}
	};
}

var g_notificationTrash = null;

function clearNotifications() {
	var toolbar = $("#toolbar");
	toolbar.empty();
	setDimensions(toolbar, "100%", 0);
	$("#pager-notifications").empty();
}

function countNotifications() {
	var token = getToken();
	if (!token) return;
	var paramsBubble = {};
	paramsBubble[g_keyToken] = token;
	
	$.ajax({
		type: "GET",
		url: "/notification/count",
		data: paramsBubble,
		success: function(data, status, xhr) {
			if (!g_loggedInPlayer) return;
			if (isTokenExpired(data)) {
				logout(null);
				return;
			}
			var count = parseInt(data[g_keyCount]);
			g_loggedInPlayer.unreadCount = count;
			g_postLoginMenu.bubble.update(count);
		}, 
		error: function(xhr, status, err) {

		}
	});
}
 
function NotificationTrash(toolbar) {
	this.toolbar = toolbar;
	this.btnTrash = null;
	this.btnDelete = null;
	this.btnBack = null;
	this.cells = [];
	this.isActive = false;

	this.activate = function() {
		for (var i = 0; i < this.cells.length; ++i) {
			var cell = this.cells[i];
			cell.prependCheckbox();
		}
		this.btnTrash.hide();
		this.btnBack.show();
		this.btnDelete.show();
		this.isActive = true;
	};

	this.deactivate = function() {
		for (var i = 0; i < this.cells.length; ++i) {
			var cell = this.cells[i];
			cell.removeCheckbox();
		}
		this.btnTrash.show();
		this.btnBack.hide();
		this.btnDelete.hide();
		this.isActive = false;
	};
	
	this.updateCells = function(cells) {
		this.cells = cells;	
		if (this.isActive) this.activate();
		else this.deactivate();
	};

	// init buttons
	this.init = function() {
		setDimensions(this.toolbar, "100%", "30px"); 
		this.btnTrash = $("<button>", {
			style: "position: absolute;"
		}).appendTo(this.toolbar);
		this.btnTrash.width("25px");
		this.btnTrash.height("25px");
		setOffset(this.btnTrash, "0px", null);
		setBackgroundImageDefault(this.btnTrash, "/assets/icons/trash.png");

		this.btnTrash.click(this, function(evt){
			evt.preventDefault();
			var aTrash = evt.data;
			if (aTrash.isActive)	return;
			aTrash.activate();
		});

		this.btnBack = $("<button>", {
			style: "position: absolute;"
		}).appendTo(this.toolbar);
		this.btnBack.width("25px");
		this.btnBack.height("25px");
		this.btnBack.hide();
		setOffset(this.btnBack, "0px", null);
		setBackgroundImageDefault(this.btnBack, "/assets/icons/back.png");

		this.btnBack.click(this, function(evt) {
			evt.preventDefault();
			var aTrash = evt.data;
			aTrash.deactivate();
		});

		this.btnDelete = $("<button>", {
			style: "position: absolute;"
		}).appendTo(this.toolbar);
		this.btnDelete.width("25px");
		this.btnDelete.height("25px");
		this.btnDelete.hide();
		setOffset(this.btnDelete, "50px", null);
		setBackgroundImageDefault(this.btnDelete, "/assets/icons/delete.png");

		this.btnDelete.click(this, function(evt){
			evt.preventDefault();

			var aTrash = evt.data;
			if (aTrash.cells.length === 0) return;
		
			var notificationIdArray = [];
			for (var i = 0; i < aTrash.cells.length; ++i) {
				var cell = aTrash.cells[i];
				if (!cell.checkbox || !isChecked(cell.checkbox)) continue;
				notificationIdArray.push(cell.notification.id);
			} 

			var params = {};
			var token = getToken();
			if (!token) return;
			params[g_keyToken] = token;
			params[g_keyBundle] = JSON.stringify(notificationIdArray) ;
		
			var aButton = $(evt.srcElement ? evt.srcElement : evt.target);
			disableField(aButton);	
			$.ajax({
				type: "POST",
				data: params,
				url: "/notification/delete",
				success: function(data, status, xhr) {
					enableField(aButton);
					if (isTokenExpired(data)) {
						logout(null);
						return;
					}
					if (!isStandardSuccess(data)) return;	
					listNotificationsAndRefresh();			
				},
				error: function(xhr, status, err) {
					enableField(aButton);
					alert("Error occurs!");
				}
			});
		});
	
	};
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
			if (isTokenExpired(data)) {
				logout(null);
				return;
			}
			onSuccess(data);
		},
		error: function(xhr, status, err) {
			onError(err);
		}
	});
}

function generateNotificationsListParams(pager, page) {
	var params = {};
	if (!g_loggedInPlayer) return null;
	var token = getToken();
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

	if (!(!pager.filters)) {
		for (var i = 0; i < pager.filters.length; ++i) {
			var filter = pager.filters[i];
			if (!filter.selector.val() || filter.selector.val() === "") continue;
			params[filter.key] = filter.selector.val();	
		}
	}
	return params;
}

// Tab Q & A a.k.a comments
function onListNotificationsSuccess(data){
	g_pagerNotifications.refreshScreen(data);
}

function onListNotificationsError(err){

}

function NotificationCell(container, notification, indicator) {

	this.container = container;
	this.notification = notification;
	this.indicator = indicator;
	this.isSelected = false;
	this.checkbox = null;

	this.prependCheckbox = function() {
		var boxCell = $("<td>").prependTo(this.container);
		this.checkbox = $("<input>", {
			type: "checkbox"
		}).appendTo(boxCell);
	};

	this.removeCheckbox = function() {
		if (!this.checkbox) return;
		this.checkbox.parent().remove();
		this.checkbox.remove();
		this.checkbox = null;
	};

	this.container.click(this.notification, function(evt) {
		if (g_notificationTrash.isActive)	return;
		evt.preventDefault();
		var aNotification = evt.data;
		window.location.hash = ("detail?" + g_keyActivityId + "=" + aNotification.activityId);
	});

	if (this.notification.isRead !== 0) {
		var readIndicator = $("<div>", {
			"class": "notification-read"
		}).appendTo(this.indicator);
		return;
	}

	var unreadIndicator = $("<div>", {
		"class": "notification-unread"
	}).appendTo(this.indicator);

	this.container.click(this, function(evt) {
		if (g_notificationTrash.isActive)	return;
		evt.preventDefault();
		var aCell = evt.data;
		var aNotification = aCell.notification;
		var token = getToken();
		if (!token) return;

		var params = {};
		params[g_keyToken] = token;
		params[g_keyId] = aNotification.id;
		params[g_keyIsRead] = 1;
		$.ajax({
			type: "POST",
			url: "/el/notification/mark",
			data: params,
			success: function(data, status, xhr) {
				if (isTokenExpired(data)) {
					logout(null);
					return;
				}
				window.location.hash = ("detail?" + g_keyActivityId + "=" + aNotification.activityId);
			},
			error: function(xhr, status, err) {

			}
		});	
	});

}

function generateNotificationCell(par, notification) {
	var container = $("<tr class = 'notifications-viewer-row'>", {
	}).appendTo(par);
	var idColumn = $("<td>", {
		text: notification.id,
		"class": "notification-id"
	}).appendTo(container);
	var content = $("<td>", {
		text: notification.content,
		"class": "notification-content"
	}).appendTo(container);
	var timestamp = $("<td>", {
		text: gmtMiilisecToLocalYmdhis(notification.generatedTime),
		 "class": "notification-time"
	}).appendTo(container); 

	var indicator = $("<td>", {
		"class": "notification-envelope"
	}).appendTo(container); 
	return new NotificationCell(container, notification, indicator);
}

function requestNotifications() {
	clearHome();
	clearProfile();
	clearDetail();

	// initialize trash
	g_notificationTrash = new NotificationTrash($("#toolbar"));
	g_notificationTrash.init();

	// initialize pager 
	var filterMap = {};
	filterMap[g_keyIsRead] = [[TITLES.all, TITLES.unread, TITLES.read], ["", 0, 1]];

	g_pagerNotifications = new NotificationPager(10, "/notification/list", generateNotificationsListParams, null, 5, filterMap, onListNotificationsSuccess, onListNotificationsError);
	g_pagerNotifications.appendTo($("#pager-notifications"));
	g_pagerNotifications.refresh();

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
