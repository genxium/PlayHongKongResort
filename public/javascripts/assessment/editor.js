var g_assessmentEditor=null;

function generateAssessmentEditor(participants){
	var ret=$("<div>");
	for(var i=0;i<participants.length;i++){
		var participant=participants[i];
		var name=$("<plaintext>", {
			text: participant.name
		}).appendTo(ret);	
	}	
	return ret;
}
