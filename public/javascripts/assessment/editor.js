var g_batchAssessmentEditor = null;
var g_tabAssessments = null;

var g_updatingAttendance = false;
var g_onRefresh = null;
var g_lockedCount = 0;
var g_btnSubmit = null;

var g_sectionAssessmentEditors = null;
var g_sectionAssessmentButtons = null;

function createAssessment(content, to) {
	var assessmentJson = {};
	assessmentJson["content"] = content;
	assessmentJson["to"] = to;
	return new Assessment(assessmentJson);
}

function SingleAssessmentEditor(){
	this.participantId = 0;
	this.name = "";
	this.content = "";
	this.lock = null;
}

function BatchAssessmentEditor(){
	this.switchAttendance = null;
	this.selectAll = null;
	this.editors = null;	
}

function generateAssessmentEditor(par, participant, activity, batchEditor){
	var singleEditor = new SingleAssessmentEditor();
	var row = $('<div>', {
		"class": "assessment-input-row"
	}).appendTo(par);
	var avatar = $("<img>", {
		src: participant.avatar,
		"class": "assessment-avatar"
	}).click(function(evt) {
		evt.preventDefault();
		window.location.hash = ("profile?" + g_keyVieweeId + "=" + participant.id.toString());
	}).appendTo(row);
	var name = $('<a>', {
		href: "#",
		text: participant.name
	}).appendTo(row);
	name.click(function(evt) {
		evt.preventDefault();
		window.location.hash = ("profile?" + g_keyVieweeId + "=" + participant.id.toString());	
	});
	singleEditor.participantId = participant.id;
	singleEditor.name = participant.name;
	if ( activity.containsRelation() && ((activity.relation & assessed) == 0) )	generateUnassessedView(row, singleEditor, batchEditor);
	else generateAssessedView(row, participant, activity);
	if(g_loggedInUser != null && g_loggedInUser.id == participant.id) row.hide(); 
	return singleEditor;
}

function generateAssessmentEditors(par, activity, batchEditor) {
	par.empty();
	var participants = activity.selectedParticipants; 
	var editors = new Array();
	for(var i = 0; i < participants.length; i++){
		var editor = generateAssessmentEditor(par, participants[i], activity, batchEditor);
		editors.push(editor);
	}				
	return editors;
}

function generateAssessmentButtons(par, activity, batchEditor){
	par.empty();
	if(batchEditor.editors == null || batchEditor.editors.length <= 1) return;
	var row = $('<div>', {
		"class": "assessment-button"
	}).appendTo(par);
	var btnCheckAll = $("<button>", {
		text: TITLES["check_all"],
		"class": "gray assessment-button"
	}).appendTo(row);   

	btnCheckAll.click(batchEditor, function(evt){
		evt.preventDefault();
		for(var i = 0; i < evt.data.editors.length; i++) {
			var editor = evt.data.editors[i];
			editor.lock.prop("checked", true).change();
		}
	});

	var btnUncheckAll = $("<button>", {
	    text: TITLES["uncheck_all"],
	    "class": "gray assessment-button"
	}).appendTo(row);
	btnUncheckAll.click(batchEditor, function(evt){
		evt.preventDefault();
		for(var i = 0; i < evt.data.editors.length; i++) {
			var editor = evt.data.editors[i];
			editor.lock.prop("checked", false).change();
		}
	});

	g_btnSubmit = $("<button>", {
		text: TITLES["submit"],
		"class": "assessment-button positive-button"
	}).appendTo(row);

	g_btnSubmit.click({editor: batchEditor, activity: activity}, function(evt){
		evt.preventDefault();
		if (g_loggedInUser == null) return;
		var aBatchEditor = evt.data.editor;
		var aActivity = evt.data.activity;
		var assessments = new Array();
		for(var i = 0; i < aBatchEditor.editors.length; i++) {
			var editor = aBatchEditor.editors[i];
			var content = editor.content;
			var to = editor.participantId;
			if(to == g_loggedInUser.id) continue;
			var assessment = createAssessment(content, to); 
			assessments.push(assessment);	
		}
		if (assessments.length == 0) return;

		var params = {};
		var token = $.cookie(g_keyToken);
		params[g_keyToken] = token;
		params[g_keyActivityId] = aActivity.id; 
		params[g_keyBundle] = JSON.stringify(assessments);

		var aButton = $(this);	
		disableField(aButton);
		$.ajax({
			type: "POST", 
			url: "/assessment/submit",
			data: params,
			success: function(data, status, xhr){
				enableField(aButton);
				if (isTokenExpired(data)) {
					logout(null);
					return;
				}
				alert(ALERTS["assessment_submitted"]);
				aActivity.relation |= assessed;
				refreshBatchEditor(aActivity);
			},
			error: function(xhr, status, err){
				enableField(aButton);
				alert(ALERTS["assessment_not_submitted"]);
			}
		});
	}).appendTo(row);
	disableField(g_btnSubmit);
}

function generateBatchAssessmentEditor(par, activity, onRefresh){
	par.empty();

	if(g_onRefresh == null)	g_onRefresh = onRefresh;

	g_lockedCount = 0; // clear lock count on batch editor generated
	g_batchAssessmentEditor = new BatchAssessmentEditor();

	if(activity == null) return g_batchAssessmentEditor;
	var editors = [];
	var sectionAll = $('<div>', {
		"class": "assessment-container"
	}).appendTo(par);
	
	var initVal = false;
	var disabled = false;

	// Determine attendance switch initial state based on viewer-activity-relation
	if (g_loggedInUser != null && activity.host.id == g_loggedInUser.id) {
		// host cannot choose presence
		initVal = true;
		disabled = true;
	} else if((activity.relation & present) > 0) {
		// present participants
		initVal = true;
	} else if((activity.relation & selected) > 0 || (activity.relation & absent) > 0) {
		// selected but not present
		initVal = false;
	} else {
		disabled = true;
	}

	var attendanceSwitch = createBinarySwitch(sectionAll, disabled, initVal, TITLES["assessment_disabled"], TITLES["present"], TITLES["absent"], "switch-attendance");	
	g_sectionAssessmentEditors = $('<div>', {
		style: "margin-top: 5pt"
	}).appendTo(sectionAll);

	g_sectionAssessmentButtons = $('<div>', {
		style: "margin-top: 5pt"
	}).appendTo(sectionAll);

	if( g_loggedInUser != null && ( ((activity.relation & present) > 0) || (activity.containsRelation() == false) ) ) {
		/* 
 		 * 	show list for logged-in users
		 */
		refreshBatchEditor(activity);
	}

	var onSuccess = function(data){	    
		g_updatingAttendance = false;

		// update activity.relation by returned value
		var relationJson = data;
		activity.relation = parseInt(relationJson[g_keyRelation]);

		g_sectionAssessmentEditors.empty();
		g_sectionAssessmentButtons.empty();

		var value = getBinarySwitchState(attendanceSwitch);
		if(!value) return;
		// assessed participants cannot edit or re-submit assessments
		refreshBatchEditor(activity);
	};

	var onError = function(err){
		g_updatingAttendance = false;
		// reset switch status if updating attendance fails
		var value = getBinarySwitchState(attendanceSwitch);
		var resetVal = !value;
		setBinarySwitch(attendanceSwitch, resetVal);
	};

	var onClick = function(evt){
		evt.preventDefault();
		if(activity.relation == invalid) return;
		if(!activity.hasBegun()) {
			alert(ALERTS["activity_not_begun"]);
			return; 
		}
		var value = getBinarySwitchState(attendanceSwitch);
		var newVal = !value;
		setBinarySwitch(attendanceSwitch, newVal);	
		attendance = activity.relation;
		if(newVal) attendance = present;
		else attendance = absent;
		updateAttendance(activity.id, attendance, onSuccess, onError);
	};
	
	setBinarySwitchOnClick(attendanceSwitch, onClick);
	return g_batchAssessmentEditor;
}

function updateAttendance(activityId, attendance, onSuccess, onError){
	// prototypes: onSuccess(data), onError(err)
	if(g_updatingAttendance) return;
	var token = $.cookie(g_keyToken);
	if(token == null) return; 
	var params={};
	params[g_keyRelation] = attendance;
	params[g_keyToken] = token;
	params[g_keyActivityId] = activityId;
	g_updatingAttendance = true;
	$.ajax({
		type: "PUT",
		url: "/activity/mark",
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


function generateAssessedView(row, participant, activity) {
	var btnView = $('<span>', {
		text: TITLES["view_assessment"],
		style: "display: inline; color: blue; margin-left: 5pt; cursor: pointer"
	}).appendTo(row);					
	btnView.click(function(evt){
		evt.preventDefault();
		queryAssessmentsAndRefresh(participant.id, activity.id);	
	});
}

function generateUnassessedView(row, singleEditor, batchEditor) {
	var lock = $('<input>', {
		type: "checkbox",
		"class": "left"
	}).appendTo(row);
	var contentInput = $('<input>', {
		type: 'text'
	}).appendTo(row); 
	contentInput.on("input paste keyup", singleEditor, function(evt){
		evt.data.content = $(this).val();	
	});	
	lock.change({input: contentInput, editor: batchEditor}, function(evt){
		var aInput = evt.data.input;
		var aBatchEditor = evt.data.editor;
		evt.preventDefault();
		var checked = isChecked($(this));
		if(!checked) {
			enableField(aInput);
			--g_lockedCount;
			if(g_btnSubmit != null) disableField(g_btnSubmit);
		} else {
			disableField(aInput);
			++g_lockedCount;
			if(g_lockedCount >= (aBatchEditor.editors.length - 1) && g_btnSubmit != null) enableField(g_btnSubmit);
		}
	});
	singleEditor.lock = lock;
}

function refreshBatchEditor(activity) {
	if (!activity.hasBegun()) return;

	if(g_batchAssessmentEditor == null || g_sectionAssessmentEditors == null || g_sectionAssessmentButtons == null) return;
	var editors = generateAssessmentEditors(g_sectionAssessmentEditors, activity, g_batchAssessmentEditor);
	g_batchAssessmentEditor.editors = editors;
	g_sectionAssessmentButtons.empty();
	if(!activity.containsRelation() || (activity.containsRelation() && (activity.relation & assessed) > 0) || g_batchAssessmentEditor.editors.length <= 1) return;
	generateAssessmentButtons(g_sectionAssessmentButtons, activity, g_batchAssessmentEditor);

}
