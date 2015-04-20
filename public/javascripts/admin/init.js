var g_keyStatusIndicator = "status-indicator";

function OrderOption (editor, bitCode, labelName, initChecked) {
	this.editor = editor;
	this.bitCode = bitCode;
	this.labelName = labelName;
	this.checkbox = null;
	this.initChecked = initChecked;
	this.appendTo = function(par) {
		var container = $("<p>", {
			"class": "order-option"
		}).appendTo(par);

		var label = $('<label>', {
			"class": "order-label"
		}).appendTo(container);

		this.checkbox = $("<input>",{
			type: "checkbox",
			"class": "order-checkbox"
		}).appendTo(label);

		var name = $("<plaintext>", {
			"class": "order-label-name",
			text: this.labelName	
		}).appendTo(label);
	
		var aEditor = this.editor;
		this.checkbox.change(function(evt) {
			aEditor.enableSubmit();
		});
		if (this.initChecked > 0)	this.check();
	};
	this.check = function() {
		if (this.checkbox == null) return;
		checkField(this.checkbox); 	
	};
	this.uncheck = function() {
		if (this.checkbox == null) return;
		uncheckField(this.checkbox); 	
	};
}

function PriorityEditor (activity) {
	this.activity = activity;
	this.btnCheckAll = null;
	this.btnUncheckAll = null;
	this.orderOptionList = null;
	this.selectPriority = null;
	this.btnSubmit = null;
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
			"class": "priority-editor-check-all",
			text: TITLES["check_all"]
		}).appendTo(buttonsRow);

		this.btnUncheckAll = $("<button>", {
			"class": "priority-editor-uncheck-all",
			text: TITLES["uncheck_all"]
		}).appendTo(buttonsRow);

		this.btnCheckAll.click(function(evt) {
			evt.preventDefault();
			for (var i = 0; i < aEditor.orderOptionList.length; ++i) {
				var orderOption = aEditor.orderOptionList[i];
				orderOption.check();
				aEditor.enableSubmit();
			}
		});

		this.btnUncheckAll.click(function(evt) {
			evt.preventDefault();
			for (var i = 0; i < aEditor.orderOptionList.length; ++i) {
				var orderOption = aEditor.orderOptionList[i];
				orderOption.uncheck();
				aEditor.enableSubmit();	
			}
		});
		
		var sectionOrderList = $("<p>", {
			"class": "priority-editor-order-option-list"
		}).appendTo(container);
		this.orderOptionList = [];

		var labelNameList = [TITLES["last_accepted_time"], TITLES["begin_time"], TITLES["deadline"]];
		var bitCodeList = [1, 2, 4];

		var length = labelNameList.length;
		
		for (var i = 0; i < length; ++i) {
			var labelName = labelNameList[i];
			var bitCode = bitCodeList[i];
			var initChecked = ((activity.orderMask & bitCode) > 0 ? 1 : 0);
			var orderOption = new OrderOption(this, bitCode, labelName, (activity.orderMask & bitCode));
			orderOption.appendTo(sectionOrderList);
			this.orderOptionList.push(orderOption);	
		}

		var sectionSubmit = $("<p>", {
			"class": "priority-editor-buttons-row"
		}).appendTo(container);
		
		this.btnSubmit = $("<button>", {
			text: TITLES["update"],
			"class": "priority-editor-submit"
		}).appendTo(sectionSubmit);

		this.btnSubmit.click(function(evt) {
			evt.preventDefault();
			var token = $.cookie(g_keyToken);
			if (token == null) return;

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
				url: "/admin/prioritize",
				data: params,
				success: function(data, status, xhr) {
					aEditor.activity.orderMask = orderMask;
				},
				error: function(xhr, status, err) {
					alert(ALERTS["not_updated"]);
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

	var btnAccept = $(evt.srcElement ? evt.srcElement : evt.target);

	evt.preventDefault();
	var data = evt.data;
	var token = $.cookie(g_keyToken);
	var params = {};
 	params[g_keyActivityId] = data[g_keyActivityId];
	params[g_keyToken] = token;

	disableField(btnAccept);
	$.ajax({
		type: "POST",
		url: "/el/admin/activity/accept",
		// url: "/admin/accept",
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
			indicator.text("Accepted");
		},
		error: function(xhr, status, err) {
			enableField(btnAccept);
		}
	});
}

function onBtnRejectClicked(evt) {

	var btnReject = $(evt.srcElement ? evt.srcElement : evt.target);

	evt.preventDefault();
	var data = evt.data;
	var token = $.cookie(g_keyToken);
	var params = {};
 	params[g_keyActivityId] = data[g_keyActivityId];
	params[g_keyToken] = token;

	disableField(btnReject);
	$.ajax({
		type: "POST",
		url: "/el/admin/activity/reject", 
		// url: "/admin/reject",
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

	var btnDelete = $(evt.srcElement ? evt.srcElement : evt.target);

	evt.preventDefault();
	var data = evt.data;
	var token = $.cookie(g_keyToken);
	var params = {};
	params[g_keyActivityId] = data[g_keyActivityId];
	params[g_keyToken] = token;

	disableField(btnDelete);
	$.ajax({
		type: "POST",
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
	var jsonResponse = data;

	var pageSt = parseInt(jsonResponse[g_keyPageSt]);
	var pageEd = parseInt(jsonResponse[g_keyPageEd]);
	var page = pageSt;

	var activitiesJson = jsonResponse[g_keyActivities];
	var length = Object.keys(activitiesJson).length;

	g_pager.screen.empty();
	var activities = [];
	for(var idx = 1; idx <= length; ++idx) {
		var activityJson = activitiesJson[idx - 1];
		var activity = new Activity(activityJson);
		activities.push(activity);
		if (page == g_pager.page) {
			generateActivityCellForAdmin(g_pager.screen, activity);
		}

		if (idx % g_pager.nItems != 0) continue;
		g_pager.cache.putPage(page, activities);
		activities = [];
		++page;
	}
	if (activities != null && activities.length > 0) {
		// for the last page
		g_pager.cache.putPage(page, activities);
	}

	g_pager.refreshBar();
} 

function onListActivitiesErrorAdmin(err) {

}

// Generators
function generateActivityCellForAdmin(par, activity) {

	var arrayStatusName = ['created','pending','rejected','accepted','expired'];

	var coverImageUrl = null;

	var ret = $("<div>", {
		style: "display: block;"
	}).appendTo(par);

	var infoWrap = $("<div>", {
		style: "margin-left: 5pt;"	
	}).appendTo(ret);

	if(activity.images != null) {
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
		"class": "truncate",	
		style: "font-size: 1.2em; width: 100%;",
		text: activity.content
	}).appendTo(infoWrap);

	$("<br/><br/>").appendTo(ret);

	var statusIndicator = $("<div>", {
		style: "color: red; font-size: 15pt; margin-left: 5pt; display: inline-block",
		text: arrayStatusName[parseInt(activity.status)]
	}).appendTo(ret);

	ret.data(g_keyStatusIndicator, statusIndicator);
	
	var buttonsWrap = $("<div>", {
	    style: "display: inline-block;"
	}).appendTo(ret);

	// this condition is temporarily hard-coded
	if(activity.status != g_statusAccepted){
            var btnAccept = $("<button>", {
		        style: "width: 64pt; height: 36pt; font-size: 16pt; color: DarkSlateBlue; margin-left: 5pt; background-color: #aaaaaa;",
                text: 'Accept'
            }).appendTo(buttonsWrap);
            var dAccept = {};
            dAccept[g_keyActivityId] = activity.id;
            btnAccept.click(dAccept, onBtnAcceptClicked);
        }

	if(activity.status != g_statusRejected){
            var btnReject = $("<button>", {
		        style: " width: 64pt; height: 36pt; font-size: 16pt; color: purple; margin-left: 5pt; background-color: #aaaaaa;",
                text: 'Reject'
            }).appendTo(buttonsWrap);
            var dReject = {};
            dReject[g_keyActivityId] = activity.id;
            btnReject.click(dReject, onBtnRejectClicked);
        }

	var btnDelete = $("<button>", {
		style: "width: 64pt; height: 36pt; font-size: 16pt; color: IndianRed; margin-left: 5pt; background-color: #aaaaaa;",
		text: 'Delete'
	}).appendTo(buttonsWrap);
	var dDelete = {};
	dDelete[g_keyActivityId] = activity.id;
	btnDelete.click(dDelete, onBtnDeleteClicked);

	if (activity.status == g_statusAccepted) {
		var sectionPriorityEditor = $("<div>", {
			style: "padding: 2px;"
		}).appendTo(ret);
		var editor = new PriorityEditor(activity);
		editor.appendTo(sectionPriorityEditor);
	}

	var hr = $("<hr>", {
		style: "height: 1pt; color: black; background-color: black"
	}).appendTo(ret);

	return ret;
}

function requestAdmin() {

	var selector = createSelector($("#pager-filters"), ["pending", "accepted", "rejected"], [g_statusPending, g_statusAccepted, g_statusRejected], null, null, null, null);
	var filter = new PagerFilter(g_keyStatus, selector);
	var filters = [filter];	

	var pagerCache = new PagerCache(10);

	// initialize pager widgets
	g_pager = new Pager($("#pager-screen-activities"), $("#pager-bar-activities"), g_numItemsPerPage, "/activity/list", generateActivitiesListParams, null, pagerCache, filters, onListActivitiesSuccessAdmin, onListActivitiesErrorAdmin);

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
