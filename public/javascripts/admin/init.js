var g_keyStatusIndicator = "status-indicator";

// Assistant Handlers
function onBtnAcceptClicked(evt){

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

function onBtnRejectClicked(evt){

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

function onListActivitiesSuccessAdmin(data){
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

function onListActivitiesErrorAdmin(err){

}

// Generators
function generateActivityCellForAdmin(par, activity){

	var arrayStatusName = ['created','pending','rejected','accepted','expired'];

	var coverImageUrl = null;

	var ret = $("<p>", {
		style: "display: block;"
	}).appendTo(par);

	var infoWrap = $("<div>", {
		style: "margin-left: 5pt; display: inline-block;"	
	}).appendTo(ret);

	if(activity.images != null) {
		var imagesContainer = $('<div>', {
            class: "activity-image-container clearfix"
        }).appendTo(infoWrap);
        for(var i = 0; i < activity.images.length; ++i){
            var imageNode = $('<div>', {
                class: "activity-image left"
            }).appendTo(imagesContainer);
            $('<span>',{
                class: "image-helper"
            }).appendTo(imageNode);
            $('<img>',{
                src: activity.images[i].url,
            }).appendTo(imageNode);
        }
	}

	var cellActivityTitle = $("<a>", {
	    href: window.location.protocol + "//" + window.location.host + "#" +("detail?" + g_keyActivityId + "=" + activity.id.toString()),
		class: "activity-title",
		text: activity.title
	}).appendTo(infoWrap);

	var cellActivityContent = $("<div>", {
		style: "display: block; font-size: 1.2em;",
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

	g_preLoginForm = generatePreLoginForm(g_sectionLogin, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError);

	checkLoginStatus();

}

$(document).ready(function(){

	initTopbar($("#topbar"));
	initActivityEditor($("#wrap"), listActivitiesAndRefreshAdmin);

	requestAdmin();
});
