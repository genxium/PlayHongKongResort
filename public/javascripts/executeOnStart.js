$(document).ready(function(){
	// execute on page loaded
	refreshOnEnter();
	checkLoginStatus();
	$("#checkConnectionButton").bind("click", onCheckConnectionClicked);
	$("#registerButton").bind("click", onRegisterButtonClicked);
	$("#loginButton").bind("click", onLoginButtonClicked);
	$("#saveButton").bind("click", onSaveButtonClicked);
	$("#createButton").bind("click", onCreateButtonClicked);
	$("#uploadImageButton").bind("click", onUploadImageButtonClicked);
});
