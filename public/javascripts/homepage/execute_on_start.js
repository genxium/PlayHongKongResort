$(document).ready(function(){
	// execute on page loaded
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
