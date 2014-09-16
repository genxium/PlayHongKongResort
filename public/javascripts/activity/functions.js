var g_vieweeId = null; // should always be null except in profile's page
var g_activitiesFilter = null;
var g_activitiesSorter = null;
var g_pagerContainer = null;

function queryActivities(refIndex, numItems, orientation, direction, vieweeId, relation, status, onSuccess, onError){
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
		success: onSuccess,
		error: onError 
	});
}

function onPagerButtonClicked(evt) {
	var button = evt.data;
	var container = button.container;
	var page = button.page;
	if (page == container.page) return;
	var direction = page > container.page ? g_directionForward : g_directionBackward;
	var refIndex = page > container.page ? g_pagerContainer.ed : g_pagerContainer.st;

	queryActivities(refIndex, container.nItems, container.orientation, direction, g_vieweeId, container.relation, container.status, onQueryActivitiesSuccess, onQueryActivitiesError);
}

function onQueryActivitiesSuccess(data, status, xhr){
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
	btnPrevious.on("click", previous, onPagerButtonClicked);

	var next = new PagerButton(g_pagerContainer, page + 1);
	var btnNext = $("<button>", {
		text: "下一頁"
	}).appendTo(g_pagerContainer.bar);
	btnNext.on("click", next, onPagerButtonClicked);
} 

function onQueryActivitiesError(xhr, status, err){

}
