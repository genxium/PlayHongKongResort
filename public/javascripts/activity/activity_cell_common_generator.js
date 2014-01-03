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
				if(status=="success") return true;
				else return false;
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