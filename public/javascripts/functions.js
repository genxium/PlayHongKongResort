function checkLoginStatus(evt){
	var token = $.cookie(g_keyLoginStatus.toString());
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
	    				g_userName=obj['UserEmail'];
	    				// store token in cookie iff query succeeds
	    				$.cookie(g_keyLoginStatus.toString(), obj['token']);
	    				// refresh screen
	    				refreshOnLoggedIn();
		    		} else{
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

function queryActivitiesHostedByUser(){
	$("#sectionActivities").empty();
	var token = $.cookie(g_keyLoginStatus.toString());
	try{
		$.post("/queryActivitiesHostedByUser", 
			{
				token: token.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					$("#sectionActivities").html("");
    					var jsonResponse=JSON.parse(data);
    					for(var key in jsonResponse){
    						var original=$("#sectionActivities").html();
    						var jsonActivity=jsonResponse[key];
    						var cell=generateActivityCell(jsonActivity);
    						$("#sectionActivities").append(cell);
    					}
    				} else{
    					
    				}
			}
		);
	} catch(err){

	}
}

function refreshOnEnter(){
	$("#sectionAccount").show();
	$("#domainActivities").hide();
	$("#sectionImage").hide();
	$("#sectionProgress").hide();
	$("#btnLogout").hide();
	$("#sectionUserInfo").html("");
}

function refreshOnLoggedIn(){
	$("#sectionAccount").hide();
	$("#domainActivities").show();
	$("#sectionImage").show();
	$("#btnLogout").show();
	$("#sectionUserInfo").html("Hello, "+g_userName.toString());
	queryActivitiesHostedByUser();
}