var g_vieweeId = null; // should always be null except in profile's page
var g_pagerActivity = null;
var g_onJoined = null;

function onBtnEditClicked(evt){
	evt.preventDefault();
	var data = evt.data;
	var activity = data[g_keyActivity];
	if (!g_activityEditor) return;
	g_activityEditor.refresh(activity);
	g_activityEditor.show();
}

function listActivities(page, onSuccess, onError) {
	// prototypes: onSuccess(data), onError(err)
	var params = generateActivitiesListParams(g_pagerActivity, page);

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
	if (!page) return null;

	var params = {};
	
	if (!(!g_vieweeId))	params[g_keyVieweeId] = g_vieweeId;
	var pageSt = page - 2;
	var pageEd = page + 2;
	var offset = pageSt < 1 ? (pageSt - 1) : 0;
	pageSt -= offset;
	pageEd -= offset;
	params[g_keyPageSt] = pageSt;
	params[g_keyPageEd] = pageEd;
	if (!(!pager.nItems)) params[g_keyNumItems] = pager.nItems;
	if (!(!g_vieweeId)) params[g_keyVieweeId] = g_vieweeId;

	if (!(!pager.filterList)) {
		for (var i = 0; i < pager.filterList.length; ++i) {
			var filter = pager.filterList[i];
			params[filter.key] = filter.selector.val();	
		}
	}

	if (!params.hasOwnProperty(g_keyOrientation)) params[g_keyOrientation] = g_orderDescend;

	var token = getToken();
	if (!(!token))	params[g_keyToken] = token;
	return params;
}

function onListActivitiesSuccess(data){
	g_pagerActivity.refreshScreen(data);
} 

function onListActivitiesError(err){

}

function displayTimesTable(par, activity) {
	// deadline and begin time
	var deadlineRow = $("<div>", {
		"class": "time-table dealine clearfix"
	}).appendTo(par);
	var deadlineTitle = $("<div>", {
		text: TITLES.deadline,
		"class": "time-label left label-active-alpha"
	}).appendTo(deadlineRow);
	var deadline = $("<div>", {
		text: gmtMiilisecToLocalYmdhis(activity.applicationDeadline),
		"class": "time-detail left"
	}).appendTo(deadlineRow);
	if (activity.isDeadlineExpired()) {
		deadlineTitle.addClass("label-disabled");
	}

	var beginTimeRow = $("<div>", {
		"class": "time-table begin clearfix"
	}).appendTo(par);
	var beginTimeTitle = $("<div>", {
		text: TITLES.begin_time,
		"class": "time-label left label-active-beta"
	}).appendTo(beginTimeRow);
	var beginTime = $("<div>", {
		text: gmtMiilisecToLocalYmdhis(activity.beginTime),
		"class": "time-detail left"
	}).appendTo(beginTimeRow);

	if (activity.hasBegun()) {
		beginTimeTitle.addClass("label-disabled");
	} 
}

function displayParticipantStatistics(par, activity) {
	var attend = $("<ul>", {
		"class": "clearfix"
	}).appendTo(par);
	var spanSelected = $("<li>", {
		text: activity.numSelected.toString() + " " + TITLES.selected,
		"class": "selected left"
	}).appendTo(attend);

	var spanApplied = $("<li>", {
		text: (activity.numApplied + activity.numSelected).toString() + " " + TITLES.applied, // display the total number of applied players including the selected ones
		"class": "applied left"
	}).appendTo(attend);

}

function onBtnJoinClicked(evt){

	var btnJoin = $(this);

	evt.preventDefault();
	var activity = evt.data;

	if (activity.isDeadlineExpired()) {
		alert(ALERTS.deadline_expired);
		return;
	}

	// prevent number limit violation
	if (activity.numApplied >= g_maxApplied) {
		alert(ALERTS.applicant_num_exceeded);
		return;
	} 

	var token = getToken();

	var params={};
	params[g_keyActivityId] = activity.id;
	params[g_keyToken] = token;

	var aButton = $(evt.srcElement ? evt.srcElement : evt.target);
	disableField(aButton);
	$.ajax({
		type: "POST",
		// url: "/el/activity/join",
		url: "/activity/join",
		data: params,
		success: function(data, status, xhr){
			enableField(aButton);
			if (isTokenExpired(data)) {
				logout(null);
				return;
			}
			if (isApplicantLimitExceeded(data)) {
				alert(ALERTS.applicant_num_exceeded);
				return;
			}
			if (!isStandardSuccess(data)) return;
			if (!g_onJoined) return;
			g_onJoined(activity.id);
		},
		error: function(xhr, status, err){
			enableField(aButton);
		}
	});
}

function attachJoinButton(par, activity) {

	if(!activity.relation && !activity.isDeadlineExpired()){
		var btnJoin = $('<button>', {
			"class": "btn-join right positive-button",
			text: TITLES.join
		}).appendTo(par);
		btnJoin.click(activity, onBtnJoinClicked);
	} else {
		attachRelationIndicator(par, activity, false);
	}

}

function attachRelationIndicator(par, activity, inListCell) {

	if(!activity.relation || !g_loggedInPlayer || g_loggedInPlayer.id == activity.host.id) return;

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
	// NOTE: when activity.status === 0, using (!activity.status) to check field-existence fails 
	if(activity.status === null || activity.status === undefined) return;

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
	// TODO: refactor by BaseWidget

	var coverImageUrl = null;
	if(!(!activity.images)) {
            for(var key in activity.images){
               var img = activity.images[key];
               coverImageUrl = img.url;
               break;
            }
	}

	var ret = null; 
	
	if (activity.priority > 0) ret = $("<div>", {
		"class": "cell-container clearfix prioritized-cell"
	}).appendTo(par); 
	else ret = $("<div>", {
		"class": "cell-container clearfix non-prioritized-cell"
	}).appendTo(par);

	var left = $("<div>", {
		"class": "activity-cover left"
	}).appendTo(ret);
	var helper = $("<span>", {
		"class": "image-helper"
	}).appendTo(left);
	if(!(!coverImageUrl)){
		//setBackgroundImageDefault(left, coverImageUrl);
		var cover = $("<img>", {
			src: coverImageUrl
		}).appendTo(left);
	}

	var middle = $("<div>", {
		"class": "activity-info left"
	}).appendTo(ret);
	var title = $("<p>", {
		"class": "activity-title truncate title-alpha",
		text: activity.title
	}).appendTo(middle);
	
	var addr = $("<p>", {
		"class": "activity-addr truncate title-beta",
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
	if (!(!activity.selectedParticipants)) {
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

	var right = $("<div>", {
		"class": "activity-action right"
	}).appendTo(ret);

	var btnDetailMiddle = $('<button>', {
		"class": "activity-detail positive-button",
		text: TITLES.view
	}).appendTo(right).click(activity, function(evt){
		evt.preventDefault();
		var act = evt.data;
		window.location.hash = ("detail?" + g_keyActivityId + "=" + act.id);
	});

	attachStatusIndicator(right, activity);
	attachRelationIndicator(right, activity, true);
}
