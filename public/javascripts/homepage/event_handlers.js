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
	    				$("#responseSection").html(jsonResponse['UserId']);
	    				g_userName=jsonResponse['UserEmail'];
	    				// store token in cookie iff query succeeds
	    				$.cookie(g_keyLoginStatus.toString(), jsonResponse['UserToken']);
	    				// refresh screen
	    				refreshOnLoggedIn();
				} else{

	    		}
		    }
	);
}

function onBtnCreateClicked(evt){

	evt.preventDefault();

	targetSection=$("#domainActivities");
	targetSection.empty();
	var userToken=$.cookie(g_keyLoginStatus.toString());
	
	try{
		$.post("/createActivity", 
			{
				UserToken: userToken.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					var jsonActivity=JSON.parse(data);
						var editor=generateActivityEditorByJson(jsonActivity);
						targetSection.append(editor);
						queryActivitiesHostedByUser();
    				} else{
    					
    				}
			}
		);
	} catch(err){
		targetSection.html(err.message);
	}
}