var g_participantsForm = null;
var g_aliasApplied = 0;
var g_aliasSelected = 1;
var g_tabParticipants = null;

function ParticipantsForm(activity, labels, boxes, idList, statusList){
	this.activity = activity;
	this.idList = idList;
	this.labels = labels;
	this.boxes = boxes;
	this.statusList = statusList;
}

function generateParticipantsSelectionForm(par, activity) {
	var form = $('<form>', {
		class: "participant-form"
	}).appendTo(par);

	var currentGmTMillis = currentMillis();
	var editable = (g_loggedInUser != null && g_loggedInUser.id == activity.host.id && currentGmTMillis < activity.beginTime);

	var labels = new Array();
	var boxes = new Array();
	var idList = new Array();
	var statusList = new Array();
	for(var i = 0; i < activity.selectedParticipants.length; ++i){
		var participant = activity.selectedParticipants[i];
		idList.push(participant.id);
		statusList.push(g_aliasSelected);
		var row = $("<p>").appendTo(form);
		var label = $('<label>', {
			class: "participant-label"
		}).appendTo(row);
		labels.push(label);

		var checkbox = $("<input>",{
			type: "checkbox",
			class: "participant-checkbox"
		}).appendTo(label);
		boxes.push(checkbox);
		checkbox.hide();

		var avatar = $("<img>", {
			src: participant.avatar,
			class: "participant-avatar"
		}).click(participant, function(evt) {
			evt.preventDefault();
			var aParticipant = evt.data;
			window.location.hash = ("profile?" + g_keyVieweeId + "=" + aParticipant.id.toString());
		}).appendTo(label);

		var text = $("<plaintext>", {
			class: "participant-label-name selected-participant",
			text: participant.name	
		}).appendTo(label);
	}

	for(var i = 0; i < activity.appliedParticipants.length; ++i){
		var participant = activity.appliedParticipants[i];
		idList.push(participant.id);
		statusList.push(g_aliasApplied);
		var row = $("<p>").appendTo(form);	
		var label = $('<label>', {
			class: "participant-label"
		}).appendTo(row);
		labels.push(label);

		var checkbox = $("<input>",{
			type: "checkbox",
			class: "participant-checkbox"
		}).appendTo(label);
		boxes.push(checkbox);
		if (!editable) checkbox.hide();

		var avatar = $("<img>", {
			src: participant.avatar,
			class: "participant-avatar"
		}).click(function(evt) {
			evt.preventDefault();
			window.location.hash = ("profile?" + g_keyVieweeId + "=" + participant.id.toString());
		}).appendTo(label);

		var text = $("<plaintext>", {
			class: "participant-label-name applied-participant",
			text: participant.name	
		}).appendTo(label);
	}

	var ret = new ParticipantsForm(activity, labels, boxes, idList, statusList); 

	if (!editable || activity.appliedParticipants.length == 0 || boxes.length <= 1) return ret; // no submit button is needed	

	var btnSubmit = $("<button>",{
		text: TITLES["submit_participant_selection"],
		class: "purple participant-confirm"
	}).appendTo(form);

	btnSubmit.click(ret, function(evt) {
		evt.preventDefault(); // prevent default action.
		var aForm = evt.data;
		var participantIdList = new Array();
		for(var i = 0; i < aForm.labels.length; i++) {
			var box = aForm.boxes[i];
			if (isHidden(box) || !isChecked(box)) continue;
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
			alert(ALERTS["selected_num_exceeded"]);
			return;
		}	

		var params={};
		params[g_keyToken] = token;
		params[g_keyActivityId] = aForm.activity.id;
		params[g_keyBundle] = JSON.stringify(participantIdList);

		var aButton = (evt.srcElement ? evt.srcElement : evt.target);
		disableField(aButton);

		$.ajax({
			type: "POST",
			url: "/el/activity/participants/update",
			// url: "/activity/participants/update",
			data: params,
			success: function(data, status, xhr){
				enableField(aButton);
				// report violation
				if (isTokenExpired(data)) {
					logout(null);
					return;
				}
				if (isSelectedLimitExceeded(data)) {
					alert(ALERTS["selected_num_exceeded"]);
					return;
				}
				if (!isStandardSuccess(data)) return;
				for(var i = 0; i < aForm.labels.length; ++i){
					var label = aForm.labels[i];
					// ignore selected participants
					if(aForm.statusList[i] == g_aliasSelected) continue;
					var box = aForm.boxes[i];
					if (isHidden(box) || !isChecked(box)) continue;
					label.removeClass("applied-participant");
					label.addClass("selected-participant");
					box.hide();
					aForm.statusList[i] = g_aliasSelected;
				}
			},
			error: function(xhr, status, err) {
				enableField(aButton);
			}
		});
	});
	$('<hr>').appendTo(form);

	return ret; 
		
}
