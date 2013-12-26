function onBtnRegisterClicked(evt){
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
		    			refreshOnEnter();
		    		}
		    		else{
		    			
		    		}
		    }
	);
}

function onBtnLoginClicked(evt){
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
					var jsonResponse=JSON.parse(data);
	    				$("#responseSection").html(jsonResponse['userId']);
	    				g_userName=jsonResponse['email'];
	    				// store token in cookie iff query succeeds
	    				$.cookie(g_keyLoginStatus.toString(), jsonResponse['token']);
	    				// refresh screen
	    				refreshOnLoggedIn();
				} else{
	    				$("#responseSection").html("Query failed");
	    			}
		    }
	);
}

function onBtnUpdateClicked(evt){
	if (!evt) {evt = window.event;}

	var id=g_editingActivityId;
	var title=$("#activityTitle").val();
	var content=$("#activityContent").val();
	var token=$.cookie(g_keyLoginStatus.toString());

	try{
		$.post("/updateActivity", 
			{
				activityId: id.toString(),
				activityTitle: title.toString(),
				activityContent: content.toString(),
				token: token.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					refreshOnLoggedIn();
    				} else{
    					
    				}
			}
		);
	} catch(err){
		$("#sectionActivities").html(err.message);
	}
}

function onBtnCreateClicked(evt){

	if (!evt) {evt = window.event;}
	evt.preventDefault();

	$("#domainActivities").empty();
	var userToken=$.cookie(g_keyLoginStatus.toString());

	try{
		$.post("/createActivity", 
			{
				token: userToken.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					var jsonActivity=JSON.parse(data);
						var editor=generateBlankActivityEditor();
						$("#domainActivities").append(editor);
    				} else{
    					
    				}
			}
		);
	} catch(err){
		$("#sectionActivities").html(err.message);
	}
}

function onBtnSubmitClicked(evt){
	if (!evt) {evt = window.event;}
	evt.preventDefault();
    /*
	$("#formActivity").submit( function(evt){
		evt.preventDefault();
	});

	$("#formActivity").submit();
	*/
}

function onBtnUploadImageClicked(evt){

	if(validateImage()==false){
		return;
	}
	
	$("#imageForm").submit( function(e){
		var formObj = $(this);
		var formData = new FormData(this);
		
		// append an user token for identity
		var token = $.cookie(g_keyLoginStatus.toString());
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

function onBtnLogoutClicked(evt){
	var token = $.cookie(g_keyLoginStatus.toString());
	try{
		$.post("/logout", 
			{
				token: token.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					$.removeCookie(g_keyLoginStatus.toString());
    					refreshOnEnter();
    				} else{

    				}
			}
		);
	} catch(err){

	}
}