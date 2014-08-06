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

function generateBatchAssessmentEditor(par, participants){
    par.empty();
	var batchEditor=new BatchAssessmentEditor();
	var editors=new Array();
	var section=$('<div>').appendTo(par);		
	var switchAttendencyContainer=$("<div class='onoffswitch'>").appendTo(section);
	var switchAttendency=$("<input type='checkbox' class='onoffswitch-checkbox' id='switch_attendency'>").appendTo(switchAttendencyContainer);	
	var label=$("<label class='onoffswitch-label' for='switch_attendency'>").appendTo(switchAttendencyContainer);
	var labelInner=$("<span class='onoffswitch-inner'>").appendTo(label); labelInner.attr('content', "Absent");
	var labelSwitch=$("<span class='onoffswitch-switch'>").appendTo(label);	

	var onSuccess=function(data, status, xhr){
		for(var i=0;i<participants.length;i++){
			var participant = participants[i];
			var editor = generateAssessmentEditor(section, participant);	
			editors.push(editor);
		}	
		batchEditor.editors=editors;
	};

	var onError=function(xhr, status, err){};
		
	switchAttendency.on("change", function(evt){
		var attendency=$(this).is(":checked");
		if(attendency==true){
			labelInner.attr('content', "Present");
			updateAttendency(activityId, attendency, onSuccess, onError);	
		} else {
			labelInner.attr('content', "Absent");
		}
	});
		
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
