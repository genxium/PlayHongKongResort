var g_updatingAttendance = false;
var g_refreshCallback = null;
var g_lockedCount = 0;
var g_btnSubmit = null;

/*
	Trying out new style of info gathering for DOMs
*/
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

/*
	generateAssessmentEditor(par: DOM, participant: User)
*/
function generateAssessmentEditor(par, participant, batchEditor){
	var singleEditor = new SingleAssessmentEditor();
	var row = $('<p>').appendTo(par);
	var name = $('<a>', {
		href: "/user/profile/show?" + g_keyUserId + "=" + participant.id,
		text: "@" + participant.name,
		style: "margin-left: 5pt; display: inline; cursor: pointer; color: BlueViolet"
	}).appendTo(row);
	singleEditor.participantId = participant.id;
	singleEditor.name = participant.name;
	var content=$('<input>', {
		type: 'text',
		style: "margin-left: 10pt; display: inline"
	}).appendTo(row); 
	content.on("input paste keyup", {editor: singleEditor}, function(evt){
		var data = evt.data;
		var editor = data.editor;
		editor.content = $(this).val();	
	});	
	var lock=$('<input>', {
		type: "checkbox",
		style: "margin-left: 10pt; display: inline"
	}).appendTo(row);
	lock.on("change", function(evt){
		evt.preventDefault();
		var checked = $(this).is(":checked");
		if(!checked) {
			content.prop("disabled", false);
			--g_lockedCount;
			if(g_btnSubmit != null) g_btnSubmit.prop("disabled", true);
		} else {
			content.prop("disabled", true);
			++g_lockedCount;
			if(g_lockedCount == batchEditor.editors.length && g_btnSubmit != null) g_btnSubmit.prop("disabled", false);
		}
	});
	singleEditor.lock = lock;
	return singleEditor;
}

function generateAssessmentEditors(par, participants, batchEditor) {
	par.empty();
	var editors = new Array();
	for(var i = 0; i < participants.length; i++){
		var editor = generateAssessmentEditor(par, participants[i], batchEditor);
		editors.push(editor);
	}				
	return editors;
}

function generateAssessmentButtons(par, activity, batchEditor){
    if(batchEditor.editors == null || batchEditor.editors.length <= 0) return;
	var row = $('<p>').appendTo(par);
	var btnCheckAll = $('<button>', {
		text: "Check All",
		style: "display: inline"
	}).appendTo(row);   
	btnCheckAll.on("click", function(evt){
		evt.preventDefault();
		for(var i = 0; i < batchEditor.editors.length; i++) {
			var editor = batchEditor.editors[i];
			editor.lock.prop("checked", true).change();
		}
	});
	var btnUncheckAll = $('<button>', {
	    text: "Uncheck All",
	    style: "display: inline; margin-left: 5pt"
	}).appendTo(row);
	btnUncheckAll.on("click", function(evt){
		evt.preventDefault();
		for(var i = 0; i < batchEditor.editors.length; i++) {
			var editor = batchEditor.editors[i];
			editor.lock.prop("checked", false).change();
		}
	});
	g_btnSubmit = $('<button>', {
		text: "Submit",
		style: "display: inline; margin-left: 5pt"
	}).appendTo(row);
	g_btnSubmit.on("click", function(evt){
		evt.preventDefault();
		var assessments = new Array();
		for(var i = 0; i < batchEditor.editors.length; i++) {
			var editor = batchEditor.editors[i];
			var content = editor.content;
			var to = editor.participantId;
			var assessment = new Assessment(content, to); 
			assessments.push(assessment);	
		}
		var params = {};
		var token = $.cookie(g_keyToken);
		params[g_keyToken] = token;
		params[g_keyActivityId] = activity.id; 
		params[g_keyBundle] = JSON.stringify(assessments);
			
		$.ajax({
			type: "POST", 
			url: "/assessment/submit",
			data: params,
			success: function(data, status, xhr){
				alert("Assessment submitted!");
				row.remove();
			},
			error: function(xhr, status, err){

			}
		});
	}).appendTo(row);
	g_btnSubmit.prop("disabled", true);
}

function generateBatchAssessmentEditor(par, activity, participants, refreshCallback){
	par.empty();
	g_lockedCount = 0; // clear lock count on batch editor generated
	g_refreshCallback = refreshCallback;
	var batchEditor = new BatchAssessmentEditor();

	if(activity == null) return batchEditor;
	var editors = new Array();
	var sectionAll = $('<div>').appendTo(par);
	
	var initVal = false;
	var disabled = false;

	// Determine attendance switch initial state based on viewer-activity-relation
	if (activity.relation == hosted) {
		// host cannot choose
		initVal = true;
		disabled = true;
	} else if((activity.relation & present) > 0) {
		// present participants but not
		initVal = true;
	} else if((activity.relation & selected) > 0 || (activity.relation & absent) > 0) {
		// selected but not present
		initVal = false;
	} else {
		disabled = true;
	}
	var attendanceSwitch = createBinarySwitch(sectionAll, disabled, initVal, "N/A", "Present", "Absent", "switch-attendance");	
	var sectionEditors = $('<div>', {
		style: "margin-top: 5pt"
	}).appendTo(sectionAll);

	var sectionButtons = $('<div>', {
		style: "margin-top: 5pt"
	}).appendTo(sectionAll);

	if( ((activity.relation & present) > 0 || (activity.relation == hosted))
	     && (activity.relation & assessed) == 0) {
	     // present but not yet assessed participants
		var editors = generateAssessmentEditors(sectionEditors, activity.presentParticipants, batchEditor);
		batchEditor.editors = editors;
		generateAssessmentButtons(sectionButtons, activity, batchEditor);
	}

	var onSuccess = function(data, status, xhr){	    
		g_updatingAttendance = false;

		// update activity.relation by returned value
		var relationJson = JSON.parse(data);
		activity.relation = parseInt(relationJson[g_keyRelation]);

		sectionEditors.empty();
		sectionButtons.empty();

		var value = getBinarySwitchState(attendanceSwitch);
		if(!value || (activity.relation & assessed) > 0) return;
		// assessed participants cannot edit or re-submit assessments

		var editors = generateAssessmentEditors(sectionEditors, activity.presentParticipants, batchEditor);
		batchEditor.editors = editors;
		generateAssessmentButtons(sectionButtons, activity, batchEditor);
	};

	var onError = function(xhr, status, err){
		g_updatingAttendance = false;
		// reset switch status if updating attendance fails
		var value = getBinarySwitchState(attendanceSwitch);
		var resetVal = !value;
		setBinarySwitch(attendanceSwitch, resetVal);
	};

	var onClick = function(evt){
		evt.preventDefault();
		if(activity.relation == invalid) return;
		var value = getBinarySwitchState(attendanceSwitch);
		var newVal = !value;
		setBinarySwitch(attendanceSwitch, newVal);	
		attendance = activity.relation;
		if(newVal) attendance = present;
		else attendance = absent;
		updateAttendance(activity.id, attendance, onSuccess, onError);
	};
	
	setBinarySwitchOnClick(attendanceSwitch, onClick);
	return batchEditor;
}

function updateAttendance(activityId, attendance, onSuccess, onError){
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
		success: onSuccess,
		error: onError
	});
}

function queryAssessments(userId, activityId, onSuccess, onError) {
	var params = {};	
	params[g_keyUserId] = userId; 
	params[g_keyActivityId] = activityId;
	
	params[g_keyRefIndex] = 0;
	params[g_keyNumItems] = 20;
	params[g_keyDirection] = 1;
	var token = $.cookie(g_keyToken);
	params[g_keyToken] = token;

	$.ajax({
		type: "GET",
		url: "/assessment/query",
		data: params,
		success: onSuccess,
		error: onError
	});
}
