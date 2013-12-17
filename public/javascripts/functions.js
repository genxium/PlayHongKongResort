function checkLoginStatus(evt){
	var token = $.cookie(loginStatusTokenKey.toString());
	if(token!=null){
		validateToken(token);
	}
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
	    			userName=obj['UserEmail'];
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