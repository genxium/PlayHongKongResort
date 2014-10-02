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

function onBtnDetailClicked(evt){
        evt.preventDefault();
        var data = evt.data;
        var activityId = data[g_keyActivityId];

	var detailPagePath = "/activity/detail/show?" + g_keyActivityId + "=" + activityId;
	window.open(detailPagePath);
}

// Generators

function generateActivityCell(par, activity){

	var arrayStatusName = ["created", "pending", "rejected", "accepted", "expired"];

	var coverImageUrl = null;
	if(activity.images != null) {
            for(var key in activity.images){
               var img = activity.images[key];
               coverImageUrl = img.url;
               break;
            }
	}

	var statusStr = arrayStatusName[activity.status];

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

		if(activity.status == g_statusCreated || activity.status == g_statusRejected){
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

	attachJoinButton(btnRow, activity);

	var btnDetail = $('<button>', {
		class: g_classBtnDetail,
		text: 'Detail'
	}).appendTo(btnRow);
	var dDetail = {};
	dDetail[g_keyActivityId] = activity.id;
	btnDetail.on("click", dDetail, onBtnDetailClicked);

    displayTimesTable(ret, activity);

    var participantsRow = $("<p>", {
        style: "margin-top: 2px"
    }).appendTo(ret);

    var spanSelected = $("<span>", {
        text: activity.numSelected.toString() + " selected",
        style: "color: PaleVioletRed"
    }).appendTo(participantsRow);

    var spanSlash = $("<span>", {
        text: " / "
    }).appendTo(participantsRow);

    var spanApplied = $("<span>", {
        text: (activity.numApplied + activity.numSelected).toString() + " applied", // display the total number of applied users including the selected ones
        style: "color: purple"
    }).appendTo(participantsRow);

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
