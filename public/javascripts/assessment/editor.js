var g_assessmentEditor=null;

/*
	generateAssessmentEditor(participants: User[])
*/
function generateAssessmentEditor(participants){
	var ret=$("<div>");
	for(var i=0;i<participants.length;i++){
		var participant=participants[i];
		var row=generateSingleAssessmentEditor(participant);	
		ret.append(row);
	}	
	return ret;
}

/*
	generateSingleAssessmentEditor(participant: User)
*/
function generateSingleAssessmentEditor(participant){
	var ret=$('<p>');
	var name=$('<plaintext>', {
		text: participant.name
		class: 'name'
	}).appendTo(ret);	
	var content=$('<input>', {
		type: 'text'
		class: 'content'	
	}).appendTo(ret); 
	var lock=$('<checkbox>', {
		style: 'color: red',	
		class: 'lock'
	}).appendTo(ret);
	return ret;	
}

/*
*/
function getAssessment(singleEditor){
	var name=$(singleEditor.children('.name')[0]).attr('text');	
	var content=$(singleEditor.children('.content')[0]).val();
	var lock=$(singleEditor.children('.lock')[0]).is(':checked');
}

