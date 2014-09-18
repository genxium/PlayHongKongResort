var g_updatingAttendance = false;
var g_refreshCallback = null;
var g_lockedCount = 0;
var g_btnSubmit = null;

var g_sectionAssessmentsViewer = null;
var g_modalAssessmentsViewer = null;
var g_assessmentsViewer = null;

function createAssessment(content, to) {
	var assessmentJson = {};
	assessmentJson["content"] = content;
	assessmentJson["to"] = to;
	return new Assessment(assessmentJson);
}

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

function generateAssessmentEditor(par, participant, activity, batchEditor){
	var singleEditor = new SingleAssessmentEditor();
	var row = $('<p>').appendTo(par);
	var name = $('<a>', {
		href: "/user/profile/show?" + g_keyVieweeId + "=" + participant.id,
		text: "@" + participant.name,
		target: "_blank",
		style: "margin-left: 5pt; display: inline; cursor: pointer; color: BlueViolet"
	}).appendTo(row);
	singleEditor.participantId = participant.id;
	singleEditor.name = participant.name;
	if((activity.relation & assessed) == 0) {
		var content = $('<input>', {
			type: 'text',
			style: "margin-left: 10pt; display: inline"
		}).appendTo(row); 
		content.on("input paste keyup", {editor: singleEditor}, function(evt){
			evt.preventDefault();
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
				if(g_lockedCount >= (batchEditor.editors.length - 1) && g_btnSubmit != null) g_btnSubmit.prop("disabled", false);
			}
		});
		singleEditor.lock = lock;
	} else {
		var btnView = $('<span>', {
			text: "View assessments on " + participant.email +" >>",
			style: "display: inline; color: blue; margin-left: 5pt; cursor: pointer"
		}).appendTo(row);					
		var dBtnView = {};
		dBtnView[g_keyVieweeId] = participant.id;
		dBtnView[g_keyActivityId] = activity.id; 
		btnView.on("click", dBtnView, function(evt){
			evt.preventDefault();
			var onSuccess = function(data, status, xhr) {
				var jsonResponse = JSON.parse(data);
				if(jsonResponse == null || Object.keys(jsonResponse).length == 0) return;
				var assessments = new Array();
				for(var key in jsonResponse) {
					var assessmentJson = jsonResponse[key];
					var assessment = new Assessment(assessmentJson);
					assessments.push(assessment);
				}
				
				showAssessmentsViewer(assessments);				
				
			};
			var onError = function(xhr, status, err) {

			};
			queryAssessments(evt.data[g_keyVieweeId], evt.data[g_keyActivityId], onSuccess, onError);	
		});
	}
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
			if(to == g_loggedInUser.id) continue;
			var assessment = createAssessment(content, to); 
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

function generateBatchAssessmentEditor(par, activity, refreshCallback){
	par.empty();

	initAssessmentsViewer();

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

	if( (activity.relation & present) > 0 || (activity.relation == hosted) ) {
	     // present but not yet assessed participants
		var editors = generateAssessmentEditors(sectionEditors, activity, batchEditor);
		batchEditor.editors = editors;
		if((activity.relation & assessed) == 0 && batchEditor.editors.length > 1) generateAssessmentButtons(sectionButtons, activity, batchEditor);
	}

	var onSuccess = function(data, status, xhr){	    
		g_updatingAttendance = false;

		// update activity.relation by returned value
		var relationJson = JSON.parse(data);
		activity.relation = parseInt(relationJson[g_keyRelation]);

		sectionEditors.empty();
		sectionButtons.empty();

		var value = getBinarySwitchState(attendanceSwitch);
		if(!value) return;
		// assessed participants cannot edit or re-submit assessments

		var editors = generateAssessmentEditors(sectionEditors, activity, batchEditor);
		batchEditor.editors = editors;
		if((activity.relation & assessed) == 0 && batchEditor.editors.length > 1)	generateAssessmentButtons(sectionButtons, activity, batchEditor);
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

function queryAssessments(to, activityId, onSuccess, onError) {
	var params = {};	

	params[g_keyRefIndex] = 0;
	params[g_keyNumItems] = 20;
	params[g_keyDirection] = 1;
	var token = $.cookie(g_keyToken);
	if(token != null) params[g_keyToken] = token;
        params[g_keyTo] = to;
	params[g_keyActivityId] = activityId;

	$.ajax({
		type: "GET",
		url: "/assessment/query",
		data: params,
		success: onSuccess,
		error: onError
	});
}

function removeAssessmentsViewer(){
        if(g_sectionAssessmentsViewer == null) return;
        g_sectionAssessmentsViewer.hide();
        g_sectionAssessmentsViewer.modal("hide");
        if(g_modalAssessmentsViewer == null) return;
        g_modalAssessmentsViewer.empty();
        if(g_assessmentsViewer == null) return;
        g_assessmentsViewer.remove();
}

function initAssessmentsViewer(){
	var wrap = $("#wrap");
	/*
		Note: ALL attributes, especially the `class` attribute MUST be written INSIDE the div tag, bootstrap is NOT totally compatible with jQuery!!!
	*/
	g_sectionAssessmentsViewer = $("<div class='modal fade' tabindex='-1' role='dialog' aria-labelledby='Create an activity!' aria-hidden='true'>", {
		style: "height: 80%; position: absolute"
	}).appendTo(wrap);
	var dialog = $("<div>", {
		class: "modal-dialog modal-lg"
	}).appendTo(g_sectionAssessmentsViewer);
	g_modalAssessmentsViewer = $("<div>", {
		class: "modal-content"
	}).appendTo(dialog);	

	removeAssessmentsViewer();
}

function showAssessmentsViewer(assessments) {
		
	g_assessmentsViewer = generateAssessmentsViewer(assessments);
        g_modalAssessmentsViewer.empty();
        g_modalAssessmentsViewer.append(g_assessmentsViewer);

        g_sectionAssessmentsViewer.css("position", "absolute");
        g_sectionAssessmentsViewer.css("height", "90%");
        g_sectionAssessmentsViewer.css("padding", "5pt");
        g_sectionAssessmentsViewer.modal({
                show: true
        });

}

function generateAssessmentsViewer(assessments) {
        var ret = $("<div>", {
            style: "padding: 10%"
        });

	var tbl = $("<table class='assessments-viewer'>").appendTo(ret);

	try {
		if (assessments == null) throw new NullPointerException();
                var head = $("<tr class='assessments-viewer-row'>").appendTo(tbl);

                $('<th>', {
                    text: "Content"
                }).appendTo(head);

                $('<th>', {
                    text: "From"
                }).appendTo(head);

		for(var i = 0; i < assessments.length; i++) {
		        var assessment = assessments[i];
			var row = $("<tr class='assessments-viewer-row'>").appendTo(tbl);
                        $('<td>', {
                            text: assessment.content
                        }).appendTo(row);
                        $('<td>', {
                            text: assessment.from_name
                        }).appendTo(row);
		}
	} catch (e) {
		alert(e.message);
	}
	return ret;
} 
