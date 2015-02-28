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
		"class": "time-table dealine clearfix"
	}).appendTo(par);
	var deadlineTitle = $("<div>", {
		text: TITLES["deadline"],
		"class": "time-label left"
	}).appendTo(deadlineRow);
	var deadline = $("<div>", {
		text: gmtMiilisecToLocalYmdhis(activity.applicationDeadline),
		"class": "time-detail left"
	}).appendTo(deadlineRow);
	if (activity.isDeadlineExpired()) {
		deadlineRow.addClass("expired");
	}

	var beginTimeRow = $("<div>", {
		"class": "time-table begin clearfix"
	}).appendTo(par);
	var beginTimeTitle = $("<div>", {
		text: TITLES["begin_time"],
		"class": "time-label left"
	}).appendTo(beginTimeRow);
	var beginTime = $("<div>", {
		text: gmtMiilisecToLocalYmdhis(activity.beginTime),
		"class": "time-detail left"
	}).appendTo(beginTimeRow);

	if (activity.hasBegun()) {
		beginTimeRow.addClass("expired");
	} 
}

function displayParticipantStatistics(par, activity) {
	var attend = $("<ul>", {
		"class": "clearfix"
	}).appendTo(par);
	var spanSelected = $("<li>", {
		text: activity.numSelected.toString() + " " + TITLES["selected"],
		"class": "selected left"
	}).appendTo(attend);

	var spanApplied = $("<li>", {
		text: (activity.numApplied + activity.numSelected).toString() + " " + TITLES["applied"], // display the total number of applied users including the selected ones
		"class": "applied left"
	}).appendTo(attend);

}

function onBtnJoinClicked(evt){

	var btnJoin = $(this);

	evt.preventDefault();
	var activity = evt.data;

	if (activity.isDeadlineExpired()) {
		alert(ALERTS["deadline_expired"]);
		return;
	}

	// prevent number limit violation
	if (activity.numApplied >= g_maxApplied) {
		alert(ALERTS["applicant_num_exceeded"]);
		return;
	} 

	var token = $.cookie(g_keyToken).toString();

	var params={};
	params[g_keyActivityId] = activity.id;
	params[g_keyToken] = token;

	var aButton = $(evt.srcElement ? evt.srcElement : evt.target);
	disableField(aButton);
	$.ajax({
		type: "POST",
		url: "/el/activity/join",
		// url: "/activity/join",
		data: params,
		success: function(data, status, xhr){
			enableField(aButton);
			if (isTokenExpired(data)) {
				logout(null);
				return;
			}
			if (isApplicationLimitExceeded(data)) {
				alert(ALERTS["applicant_num_exceeded"]);
				return;
			}
			if (!isStandardSuccess(data)) return;
			if (g_onJoined == null) return;
			g_onJoined(activity.id);
		},
		error: function(xhr, status, err){
			enableField(aButton);
		}
	});
}

function attachJoinButton(par, activity) {

	if(activity.relation == null && !activity.isDeadlineExpired()){
		var btnJoin = $('<button>', {
			"class": "btn-join purple right",
			text: TITLES["join"]
		}).appendTo(par);
		btnJoin.click(activity, onBtnJoinClicked);
	} else {
		attachRelationIndicator(par, activity, false);
	}

}

function attachRelationIndicator(par, activity, inListCell) {

	if(activity.relation == null || g_loggedInUser == null || g_loggedInUser.id == activity.host.id) return;

	var mapRelationName = {};
	mapRelationName[applied] = RELATION_NAMES["applied"];
	mapRelationName[selected] = RELATION_NAMES["selected"];
	mapRelationName[present] = RELATION_NAMES["present"];
	mapRelationName[absent] = RELATION_NAMES["absent"];
	mapRelationName[assessed] = RELATION_NAMES["assessed"];
	mapRelationName[hosted] = RELATION_NAMES["hosted"];
		
	if (inListCell) {
		$("<div>", {
			"class": "activity-cell-relation",
			text: mapRelationName[getPriorRelation(activity)]
		}).appendTo(par);
	} else {
		$("<div>", {
			"class": "activity-detail-relation right",
			text: mapRelationName[getPriorRelation(activity)]
		}).appendTo(par);
	}
}

function attachStatusIndicator(par, activity) {
	if(activity.status == null) return;

	var arrayStatusName = [STATUS_NAMES["created"], STATUS_NAMES["pending"], STATUS_NAMES["rejected"], STATUS_NAMES["accepted"]];
	
	var statusIndicator = $('<div>',{
		"class": "activity-cell-status",
		text: arrayStatusName[activity.status]
	}).appendTo(par);

	if(activity.status != g_statusCreated && activity.status != g_statusRejected) return;
	var btnEdit = $('<button>', {
		"class": 'activity-edit'
	}).appendTo(statusIndicator);
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
		"class": "cell-container clearfix"
	}).appendTo(par);

	var left = $("<div>", {
		"class": "activity-cover left"
	}).appendTo(ret);
	var helper = $("<span>", {
		"class": "image-helper"
	}).appendTo(left);
	if(coverImageUrl != null){
		//setBackgroundImageDefault(left, coverImageUrl);
		var cover = $("<img>", {
			src: coverImageUrl
		}).appendTo(left);
	}

	var middle = $("<div>", {
		"class": "activity-info left"
	}).appendTo(ret);
	var title = $("<p>", {
		"class": "activity-title truncate",
		text: activity.title
	}).appendTo(middle);
	
	var addr = $("<p>", {
		"class": "activity-addr truncate",
		text: activity.address
	}).appendTo(middle);

	displayTimesTable(middle, activity);
	var midBottom = $("<div>", {
		"class": "activity-attend"
	}).appendTo(middle);
	displayParticipantStatistics(midBottom, activity);
	
	var selectedSnippet = $("<div>", {
		"class": "selected-snippet"
	}).appendTo(middle);
	if (activity.selectedParticipants != null) {
		var count = activity.selectedParticipants.length <= 3 ? activity.selectedParticipants.length : 3;
		for (var i = 0; i < count; ++i) {
			var participant = activity.selectedParticipants[i];
			$("<img>", {
				title: participant.name,
				src: participant.avatar,
				"class": "selected-snippet-avatar left"
			}).click(participant, function(evt) {
				evt.preventDefault();
				var aParticipant = evt.data;
				window.location.hash = ("profile?" + g_keyVieweeId + "=" + aParticipant.id.toString());
			}).appendTo(selectedSnippet);
		}
	}

	var rightMiddle = $("<div>", {
		"class": "activity-action-small right"
	}).appendTo(middle);

	var btnDetailMiddle = $('<button>', {
		"class": "activity-detail-small purple right",
	}).appendTo(rightMiddle);

	btnDetailMiddle.click(activity, function(evt){
		evt.preventDefault();
		var act = evt.data;
		window.location.hash = ("detail?" + g_keyActivityId + "=" + act.id.toString());
	});

	attachStatusIndicator(rightMiddle, activity);
	attachRelationIndicator(rightMiddle, activity, true);

	var right = $("<div>", {
		"class": "activity-action right"
	}).appendTo(ret);

	var btnDetail = $('<button>', {
		"class": "activity-detail purple",
		text: TITLES["view"]
	}).appendTo(right);
	
	btnDetail.click(activity, function(evt){
		evt.preventDefault();
		var act = evt.data;
		window.location.hash = ("detail?" + g_keyActivityId + "=" + act.id.toString());
	});

	attachStatusIndicator(right, activity);
	attachRelationIndicator(right, activity, true);
}
