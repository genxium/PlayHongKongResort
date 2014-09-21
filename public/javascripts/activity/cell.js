/*
 * variables
 */

// general dom elements
var g_classCellActivityContainer="cell-container";
var g_classCellActivityTitle="cell-title";
var g_classCellActivityContent="cell-content";

var g_classActivityCoverImage="cell-cover";

var g_classCellRelationIndicator="cell-relation-indicator";

// button keys
var g_classBtnJoin = "btn-join";
var g_classBtnDetail = "btn-detail";

// Assistant Handlers
function onBtnEditClicked(evt){
    	evt.preventDefault();
    	var data = evt.data;
        var activity = data[g_keyActivity];
	showActivityEditor(activity);
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
	params[g_keyActivityId] = data[g_keyActivityId];
	params[g_keyToken] = token;

	$.ajax({
		type: "POST",
		url: "/activity/join",
		data: params,
		success: function(data, status, xhr){
			var cell = btnJoin.parent();
			btnJoin.remove();

			$('<div>', {
				class: g_classCellRelationIndicator,
				text: 'Applied'
			}).appendTo(cell);
		},
		error: function(xhr, status, errThrown){

		}
	});
}

function onBtnDetailClicked(evt){
        evt.preventDefault();
        var data = evt.data;
        var activityId = data[g_keyActivityId];

	var detailPagePath="/activity/detail/show?"+g_keyActivityId+"="+activityId;
	window.open(detailPagePath);
}

// Generators

function generateActivityCell(par, activity){

	var arrayStatusName = ['created', 'pending', 'rejected', 'accepted', 'expired'];

	var coverImageUrl = null;
	if(activity.images != null) {
            for(var key in activity.images){
               var img = activity.images[key];
               coverImageUrl = img.url;
               break;
            }
	}

	var statusStr = arrayStatusName[parseInt(activity.status)];

	var ret = $('<div>', {
		class: g_classCellActivityContainer
	}).appendTo(par);

	var titleRow = $("<p>", {
		style: "width: 100%; padding-bottom: 3pt; border-bottom: 1px solid #00ccff"
	}).appendTo(ret);

	var cellActivityTitle = $('<span>', {	
		class: g_classCellActivityTitle,
		text: activity.title
	}).appendTo(titleRow);

	if(activity.status != null){

		var statusIndicator = $('<span>',{
		    style: "color: red; font-size: 12pt; clear: left; float: right; text-align: right; vertical-align: center",
		    text: statusStr
		}).appendTo(titleRow);

		if(parseInt(activity.status) == g_statusCreated){
			var btnWrapper = $("<span>").appendTo(titleRow);
			// this condition is temporarily hard-coded
			var btnEdit = $('<button>', {
				class: g_classBtnEdit,
				text: 'Edit'
			}).appendTo(btnWrapper);
			var dEdit = {};
			dEdit[g_keyActivity] = activity;
			btnEdit.on("click", dEdit, onBtnEditClicked);
		}
	}

	var btnRow = $("<p>", {
		style: "margin-top: 5pt; clear: left"
	}).appendTo(ret);

	if(activity.relation == null && !activity.isDeadlineExpired()){
		var btnJoin = $('<button>', {
			class: g_classBtnJoin,
			text: 'Join'
		}).appendTo(btnRow);
		var dJoin = {};
		dJoin[g_keyActivityId] = activity.id;
		dJoin[g_keyActivity] = activity;
		btnJoin.on("click", dJoin, onBtnJoinClicked);

	} else if((activity.relation & applied) > 0
	            && (g_loggedInUser != null && g_loggedInUser.id != activity.host.id)) {
		
		var appliedIndicator = $('<div>', {
			class: g_classCellRelationIndicator,
			text: 'Applied'
		}).appendTo(btnRow);
	} else;

	var btnDetail = $('<button>', {
		class: g_classBtnDetail,
		text: 'Detail'
	}).appendTo(btnRow);
	var dDetail = {};
	dDetail[g_keyActivityId] = activity.id;
	btnDetail.on("click", dDetail, onBtnDetailClicked);

        displayTimesTable(ret, activity);

	if(coverImageUrl != null){
		var imgRow = $("<p>", {
			style: "height: 100%"
		}).appendTo(ret);
		var coverImage=$('<img>', {
			class: g_classActivityCoverImage,
			src: coverImageUrl
		}).appendTo(imgRow);
	}
}
