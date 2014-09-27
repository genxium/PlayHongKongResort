// general dom elements
var g_sectionActivity = null;
var g_tabParticipants = null;

// local variables
var g_activityId = null;
var g_activity = null;

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

        displayTimesTable(ret, g_activity);

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
			href: "/user/profile/show?" + g_keyVieweeId + "=" + g_activity.host.id.toString(),
			text: "@"+g_activity.host.name,
			target: "_blank",
			style: "font-size: 14pt; font-weight: bold;"
		}).appendTo(sp2);
	}

	var content=$('<div>',{
		text: g_activity.content.toString(),
		style: "font-size: 15pt"
	}).appendTo(ret);

	if(g_activity.images != null) {
		// the images are expected to be arranged in a non-uniform manner(not confirmed), thus they should not be bounded to static CSS styling, the current style is a temporary solution
		var constantHeight = 128;
		var imagesNode=$('<p>').appendTo(ret);
		for(var i=0;i<g_activity.images.length;++i){
			$('<img>',{
                            src: g_activity.images[i].url,
			    style: "width: auto; height: " + constantHeight.toString() + "px;"
			}).appendTo(imagesNode);
		}
	}	

	// Tab participants
	g_tabParticipants.empty();
	g_participantsForm = generateParticipantsSelectionForm(g_tabParticipants, g_activity);

	queryCommentsAndRefresh(g_activity);

	// Tab assessments
	var viewer = null;
	if(g_activity.hasOwnProperty("viewer")) viewer = g_activity.viewer;
	g_batchAssessmentEditor = generateBatchAssessmentEditor(g_tabAssessments, g_activity, queryActivityDetail);

	var token = $.cookie(g_keyToken);
	if(token == null)   return ret;

	if(g_activity.hasBegun()) {
	    $("<p>", {
	        style: "color: red; font-size: 13pt",
	        text: "Q & A is disabled because the activity has begun. You can still view existing conversations"
	    }).appendTo(ret);
	    return ret;
	}

	// Comment editor
	generateCommentEditor(ret, g_activity);
	g_onCommentSubmitSuccess = function() {
	    queryCommentsAndRefresh(g_activity);
	}

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
			    // ignore selected participants
			    if(g_participantsForm.participantsStatus[i] == g_aliasSelected) continue;
			    var box = g_participantsForm.boxes[i];
			    if(!box.is(":checked")) continue;
                            label.css("background-color", "aquamarine");
                            box.hide();
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
