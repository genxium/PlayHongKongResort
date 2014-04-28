$(document).ready(function(){

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
    $("#"+g_idBtnCheckUsernameDuplicate).on("click", function(){
        do{
            var username=$("#"+g_idFieldUsername).val();
            if(username==null || username.length==0) break;
            var params={};
            params["username"]=username;
            $.ajax({
                type: "GET",
                url: "/user/duplicate",
                data: params,
                success: function(data, status, xhr){
                    
                },
                error: function(xhr, status, err){
                    
                }
            });
        }while(false);
    });	
	$("#"+g_idBtnPreviousPage).on("click", onBtnPreviousPageClicked);
	$("#"+g_idBtnNextPage).on("click", onBtnNextPageClicked);
});
