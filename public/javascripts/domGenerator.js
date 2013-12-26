// Assistant Handlers
function onActivityItemClicked(evt){
	if (!evt) {evt = window.event;}
	var sender = (evt.srcElement || evt.target);

	var activityId=jQuery.data(sender, g_keyActivityId);
	var activityTitle=jQuery.data(sender, g_keyActivityTitle);
	var activityContent=jQuery.data(sender, g_keyActivityContent);
	
	targetSection=$("#domainActivities");
	targetSection.empty();

	var editor=generateActivityEditor(activityId, activityTitle, activityContent);
	targetSection.append(editor);
    
}

function onBtnUpdateClicked(evt){
	if (!evt) {evt = window.event;}
	evt.preventDefault();
	var sender = (evt.srcElement || evt.target);
	var activityId=jQuery.data(sender, g_keyActivityId);
	
	var id=activityId;
	var title=$("#activityTitle").val();
	var content=$("#activityContent").val();
	var token=$.cookie(g_keyLoginStatus.toString());

	try{
		$.post("/updateActivity", 
			{
				activityId: id.toString(),
				activityTitle: title.toString(),
				activityContent: content.toString(),
				token: token.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					refreshOnLoggedIn();
    				} else{
    					
    				}
			}
		);
	} catch(err){
		$("#sectionActivities").html(err.message);
	}
}

// Generators
function generateActivityCell(jsonActivity){
	var activityId=jsonActivity[g_keyActivityId];
	var activityTitle=jsonActivity[g_keyActivityTitle];
	var activityContent=jsonActivity[g_keyActivityContent];
	var cellContent=activityId+" "+activityTitle+" "+activityContent+"<br/>";
	var ret=$('<div>',
				{
					class: 'cellActivity'
				});
	ret.html(cellContent);
	ret.bind("click", onActivityItemClicked);
	ret.data(g_keyActivityId, activityId);
	ret.data(g_keyActivityTitle, activityTitle);
	ret.data(g_keyActivityContent, activityContent);
	return ret;
}

function generateActivityEditorByJson(jsonActivity){
	var activityId=jsonActivity[g_keyActivityId];
	var activityTitle=jsonActivity['ActivityTitle'];
	var activityContent=jsonActivity['ActivityContent'];
	
	var ret=generateActivityEditor(activityId, activityTitle, activityContent);
	return ret;
}

function generateActivityEditor(activityId, activityTitle, activityContent){
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
		 				value: activityTitle
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
	 				  	value: activityContent
	 				  });
	 ret.append(contentInput);
	 
	 var btnUpdate=$('<button>',{
	 					class: 'btnUpdate',
	 					text: 'Update' 
	 				});
	 btnUpdate.data(g_keyActivityId, activityId);
	 btnUpdate.bind("click", onBtnUpdateClicked);
	 ret.append(btnUpdate);
	 return ret;
}