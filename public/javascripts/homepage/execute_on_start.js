$(document).ready(function(){

    // initialize local DOMs
	g_sectionLogin=$("#idSectionLogin");
	var loginForm=generateLoginForm();
    g_sectionLogin.append(loginForm);

	g_callbackOnLoggedIn=function(){
		refreshOnLoggedIn();
		queryDefaultActivities(0, g_numItemsPerPage, g_directionForward);
	};

	g_callbacnOnEnter=function(){
		refreshOnEnter();
		queryDefaultActivities(0, g_numItemsPerPage, g_directionForward);
	};

	g_sectionRegister=$("#idSectionRegister");
	var registerForm=generateRegisterForm();
    g_sectionRegister.append(registerForm);

    // initialize callback functions
    g_callbackOnActivityEditorRemoved=refreshOnLoggedIn;
    g_callbackOnQueryActivitiesSuccess=onQueryActivitiesSuccess;

	// execute on page loaded
	var sectionDefaultActivities=$("#"+g_idSectionDefaultActivities); 
	sectionDefaultActivities.on("scroll", onSectionDefaultActivitiesScrolled);
	checkLoginStatus();

	$("#"+g_idBtnCreate).on("click", onBtnCreateClicked);
	$("."+g_classFieldAccount).keypress(function (evt) {
  		if (evt.which == 13) {
  			evt.preventDefault();
    		$("#"+g_idBtnLogin).click();
  		}
	});

	$("#"+g_idBtnPreviousPage).on("click", onBtnPreviousPageClicked);
	$("#"+g_idBtnNextPage).on("click", onBtnNextPageClicked);
});
