var g_keyStatusIndicator = "status-indicator";
var g_pagerActivity = null; 

function AdminActivityPager(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError) {
	this.init(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError);

	this.updateScreen = function(data) {
			
		if (!data) return;

		var pageSt = parseInt(data[g_keyPageSt]);
		var pageEd = parseInt(data[g_keyPageEd]);
		var page = pageSt;

		var activitiesData = data[g_keyActivities];
		var length = Object.keys(activitiesData).length;

		var activities = [];
		for(var idx = 1; idx <= length; ++idx) {
			var activityData = activitiesData[idx - 1];
			var activity = new Activity(activityData);
			activities.push(activity);
			if (page == this.page) {
				generateActivityCellForAdmin(this.screen, activity);
			}

			if (idx % this.nItems !== 0) continue;
			this.cache.putPage(page, activities);
			activities = [];
			++page;
		}
		if (!(!activities) && activities.length > 0) {
			// for the last page
			this.cache.putPage(page, activities);
		}

	};
}

AdminActivityPager.inherits(Pager);

function OrderOption (editor, bitCode, labelName, initChecked) {
	this.editor = editor;
	this.bitCode = bitCode;
	this.labelName = labelName;
	this.initChecked = initChecked;
	this.appendTo = function(par) {
		var container = $("<p>", {
			"class": "order-option"
		}).appendTo(par);

		var label = $('<label>', {
			"class": "order-label"
		}).appendTo(container);


		var name = $("<plaintext>", {
			"class": "order-label-name",
			text: this.labelName	
		}).appendTo(label);
	
		var aEditor = this.editor;
		this.checkbox = $("<input>",{
			type: "checkbox",
			"class": "order-checkbox"
		}).appendTo(label).change(function(evt) {
			aEditor.enableSubmit();
		});
		if (this.initChecked > 0)	this.check();
	};
	this.check = function() {
		if (!this.checkbox) return;
		checkField(this.checkbox); 	
	};
	this.uncheck = function() {
		if (!this.checkbox) return;
		uncheckField(this.checkbox); 	
	};
}

function PriorityEditor (activity) {
	this.activity = activity;
	this.disableSubmit = function () {
		disableField(this.btnSubmit);
	};
	this.enableSubmit = function () {
		enableField(this.btnSubmit);
	};
	this.appendTo = function(par) {
		var container = $("<div>", {
			"class": "priority-editor",
		}).appendTo(par);
		
		var aEditor = this;

		var sectionPriority = $("<p>").appendTo(container);
		this.selectPriority = createSelector(sectionPriority, ["none", "low", "medium", "high"], [0, 1, 2, 3], 0, 0, 0, 0);
		this.selectPriority.val(activity.priority);
		this.selectPriority.change(function(evt) {
			aEditor.enableSubmit();
		});
			
		var buttonsRow  = $("<p>", {
			"class": "priority-editor-buttons-row"
		}).appendTo(container);

		this.btnCheckAll = $("<button>", {
			"class": "positive-button",
			text: TITLES.check_all
		}).appendTo(buttonsRow).click(function(evt) {
			evt.preventDefault();
			for (var i = 0; i < aEditor.orderOptionList.length; ++i) {
				var orderOption = aEditor.orderOptionList[i];
				orderOption.check();
			}
			aEditor.enableSubmit();
		});

		this.btnUncheckAll = $("<button>", {
			"class": "positive-button",
			text: TITLES.uncheck_all
		}).appendTo(buttonsRow).click(function(evt) {
			evt.preventDefault();
			for (var i = 0; i < aEditor.orderOptionList.length; ++i) {
				var orderOption = aEditor.orderOptionList[i];
				orderOption.uncheck();
			}
			aEditor.enableSubmit();	
		});
		
		var sectionOrderList = $("<p>", {
			"class": "priority-editor-order-option-list"
		}).appendTo(container);
		this.orderOptionList = [];

		var labelNameList = [TITLES.last_accepted_time, TITLES.begin_time, TITLES.deadline];
		var bitCodeList = [1, 2, 4];

		var length = labelNameList.length;
		
		for (var i = 0; i < length; ++i) {
			var labelName = labelNameList[i];
			var bitCode = bitCodeList[i];
			var initChecked = ((activity.orderMask & bitCode) > 0 ? 1 : 0);
			var orderOption = new OrderOption(this, bitCode, labelName, initChecked);
			orderOption.appendTo(sectionOrderList);
			this.orderOptionList.push(orderOption);	
		}

		var sectionSubmit = $("<p>", {
			"class": "priority-editor-buttons-row"
		}).appendTo(container);
		
		this.btnSubmit = $("<button>", {
			text: TITLES.update,
			"class": "positive-button"
		}).appendTo(sectionSubmit).click(function(evt) {
			evt.preventDefault();
			var token = getToken();
			if (!token) return;

			var orderMask = 0;
			for (var i = 0; i < aEditor.orderOptionList.length; ++i) {
				var orderOption = aEditor.orderOptionList[i];
				if (!isChecked(orderOption.checkbox)) continue;
				orderMask |= orderOption.bitCode;
			} 
			
			var priority = aEditor.selectPriority.val();			
			
			var params = {};
			params[g_keyToken] = token;
			params[g_keyPriority] = priority;
			params[g_keyOrderMask] = orderMask;
			params[g_keyActivityId] = aEditor.activity.id; 			
			
			aEditor.disableSubmit();		
			$.ajax({
				type: "POST",
				// url: "/el/admin/prioritize", // TODO
				url: "/admin/prioritize",
				data: params,
				success: function(data, status, xhr) {
					aEditor.activity.orderMask = orderMask;
				},
				error: function(xhr, status, err) {
					alert(ALERTS.not_updated);
					aEditor.enableSubmit();
				} 
			});
		});
		
		// initialize submit button
		this.disableSubmit();
	};
}

// Assistant Handlers
function onBtnAcceptClicked(evt) {

	var btnAccept = getTarget(evt);

	evt.preventDefault();
	var data = evt.data;
	var token = getToken();
	var params = {};
 	params[g_keyActivityId] = data[g_keyActivityId];
	params[g_keyToken] = token;

	disableField(btnAccept);
	$.ajax({
		type: "POST",
		// url: "/el/admin/activity/accept",
		url: "/admin/accept",
		data: params,
		success: function(data, status, xhr) {
			enableField(btnAccept);
			if (isTokenExpired(data)) {
				logout(null);
				return;
			}
			if (!isStandardSuccess(data)) return;
			var buttonsWrap = btnAccept.parent();
			var cell = buttonsWrap.parent(); 
			btnAccept.remove();
			var indicator = cell.data(g_keyStatusIndicator);
			indicator.text(STATUS_NAMES.accepted);
		},
		error: function(xhr, status, err) {
			enableField(btnAccept);
		}
	});
}

function onBtnRejectClicked(evt) {

	var btnReject = getTarget(evt);

	evt.preventDefault();
	var data = evt.data;
	var token = getToken();
	var params = {};
 	params[g_keyActivityId] = data[g_keyActivityId];
	params[g_keyToken] = token;

	disableField(btnReject);
	$.ajax({
		type: "POST",
		// url: "/el/admin/activity/reject", 
		url: "/admin/reject",
		data: params,
		success: function(data, status, xhr){
			enableField(btnReject);
			if (isTokenExpired(data)) {
				logout(null);
				return;
			}
			if (!isStandardSuccess(data)) return;
			var buttonsWrap = btnReject.parent(); 
			var cell = buttonsWrap.parent();
			btnReject.remove();
			var indicator = cell.data(g_keyStatusIndicator);
			indicator.text("Rejected");
		},
		error: function(xhr, status, err){
			enableField(btnReject);
		}
	});
}

function onBtnDeleteClicked(evt){

	var btnDelete = getTarget(evt);

	evt.preventDefault();
	var data = evt.data;
	var token = getToken();
	var params = {};
	params[g_keyActivityId] = data[g_keyActivityId];
	params[g_keyToken] = token;

	disableField(btnDelete);
	$.ajax({
		type: "POST",
		// url: "/el/admin/activity/delete",
		url: "/admin/delete", 
		data: params,
		success: function(data, status, xhr){
			enableField(btnDelete);
			if (isTokenExpired(data)) {
				logout(null);
				return;
			}
			if (!isStandardSuccess(data)) return;
			var buttonsWrap = btnDelete.parent(); 
			var cell = buttonsWrap.parent();
			btnDelete.remove();
			var indicator = cell.data(g_keyStatusIndicator);
			indicator.text("Deleted");
		},
		error: function(xhr, status, err){
			enableField(btnDelete);
		}
	});
}

function listActivitiesAndRefreshAdmin() {
	var page = 1;
	listActivities(page, onListActivitiesSuccessAdmin, onListActivitiesErrorAdmin);
}

function onListActivitiesSuccessAdmin(data) {

	if (isTokenExpired(data)) {
		logout(null);
		return;
	}

	g_pagerActivity.refreshScreen(data);
} 

function onListActivitiesErrorAdmin(err) {

}

function generateActivityCellForAdmin(par, activity) {
	// TODO: refactor by BaseWidget and proper css classes

	var arrayStatusName = [STATUS_NAMES.created, STATUS_NAMES.pending, STATUS_NAMES.rejected, STATUS_NAMES.accepted];

	var coverImageUrl = null;

	var ret = $("<div>").appendTo(par);

	var infoWrap = $("<div>", {
		"class": "admin-cell-info-wrap"
	}).appendTo(ret);

	if(!(!activity.images)) {
		var imagesContainer = $('<div>', {
			"class": "activity-image-container clearfix"
		}).appendTo(infoWrap);
		for(var i = 0; i < activity.images.length; ++i){
		    var imageNode = $('<div>', {
			"class": "activity-image left"
		    }).appendTo(imagesContainer);
		    $('<span>',{
			"class": "image-helper"
		    }).appendTo(imageNode);
		    $('<img>',{
			src: activity.images[i].url,
		    }).appendTo(imageNode);
		}
	}

	var cellActivityTitle = $("<a>", {
		href: window.location.protocol + "//" + window.location.host + "#" +("detail?" + g_keyActivityId + "=" + activity.id.toString()),
		"class": "activity-title",
		text: activity.title
	}).appendTo(infoWrap);

	var cellActivityContent = $("<div>", {
		"class": "truncate admin-cell-activity-content",	
		text: activity.content
	}).appendTo(infoWrap);

	var statusIndicator = $("<div>", {
		"class": "admin-cell-status-indicator",
		text: arrayStatusName[parseInt(activity.status)]
	}).appendTo(ret);

	ret.data(g_keyStatusIndicator, statusIndicator);
	
	var buttonsWrap = $("<div>", {
		"class": "admin-cell-buttons-wrap"
	}).appendTo(ret);

	// this condition is temporarily hard-coded
	if(activity.status != g_statusAccepted){
		var btnAccept = $("<button>", {
			"class": "admin-cell-button-accept positive-button",
			text: 'Accept'
		}).appendTo(buttonsWrap);
		var dAccept = {};
		dAccept[g_keyActivityId] = activity.id;
		btnAccept.click(dAccept, onBtnAcceptClicked);
        }

	if(activity.status != g_statusRejected){
		var dReject = {};
		dReject[g_keyActivityId] = activity.id;
		var btnReject = $("<button>", {
			"class": "admin-cell-button-reject negative-button",
			text: 'Reject'
		}).appendTo(buttonsWrap).click(dReject, onBtnRejectClicked);
        }

	var dDelete = {};
	dDelete[g_keyActivityId] = activity.id;
	var btnDelete = $("<button>", {
		"class": "admin-cell-button-delete negative-button",
		text: 'Delete'
	}).appendTo(buttonsWrap).click(dDelete, onBtnDeleteClicked);

	if (activity.status == g_statusAccepted) {
		var sectionPriorityEditor = $("<div>", {
			"class": "admin-cell-section-priority-editor"
		}).appendTo(ret);
		var editor = new PriorityEditor(activity);
		editor.appendTo(sectionPriorityEditor);
	}

	var hr = $("<hr>", {
		"class": "admin-cell-separator"
	}).appendTo(ret);

	return ret;
}

function requestAdmin() {

	var filterMap = {};
	filterMap[g_keyStatus] = [[STATUS_NAMES.pending, STATUS_NAMES.accepted, STATUS_NAMES.rejected], [g_statusPending, g_statusAccepted, g_statusRejected]]; 

	// initialize pager widgets
	g_pagerActivity = new AdminActivityPager(g_numItemsPerPage, "/activity/list", generateActivitiesListParams, null, 10, filterMap, onListActivitiesSuccessAdmin, onListActivitiesErrorAdmin);
	g_pagerActivity.appendTo($("#pager-activities"));
	g_pagerActivity.refresh();

	var onLoginSuccess = function(data) {
		listActivitiesAndRefreshAdmin();
	};

	var onLoginError = function(err) {

	};

	var onLogoutSuccess = function(data) {

	}; 

	var onLogoutError = null;

	// registering is disabled on admin page
	g_preLoginForm = generatePreLoginForm(g_sectionLogin, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError, false);

	checkLoginStatus();

}

$(document).ready(function() {

	initTopbar($("#topbar"));
	initActivityEditor($("#wrap"), listActivitiesAndRefreshAdmin);

	requestAdmin();
});
