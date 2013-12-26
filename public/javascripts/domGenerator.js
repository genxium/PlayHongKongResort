// Assistant Handlers
function onActivityItemClicked(evt){
	if (!evt) {evt = window.event;}
	var sender = (evt.srcElement || evt.target);
	g_editingActivityId=jQuery.data(sender, g_keyActivityId);
	alert('ActivityId '+g_editingActivityId+' selected');
}

// Generators
function generateActivityCell(jsonActivity){
	var activityId=jsonActivity[g_keyActivityId];
	var activityTitle=jsonActivity['ActivityTitle'];
	var activityContent=jsonActivity['ActivityContent'];
	var cellContent=activityId+" "+activityTitle+" "+activityContent+"<br/>";
	var ret=$('<p>');
	ret.css("display: table-cell;width: 60pt;height: 30pt;border: 3pt solid #ccccff;font-family: Tahoma, sans-serif;overflow: hidden;background-color: #FF0000;padding: 30pt");
	ret.html(cellContent);
	ret.bind("click", onActivityItemClicked);
	ret.data(g_keyActivityId, activityId);
	return ret;
}

function generateActivityEditor(jsonActivity){
	 var ret=$('<div>');
	 var titleText=$('<p>',
	 			 {
	 			 	html: 'Title'
				 });
	 ret.append(titleText);
	 var titleInput=$('<input>',
	 				{
		 				class: 'titleActivity',
		 				type: 'text',
		 				value: jsonActivity['ActivityTitle']
	 				});
	 ret.append(titleInput);
	 var contentText=$('<p>',
	 			   {
				   		html: 'Content'
 				   });

	 ret.append(contentText);
	 var contentInput=$('<TEXTAREA>',
	 				  {
	 				  	class: 'contentActivity', 
	 				  	value: jsonActivity['ActivityContent']
	 				  });
	 ret.append(contentInput);
	 return ret;
}