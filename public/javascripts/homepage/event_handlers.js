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
    				queryDefaultActivitiesByUser();
				} else{

	    		}
		    }
	);
}

function onSectionDefaultActivitiesScrolled(evt){
	if( $(this).scrollTop() + $(this).height() >= $(document).height() ){
		alert("Bottom!");
	}
}