function checkConnection(){
	$("#jspData").html("Calling check function");
	// this POST path is defined in conf/routes
	$.post("/checkConnection",
			{
			},
			// post response callback function
			function(data, status, xhr){
				if(status=="success"){
		    			$("#jspData").html("Connection: "+data);
		    		}
		    		else{
		    			$("#jspData").html("Query failed");
		    		}
		    }
	);
}

function login(){
	$("#jspData").html("Calling login function");
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
		    			$("#jspData").html("user: "+data);
		    			// store token in cookie iff query succeeds
		    			$.cookie(loginStatusTokenKey.toString(), data.toString());
		    		}
		    		else{
		    			$("#jspData").html("jsp query failed");
		    		}
		    }
	);
}

function register(){
	$("#jspData").html("Calling register function");
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
		    			$("#jspData").html("SQL query: "+data);
		    		}
		    		else{
		    			$("#jspData").html("Query failed");
		    		}
		    }
	);
}

function checkLoginStatus(){
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
		    			$("#jspData").html(data);
		    		}
		    		else{
		    			$("#jspData").html("Query failed");
		    		}
		    }
	);
}

function ajaxUpload(){
	$("#imageSection").submit( function(e){
		var formObj = $(this);
		var formURL = formObj.attr("action");
		var formData = new FormData(this);
		if(validateImage()==false) return; 
		$.ajax({
			method: "POST",
			url: "/uploadingHandler", 
			data: formData,
			mimeType: "mutltipart/form-data",
			contentType: false,
			processData: false,
			success: function(data, status, xhr){
		    			if(status=="success"){
		    				$("#jspData").html(data);
		    			}
		    			else{
		    				$("#jspData").html("Upload failed");
		    			}
			},
			error: function(xhr, status, errorThrown){
				
			}
		});
		e.preventDefault(); //Prevent Default action.
	});
	$("#imageSection").submit();
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