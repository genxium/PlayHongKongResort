// Assistant functions
function refreshOnEnter(){

	$("#"+g_idFieldEmail).empty();
	$("#"+g_idFieldEmail).val("");
	$("#"+g_idFieldPassword).empty();
	$("#"+g_idFieldPassword).val("");

	var sectionUserProfileEditor=$("#"+g_idSectionUserProfileEditor);
	sectionUserProfileEditor.empty();
	sectionUserProfileEditor.hide();

	$("#"+g_idSectionAccount).show();
	
	var sectionActivityEditor=$("#"+g_idSectionActivityEditor);
	sectionActivityEditor.empty();
	sectionActivityEditor.hide();

	var sectionUserInfo=$("#"+g_idSectionUserInfo);
	sectionUserInfo.empty();
	sectionUserInfo.hide();

	$("#"+g_idSectionOwnedActivities).hide();
	$("#"+g_idBtnCreate).hide();
	$("."+g_classActivityEditor).hide();

	var sectionDefaultActivities=$("#"+g_idSectionDefaultActivities);
	sectionDefaultActivities.show();
	queryDefaultActivities();
}

function queryDefaultActivities(){
	var targetSection=$("#"+g_idSectionDefaultActivities);
	targetSection.empty();
	try{
		$.post("/queryDefaultActivities", 
			{

			},
			function(data, status, xhr){
    				if(status=="success"){
    					var jsonResponse=JSON.parse(data);
    					for(var key in jsonResponse){
    						var jsonActivity=jsonResponse[key];
    						var cell=generateDefaultActivityCell(jsonActivity);
							targetSection.append(cell);
    					}
    				} else{
    					
    				}
			}
		);
	} catch(err){

	}
}

// Assistant Handlers
function onBtnEditClicked(evt){

	var activityId=jQuery.data(this, g_keyActivityId);
	var activityTitle=jQuery.data(this, g_keyActivityTitle);
	var activityContent=jQuery.data(this, g_keyActivityContent);
	
	sectionActivityEditor=$("#"+g_idSectionActivityEditor);
	sectionActivityEditor.empty();

	var editor=generateActivityEditor(activityId, activityTitle, activityContent);
	sectionActivityEditor.append(editor);
}

function onMouseEnterOwnedActivityCell(evt){
	
}

function onMouseLeaveOwnedActivityCell(evt){

}

function onMouseEnterDefaultActivityCell(evt){
	var btnJoin=jQuery.data(this, g_indexBtnJoin);
	btnJoin.show();
}

function onMouseLeaveDefaultActivityCell(evt){
	var btnJoin=jQuery.data(this, g_indexBtnJoin);
	btnJoin.hide();
}


function onBtnUpdateClicked(evt){
	
	var activityId=jQuery.data(this, g_keyActivityId);
	var id=activityId;
	var title=$("."+g_classFieldActivityTitle).val();
	var content=$("."+g_classFieldActivityContent).val();
	var token=$.cookie(g_keyLoginStatus.toString());

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
    					refreshOnLoggedIn();
    				} else{
    					
    				}
			}
		);
	} catch(err){
		$("#"+g_idSectionOwnedActivities).html(err.message);
	}
}

function onBtnDeleteClicked(evt){
	
	var activityId=jQuery.data(this, g_keyActivityId);
	var id=activityId;
	var token=$.cookie(g_keyLoginStatus.toString());

	try{
		$.post("/deleteActivity", 
			{
				ActivityId: id.toString(),
				UserToken: token.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					refreshOnLoggedIn();
    				} else{
    					
    				}
			}
		);
	} catch(err){
		$("#"+g_idSectionOwnedActivities).html(err.message);
	}
}

function onBtnSubmitClicked(evt){
	
	var activityId=jQuery.data(this, g_keyActivityId);
	var id=activityId;
	var title=$("."+g_classFieldActivityTitle).val();
	var content=$("."+g_classFieldActivityContent).val();
	var token=$.cookie(g_keyLoginStatus.toString());

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
    					refreshOnLoggedIn();
    				} else{
    					
    				}
			}
		);
	} catch(err){
		$("#"+g_idSectionOwnedActivities).html(err.message);
	}
}

function onBtnCancelClicked(evt){
	$("."+g_classActivityEditor).remove();
	$("#"+g_idBtnCreate).show();
}

function onBtnJoinClicked(evt){
	evt.preventDefault();
	var token = $.cookie(g_keyLoginStatus.toString());
	var activityId=jQuery.data(this, g_keyActivityId);
	var id=parseInt(activityId);
	try{
		$.post("/joinActivity", 
			{
				ActivityId: activityId.toString(),
				UserToken: token.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					refreshOnLoggedIn();
    				} else{
    					
    				}
			}
		);
	} catch(err){
		$("#"+g_idSectionOwnedActivities).html(err.message);
	}
}

function onBtnLogoutClicked(evt){
	var token = $.cookie(g_keyLoginStatus.toString());
	try{
		$.post("/logout", 
			{
				UserToken: token.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					$.removeCookie(g_keyLoginStatus.toString());
    					refreshOnEnter();
    				} else{

    				}
			}
		);
	} catch(err){

	}
}

function onBtnProfileClicked(evt){
	try{
		var userProfileEditorPath="/user_profile_editor.html";
		var profileEditorPage=$('<iframe>',
								{
									class: g_classIFrameUserProfileEditor,
									src: userProfileEditorPath
								});

		var sectionUserProfileEditor=$("#"+g_idSectionUserProfileEditor);
		sectionUserProfileEditor.empty();
		sectionUserProfileEditor.append(profileEditorPage);
	} catch (err){

	}
}

// Generators
function generateActivityCell(jsonActivity){

	var arrayStatusName=['created','pending','rejected','accepted','expired'];

	var activityId=jsonActivity[g_keyActivityId];
	var activityTitle=jsonActivity[g_keyActivityTitle];
	var activityContent=jsonActivity[g_keyActivityContent];
	var activityStatus=jsonActivity[g_keyActivityStatus];

	var ret=$('<div>',
				{
					class: g_classCellActivityContainer
				});

	var cellActivityTitle=$('<div>',
				{	
					class: g_classCellActivityTitle,
					html: activityTitle
				});
	ret.append(cellActivityTitle);

	var cellActivityContent=$('<div>',
				{
					class: g_classCellActivityContent,
					html: activityContent
				});

	ret.append(cellActivityContent);

	var statusIndicator=$('<div>',{
					class: g_classActivityStatusIndicator,
					html: arrayStatusName[parseInt(activityStatus)] 
				});
	ret.append(statusIndicator);
	
	if(parseInt(activityStatus)==0){ 
		// this condition is temporarily hard-coded
		var btnEdit=$('<button>', {
			class: g_classBtnEdit,
			text: 'Edit'
		});
		btnEdit.bind("click", onBtnEditClicked);

		btnEdit.data(g_keyActivityId, activityId);
		btnEdit.data(g_keyActivityTitle, activityTitle);
		btnEdit.data(g_keyActivityContent, activityContent);

		ret.append(btnEdit);
	}
	
	ret.data(g_keyActivityId, activityId);
	ret.data(g_keyActivityTitle, activityTitle);
	ret.data(g_keyActivityContent, activityContent);

	
	return ret;
}

function generateDefaultActivityCell(jsonRecord){

	var activityId=jsonRecord[g_keyActivityId];
	var activityTitle=jsonRecord[g_keyActivityTitle];
	var activityContent=jsonRecord[g_keyActivityContent];
	var userActivityRelationId=jsonRecord[g_keyUserActivityRelationId];
	
	var ret=$('<div>',
				{
					class: g_classCellActivityContainer
				});

	var cellActivityTitle=$('<div>',
				{	
					class: g_classCellActivityTitle,
					html: activityTitle
				});
	ret.append(cellActivityTitle);

	var cellActivityContent=$('<div>',
				{
					class: g_classCellActivityContent,
					html: activityContent
				});

	ret.append(cellActivityContent);

	if(userActivityRelationId==null){
		var btnJoin=$('<button>',
					{
						class: g_classBtnJoin,
						text: 'Join'
					});
		btnJoin.data(g_keyActivityId, activityId);
		btnJoin.bind("click", onBtnJoinClicked);
		ret.append(btnJoin);

		ret.bind("mouseenter", onMouseEnterDefaultActivityCell);
		ret.bind("mouseleave", onMouseLeaveDefaultActivityCell);
		btnJoin.hide();
	} else{
		var appliedIndicator=$('<div>',
							{
								class: g_classAppliedIndicator,
								html: 'Applied'
							});
		ret.append(appliedIndicator);
	}
	
	ret.data(g_indexBtnJoin, btnJoin);
	ret.data(g_keyActivityId, activityId);
	ret.data(g_keyActivityTitle, activityTitle);
	ret.data(g_keyActivityContent, activityContent);
	

	return ret;
}

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

	 var formContainer=$('<div>',
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
	 ret.append(formContainer);
	 
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

function generateLoggedInUserMenu(){

	var ret=$('<div>',
			{
				class: g_classLoggedInUserMenu
			});

	var btnLogout=$('<button>',
			{
				class: g_classBtnLogout,
				text: 'Logout'		
			});
	btnLogout.bind("click", onBtnLogoutClicked);
	ret.append(btnLogout);
	ret.data(g_indexBtnLogout, btnLogout);

	var btnProfile=$('<button>',
			{
				class: g_classBtnProfile,
				text: 'Profile'
			});

	btnProfile.bind("click", onBtnProfileClicked);
	ret.append(btnProfile);
	ret.data(g_indexBtnProfile, btnProfile);

	return ret;
}
