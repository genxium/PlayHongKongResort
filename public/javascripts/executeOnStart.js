$(document).ready(function(){
	// execute on page loaded
	refreshOnEnter();
	checkLoginStatus();
	$("#btnRegister").bind("click", onBtnRegisterClicked);
	$("#btnLogin").bind("click", onBtnLoginClicked);
	$("#idBtnCreate").bind("click", onBtnCreateClicked);
	$("#btnSubmit").bind("click", onBtnSubmitClicked);
	$("#btnUploadImage").bind("click", onBtnUploadImageClicked);
	$("#btnLogout").bind("click", onBtnLogoutClicked);
});
