function onRegisterClicked(evt){
	$("#responseSection").html("Calling register function");
	var email=$("#emailField").val();
	var password=$("#passwordField").val();

	$.post("/register",
			{
				email: email.toString(),
				password: password.toString()
			},
			// post response callback function
			function(data, status, xhr){
		    		if(status=="success"){
		    			$("#responseSection").html("SQL query: "+data);
		    		}
		    		else{
		    			$("#responseSection").html("Query failed");
		    		}
		    }
	);
}

function onLoginClicked(evt){
	$("#responseSection").html("Calling login function");
	var email=$("#emailField").val();
	var password=$("#passwordField").val();

	$.post("/login",
			{
				email: email.toString(),
				password: password.toString()
			},
			// post response callback function
			function(data, status, xhr){
				if(status=="success"){
					var obj=JSON.parse(data);
	    			$("#responseSection").html(obj['userId']);
	    			userName=obj['email'];
	    			// store token in cookie iff query succeeds
	    			$.cookie(loginStatusTokenKey.toString(), obj['token']);
	    			// refresh screen
	    			refreshOnLoggedIn();
		    	}
	    		else{
	    			$("#responseSection").html("Query failed");
	    		}
		    }
	);
}

function onSaveButtonClicked(evt){

}

function onCreateButtonClicked(evt){
	
}

function onUploadClicked(evt){

}