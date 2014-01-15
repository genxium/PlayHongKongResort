// Assistant Callback Functions
function onUpdateFormSubmission(formEvt){
	do{
		formEvt.preventDefault(); // prevent default action.

		var formObj = $(this);
		var formData = new FormData(this);
		/*
		// check files
		var iFiles=0;
		for (iFiles = 0; iFiles < g_maxNumberOfImagesForSingleActivity; iFiles++) {
		 	var imageName=g_indexImageOfActivityPrefix+iFiles;
		 	var imageField=formObj.data(imageName);
		 	var files=imageField.files;
		 	var length=files.length;
		 	if(length>1) break; // invalid field exist
		 	var file=files[0];
		 	var checkResult=isFileValid(file);
		 	if(checkResult==false) break;
		}
		if(iFiles<g_maxNumberOfImagesForSingleActivity) break;
		*/
		// append user token and activity id for identity
		var token = $.cookie(g_keyLoginStatus.toString());
		formData.append(g_keyUserToken, token);

		var activityId = $(this).data(g_keyActivityId);
		formData.append(g_keyActivityId, activityId.toString());
		
		// append activity title and content 
		var activityTitle=$("."+g_classFieldActivityTitle).val();
		formData.append(g_keyActivityTitle, activityTitle);
		
		var activityContent=$("."+g_classFieldActivityContent).val();
		formData.append(g_keyActivityContent, activityContent);
		
		$.ajax({
			method: "POST",
			url: "/updateActivity", 
			data: formData,
			mimeType: "mutltipart/form-data",
			contentType: false,
			processData: false,
			success: function(data, status, xhr){
				formObj.remove();
				if(g_callbackOnActivityEditorRemoved!=null){
					g_callbackOnActivityEditorRemoved(0);
				}
			},
			error: function(xhr, status, errorThrown){

			}
		});
	}while(false);
}

function onSubmitFormSubmission(formEvt){
	do{
		formEvt.preventDefault(); // prevent default action.

		var formObj = $(this);
		var formData = new FormData(this);
		/*
		// check files
		var iFiles=0;
		for (iFiles = 0; iFiles < g_maxNumberOfImagesForSingleActivity; iFiles++) {
		 	var imageName=g_indexImageOfActivityPrefix+iFiles;
		 	var imageField=formObj.data(imageName);
		 	var files=imageField.files;
		 	var length=files.length;
		 	if(length>1) break; // invalid field exist
		 	var file=files[0];
		 	var checkResult=isFileValid(file);
		 	if(checkResult==false) break;
		}
		if(iFiles<g_maxNumberOfImagesForSingleActivity) break;
		*/
		// append user token and activity id for identity
		var token = $.cookie(g_keyLoginStatus.toString());
		formData.append(g_keyUserToken, token);

		var activityId = $(this).data(g_keyActivityId);
		formData.append(g_keyActivityId, activityId.toString());
		
		// append activity title and content 
		var activityTitle=$("."+g_classFieldActivityTitle).val();
		formData.append(g_keyActivityTitle, activityTitle);
		
		var activityContent=$("."+g_classFieldActivityContent).val();
		formData.append(g_keyActivityContent, activityContent);
		
		$.ajax({
			method: "POST",
			url: "/submitActivity", 
			data: formData,
			mimeType: "mutltipart/form-data",
			contentType: false,
			processData: false,
			success: function(data, status, xhr){
				formObj.remove();
				if(g_callbackOnActivityEditorRemoved!=null){
					g_callbackOnActivityEditorRemoved(0);
				}
			},
			error: function(xhr, status, errorThrown){

			}
		});
	}while(false);
}

// Assistant functions
function isFileValid(file){
	var ret=false;
	do{
		var fileSizeLimit= (1<<20)// 2 mega bytes
		if(file.size>fileSizeLimit) break;
		ret=true;
	}while(false);
 	return ret;
}

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

	evt.preventDefault();
	var editor=$(this).parent();

	try{
		// assign callback function
		editor.submit(onUpdateFormSubmission);
		// invoke submission
		editor.submit();
	} catch(err){
		
	}
}

function onBtnDeleteClicked(evt){

	evt.preventDefault();
	
	var activityId=$(this).data(g_keyActivityId);
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
						g_callbackOnActivityEditorRemoved(0);
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

	evt.preventDefault();
	var editor=$(this).parent();

	try{
		// assign callback function
		editor.submit(onSubmitFormSubmission);
		// invoke submission
		editor.submit();
	} catch(err){
		
	}
}

function onBtnCancelClicked(evt){
	evt.preventDefault();
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
	 var ret=$('<form>',
	 			{
	 				class: g_classActivityEditor	
	 			});

	 var titleText=$('<p>',
	 			 {
	 			 	html: 'Title'
				 });
	 ret.append(titleText);
	 var titleInput=$('<input>',
	 				{
		 				class: g_classFieldActivityTitle,
		 				type: 'text',
		 				value: activityTitle,
		 			 	name: g_keyActivityTitle
	 				});
	 ret.append(titleInput);
	 
	 var contentText=$('<p>',
	 			   {
				   		html: 'Content'
 				   });

	 ret.append(contentText);
	 var contentInput=$('<TEXTAREA>',
	 				  {
	 				  	class: g_classFieldActivityContent, 
	 				  	name: g_keyActivityContent
	 				  });
	 contentInput.val(activityContent);
	 ret.append(contentInput);

	 for (var i = 0; i < g_maxNumberOfImagesForSingleActivity; i++) {
	 	var imageName=g_indexImageOfActivityPrefix+i;
	 	var imageField=$('<input>',
				 		{
				 			class: g_classFieldImageOfActivity,
							type: 'file',
							name: imageName 	 			
				 		});
	 	ret.append(imageField);
	 	ret.data(imageName, imageField);
	 }

	 var sectionBeginTime=$('<div>');
	 var selectionBeginTimeYear=$('<select>',
						 	{
						 		class: g_classSelectionBeginTime	
						 	});

	 var years=[2014, 2015, 2016, 2017];
	 for(year in years){
	 	var option=$('<option>',
	 				{
	 					value: year.toString(),
	 					text: year.toString(), 
	 				}).appendTo(selectionBeginTimeYear);
	 }
	 ret.append(sectionBeginTime);

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

	 ret.data(g_keyActivityId, activityId);
	 
	 return ret;
}