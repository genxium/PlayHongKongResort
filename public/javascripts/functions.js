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

function upload(){
	var file =  document.getElementById('picture');
	var fileName=file.value;
	$("#jspData").html(fileName);
}