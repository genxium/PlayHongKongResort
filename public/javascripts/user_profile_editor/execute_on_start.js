$(document).ready(function(){
	// execute on page loaded
	var sectionOwnedActivities=$("#"+g_idSectionOwnedActivities); 
	sectionOwnedActivities.bind("scroll", onSectionOwnedActivitiesScrolled);
	sectionOwnedActivities.data(g_keyPageIndex, 0);

	$("#"+g_idBtnUploadAvatar).bind("click", onBtnUploadAvatarClicked);
 	g_callbackOnActivityEditorRemoved=queryActivitiesHostedByUser;
 	queryActivitiesHostedByUser(0, g_numItemsPerPage, g_directionForward);

 	$("#"+g_idBtnPreviousPage).bind("click", onBtnPreviousPageClicked);
	$("#"+g_idBtnNextPage).bind("click", onBtnNextPageClicked);
});
