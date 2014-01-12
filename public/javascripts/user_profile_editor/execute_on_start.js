$(document).ready(function(){
	// execute on page loaded
	var sectionOwnedActivities=$("#"+g_idSectionOwnedActivities); 
	sectionOwnedActivities.bind("scroll", onSectionOwnedActivitiesScrolled);
	sectionOwnedActivities.data(g_pageIndexKey, 0);

	$("#"+g_idBtnUploadAvatar).bind("click", onBtnUploadAvatarClicked);
 	g_callbackOnActivityEditorRemoved=queryActivitiesHostedByUser;
 	queryActivitiesHostedByUser(0);

 	$("#"+g_idBtnPreviousPage).bind("click", onBtnPreviousPageClicked);
	$("#"+g_idBtnNextPage).bind("click", onBtnNextPageClicked);
});