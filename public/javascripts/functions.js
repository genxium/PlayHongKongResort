function queryAllPost(){
	$.post("../../JavaServer/controller.jsp",
          	{
      			param: 'this is a javascript call'
            },
            // post response callback function
		    function(data, status, xhr){
		    	if(status=="success"){
		    		$("#jspData").html(data);
		    	}
		    	else{
		    		$("#jspData").html("jsp query failed");
		    	}
		    }
   	);
}

function checkConnection(){
	$("#jspData").html("Calling check function");
	var email=$("#emailField").val();
	var password=$("#passwordField").val();
	// this POST path is defined in conf/routes
	$.post("/checkConnection",
			{
				r: 0, // checkConnection is request type 1
				email: email.toString(),
				password: password.toString()
			},
			// post response callback function
			function(data, status, xhr){
				if(status=="success"){
		    			$("#jspData").html("user: "+data);
		    		}
		    		else{
		    			$("#jspData").html("jsp query failed");
		    		}
		    }
	);
}

function login(){
	$("#jspData").html("Calling login function");
	var email=$("#emailField").val();
	var password=$("#passwordField").val();

	$.post("../../JavaServer/controller.jsp",
			{
				r: 1, // login is request type 1
				email: email.toString(),
				password: password.toString()
			},
			// post response callback function
			function(data, status, xhr){
		    	if(status=="success"){
		    		$("#jspData").html("user: "+data);
		    		// store token in cookie iff query succeeds
                    $.cookie(loginStatusTokenKey.toString(),data.toString());
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

	$.post("../../JavaServer/controller.jsp",
			{
				r: 2, // register is request type 2
				email: email.toString(),
				password: password.toString()
			},
			// post response callback function
			function(data, status, xhr){
		    	if(status=="success"){
		    		$("#jspData").html("user: "+data);
		    	}
		    	else{
		    		$("#jspData").html("jsp query failed");
		    	}
		    }
	);
}

function checkLoginStatus(){
	var token = $.cookie(loginStatusTokenKey.toString());
	validateToken(token);
}

function validateToken(token){
	$.post("../../JavaServer/controller.jsp",
			{
				r: 3, // validateToken is request type 3
				token: token.toString(),
			},
			// post response callback function
			function(data, status, xhr){
		    	if(status=="success"){
		    		$("#jspData").html(data);
		    	}
		    	else{
		    		$("#jspData").html("jsp query failed");
		    	}
		    }
	);
}