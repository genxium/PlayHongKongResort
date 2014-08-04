/*
	Trying out new style of info gathering for DOMs
*/
function SingleAssessmentEditor(){
	this.name = "";
	this.content = "";
	this.lock = false;
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
	var lock=$('<checkbox>').appendTo(row);
	lock.on("change", {editor: singleEditor}, function(){
		var data = evt.data;
		var editor = data.editor;
		editor.lock=$(this).is(':checked');
	});
	return singleEditor;	
}

/*
	generateAssessmentEditors(par: DOM, participants: User[])
*/
function generateAssessmentEditors(par, participants){
	var editors=new Array();
	var section=$('<div>').appendTo(par);		
	for(var i=0;i<participants.length;i++){
		var participant = participants[i];
		var editor = generateAssessmentEditor(section, participant);	
		editors.push(editor);
	}	
	return editors;
}

