$(document).ready(function(){

    // initialize local DOMs
    g_sectionLogin=$("#"+g_idSectionLogin);
    g_sectionRegister=$("#"+g_idSectionRegister);

    g_loginUserHandle=$("#idLoginUserHandle");
    g_loginPassword=$("#idLoginPassword");

    g_registerUsername=$("#idRegisterUsername");
    g_registerEmail=$("#idRegisterEmail");
    g_registerPassword=$("#idRegisterPassword");

    g_spanCheckUsername=$("#idSpanCheckUsername");
    g_spanCheckEmail=$("#idSpanCheckEmail");

    // initialize callback functions
    g_callbackOnActivityEditorRemoved=refreshOnLoggedIn;
    g_callbackOnQueryActivitiesSuccess=onQueryActivitiesSuccess;

	// execute on page loaded
	var sectionDefaultActivities=$("#"+g_idSectionDefaultActivities); 
	sectionDefaultActivities.on("scroll", onSectionDefaultActivitiesScrolled);
	checkLoginStatus();

	$("#"+g_idBtnRegister).on("click", onBtnRegisterClicked);
	$("#"+g_idBtnLogin).on("click", onBtnLoginClicked);
	$("#"+g_idBtnCreate).on("click", onBtnCreateClicked);
	$("."+g_classFieldAccount).keypress(function (evt) {
  		if (evt.which == 13) {
  			evt.preventDefault();
    		$("#"+g_idBtnLogin).click();
  		}
	});

    g_registerUsername.on("change keyup paste", function(evt){
        do{
            evt.preventDefault();
            g_spanCheckUsername.empty();
            var username=$(this).val();
            if(username==null || username.length==0) break;

            var params={};
            params["username"]=username;
            $.ajax({
                type: "GET",
                url: "/user/name/duplicate",
                data: params,
                success: function(data, status, xhr){
                    $("#idSpanCheckUsername").text("This username can be used :)");        
                },
                error: function(xhr, status, err){
                    $("#idSpanCheckUsername").text("This username cannot be used :(");        
                }
            });
        }while(false);
    });	
    
    g_registerEmail.on("change keyup paste", function(evt){
        do{
            evt.preventDefault();
            g_spanCheckEmail.empty();
            var email=$(this).val();
            if(email==null || email.length==0) break;
            if(validateEmail(email)==false) break;
            var params={};
            params["email"]=email;
            $.ajax({
                type: "GET",
                url: "/user/email/duplicate",
                data: params,
                success: function(data, status, xhr){
                    $("#idSpanCheckEmail").text("This email can be used :)");        
                },
                error: function(xhr, status, err){
                    $("#idSpanCheckEmail").text("This email cannot be used :(");        
                }
            });
        }while(false);
    });	
	$("#"+g_idBtnPreviousPage).on("click", onBtnPreviousPageClicked);
	$("#"+g_idBtnNextPage).on("click", onBtnNextPageClicked);
});
