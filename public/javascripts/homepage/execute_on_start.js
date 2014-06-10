$(document).ready(function(){

    // initialize local DOMs
	g_sectionLogin=$("#idSectionLogin");
	var loginForm=generateLoginForm();
    g_sectionLogin.append(loginForm);

	g_callbackOnLoginSuccess=function(){
		refreshOnLoggedIn();
		queryActivities(0, g_numItemsPerPage, g_directionForward);
	};

	g_callbackOnLoginError=null;

	g_callbackOnEnter=function(){
		refreshOnEnter();
		queryActivities(0, g_numItemsPerPage, g_directionForward);
	};

	g_callbackOnRegisterSuccess=function(){
		refreshOnEnter();
		queryActivities(0, g_numItemsPerPage, g_directionForward);
	}

	g_callbackOnRegisterError=null;

	g_sectionRegister=$("#idSectionRegister");
	var registerForm=generateRegisterForm();
    g_sectionRegister.append(registerForm);

	initActivityEditor();
	 
	// initialize callback functions
    g_callbackOnActivityEditorRemoved=refreshOnLoggedIn;
    g_callbackOnQueryActivitiesSuccess=onQueryActivitiesSuccess;

	// execute on page loaded
	g_sectionDefaultActivities=$("#idSectionDefaultActivities"); 
	g_sectionDefaultActivities.on("scroll", onSectionDefaultActivitiesScrolled);
	checkLoginStatus();

	$("#"+g_idBtnPreviousPage).on("click", onBtnPreviousPageClicked);
	$("#"+g_idBtnNextPage).on("click", onBtnNextPageClicked);
});
