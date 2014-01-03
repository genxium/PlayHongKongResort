$(document).ready(function(){
	// execute on page loaded
	$("#"+g_idBtnUploadAvatar).bind("click", onBtnUploadAvatarClicked);
 	g_callbackOnActivityEditorRemoved=queryActivitiesHostedByUser;
 	g_callbackOnActivityEditorRemoved();
});