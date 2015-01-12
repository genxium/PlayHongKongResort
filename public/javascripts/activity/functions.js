var g_vieweeId = null; // should always be null except in profile's page
var g_pager = null;
var g_onJoined = null;

function onBtnEditClicked(evt){
    	evt.preventDefault();
    	var data = evt.data;
        var activity = data[g_keyActivity];
	showActivityEditor(activity);
}

function listActivities(page, onSuccess, onError) {
	// prototypes: onSuccess(data), onError(err)
	var params = generateActivitiesListParams(g_pager, page);

	$.ajax({
		type: "GET",
		url: "/activity/list",
		data: params,
		success: function(data, status, xhr) {
		    onSuccess(data);
		},
		error: function(xhr, status, err) {
		    onError(err);
		}
	});
}

function listActivitiesAndRefresh() {
	var page = 1;
	listActivities(page, onListActivitiesSuccess, onListActivitiesError);
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

function onListActivitiesError(err){

}

function displayTimesTable(par, activity) {
    	// deadline and begin time
    	var deadlineRow = $("<div>", {
		class: "table-time-dealine"
	}).appendTo(par);
    	var deadlineTitle = $("<div>", {
    		text: "Application Deadline",
			class: "label"
    	}).appendTo(deadlineRow);
    	var deadline = $("<div>", {
    		text: gmtMiilisecToLocalYmdhis(activity.applicationDeadline),
			class: "detail"
    	}).appendTo(deadlineRow);
	if (activity.isDeadlineExpired()) {
		//stencilize(deadlineTitle);
		//stencilize(deadline);
		deadlineRow.addClass("expired");
	}

    	var beginTimeRow = $("<div>", {
		class: "table-time-begin"
	}).appendTo(par);
    	var beginTimeTitle = $("<div>", {
    		text: "Begin Time",
    		class: "label"
    	}).appendTo(beginTimeRow);
    	var beginTime = $("<td>", {
    		text: gmtMiilisecToLocalYmdhis(activity.beginTime),
    		class: "detail"
    	}).appendTo(beginTimeRow);

	if (activity.hasBegun()) {
		//stencilize(beginTimeTitle);
		//stencilize(beginTime);
		beginTimeRow.addClass("expired");
	} 
}

function displayParticipantStatistics(par, activity) {

	var spanSelected = $("<div>", {
		text: activity.numSelected.toString() + " selected"
	}).appendTo(par);

	var spanApplied = $("<span>", {
		text: (activity.numApplied + activity.numSelected).toString() + " applied" // display the total number of applied users including the selected ones
	}).appendTo(par);

}

function onBtnJoinClicked(evt){

	var btnJoin = $(this);

	evt.preventDefault();
	var activity = evt.data;

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
		url: "/el/activity/join",
		data: params,
		success: function(data, status, xhr){
			if (!isStandardSuccess(data)) return;
			if (g_onJoined == null) return;
			g_onJoined(activity.id);
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
		btnJoin.click(activity, onBtnJoinClicked);
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
		style: "color: violet; font-size: 13pt;",
		text: mapRelationName[getPriorRelation(activity)]
	}).appendTo(par);

}

function attachStatusIndicator(par, activity) {
	if(activity.status == null) return;

	var arrayStatusName = ["created", "pending", "rejected", "accepted", "expired"];

	var statusIndicator = $('<span>',{
		style: "color: red; margin-left: 10pt; font-size: 13pt; vertical-align: center;",
		text: arrayStatusName[activity.status]
	}).appendTo(par);

	if(activity.status != g_statusCreated && activity.status != g_statusRejected) return;
	var btnWrapper = $("<span>").appendTo(par);
	var btnEdit = $('<button>', {
		style: "background-color: white; width: 30px; height: 30px; margin-left: 10px"
	}).appendTo(btnWrapper);
	setBackgroundImageDefault(btnEdit, "/assets/icons/edit.png");
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

function generateActivityCell(par, activity){

	var coverImageUrl = null;
	if(activity.images != null) {
            for(var key in activity.images){
               var img = activity.images[key];
               coverImageUrl = img.url;
               break;
            }
	}

	var ret = $("<div>", {
		class: "cell-container clearfix"
	}).appendTo(par);

	var left = $("<div>", {
		class: "activity-cover left"
	}).appendTo(ret);
	if(coverImageUrl != null){
		//setBackgroundImageDefault(left, coverImageUrl);
		var cover = $("<img>", {
			src: coverImageUrl
		}).appendTo(left);
	}

	var middle = $("<div>", {
		class: "activity-info left"
	}).appendTo(ret);
	var title = $("<p>", {
		class: "activity-title",
		text: activity.title
	}).appendTo(middle);
	attachRelationIndicator(title, activity);
	displayTimesTable(middle, activity);
	var midBottom = $("<div>", {
		class: "activity-attend"
	}).appendTo(middle);
	displayParticipantStatistics(midBottom, activity);

	var right = $("<div>", {
		class: "activity-action left"
	}).appendTo(ret);

	var btnDetail = $('<button>', {
		class: "purple",
		text: "Go"
	}).appendTo(right);
	var dDetail = {};
	btnDetail.click(activity, function(evt){
		evt.preventDefault();
		var act = evt.data;
		window.location.hash = (g_keyActivityId + "=" + act.id.toString());
	});
	attachStatusIndicator(right, activity);
}
