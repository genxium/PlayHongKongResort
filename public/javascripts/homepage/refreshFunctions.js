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

	$("#"+g_idBtnCreate).hide();
	$("."+g_classActivityEditor).hide();

	var sectionDefaultActivities=$("#"+g_idSectionDefaultActivities);
	sectionDefaultActivities.show();
}