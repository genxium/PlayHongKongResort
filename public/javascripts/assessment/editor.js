var g_updatingAttendance = false;
var g_refreshCallback = null;
var g_lockedCount = 0;
var g_btnSubmit = null;

/*
	Trying out new style of info gathering for DOMs
*/
function SingleAssessmentEditor(){
	this.name = "";
	this.content = "";
	this.lock = null;
}

function BatchAssessmentEditor(){
	this.switchAttendance=null;
	this.selectAll=null;
	this.editors=null;	
}

/*
	generateAssessmentEditor(par: DOM, participant: User)
*/
function generateAssessmentEditor(par, participant, batchEditor){
	var singleEditor = new SingleAssessmentEditor();
	var row=$('<p>').appendTo(par);
	var name=$('<a>', {
	    href: "/user/profile/show?"+g_keyUserId+"="+participant.id,
		text: "@"+participant.name,
		style: "margin-left: 5pt; display: inline; cursor: pointer; color: BlueViolet"
	}).appendTo(row);
	singleEditor.name = participant.name; // name is a static part
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
	    }
	    else {
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

function generateAssessmentButtons(par, batchEditor){
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
	}).appendTo(row);
	g_btnSubmit.prop("disabled", true);
}

function generateBatchAssessmentEditor(par, activity, participants, refreshCallback){
	par.empty();
	g_lockedCount = 0; // clear lock count on batch editor generated
	g_refreshCallback = refreshCallback;
	var batchEditor=new BatchAssessmentEditor();
	do{
		if(activity==null) break;
		var editors=new Array();
		var sectionAll=$('<div>').appendTo(par);
		
		var initVal = true;
		var disabled = false;

		// Determine attendance switch initial state based on viewer-activity-relation
		if (activity.relation == hosted) {
			initVal = true;
			disabled = true;
		} else if((activity.relation & present) > 0) {
		    initVal = true;
		} else if((activity.relation & selected) > 0 || (activity.relation & absent) > 0) {
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

		if((activity.relation & present) > 0) {
			var editors = generateAssessmentEditors(sectionEditors, activity.presentParticipants, batchEditor);
			batchEditor.editors = editors;
            generateAssessmentButtons(sectionButtons, batchEditor);
		}

		var onSuccess = function(data, status, xhr){	    
			g_updatingAttendance = false;
			var value = getBinarySwitchState(attendanceSwitch);
			if(value)   activity.relation = present;
			else    activity.relation = absent;
			
			sectionEditors.empty();
            sectionButtons.empty();

			if(!value) return;

			var editors = generateAssessmentEditors(sectionEditors, activity.presentParticipants, batchEditor);
			batchEditor.editors = editors;
            generateAssessmentButtons(sectionButtons, batchEditor);
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
	}while(false);
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
