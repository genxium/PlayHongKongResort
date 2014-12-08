var g_participantsForm = null;
var g_aliasApplied = 0;
var g_aliasSelected = 1;
var g_tabParticipants = null;

function ParticipantsForm(labels, participantsId, participantsStatus){
	this.boxes = null;
	this.participantsId = participantsId;
	this.labels = labels;
	this.participantsStatus = participantsStatus;
	this.setBoxes = function(boxes){
		this.boxes = boxes;
	}
}

function generateParticipantsSelectionForm(par, activity) {
	var form = $('<form>').appendTo(par);

	var labels = new Array();
	var participantsId = new Array();
	var participantsStatus = new Array();
	for(var i = 0; i < activity.selectedParticipants.length; ++i){
		var participant = g_activity.selectedParticipants[i];
		participantsId.push(participant.id);
		participantsStatus.push(g_aliasSelected);
		var label = $('<label>', {
			text: participant.name,
			style: "background-color: aquamarine"
		}).appendTo(form);
		labels.push(label);
		$('<br>').appendTo(form);
	}

	for(var i = 0; i < activity.appliedParticipants.length; ++i){
		var participant = activity.appliedParticipants[i];
		participantsId.push(participant.id);
		participantsStatus.push(g_aliasApplied);
		var label = $('<label>', {
			text: participant.email,
			style: "background-color: pink"
		}).appendTo(form);
		labels.push(label);
		$('<br>').appendTo(form);
	}

	var ret = new ParticipantsForm(labels, participantsId, participantsStatus); 
	if(g_loggedInUser == null) return ret;
	if(g_loggedInUser.id != activity.host.id) return ret; 
	if(currentMillis() > activity.beginTime) return ret;

	var boxes = new Array();
	for(var i = 0; i < labels.length; i++){
		var label = labels[i];
		var participantId = participantsId[i];
		var checkbox = $('<input>',{
			type: "checkbox"
		}).appendTo(label);
		boxes.push(checkbox);
		if(participantsStatus[i] == g_aliasSelected) checkbox.hide();
	}
	ret.setBoxes(boxes);
	if (boxes.length <= 1) return ret; // no submit button is needed	

	var btnSubmit=$('<button>',{
		text: 'Confirm Selection',
		style: 'color: white; background-color:black; font-size: 13pt'
	}).appendTo(form);
	btnSubmit.on("click", onBtnSubmitClicked);
	$('<hr>').appendTo(form);

	return ret; 
		
}
