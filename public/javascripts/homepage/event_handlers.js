function onBtnRegisterClicked(evt){

    do{
        var username=g_registerUsername.val();
        var email=g_registerEmail.val();
        var password=g_registerPassword.val();

        if(username==null || username.length==0
            || email==null || email.length==0
            || password==null || password.length==0) break;

        var params={};
        params[g_keyUsername]=username;
        params[g_keyUserEmail]=email;
        params[g_keyUserPassword]=password;

        $.ajax({
            type: "POST",
            url: "/user/register",
            data: params,
            success: function(data, status, xhr){
                refreshOnEnter();
            },
            error: function(xhr, status, err){
                
            }
        });
    }while(false);
}

function onBtnLoginClicked(evt){
	
    do{
        var email=g_loginUserHandle.val();
        var password=g_loginPassword.val();

        if( (email==null || email.length==0 || validateEmail(email)==false)
            || password==null || password.length==0) break;

        var params={};
        params[g_keyUserEmail]=email;
        params[g_keyUserPassword]=password;
        
        $.ajax({
            type: "POST",
            url: "/user/login",
            data: params,
            success: function(data, status, xhr){
                var jsonResponse=JSON.parse(data);
                g_userName=jsonResponse[g_keyUserEmail];
                g_userAvatarURL=jsonResponse[g_keyImageURL];
                // store token in cookie iff query succeeds
                $.cookie(g_keyLoginStatus.toString(), jsonResponse[g_keyUserToken]);
                // refresh screen
                refreshOnLoggedIn();
                queryDefaultActivities(0, g_numItemsPerPage, g_directionForward);
            },
            error: function(xhr, status, err){
                
            }
        });
    }while(false);
}

function onBtnPreviousPageClicked(evt){
	var targetSection=$("#"+g_idSectionDefaultActivities);
	var pageIndex=targetSection.data(g_keyPageIndex);
    var startingIndex=targetSection.data(g_keyStartingIndex);
    var endingIndex=targetSection.data(g_keyEndingIndex);

    queryDefaultActivities(startingIndex, g_numItemsPerPage, g_directionBackward);
}

function onBtnNextPageClicked(evt){
	var targetSection=$("#"+g_idSectionDefaultActivities);

    var pageIndex=targetSection.data(g_keyPageIndex);
    var startingIndex=targetSection.data(g_keyStartingIndex);
    var endingIndex=targetSection.data(g_keyEndingIndex);

    queryDefaultActivities(endingIndex, g_numItemsPerPage, g_directionForward);
}

function onSectionDefaultActivitiesScrolled(evt){
	if( $(this).scrollTop() + $(this).height() >= $(document).height() ){
		evt.preventDefault();
		alert("Bottom!");
	}
}
