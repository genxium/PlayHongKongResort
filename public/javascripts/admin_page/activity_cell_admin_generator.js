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

					var appliedIndicator=$('<div>',
					{
						class: g_classAcceptedIndicator,
						html: 'Accepted'
					});
					cell.append(appliedIndicator);
					cell.data(g_indexStatusIndicator, appliedIndicator);
					
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
				});
	ret.append(statusIndicator);
	 
	// this condition is temporarily hard-coded
	var btnAccept=$('<button>', {
		class: g_classBtnAccept,
		text: 'Accept'
	});
	btnAccept.bind("click", onBtnAcceptClicked);

	btnAccept.data(g_keyActivityId, activityId);
	btnAccept.data(g_keyActivityTitle, activityTitle);
	btnAccept.data(g_keyActivityContent, activityContent);

	ret.append(btnAccept);
	ret.data(g_indexBtnAccept, btnAccept);

	ret.data(g_keyActivityId, activityId);
	ret.data(g_keyActivityTitle, activityTitle);
	ret.data(g_keyActivityContent, activityContent);
	
	return ret;
}