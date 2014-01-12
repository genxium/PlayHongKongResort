$(document).ready(function(){
	// execute on page loaded
	var sectionDefaultActivities=$("#"+g_idSectionDefaultActivities); 
	sectionDefaultActivities.bind("scroll", onSectionDefaultActivitiesScrolled);
	sectionDefaultActivities.data(g_indexSectionDefaultActivitiesPageIndex, 0);

	checkLoginStatus();
	g_callbackOnActivityEditorRemoved=refreshOnLoggedIn;
	$("#"+g_idBtnRegister).bind("click", onBtnRegisterClicked);
	$("#"+g_idBtnLogin).bind("click", onBtnLoginClicked);
	$("#"+g_idBtnCreate).bind("click", onBtnCreateClicked);
	$("."+g_classFieldAccount).keypress(function (evt) {
  		if (evt.which == 13) {
  			evt.preventDefault();
    		$("#"+g_idBtnLogin).click();
  		}
	});
});
