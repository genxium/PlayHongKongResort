// general dom elements
var g_sectionActivity = null;
var g_tabParticipants = null;
var g_tabComments = null;
var g_tabAssessments = null;

// local variables
var g_activityId = null;
var g_activity = null;
var g_participantsForm = null;

var g_aliasApplied = 0;
var g_aliasSelected = 1;

function ParticipantsForm(labels, participantsId, participantsStatus){
	this.boxes = null;
	this.participantsId = participantsId;
	this.labels = labels;
	this.participantsStatus = participantsStatus;
	this.setBoxes = function(boxes){
		this.boxes = boxes;
	}
}

// Assistive Functions
function refreshOnEnter(){
	queryActivityDetail(g_activityId);
}

function refreshOnLoggedIn(){
	queryActivityDetail(g_activityId);
}

function queryActivityDetail(activityId){

		var token = $.cookie(g_keyToken);
    	var params = {};
    	params[g_keyActivityId] = activityId;
        if(token != null)	params[g_keyToken] = token;

        $.ajax({
            method: "GET",
            url: "/activity/detail",
            data: params,
            success: function(data, status, xhr){
                var activityJson = JSON.parse(data);
    			g_activity = new Activity(activityJson);
                displayActivityDetail(g_sectionActivity);
            },
            error: function(xhr, status, err){

            }
        });
}

function displayActivityDetail(par){
	par.empty();
	var ret=$('<div>').appendTo(par);

	var title=$('<p>',{
		text: g_activity.title.toString(),
		style: "font-size: 18pt; color: blue"
	}).appendTo(ret);

	// deadline and begin time
	var times=$("<table border='1'>", {
		style: "margin-bottom: 5pt"
	}).appendTo(ret);
	var deadlineRow = $('<tr>').appendTo(times);
	var deadlineTitle = $('<td>', {
		text: "Application Deadline",
		style: "padding-left: 5pt; padding-right: 5pt"
	}).appendTo(deadlineRow);
	var deadline = $('<td>', {
		text: g_activity.applicationDeadline.toString(),
		style: "color: red; padding-left: 8pt; padding-right: 5pt"
	}).appendTo(deadlineRow);

	var beginTimeRow = $('<tr>').appendTo(times);
	var beginTimeTitle = $('<td>', {
		text: "Begin Time",
		style: "padding-left: 5pt; padding-right: 5pt"
	}).appendTo(beginTimeRow);
	var beginTime = $('<td>', {
		text: g_activity.beginTime.toString(),
		style: "color: blue; padding-left: 8pt; padding-right: 5pt"
	}).appendTo(beginTimeRow);

	if(g_activity.host.id != null && g_activity.host.name != null){
		var d = $('<div>', {
			style: "margin-top: 2pt; margin-bottom: 3pt"
		}).appendTo(ret);
		var sp1 = $('<span>', {
			text: "--by",
			style: "margin-left: 20%; font-size : 14pt"
		}).appendTo(d);
		var sp2 = $('<span>', {
			style: "margin-left: 5pt"
		}).appendTo(d);
		var host = $('<a>', {
			href: '/user/profile/show?'+g_keyUserId+"="+g_activity.host.id.toString(),
			text: "@"+g_activity.host.name,
			style: "font-size: 14pt; font-weight: bold;"
		}).appendTo(sp2);
	}

	var content=$('<div>',{
		text: g_activity.content.toString(),
		style: "font-size: 15pt"
	}).appendTo(ret);

	if(g_activity.images != null) {
		var imagesNode=$('<p>').appendTo(ret);

		for(var i=0;i<g_activity.images.length;++i){
			var imageNode=$('<img>',{
				src: g_activity.images[i].url
			}).appendTo(imagesNode);
		}
	}	
	g_tabParticipants.empty();
	var selectionForm = $('<form>').appendTo(g_tabParticipants);

	var labels = new Array();
	var participantsId = new Array();
	var participantsStatus = new Array();
	for(var i = 0; i < g_activity.selectedParticipants.length; ++i){
		var participant = g_activity.selectedParticipants[i];
		participantsId.push(participant.id);
		participantsStatus.push(g_aliasSelected);
		var label = $('<label>', {
			text: participant.email,
			style: "background-color: aquamarine"
		}).appendTo(selectionForm);
		labels.push(label);
		$('<br>').appendTo(selectionForm);
	}

	for(var i = 0; i < g_activity.appliedParticipants.length; ++i){
		var participant = g_activity.appliedParticipants[i];
		participantsId.push(participant.id);
		participantsStatus.push(g_aliasApplied);
		var label=$('<label>', {
			text: participant.email,
			style: "background-color: pink"
		}).appendTo(selectionForm);
		labels.push(label);
		$('<br>').appendTo(selectionForm);
	}

	g_participantsForm = new ParticipantsForm(labels, participantsId, participantsStatus);

	var params={};
	params[g_keyActivityId] = g_activity.id;
	params[g_keyRefIndex] = 0;
	params[g_keyNumItems] = 20;
	params[g_keyDirection] = 1;

	var onSuccess=function(data, status, xhr){
		g_tabComments.empty();
		var jsonResponse=JSON.parse(data);
		if(jsonResponse == null || Object.keys(jsonResponse).length == 0) return;
		for(var key in jsonResponse){
			var commentJson = jsonResponse[key];
			generateCommentCell(g_tabComments, commentJson, g_activity.id).appendTo(g_tabComments);
			$('<br>').appendTo(g_tabComments);
		}
	};
	var onError = function(xhr, status, err){};
	queryComments(params, onSuccess, onError);

	var viewer = null;
	if(g_activity.hasOwnProperty("viewer")) viewer = g_activity.viewer;
	var batchAssessmentEditor = generateBatchAssessmentEditor(g_tabAssessments, g_activity, queryActivityDetail);

	var token = $.cookie(g_keyToken);
	if(token == null) return;

	var params = {};
	params[g_keyToken] = token;
	params[g_keyActivityId] = g_activity.id;

	$.ajax({
		type: "GET",
		url: "/activity/ownership",
		data: params,
		success: function(data, status, xhr){
			if(data == null || data == "") return;
			var boxes = new Array();
			for(var i = 0; i < g_participantsForm.labels.length; i++){
				var label = g_participantsForm.labels[i];
				var participantId = g_participantsForm.participantsId[i];
				var checkStatus = null;
				if(g_participantsForm.participantsStatus[i] == g_aliasSelected)	checkStatus = true;
				else	checkStatus = false;
				var checkbox = $('<input>',{
					type: "checkbox",
					checked: checkStatus
				}).appendTo(label);
				boxes.push(checkbox);
				if(participantId == g_activity.host.id) checkbox.hide();
			}
			g_participantsForm.setBoxes(boxes);
			if(boxes.length > 0){
				var btnSubmit=$('<button>',{
					text: 'Confirm Selection',
					style: 'color: white; background-color:black; font-size: 13pt'
				}).appendTo(selectionForm);
				btnSubmit.on("click", onBtnSubmitClicked);
			}
			$('<hr>').appendTo(selectionForm);
		},
		error: function(xhr, status, errThrown){

	       }
	});

	generateCommentEditor(ret, g_activity.id);

	return ret;
}

// Callback Functions
function onParticipantsSelectionFormSubmission(formEvt){
	
	formEvt.preventDefault(); // prevent default action.
	var appliedParticipants = new Array();
	var selectedParticipants = new Array();
	for(var i = 0; i < g_participantsForm.labels.length; i++) {
		var box = g_participantsForm.boxes[i];
		var participantId = g_participantsForm.participantsId[i];
		if(participantId == g_activity.host.id) continue;
		if(box.is(":checked"))	selectedParticipants.push(participantId);
		else	appliedParticipants.push(participantId);
	}
	// append user token and activity id for identity
	var token = $.cookie(g_keyToken);
	if(token == null) return;

	var params={};
	params[g_keyToken] = token;
	params[g_keyActivityId] = g_activityId;
	params[g_keyAppliedParticipants] = JSON.stringify(appliedParticipants);
	params[g_keySelectedParticipants] = JSON.stringify(selectedParticipants);

	$.ajax({
		type: "POST",
		url: "/activity/participants/update",
		data: params,
		success: function(data, status, xhr){
			for(var i = 0; i < g_participantsForm.labels.length; ++i){
			    var label = g_participantsForm.labels[i];
			    var box = g_participantsForm.boxes[i];
			    if(box.is(":checked"))	label.css("background-color", "aquamarine");
			    else	label.css("background-color", "pink");
			}
		},
		error: function(xhr, status, err) {

		}
	});
}

// Assistive Event Handlers
function onBtnSubmitClicked(evt){
	evt.preventDefault();
	var selectionForm=$(this).parent();	
	selectionForm.submit(onParticipantsSelectionFormSubmission);
	selectionForm.submit();
}

// execute on start
$(document).ready(function(){

	g_sectionActivity = $("#section-activity");
	g_tabComments = $("#tab-comments");
	g_tabParticipants = $("#tab-participants");
	g_tabAssessments = $("#tab-assessments");

	var params = extractParams(window.location.href);
	for(var i = 0; i < params.length; i++){
		var param = params[i];
		var pair = param.split("=");
		if(pair[0] == g_keyActivityId){
			g_activityId = pair[1];
			break;
		}
	}
	initTopbar();

	g_onLoginSuccess = refreshOnLoggedIn;
	g_onLoginError = null;

	g_onEnter = refreshOnEnter;

	initActivityEditor();
	
	checkLoginStatus();
});
