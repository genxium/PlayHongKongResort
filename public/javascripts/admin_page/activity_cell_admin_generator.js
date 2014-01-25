// Assistant Handlers
function onBtnAcceptClicked(evt){

	evt.preventDefault();
	var btnAccept=this;
	var token = $.cookie(g_keyLoginStatus.toString());
	var activityId=$(this).data(g_keyActivityId);

	try{
		$.post("/acceptActivity", 
			{
				ActivityId: activityId.toString(),
				UserToken: token.toString()
			},
			function(data, status, xhr){
				if(status=="success"){

					var cellNode=btnAccept.parentNode; // javascript dom element
					var cell=$(cellNode); // convert to jQuery element object
					btnAccept.remove();

					var acceptedIndicator=$('<div>',
					{
						class: g_classAcceptedIndicator,
						html: 'Accepted'
					}).appendTo(cell);
					cell.data(g_indexStatusIndicator, acceptedIndicator);
					
				} else{

				}
			}
		);
	} catch(err){

	}
}

function onBtnDeleteClicked(evt){
	evt.preventDefault();
	var btnDelete=this;
	var token = $.cookie(g_keyLoginStatus.toString());
	var activityId=$(this).data(g_keyActivityId);

	try{
		$.post("/deleteActivityByAdmin", 
			{
				ActivityId: activityId.toString(),
				UserToken: token.toString()
			},
			function(data, status, xhr){
				if(status=="success"){

					var cellNode=btnDelete.parentNode; // javascript dom element
					var cell=$(cellNode); // convert to jQuery element object
					btnDelete.remove();

					var deletedIndicator=$('<div>',
					{
						class: g_classDeletedIndicator,
						html: 'Deleted'
					}).appendTo(cell);
					cell.data(g_indexStatusIndicator, deletedIndicator);
					
				} else{

				}
			}
		);
	} catch(err){

	}
}

// Generators
function generateActivityCellForAdmin(activityJson){
	var arrayStatusName=['created','pending','rejected','accepted','expired'];

	var activityId=activityJson[g_keyActivityId];
	var activityTitle=activityJson[g_keyActivityTitle];
	var activityContent=activityJson[g_keyActivityContent];
	var activityStatus=activityJson[g_keyActivityStatus];
	var coverImageURL=activityJson[g_keyImageURL];

	var ret=$('<div>',
				{
					class: g_classCellActivityContainer
				});

	if(coverImageURL!=null){
		var coverImage=$('<img>',
							{
								class: g_classActivityCoverImage,
								src: coverImageURL
							});
		ret.append(coverImage);
	}

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
				}).appendTo(ret);
	
	// this condition is temporarily hard-coded
	if(parseInt(activityStatus)==1){
        var btnAccept=$('<button>', {
            class: g_classBtnAccept,
            text: 'Accept'
        }).appendTo(ret);
        btnAccept.bind("click", onBtnAcceptClicked);
        btnAccept.data(g_keyActivityId, activityId);
        ret.data(g_indexBtnAccept, btnAccept);
    }

    if(parseInt(activityStatus)==3){
        var btnDelete=$('<button>', {
            class: g_classBtnDelete,
            text: 'Delete'
        }).appendTo(ret);
        btnDelete.bind("click", onBtnDeleteClicked);
        btnDelete.data(g_keyActivityId, activityId);
        ret.data(g_indexBtnDelete, btnDelete);
    }
    
	ret.data(g_keyActivityId, activityId);
	ret.data(g_keyActivityTitle, activityTitle);
	ret.data(g_keyActivityContent, activityContent);
	
	return ret;
}
