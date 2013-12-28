// Assistant functions
function refreshOnEnter(){

	$("#"+g_idFieldEmail).empty();
	$("#"+g_idFieldPassword).empty();

	$("#sectionAccount").show();
	$("#domainActivities").hide();
	$("#sectionImage").hide();
	$("#sectionProgress").hide();

	var sectionUserInfo=$("#"+g_idSectionUserInfo);
	sectionUserInfo.empty();
	sectionUserInfo.hide();

	$("#sectionActivities").hide();
	$("#"+g_idBtnCreate).hide();
}

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
	var title=$("."+g_classActivityTitle).val();
	var content=$("."+g_classActivityContent).val();
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
				token: token.toString()
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