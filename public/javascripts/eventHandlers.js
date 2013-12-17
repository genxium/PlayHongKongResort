function onCheckConnectionClicked(evt){
	$("#responseSection").html("Calling check function");
	// this POST path is defined in conf/routes
	$.post("/checkConnection",
			{
			},
			// post response callback function
			function(data, status, xhr){
				if(status=="success"){
		    			$("#responseSection").html("Connection: "+data);
		    		}
		    		else{
		    			$("#responseSection").html("Query failed");
		    		}
		    }
	);
}

function onRegisterButtonClicked(evt){
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

function onLoginButtonClicked(evt){
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
	
	if (!evt) {evt = window.event;}
    var sender = (evt.srcElement || evt.target);	
    toggleScaling(sender);
	 
	var title=$("#activityTitle").val();
	var content=$("#activityContent").val();
	var token=$.cookie(loginStatusTokenKey.toString());

	try{
		$.post("/saveActivity", 
			{
				activityTitle: title.toString(),
				activityContent: content.toString(),
				token: token.toString()
			},
			function(data, status, xhr){
    			if(status=="success"){

    			}
    			else{

    			}
			}
		);
	} catch(err){
		$("#activityContent").html(err.message);
	}
}

function onSubmitButtonClicked(evt){
	if (!evt) {evt = window.event;}
    var sender = (evt.srcElement || evt.target);
    toggleScaling(sender);
    /*
	$("#activityForm").submit( function(evt){
		evt.preventDefault();
	});

	$("#activityForm").submit();
	*/
}

function onUploadImageButtonClicked(evt){

	if(validateImage()==false){
		return;
	}
	
	$("#imageForm").submit( function(e){
		var formObj = $(this);
		var formData = new FormData(this);
		
		// append an user token for identity
		var token = $.cookie(loginStatusTokenKey.toString());
		formData.append("token", token);
		
		$.ajax({
			method: "POST",
			url: "/uploadingHandler", 
			data: formData,
			mimeType: "mutltipart/form-data",
			contentType: false,
			processData: false,
			success: function(data, status, xhr){
		    			if(status=="success"){
		    				$("#responseSection").html(data);
		    			}
		    			else{
		    				$("#responseSection").html("Upload failed");
		    			}
			},
			error: function(xhr, status, errorThrown){
				
			}
		});
		e.preventDefault(); // prevent default action.
	});
	$("#imageForm").submit();
}