var g_selectFilter=null;

var g_keyStatusIndicator = "status-indicator";

// Assistant Handlers
function onBtnAcceptClicked(evt){

	var btnAccept = $(this);

	evt.preventDefault();
	var data = evt.data;
	var token = $.cookie(g_keyToken);
	var params = {};
 	params[g_keyActivityId] = data[g_keyActivityId];
	params[g_keyToken] = token;

	$.ajax({
		type: "PUT",
		url: "/admin/accept",
		data: params,
		success: function(data, status, xhr) {
			var buttonsWrap = btnAccept.parent();
			var cell = buttonsWrap.parent(); 
			btnAccept.remove();
			var indicator = cell.data(g_keyStatusIndicator);
			indicator.text("Accepted");
		},
		error: function(xhr, status, err) {

		}
	});
}

function onBtnRejectClicked(evt){
	var btnReject = $(this);

	evt.preventDefault();
	var data = evt.data;
	var token = $.cookie(g_keyToken);
	var params = {};
 	params[g_keyActivityId] = data[g_keyActivityId];
	params[g_keyToken] = token;

	try{
		$.ajax({
			type: "PUT",
			url: "/admin/reject", 
			data: params,
			success: function(data, status, xhr){
				var buttonsWrap = btnReject.parent(); 
				var cell = buttonsWrap.parent();
				btnReject.remove();
				var indicator = cell.data(g_keyStatusIndicator);
				indicator.text("Rejected");
			},
			error: function(xhr, status, err){
				
			}			
		});
	} catch(err){

	}
}

function onBtnDeleteClicked(evt){

        var btnDelete = $(this);

        evt.preventDefault();
        var data = evt.data;
        var token = $.cookie(g_keyToken);
        var params = {};
        params[g_keyActivityId] = data[g_keyActivityId];
        params[g_keyToken] = token;

	try{
		$.ajax({
			type: "PUT",
			url: "/admin/delete", 
			data: params,
			success: function(data, status, xhr){
				var buttonsWrap = btnDelete.parent(); 
				var cell = buttonsWrap.parent();
				btnDelete.remove();
				var indicator = cell.data(g_keyStatusIndicator);
				indicator.text("Deleted");
			},
			error: function(xhr, status, err){
				
			}
		});
	} catch(err){

	}
}

function queryActivitiesAndRefresh() {
	queryActivities(0, 0, g_pagerContainer.nItems, g_pagerContainer.orientation, g_directionForward, g_vieweeId, g_pagerContainer.relation, g_pagerContainer.status, onQueryActivitiesSuccessAdmin, onQueryActivitiesErrorAdmin);
}

function onPagerButtonClickedAdmin(evt) {
	var button = evt.data;
	var container = button.container;
	var page = button.page;
	if (page == container.page) return;
	var direction = page > container.page ? g_directionForward : g_directionBackward;
	var refIndex = page > container.page ? g_pagerContainer.ed : g_pagerContainer.st;

	queryActivities(refIndex, page, container.nItems, container.orientation, direction, g_vieweeId, container.relation, container.status, onQueryActivitiesSuccessAdmin, onQueryActivitiesErrorAdmin);
}

function onQueryActivitiesSuccessAdmin(data, status, xhr){
	var jsonResponse = JSON.parse(data);
	if(jsonResponse == null) return;
	
	var oldSt = g_pagerContainer.st;
	var oldEd = g_pagerContainer.ed;

	// display pager container
	var activitiesJson = jsonResponse[g_keyActivities];
	var length = Object.keys(activitiesJson).length;
	if(length == 0) return;
	g_pagerContainer.screen.empty();

	for(var idx = 0; idx < length; ++idx) {
		var activityJson = activitiesJson[idx];
		var activity = new Activity(activityJson);
		var activityId = parseInt(activity.id);
		if(idx == 0)	g_pagerContainer.st = activityId;
		if(idx == length - 1)	g_pagerContainer.ed = activityId;
		generateActivityCellForAdmin(g_pagerContainer.screen, activity);
	}

	var page  = g_pagerContainer.page;
	var orientation = g_pagerContainer.orientation; 
	var newSt = g_pagerContainer.st; 
	var newEd = g_pagerContainer.ed;
	if(orientation == +1 && newSt > oldEd) ++page;
	if(orientation == +1 && newEd < oldSt) --page;
	if(orientation == -1 && newSt < oldEd) ++page;
	if(orientation == -1 && newEd > oldSt) --page; 
	g_pagerContainer.page = page;

	// display pager bar 
	g_pagerContainer.bar.empty();

	var previous = new PagerButton(g_pagerContainer, page - 1);
	var btnPrevious = $("<button>", {
		text: "上一頁"
	}).appendTo(g_pagerContainer.bar);
	btnPrevious.on("click", previous, onPagerButtonClickedAdmin);

	var next = new PagerButton(g_pagerContainer, page + 1);
	var btnNext = $("<button>", {
		text: "下一頁"
	}).appendTo(g_pagerContainer.bar);
	btnNext.on("click", next, onPagerButtonClickedAdmin);
} 

function onQueryActivitiesErrorAdmin(xhr, status, err){

}

// Generators
function generateActivityCellForAdmin(par, activity){

	var arrayStatusName = ['created','pending','rejected','accepted','expired'];

        var coverImageUrl=null;

        if(activity.images !=null) {
            for(var key in activity.images){
               var image = activity.images[key];
               coverImageUrl = image.url;
               break;
            }
        }

	var ret=$('<p>', {
		style: "display: block"	
	}).appendTo(par);

	var infoWrap=$('<span>', {
		style: "margin-left: 5pt; display: inline-block;"	
	}).appendTo(ret);

	if(coverImageUrl != null){
		var coverImage=$('<img>', {
			class: g_classActivityCoverImage,
			src: coverImageUrl
		}).appendTo(infoWrap);
	}

	var cellActivityTitle = $('<plaintext>', {
		style: "color: black: font-size: 15pt",
		text: activity.title
	}).appendTo(infoWrap);

	var cellActivityContent=$('<plaintext>', {
		style: "color: black; font-size: 15pt",
		text: activity.content
	}).appendTo(infoWrap);

	var statusIndicator=$('<span>', {
		style: "color: red; font-size: 15pt; margin-left: 5pt; display: inline-block",
		text: arrayStatusName[parseInt(activity.status)]
	}).appendTo(ret);

	ret.data(g_keyStatusIndicator, statusIndicator);
	
	var buttonsWrap=$('<span>', {
		style: "margin-left: 5pt; display: inline-block"
	}).appendTo(ret); 

	// this condition is temporarily hard-coded
	if(parseInt(activity.status) != g_statusAccepted){
            var btnAccept=$('<button>', {
		style: " width: 64pt; height: 36pt; font-size: 16pt; color: DarkSlateBlue; margin-left: 5pt; background-color: #aaaaaa;",
                text: 'Accept'
            }).appendTo(buttonsWrap);
            var dAccept = {};
            dAccept[g_keyActivityId] = activity.id;
            btnAccept.on("click", dAccept, onBtnAcceptClicked);
        }

	if(parseInt(activity.status) != g_statusRejected){
            var btnReject=$('<button>', {
		style: " width: 64pt; height: 36pt; font-size: 16pt; color: purple; margin-left: 5pt; background-color: #aaaaaa;",
                text: 'Reject'
            }).appendTo(buttonsWrap);
            var dReject = {};
            dReject[g_keyActivityId] = activity.id;
            btnReject.bind("click", dReject, onBtnRejectClicked);
        }

	var btnDelete=$('<button>', {
		style: " width: 64pt; height: 36pt; font-size: 16pt; color: IndianRed; margin-left: 5pt; background-color: #aaaaaa;",
		text: 'Delete'
	}).appendTo(buttonsWrap);
	var dDelete = {};
	dDelete[g_keyActivityId] = activity.id;
	btnDelete.bind("click", dDelete, onBtnDeleteClicked);
	
	var hr=$('<hr>', {
		style: "height: 1pt; color: black; background-color: black"
	}).appendTo(ret);
	return ret;
}

$(document).ready(function(){
    	// initialize local DOMs
	initTopbar();

	g_onLoginSuccess = queryActivitiesAndRefresh;
	g_onLoginError = null;
	g_onEnter = queryActivitiesAndRefresh;
	initActivityEditor();

	var pagerCache = new PagerCache(5);
	// initialize pager widgets
	g_pager = new Pager($("#pager-screen-activities"), $("#pager-bar-activities"), g_keyActivityId, g_orderDescend, g_numItemsPerPage, pagerCache, null);		
	g_pagerContainer.status = g_statusPending;

	g_selectFilter = $("#select-filter");
	g_selectFilter.on("change", function() {
		g_pagerContainer.status = $(this).val();
		queryActivitiesAndRefresh();
	});

	checkLoginStatus();
});
