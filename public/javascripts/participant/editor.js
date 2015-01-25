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
	var form = $('<form>', {
		class: "participant-form"
	}).appendTo(par);

	var labels = new Array();
	var idList = new Array();
	var statusList = new Array();
	for(var i = 0; i < activity.selectedParticipants.length; ++i){
		var participant = activity.selectedParticipants[i];
		idList.push(participant.id);
		statusList.push(g_aliasSelected);
		var row = $("<p>").appendTo(form);
		var avatar = $("<img>", {
			src: participant.avatar,
			class: "participant-avatar"
		}).click(function(evt) {
			evt.preventDefault();
			window.location.hash = ("profile?" + g_keyVieweeId + "=" + participant.id.toString());
		}).appendTo(row);
		var label = $('<label>', {
			text: participant.name,
			class: "selected-participant"
		}).appendTo(row);
		labels.push(label);
	}

	for(var i = 0; i < activity.appliedParticipants.length; ++i){
		var participant = activity.appliedParticipants[i];
		idList.push(participant.id);
		statusList.push(g_aliasApplied);
		var row = $("<p>").appendTo(form);	
		var avatar = $("<img>", {
			src: participant.avatar,
			class: "participant-avatar"
		}).click(function(evt) {
			evt.preventDefault();
			window.location.hash = ("profile?" + g_keyVieweeId + "=" + participant.id.toString());
		}).appendTo(row);
		var label = $('<label>', {
			text: participant.name,
			class: "applied-participant"
		}).appendTo(row);
		labels.push(label);
	}

	var ret = new ParticipantsForm(activity, labels, idList, statusList); 
	if(g_loggedInUser == null) return ret;
	if(g_loggedInUser.id != activity.host.id) return ret; 
	if(currentMillis() > activity.beginTime) return ret;

	var boxes = new Array();
	for(var i = 0; i < labels.length; i++){
		var label = labels[i];
		var participantId = idList[i];
		var checkbox = $("<input>",{
			type: "checkbox"
		}).appendTo(label);
		boxes.push(checkbox);
		if(statusList[i] == g_aliasSelected) checkbox.hide();
	}
	ret.setBoxes(boxes);
	if (activity.appliedParticipants.length == 0 || boxes.length <= 1) return ret; // no submit button is needed	

	var btnSubmit = $("<button>",{
		text: 'Submit Selection',
		class: "purple participant-confirm"
	}).appendTo(form);

	btnSubmit.click(ret, function(evt) {
	
		evt.preventDefault(); // prevent default action.
		var aForm = evt.data;
		var participantIdList = new Array();
		for(var i = 0; i < aForm.labels.length; i++) {
			var box = aForm.boxes[i];
			if (box == null || !isChecked(box)) continue;
			var participantId = aForm.idList[i];
			if(participantId == aForm.activity.host.id) continue;
			participantIdList.push(participantId);
		}
		if (participantIdList.length == 0) return;

		// append user token and activity id for identity
		var token = $.cookie(g_keyToken);
		if (token == null) return;

		// prevent violation
		if (participantIdList.length + aForm.activity.numSelected > g_maxSelected) {
			alert("Selected applicant number has exceeded upper limit(250)!");
			return;
		}	

		var params={};
		params[g_keyToken] = token;
		params[g_keyActivityId] = aForm.activity.id;
		params[g_keyBundle] = JSON.stringify(participantIdList);

		$.ajax({
			type: "POST",
			url: "/el/activity/participants/update",
			// url: "/activity/participants/update",
			data: params,
			success: function(data, status, xhr){
				if (!isStandardSuccess(data)) return;
				// report violation
				if (parseInt(data.ret) == 2) {
					alert("Selected applicant number has exceeded upper limit(250)!");
					return;
				}
				for(var i = 0; i < aForm.labels.length; ++i){
					var label = aForm.labels[i];
					// ignore selected participants
					if(aForm.statusList[i] == g_aliasSelected) continue;
					var box = aForm.boxes[i];
					if(!isChecked(box)) continue;
					//label.css("background-color", "aquamarine");
					label.removeClass("applied-participant");
					label.addClass("selected-participant");
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
