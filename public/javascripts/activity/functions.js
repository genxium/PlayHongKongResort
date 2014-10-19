var g_vieweeId = null; // should always be null except in profile's page
var g_pager = null;

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
	params[g_keyPageSt] = page - 1 > 0 ? page -1 : 1;
	params[g_keyPageEd] = page + 1;
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
    	var times = $("<table border='1'>", {
    		style: "margin-bottom: 5pt"
    	}).appendTo(par);
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

function getPriorRelation(activity) {
	if ((activity.relation & assessed) > 0) return assessed;
	if ((activity.relation & present) > 0) return present;
	if ((activity.relation & absent) > 0) return absent;
	if ((activity.relation & selected) > 0) return selected;
	if ((activity.relation & applied) > 0) return applied;
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
				class: g_classCellRelationIndicator,
				text: 'applied'
			}).appendTo(par);
		},
		error: function(xhr, status, errThrown){

		}
	});
}

function attachJoinButton(par, activity) {

	var mapRelationName = {};
	mapRelationName[applied] = "applied";
	mapRelationName[selected] = "selected";
	mapRelationName[present] = "present";
	mapRelationName[absent] = "absent";
	mapRelationName[assessed] = "assessed";
	mapRelationName[hosted] = "";

	if(activity.relation == null && !activity.isDeadlineExpired()){
		var btnJoin = $('<button>', {
			class: g_classBtnJoin,
			text: 'Join'
		}).appendTo(par);
		var dJoin = {};
		dJoin[g_keyActivity] = activity;
		btnJoin.on("click", dJoin, onBtnJoinClicked);

	} else if(activity.relation != null && g_loggedInUser != null && g_loggedInUser.id != activity.host.id) {
		
		var relationIndicator = $('<div>', {
			class: g_classCellRelationIndicator,
			text: mapRelationName[getPriorRelation(activity)]
		}).appendTo(par);

	} else;

}
