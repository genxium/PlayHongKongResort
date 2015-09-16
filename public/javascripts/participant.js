var g_participantsForm = null;
var g_aliasApplied = 0;
var g_aliasSelected = 1;
var g_tabParticipants = null;

function ParticipantsForm(activity, labels, boxes, idList, relationList){
	this.activity = activity;
	this.idList = idList;
	this.labels = labels;
	this.boxes = boxes;
	this.relationList = relationList;
}

function appendParticipantLabels(par, relationAlias, editable, participantList, relationList, idList, boxes, labels) {
	if (!participantList) return;
	var length = participantList.length; 
	var avatarOnClick = function(evt) {
		evt.preventDefault();
		var aParticipant = evt.data;
		window.location.hash = ("profile?" + g_keyVieweeId + "=" + aParticipant.id.toString());
	};
	for (var participant in participantList){
		if (relationAlias == g_aliasApplied)	idList.push(participant.id);
		relationList.push(relationAlias);
		var row = $("<p>").appendTo(par);
		var label = $('<label>', {
			"class": "participant-label"
		}).appendTo(row);
		labels.push(label);

		var checkbox = $("<input>",{
			type: "checkbox",
			"class": "participant-checkbox"
		}).appendTo(label);
		boxes.push(checkbox);
		if (relationAlias == g_aliasSelected) {
			checkbox.hide(); 
		} else {
			if (!editable) checkbox.hide();
		}
		var avatar = $("<img>", {
			src: participant.avatar,
			"class": "participant-avatar"
		}).click(participant, avatarOnClick).appendTo(label);

		var text = $("<plaintext>", {
			"class": "participant-label-name selected-participant title-beta",
			text: participant.name	
		}).appendTo(label);
	}
}

function generateParticipantsSelectionForm(par, activity) {
	var form = $('<form>', {
		"class": "participant-form"
	}).appendTo(par);

	var currentGmtMillis = currentMillis();
	var editable = (!(!g_loggedInPlayer) && g_loggedInPlayer.id == activity.host.id && currentGmtMillis < activity.beginTime);

	var labels = [];
	var boxes = [];
	var idList = [];
	var relationList = [];
	appendParticipantLabels(form, g_aliasSelected, editable, activity.selectedParticipants, relationList, idList, boxes, labels);
	appendParticipantLabels(form, g_aliasApplied, editable, activity.appliedParticipants, relationList, idList, boxes, labels);
	var ret = new ParticipantsForm(activity, labels, boxes, idList, relationList); 

	var appliedParticipantsLength = ((!activity.appliedParticipants) ? 0 : activity.appliedParticipants.length);
	if (!editable || appliedParticipantsLength === 0 || boxes.length <= 1) return ret; // no submit button is needed	
	var btnSubmit = $("<button>",{
		text: TITLES.submit_participant_selection,
		"class": "participant-confirm"
	}).appendTo(form);

	btnSubmit.click(ret, function(evt) {
		evt.preventDefault(); // prevent default action.
		var aForm = evt.data;
		var participantIdList = [];
		for(var i = 0; i < aForm.labels.length; i++) {
			var box = aForm.boxes[i];
			if (isHidden(box) || !isChecked(box)) continue;
			var participantId = aForm.idList[i];
			if(participantId == aForm.activity.host.id) continue;
			participantIdList.push(participantId);
		}
		if (participantIdList.length === 0) return;

		// append player token and activity id for identity
		var token = getToken();
		if (!token) return;

		// prevent violation
		if (participantIdList.length + aForm.activity.numSelected > g_maxSelected) {
			alert(ALERTS.selected_num_exceeded);
			return;
		}	

		var params={};
		params[g_keyToken] = token;
		params[g_keyActivityId] = aForm.activity.id;
		params[g_keyBundle] = JSON.stringify(participantIdList);

		var aButton = getTarget(evt);
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
					alert(ALERTS.selected_num_exceeded);
					return;
				}
				if (!isStandardSuccess(data)) return;
				for(var i = 0; i < aForm.labels.length; ++i){
					var label = aForm.labels[i];
					// ignore selected participants
					if(aForm.relationList[i] == g_aliasSelected) continue;
					var box = aForm.boxes[i];
					if (isHidden(box) || !isChecked(box)) continue;
					label.removeClass("applied-participant");
					label.addClass("selected-participant title-beta");
					box.hide();
					aForm.relationList[i] = g_aliasSelected;
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
