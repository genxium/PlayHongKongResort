var g_participantsForm = null;
var g_aliasApplied = 0;
var g_aliasSelected = 1;
var g_tabParticipants = null;

function ParticipantsForm(activity, labels, idList, statusList){
	this.boxes = null;
	this.activity = activity;
	this.idList = idList;
	this.labels = labels;
	this.statusList = statusList;
	this.setBoxes = function(boxes){
		this.boxes = boxes;
	}
}

function generateParticipantsSelectionForm(par, activity) {
	var form = $('<form>').appendTo(par);

	var labels = new Array();
	var idList = new Array();
	var statusList = new Array();
	for(var i = 0; i < activity.selectedParticipants.length; ++i){
		var participant = activity.selectedParticipants[i];
		idList.push(participant.id);
		statusList.push(g_aliasSelected);
		var label = $('<label>', {
			text: participant.name,
			style: "background-color: aquamarine"
		}).appendTo(form);
		labels.push(label);
		$('<br>').appendTo(form);
	}

	for(var i = 0; i < activity.appliedParticipants.length; ++i){
		var participant = activity.appliedParticipants[i];
		idList.push(participant.id);
		statusList.push(g_aliasApplied);
		var label = $('<label>', {
			text: participant.name,
			style: "background-color: pink"
		}).appendTo(form);
		labels.push(label);
		$('<br>').appendTo(form);
	}

	var ret = new ParticipantsForm(activity, labels, idList, statusList); 
	if(g_loggedInUser == null) return ret;
	if(g_loggedInUser.id != activity.host.id) return ret; 
	if(currentMillis() > activity.beginTime) return ret;

	var boxes = new Array();
	for(var i = 0; i < labels.length; i++){
		var label = labels[i];
		var participantId = idList[i];
		var checkbox = $('<input>',{
			type: "checkbox"
		}).appendTo(label);
		boxes.push(checkbox);
		if(statusList[i] == g_aliasSelected) checkbox.hide();
	}
	ret.setBoxes(boxes);
	if (boxes.length <= 1) return ret; // no submit button is needed	

	var btnSubmit=$('<button>',{
		text: 'Confirm Selection',
		style: 'color: white; background-color:black; font-size: 13pt'
	}).appendTo(form);

	btnSubmit.click(ret, function(evt) {
	
		evt.preventDefault(); // prevent default action.
		var aForm = evt.data;
		var participantIdList = new Array();
		for(var i = 0; i < aForm.labels.length; i++) {
			var box = aForm.boxes[i];
			if (box == null || !box.is(":checked")) continue;
			var participantId = aForm.idList[i];
			if(participantId == aForm.activity.host.id) continue;
			participantIdList.push(participantId);
		}
		if (participantIdList.length == 0) return;

		// append user token and activity id for identity
		var token = $.cookie(g_keyToken);
		if (token == null) return;

		var params={};
		params[g_keyToken] = token;
		params[g_keyActivityId] = aForm.activity.id;
		params[g_keyBundle] = JSON.stringify(participantIdList);

		$.ajax({
			type: "POST",
			url: "/activity/participants/update",
			data: params,
			success: function(data, status, xhr){
				for(var i = 0; i < aForm.labels.length; ++i){
					var label = aForm.labels[i];
					// ignore selected participants
					if(aForm.statusList[i] == g_aliasSelected) continue;
					var box = aForm.boxes[i];
					if(!box.is(":checked")) continue;
					label.css("background-color", "aquamarine");
					box.hide();
				}
			},
			error: function(xhr, status, err) {

			}
		});
	});
	$('<hr>').appendTo(form);

	return ret; 
		
}
