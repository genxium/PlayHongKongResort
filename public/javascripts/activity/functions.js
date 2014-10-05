var g_vieweeId = null; // should always be null except in profile's page
var g_activitiesFilter = null;
var g_activitiesSorter = null;
var g_pagerContainer = null;

function queryActivities(refIndex, numItems, orientation, direction, vieweeId, relation, status, onSuccess, onError) {
    // prototypes: onSuccess(data), onError
	var params = {};
	if(refIndex != null) params[g_keyRefIndex] = refIndex.toString();
	if(numItems != null) params[g_keyNumItems] = numItems;
	if(orientation != null) params[g_keyOrientation] = parseInt(orientation);
	if(direction != null) params[g_keyDirection] = parseInt(direction);
	if(vieweeId != null) params[g_keyVieweeId] = vieweeId;
	if(relation != null) params[g_keyRelation] = relation;
	if(status != null) params[g_keyStatus] = status;

	var token = $.cookie(g_keyToken);
	if(token != null)	params[g_keyToken] = token;

	$.ajax({
		type: "GET",
		url: "/activity/query",
		data: params,
		success: function(data, status, xhr) {
		    onSuccess(data);
		},
		error: function(xhr, status, err) {
		    onError();
		}
	});
}

function generateActivitiesQueryParams(container, page) {
    if (page == container.page) return null;
    var direction = page > container.page ? g_directionForward : g_directionBackward;
    var refIndex = page > container.page ? g_pagerContainer.ed : g_pagerContainer.st;

    var params = {};
    if(refIndex != null) params[g_keyRefIndex] = refIndex.toString();
    if(container.nItems != null) params[g_keyNumItems] = container.nItems;
    if(container.orientation != null) params[g_keyOrientation] = container.orientation;
    if(direction != null) params[g_keyDirection] = direction;
    if(g_vieweeId != null) params[g_keyVieweeId] = g_vieweeId;
    if(container.relation != null) params[g_keyRelation] = container.relation;
    if(container.status != null) params[g_keyStatus] = container.status;

    var token = $.cookie(g_keyToken);
    if(token != null)	params[g_keyToken] = token;
    return params;
}

function onQueryActivitiesSuccess(data){
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
		generateActivityCell(g_pagerContainer.screen, activity);
	}

	createPagerBar(g_pagerContainer, oldSt, oldEd, onQueryActivitiesSuccess, onQueryActivitiesError);
} 

function onQueryActivitiesError(xhr){

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
