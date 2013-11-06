function checkConnection(evt){

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

function login(evt){

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

function register(evt){

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

function checkLoginStatus(evt){

	var token = $.cookie(loginStatusTokenKey.toString());
	validateToken(token);
}

function validateToken(token){
	$.post("/checkLoginStatus",
			{
				token: token.toString(),
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
	    			$("#responseSection").html("data");
	    		}
		    }
	);
}

function ajaxUploadImage(){
	
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

function validateImage(){
	var file = document.getElementById("picture");
	var fileName = file.value;
	var ext = fileName.split('.').pop().toLowerCase();
	if($.inArray(ext, ['gif','png','jpg','jpeg']) == -1) {
	    alert('invalid extension!');
	    return false;
	}
	return true;
}

function validateEmail(){
	var x=document.getElementById("emailField").value;
	var atpos=x.indexOf("@");
	var dotpos=x.lastIndexOf(".");
	if (atpos<1 || dotpos<atpos+2 || dotpos+2>=x.length) {
	  alert("Not a valid e-mail address");
	  return false;
	}
	return true;
}

function saveActivity(evt){

	if (!evt) {evt = window.event;}
    var sender = (evt.srcElement || evt.target);
    sender.toggle("scale");

	$("#activityForm").submit( function(e){
		var formObj = $(this);
		var formData = new FormData(this);
		
		// append an user token for identity
		var token = $.cookie(loginStatusTokenKey.toString());
		formData.append("token", token);
		
		$.ajax({
			method: "POST",
			url: "/saveActivity", 
			data: formData,
			success: function(data, status, xhr){
	    			if(status=="success"){
	    				$("#activityDescription").html(data);
	    			}
	    			else{
	    				$("#activityDescription").html("Save failed");
	    			}
			},
			error: function(xhr, status, errorThrown){
				
			}
		});
		e.preventDefault(); // prevent default action.
	});

	$("#activityForm").submit();
}

function createActivity(evt){

	if (!evt) {evt = window.event;}
    var sender = (evt.srcElement || evt.target);
    sender.toggle("scale");
    /*
	$("#activityForm").submit( function(evt){
		evt.preventDefault();
	});

	$("#activityForm").submit();
	*/
}

function refreshOnEnter(){
	$("#accountSection").show();
	$("#activitySection").hide();
	$("#imageSection").hide();
	$("#progressSection").hide();
}

function refreshOnLoggedIn(){
	$("#accountSection").hide();
	$("#activitySection").show();
	$("#imageSection").show();
	//$("#progressSection").show();
	$("#userInformationSection").html("Hello, "+userName.toString());
}