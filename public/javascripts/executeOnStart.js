$(document).ready(function(){
	// execute on page loaded
	refreshOnEnter();
	checkLoginStatus();
	$("#btnCheckConnection").bind("click", onBtnCheckConnectionClicked);
	$("#btnRegister").bind("click", onBtnRegisterClicked);
	$("#btnLogin").bind("click", onBtnLoginClicked);
	$("#btnSave").bind("click", onBtnSaveClicked);
	$("#btnSubmit").bind("click", onBtnSubmitClicked);
	$("#btnUploadImage").bind("click", onBtnUploadImageClicked);
	$("#btnLogout").bind("click", onBtnLogoutClicked);
});
