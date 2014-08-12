/*
	Trying out new style of info gathering for DOMs
*/
function SingleAssessmentEditor(){
	this.name = "";
	this.content = "";
	this.lock = false;
}

function BatchAssessmentEditor(){
	this.switchAttendency=null;
	this.selectAll=null;
	this.editors=null;	
}

/*
	generateAssessmentEditor(par: DOM, participant: User)
*/
function generateAssessmentEditor(par, participant){
	var singleEditor = new SingleAssessmentEditor();
	var row=$('<p>').appendTo(par);
	var name=$('<plaintext>', {
		text: participant.name
	}).appendTo(ret);
	singleEditor.name = participant.name; // name is a static part
	var content=$('<input>', {
		type: 'text'
	}).appendTo(row); 
	content.on("input paste keyup", {editor: singleEditor}, function(evt){
		var data = evt.data;
		var editor = data.editor;
		editor.content = $(this).val();	
	});	
	var lock=$('<input>', {
		type:"checkbox"
	}).appendTo(row);
	lock.on("change", {editor: singleEditor}, function(){
		var data = evt.data;
		var editor = data.editor;
		editor.lock=$(this).is(':checked');
	});
	return singleEditor;	
}

function generateBatchAssessmentEditor(par, activity, participants){
	par.empty();
	var batchEditor=new BatchAssessmentEditor();
	do{
		if(activity==null) break;
		var editors=new Array();
		var section=$('<div>').appendTo(par);

		var initVal = true;
		var disabled = false;
		var disabledText = "N/A";

		// Determine attendency switch initial state based on viewer-activity-relation
		switch (activity.relation){
		    case hosted:
		    case present:
			initVal = true;
			break;
		    case applied:
		    case absent:
			initVal = false;
			break;
		    default:
			disabled = true;
			break;
		}
		var attendencySwitch = createBinarySwitch(section, disabled, initVal, disabledText, "Present", "Absent", "switch-attendency", onClick);	

		var onSuccess = function(data, status, xhr){	    
			for(var i=0;i<participants.length;i++){
				var participant = participants[i];
				var editor = generateAssessmentEditor(section, participant);
				editors.push(editor);
			}
			batchEditor.editors=editors;
		};

		var onError = function(xhr, status, err){
		};

		var onClick = function(evt){
			if(activity.relation==invalid) return;
			var value = getBinarySwitchState(attendencySwitch);
			var newVal = (value == "true"?"false":"true");
			setBinarySwitch(attendencySwitch, newVal);	
			attendency = activity.relation;
			if(value) attendency = present;
			else attendency = absent;
			updateAttendency(activity.id, attendency, onSuccess, onError);
		};
		
		setBinarySwitchOnClick(attendencySwitch, onClick);
	}while(false);
	return batchEditor;
}

function updateAttendency(activityId, attendency, onSuccess, onError){
	do{
		var token=$.cookie(g_keyToken);
		if(token==null) break; 
		var params={};
		params[g_keyRelation]=attendency;
		params[g_keyToken]=token;
		params[g_keyActivityId]=activityId;
		$.ajax({
			type: "PUT",
			url: "/activity/mark",
			data: params,
			success: onSuccess,
			error: onError
		});
	}while(false);
}
