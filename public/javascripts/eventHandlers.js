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

	if (!evt) {evt = window.event;}
	evt.preventDefault();
	var sender = (evt.srcElement || evt.target);

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

function onBtnSubmitClicked(evt){
	if (!evt) {evt = window.event;}
	evt.preventDefault();
	var sender = (evt.srcElement || evt.target);
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