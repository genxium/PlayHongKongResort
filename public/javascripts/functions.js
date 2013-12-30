function checkLoginStatus(evt){
	var token = $.cookie(g_keyLoginStatus.toString());
	if(token!=null){
		validateToken(token);
	}
}

function validateToken(token){
	$.post("/checkLoginStatus",
			{
				UserToken: token.toString(),
			},
			// post response callback function
			function(data, status, xhr){
				if(status=="success"){
					var obj=JSON.parse(data);
	    				g_userName=obj[g_keyUserEmail];
	    				// store token in cookie iff query succeeds
	    				$.cookie(g_keyLoginStatus.toString(), obj[g_keyUserToken]);
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
	var x=document.getElementById(g_idFieldEmail).value;
	var atpos=x.indexOf("@");
	var dotpos=x.lastIndexOf(".");
	if (atpos<1 || dotpos<atpos+2 || dotpos+1>=x.length) {
	  alert("Not a valid e-mail address");
	  return false;
	}
	return true;
}

function queryActivitiesHostedByUser(){
	var targetSection=$("#"+g_idSectionOwnedActivities);
	targetSection.empty();
	var token = $.cookie(g_keyLoginStatus.toString());
	try{
		$.post("/queryActivitiesHostedByUser", 
			{
				UserToken: token.toString()
			},
			function(data, status, xhr){
    				if(status=="success"){
    					var jsonResponse=JSON.parse(data);
    					for(var key in jsonResponse){
    						var jsonActivity=jsonResponse[key];
    						var cell=generateActivityCell(jsonActivity);
    						var text=cell.html();
							targetSection.append(cell);
    					}
    				} else{
    					
    				}
			}
		);
	} catch(err){

	}
}

function onMouseEnterSectionUserInfo(evt){
	if (!evt) {evt = window.event;}
	evt.preventDefault();
	var sender = (evt.srcElement || evt.target);

	g_domLoggedInUserMenu.show();	
}

function onMouseLeaveSectionUserInfo(evt){
	if (!evt) {evt = window.event;}
	evt.preventDefault();
	var sender = (evt.srcElement || evt.target);

	g_domLoggedInUserMenu.hide();	
}

function refreshOnLoggedIn(){
	$("#sectionAccount").hide();
	$("#sectionImage").show();
	$("#btnLogout").show();

	var domainActivities=$("#domainActivities");
	domainActivities.empty();
	domainActivities.show();

	// bind menu to sectionUserInfo
	var sectionUserInfo=$("#"+g_idSectionUserInfo);
	sectionUserInfo.show();
	sectionUserInfo.html("Hello, "+g_userName.toString());
	sectionUserInfo.bind("mouseenter", onMouseEnterSectionUserInfo);
	sectionUserInfo.bind("mouseleave", onMouseLeaveSectionUserInfo);
	g_domLoggedInUserMenu=generateLoggedInUserMenu();
	sectionUserInfo.append(g_domLoggedInUserMenu);
	g_domLoggedInUserMenu.hide();

	$("#"+g_idSectionOwnedActivities).show();
	$("#"+g_idBtnCreate).show();
	queryActivitiesHostedByUser();
}