function onBtnRegisterClicked(evt){

	var email=$("#"+g_idFieldEmail).val();
	var password=$("#"+g_idFieldPassword).val();

	$.post("/register",
			{
				UserEmail: email.toString(),
				UserPassword: password.toString()
			},
			// post response callback function
			function(data, status, xhr){
		    		if(status=="success"){
		    			refreshOnEnter();
		    		}
		    		else{
		    			
		    		}
		    }
	);
}

function onBtnLoginClicked(evt){
	
	var email=$("#"+g_idFieldEmail).val();
	var password=$("#"+g_idFieldPassword).val();

	$.post("/login",
			{
				UserEmail: email.toString(),
				UserPassword: password.toString()
			},
			// post response callback function
			function(data, status, xhr){
				if(status=="success"){
					var jsonResponse=JSON.parse(data);
    				g_userName=jsonResponse[g_keyUserEmail];
    				g_userAvatarURL=jsonResponse[g_keyImageURL];
    				// store token in cookie iff query succeeds
    				$.cookie(g_keyLoginStatus.toString(), jsonResponse[g_keyUserToken]);
    				// refresh screen
    				refreshOnLoggedIn();
    				queryDefaultActivitiesByUser(0, g_numItemsPerPage, g_directionForward);
				} else{

	    		}
		    }
	);
}

function onBtnPreviousPageClicked(evt){
	var targetSection=$("#"+g_idSectionDefaultActivities);

	var pageIndex=targetSection.data(g_keyPageIndex);
    var startingIndex=targetSection.data(g_keyStartingIndex);
    var endingIndex=targetSection.data(g_keyEndingIndex);

	var token = $.cookie(g_keyLoginStatus.toString());
	if(token==null){
		queryDefaultActivities(startingIndex, g_numItemsPerPage, g_directionBackward);
	} else{
		queryDefaultActivitiesByUser(startingIndex, g_numItemsPerPage, g_directionBackward, token);
	}
}

function onBtnNextPageClicked(evt){
	var targetSection=$("#"+g_idSectionDefaultActivities);

    var pageIndex=targetSection.data(g_keyPageIndex);
    var startingIndex=targetSection.data(g_keyStartingIndex);
    var endingIndex=targetSection.data(g_keyEndingIndex);

	var token = $.cookie(g_keyLoginStatus.toString());
	if(token==null){
		queryDefaultActivities(endingIndex, g_numItemsPerPage, g_directionForward);
	} else{
		queryDefaultActivitiesByUser(endingIndex, g_numItemsPerPage, g_directionForward, token);
	}
}

function onSectionDefaultActivitiesScrolled(evt){
	if( $(this).scrollTop() + $(this).height() >= $(document).height() ){
		evt.preventDefault();
		alert("Bottom!");
	}
}