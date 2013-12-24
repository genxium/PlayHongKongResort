$(document).ready(function(){
	// execute on page loaded
	refreshOnEnter();
	checkLoginStatus();
	$("#btnCheckConnection").bind("click", onBtnCheckConnectionClicked);
	$("#btnRegister").bind("click", onBtnRegisterClicked);
	$("#btnLogin").bind("click", onBtnLoginClicked);
	$("#btnCreate").bind("click", onBtnCreateClicked);
	$("#btnSubmit").bind("click", onBtnSubmitClicked);
	$("#btnUploadImage").bind("click", onBtnUploadImageClicked);
	$("#btnLogout").bind("click", onBtnLogoutClicked);
});
