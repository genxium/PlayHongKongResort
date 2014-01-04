// Assistant Handlers
function onBtnCreateClicked(evt){

	evt.preventDefault();
	$(this).hide();

	sectionActivityEditor=$("#"+g_idSectionActivityEditor);
	sectionActivityEditor.empty();
	var userToken=$.cookie(g_keyLoginStatus.toString());
	
	try{
		$.post("/createActivity", 
			{
				UserToken: userToken.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					var jsonActivity=JSON.parse(data);
						var editor=generateActivityEditorByJson(jsonActivity);
						sectionActivityEditor.append(editor);
    				} else{
    					
    				}
			}
		);
	} catch(err){
		
	}
}

function onBtnUpdateClicked(evt){
	
	var activityId=jQuery.data(this, g_keyActivityId);
	var id=activityId;
	var title=$("."+g_classFieldActivityTitle).val();
	var content=$("."+g_classFieldActivityContent).val();
	var token=$.cookie(g_keyLoginStatus.toString());

	var editor=$(this).parent();

	try{
		$.post("/updateActivity", 
			{
				ActivityId: id.toString(),
				ActivityTitle: title.toString(),
				ActivityContent: content.toString(),
				UserToken: token.toString()
			},
			function(data, status, xhr){
				if(status=="success"){
					editor.remove();
					if(g_callbackOnActivityEditorRemoved!=null){
						g_callbackOnActivityEditorRemoved();
					}
				} else{

				}
			}
		);
	} catch(err){
		
	}
}

function onBtnDeleteClicked(evt){
	
	var activityId=jQuery.data(this, g_keyActivityId);
	var id=activityId;
	var token=$.cookie(g_keyLoginStatus.toString());
	var editor=$(this).parent();

	try{
		$.post("/deleteActivity", 
			{
				ActivityId: id.toString(),
				UserToken: token.toString()
			},
			function(data, status, xhr){
				if(status=="success"){
					editor.remove();
					if(g_callbackOnActivityEditorRemoved!=null){
						g_callbackOnActivityEditorRemoved();
					}
				}
				else{

				}
			}
		);
	} catch(err){
		
	}
}

function onBtnSubmitClicked(evt){
	
	var activityId=jQuery.data(this, g_keyActivityId);
	var id=activityId;
	var title=$("."+g_classFieldActivityTitle).val();
	var content=$("."+g_classFieldActivityContent).val();
	var token=$.cookie(g_keyLoginStatus.toString());

	var editor=$(this).parent();

	try{
		$.post("/submitActivity", 
			{
				ActivityId: id.toString(),
				ActivityTitle: title.toString(),
				ActivityContent: content.toString(),
				UserToken: token.toString()
			},
			function(data, status, xhr){
    			if(status=="success"){
					editor.remove();
					if(g_callbackOnActivityEditorRemoved!=null){
						g_callbackOnActivityEditorRemoved();
					}
    			} else{

    			}
			}
		);
	} catch(err){
		
	}
}

function onBtnCancelClicked(evt){
	var editor=$(this).parent();
	editor.remove();
	$("#"+g_idBtnCreate).show();
}

// Generator
function generateActivityEditorByJson(jsonActivity){
	var activityId=jsonActivity[g_keyActivityId];
	var activityTitle=jsonActivity[g_keyActivityTitle];
	var activityContent=jsonActivity[g_keyActivityContent];
	
	var ret=generateActivityEditor(activityId, activityTitle, activityContent);
	return ret;
}

function generateActivityEditor(activityId, activityTitle, activityContent){
	 var ret=$('<div>',
	 			{
	 				class: g_classActivityEditor	
	 			});

	 var formContainer=$('<form>',
	 					{
	 						class: g_classActivityEditorContainer
	 					});

	 var titleText=$('<p>',
	 			 {
	 			 	html: 'Title'
				 });
	 formContainer.append(titleText);
	 var titleInput=$('<input>',
	 				{
		 				class: g_classFieldActivityTitle,
		 				type: 'text',
		 				value: activityTitle
	 				});
	 formContainer.append(titleInput);
	 var contentText=$('<p>',
	 			   {
				   		html: 'Content'
 				   });

	 formContainer.append(contentText);
	 var contentInput=$('<TEXTAREA>',
	 				  {
	 				  	class: g_classFieldActivityContent, 
	 				  });
	 contentInput.val(activityContent);
	 formContainer.append(contentInput);

	 var maxNumberOfImagesForSingleActivity=3;
	 for (var i = 0; i < maxNumberOfImagesForSingleActivity; i++) {
	 	var imageName=g_indexImageOfActivityPrefix+i;
	 	var imageField=$('<input>',
				 		{
				 			class: g_classFieldImageOfActivity,
							type: 'file',
							name: imageName 	 			
				 		});
	 	formContainer.append(imageField);
	 	formContainer.data(imageName, imageField);
	 };

	 ret.append(formContainer);
	 ret.data(g_indexActivityEditorContainer, formContainer);

	 var btnUpdate=$('<button>',{
	 					class: g_classBtnUpdate,
	 					text: 'Update' 
	 				});
	 btnUpdate.data(g_keyActivityId, activityId);
	 btnUpdate.bind("click", onBtnUpdateClicked);
	 ret.append(btnUpdate);

	 var btnDelete=$('<button>',{
	 					class: g_classBtnDelete,
	 					text: 'Delete' 
					 });
	 btnDelete.data(g_keyActivityId, activityId);
	 btnDelete.bind("click", onBtnDeleteClicked);
	 ret.append(btnDelete);

	 var btnSubmit=$('<button>',{
	 					class: g_classBtnSubmit,
	 					text: 'Submit'
	 				});
	 btnSubmit.data(g_keyActivityId, activityId);
	 btnSubmit.bind("click", onBtnSubmitClicked);
	 ret.append(btnSubmit);

	 var btnCancel=$('<button>',{
	 					class: g_classBtnCancel,
	 					text: 'Cancel'
	 				});
	 btnCancel.bind("click", onBtnCancelClicked);
	 ret.append(btnCancel);

	 return ret;
}