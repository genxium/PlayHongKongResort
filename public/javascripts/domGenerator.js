// Assistant functions
function refreshOnEnter(){

	$("#"+g_idFieldEmail).empty();
	$("#"+g_idFieldPassword).empty();

	$("#sectionAccount").show();
	
	var domainActivities=$("#domainActivities");
	domainActivities.empty();
	domainActivities.hide();
	
	$("#sectionImage").hide();
	$("#sectionProgress").hide();

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

function onMouseEnterActivityCell(evt){
	
}

function onMouseLeaveActivityCell(evt){}


function onBtnUpdateClicked(evt){
	if (!evt) {evt = window.event;}
	evt.preventDefault();
	var sender = (evt.srcElement || evt.target);
	
	var activityId=jQuery.data(sender, g_keyActivityId);
	var id=activityId;
	var title=$("."+g_classActivityTitle).val();
	var content=$("."+g_classActivityContent).val();
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
	if (!evt) {evt = window.event;}
	evt.preventDefault();
	var sender = (evt.srcElement || evt.target);
	
	var activityId=jQuery.data(sender, g_keyActivityId);
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
	if (!evt) {evt = window.event;}
	evt.preventDefault();
	var sender = (evt.srcElement || evt.target);
	
	var activityId=jQuery.data(sender, g_keyActivityId);
	var id=activityId;
	var title=$("."+g_classActivityTitle).val();
	var content=$("."+g_classActivityContent).val();
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
	if (!evt) {evt = window.event;}
	evt.preventDefault();
	var sender = (evt.srcElement || evt.target);

	$("."+g_classActivityEditor).remove();
	$("#"+g_idBtnCreate).show();
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

// Generators
function generateActivityCell(jsonActivity){

	var arrayStatusName=['created','pending','rejected','accepted','expired'];

	var activityId=jsonActivity[g_keyActivityId];
	var activityTitle=jsonActivity[g_keyActivityTitle];
	var activityContent=jsonActivity[g_keyActivityContent];
	var activityStatus=jsonActivity[g_keyActivityStatus];

	var ret=$('<div>',
				{
					class: 'cellActivity'
				});
	var cellContent=$('<div>',
				{
					class: g_classCellActivityContent,
					html: activityId+" "+activityTitle+" "+activityContent+"<br/>"
				});
	ret.append(cellContent);

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

function generateDefaultActivityCell(jsonActivity){

	var activityId=jsonActivity[g_keyActivityId];
	var activityTitle=jsonActivity[g_keyActivityTitle];
	var activityContent=jsonActivity[g_keyActivityContent];

	var ret=$('<div>',
				{
					class: 'cellActivity'
				});
	var cellContent=$('<div>',
				{
					class: g_classCellActivityContent,
					html: activityId+" "+activityTitle+" "+activityContent+"<br/>"
				});
	ret.append(cellContent);
	
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
		 				class: g_classActivityTitle,
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
	 				  	class: g_classActivityContent, 
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

			});
	ret.id=g_idLoggedInUserMenu;

	var btnLogout=$('<button>',
			{
				class: g_classBtnLogout,
				text: 'Logout'		
			});
	btnLogout.bind("click", onBtnLogoutClicked);

	ret.append(btnLogout);

	return ret;
}