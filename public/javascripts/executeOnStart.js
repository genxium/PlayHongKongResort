$(document).ready(function(){
	// execute on page loaded
	refreshOnEnter();
	checkLoginStatus();
	$("#"+g_idBtnRegister).bind("click", onBtnRegisterClicked);
	$("#"+g_idBtnLogin).bind("click", onBtnLoginClicked);
	$("#"+g_idBtnCreate).bind("click", onBtnCreateClicked);
	$("#btnUploadImage").bind("click", onBtnUploadImageClicked);
});
