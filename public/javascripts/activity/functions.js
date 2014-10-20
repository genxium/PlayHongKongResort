var g_vieweeId = null; // should always be null except in profile's page
var g_pager = null;

function onBtnEditClicked(evt){
    	evt.preventDefault();
    	var data = evt.data;
        var activity = data[g_keyActivity];
	showActivityEditor(activity);
}

function listActivities(page, onSuccess, onError) {
	// prototypes: onSuccess(data), onError
	var params = generateActivitiesListParams(g_pager, page);

	$.ajax({
		type: "GET",
		url: "/activity/list",
		data: params,
		success: function(data, status, xhr) {
		    onSuccess(data);
		},
		error: function(xhr, status, err) {
		    onError();
		}
	});
}

function generateActivitiesListParams(pager, page) {
	if (page == null) return null;

	var params = {};
	
	if (g_vieweeId != null)	params[g_keyVieweeId] = g_vieweeId;
	var pageSt = page - 2;
	var pageEd = page + 2;
	var offset = pageSt < 1 ? (pageSt - 1) : 0;
	pageSt -= offset;
	pageEd -= offset;
	params[g_keyPageSt] = pageSt;
	params[g_keyPageEd] = pageEd;
	if (pager.nItems != null) params[g_keyNumItems] = pager.nItems;
	if (g_vieweeId != null) params[g_keyVieweeId] = g_vieweeId;

	if (pager.filters != null) {
		for (var i = 0; i < pager.filters.length; ++i) {
			var filter = pager.filters[i];
			params[filter.key] = filter.selector.val();	
		}
	}

	if (!params.hasOwnProperty(g_keyOrientation)) params[g_keyOrientation] = g_orderDescend;
	if (!params.hasOwnProperty(g_keyRelation) && !params.hasOwnProperty(g_keyStatus)) params[g_keyStatus] = g_statusAccepted;

	var token = $.cookie(g_keyToken);
	if (token != null)	params[g_keyToken] = token;
	return params;
}

function onListActivitiesSuccess(data){
	var jsonResponse = JSON.parse(data);
	if(jsonResponse == null) return;

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
			generateActivityCell(g_pager.screen, activity);
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

function onListActivitiesError(xhr){

}

function displayTimesTable(par, activity) {
    	// deadline and begin time
    	var times = $("<table border='1'>").appendTo(par);
    	var deadlineRow = $('<tr>').appendTo(times);
    	var deadlineTitle = $('<td>', {
    		text: "Application Deadline",
    		style: "padding-left: 5pt; padding-right: 5pt"
    	}).appendTo(deadlineRow);
    	var deadline = $('<td>', {
    		text: activity.applicationDeadline.toString(),
    		style: "color: red; padding-left: 8pt; padding-right: 5pt"
    	}).appendTo(deadlineRow);

    	var beginTimeRow = $('<tr>').appendTo(times);
    	var beginTimeTitle = $('<td>', {
    		text: "Begin Time",
    		style: "padding-left: 5pt; padding-right: 5pt"
    	}).appendTo(beginTimeRow);
    	var beginTime = $('<td>', {
    		text: activity.beginTime.toString(),
    		style: "color: blue; padding-left: 8pt; padding-right: 5pt"
    	}).appendTo(beginTimeRow);
}

function displayParticipantStatistics(par, activity) {

	var spanSelected = $("<span>", {
		text: activity.numSelected.toString() + " selected",
		style: "color: PaleVioletRed"
	}).appendTo(par);

	var spanSlash = $("<span>", {
		text: " / "
	}).appendTo(par);

	var spanApplied = $("<span>", {
		text: (activity.numApplied + activity.numSelected).toString() + " applied", // display the total number of applied users including the selected ones
		style: "color: purple"
	}).appendTo(par);

}

function onBtnJoinClicked(evt){

	var btnJoin = $(this);

	evt.preventDefault();
	var data = evt.data;
	var activity = data[g_keyActivity];

	if(activity.isDeadlineExpired()) {
		alert("Application deadline has expired!");
		return;
	}

	var token = $.cookie(g_keyToken).toString();

	var params={};
	params[g_keyActivityId] = activity.id;
	params[g_keyToken] = token;

	$.ajax({
		type: "POST",
		url: "/activity/join",
		data: params,
		success: function(data, status, xhr){
			var par = btnJoin.parent();
			btnJoin.remove();
			activity.relation |= selected;
			$('<div>', {
				class: "indicator-relation",
				text: 'applied'
			}).appendTo(par);
		},
		error: function(xhr, status, errThrown){

		}
	});
}

function attachJoinButton(par, activity) {

	if(activity.relation == null && !activity.isDeadlineExpired()){
		var btnJoin = $('<button>', {
			class: "btn-join",
			text: 'Join'
		}).appendTo(par);
                setDimensions(btnJoin, "33%", null);
		var dJoin = {};
		dJoin[g_keyActivity] = activity;
		btnJoin.click(dJoin, onBtnJoinClicked);
	} else {
		attachRelationIndicator(par, activity);
	}

}

function attachRelationIndicator(par, activity) {

	if(activity.relation == null || g_loggedInUser == null || g_loggedInUser.id == activity.host.id) return;

	var mapRelationName = {};
	mapRelationName[applied] = "applied";
	mapRelationName[selected] = "selected";
	mapRelationName[present] = "present";
	mapRelationName[absent] = "absent";
	mapRelationName[assessed] = "assessed";
	mapRelationName[hosted] = "";
		
	var relationIndicator = $('<span>', {
		style: "color: violet; margin-left: 10pt; font-size: 12pt; text-align: right; vertical-align: center;",
		text: mapRelationName[getPriorRelation(activity)]
	}).appendTo(par);

}

function attachStatusIndicator(par, activity) {
	if(activity.status == null) return;

	var arrayStatusName = ["created", "pending", "rejected", "accepted", "expired"];

	var statusIndicator = $('<span>',{
		style: "color: red; margin-left: 10pt; font-size: 12pt; text-align: right; vertical-align: center;",
		text: arrayStatusName[activity.status]
	}).appendTo(par);

	if(activity.status != g_statusCreated && activity.status != g_statusRejected) return;
	var btnWrapper = $("<span>").appendTo(par);
	var btnEdit = $('<button>', {
		class: "btn-edit",
		text: 'Edit'
	}).appendTo(btnWrapper);
	var dEdit = {};
	dEdit[g_keyActivity] = activity;
	btnEdit.click(dEdit, onBtnEditClicked);
}

function getPriorRelation(activity) {
	if ((activity.relation & assessed) > 0) return assessed;
	if ((activity.relation & present) > 0) return present;
	if ((activity.relation & absent) > 0) return absent;
	if ((activity.relation & selected) > 0) return selected;
	if ((activity.relation & applied) > 0) return applied;
}
