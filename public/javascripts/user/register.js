var g_sectionRegister=null;

var g_registerUsername=null;
var g_registerEmail=null;
var g_registerPassword=null;
var g_registerPasswordConfirm=null;

var g_spanCheckUsername=null;
var g_spanCheckEmail=null;
var g_spanCheckPassword=null;
var g_spanCheckPasswordConfirm=null;

var g_onRegisterSuccess=null;
var g_onRegisterError=null;

function initRegisterWidget(){	
	g_sectionRegister=$("#section-register");
	var registerForm=generateRegisterForm();
    	g_sectionRegister.append(registerForm);
}

function emptyRegisterFields(){
	if(g_registerUsername){
		g_registerUsername.empty();
		g_registerUsername.val("");
	}
	if(g_registerEmail){
		g_registerEmail.empty();
		g_registerEmail.val("");
	}
	if(g_registerPassword){
		g_registerPassword.empty();
		g_registerPassword.val("");
	}
	if(g_registerPasswordConfirm){
		g_registerPasswordConfirm.empty();
		g_registerPasswordConfirm.val("");
	}
	if(g_spanCheckUsername){
		g_spanCheckUsername.empty();
	}
	if(g_spanCheckEmail){
		g_spanCheckEmail.empty();
	}
	if(g_spanCheckPassword){
		g_spanCheckPassword.empty();
	}
	if(g_spanCheckPasswordConfirm){
		g_spanCheckPasswordConfirm.empty();
	}
}

function onBtnRegisterClicked(evt){
        var username=g_registerUsername.val();
        var email=g_registerEmail.val();
        var password=g_registerPassword.val();

        if(username == null || username.length == 0
            || email == null || email.length == 0 || !validateEmail(email)
            || password==null || password.length==0 || !validatePassword(password) || !validatePasswordConfirm()) return;

        var params={};
        params[g_keyName]=username;
        params[g_keyEmail]=email;
        params[g_keyPassword]=password;

        $.ajax({
		type: "POST",
		url: "/user/register",
		data: params,
		success: function(data, status, xhr){
			if(g_onRegisterSuccess == null) return;
			g_onRegisterSuccess();
		},
		error: function(xhr, status, err){
			if(g_onRegisterError == null)
			g_onRegisterError();
		}
        });
}

function generateRegisterForm(){
	var ret=$('<table>', {
		style: "border-collapse:separate; border-spacing:5pt; margin-bottom: 2pt"
	});

	var row1=$('<tr>').appendTo(ret);
	var cell11=$('<td>').appendTo(row1);
	g_registerUsername=$('<input>', {
		type: "text",
		style: "font-size: 15pt",
		placeHolder: "Username"	
	}).appendTo(cell11);
	var cell12=$('<td>').appendTo(row1);
	g_spanCheckUsername=$('<span>').appendTo(cell12);

	var row2=$('<tr>').appendTo(ret);
	var cell21=$('<td>').appendTo(row2);
	g_registerEmail=$('<input>', {
		type: "text",
		style: "font-size: 15pt",
		placeHolder: "Email"
	}).appendTo(cell21);
	var cell22=$('<td>').appendTo(row2);
	g_spanCheckEmail=$('<span>').appendTo(cell22);

	var row3=$('<tr>').appendTo(ret);
	var cell31=$('<td>').appendTo(row3);
	var cell32=$('<td>').appendTo(row3);
	g_registerPassword=$('<input>', {
		type: "password",
		style: "font-size: 15pt",
		placeHolder: "Password"	
	}).appendTo(cell31);
	g_spanCheckPassword=$('<span>').appendTo(cell32);

	var row4=$('<tr>').appendTo(ret);
	var cell41=$('<td>').appendTo(row4);
	var cell42=$('<td>').appendTo(row4);
	g_registerPasswordConfirm=$('<input>', {
		type: "password",
		style: "font-size: 15pt",
		placeHolder: "Confirm Password"	
	}).appendTo(cell41);
	g_spanCheckPasswordConfirm=$('<span>').appendTo(cell42);

	g_registerUsername.on("input keyup paste", function(evt){
		do{
		    evt.preventDefault();
		    g_spanCheckUsername.empty();
		    var name=$(this).val();
		    if(name==null || name.length==0) break;

		    var params={};
		    params[g_keyName]=name;
		    $.ajax({
			type: "GET",
			url: "/user/name/duplicate",
			data: params,
			success: function(data, status, xhr){
			    g_spanCheckUsername.text(" This username can be used :)");        
			},
			error: function(xhr, status, err){
			    g_spanCheckUsername.text(" This username cannot be used :(");        
			}
		    });
		}while(false);
	});	

	g_registerEmail.on("input keyup paste", function(evt){
		evt.preventDefault();
		g_spanCheckEmail.empty();
		var email=$(this).val();
		if(email == null || email.length == 0) return;
		if(!validateEmail(email)) {
			 g_spanCheckEmail.text(" Not valid email format");
			 return;
		}
		var params={};
		params[g_keyEmail]=email;
		$.ajax({
			type: "GET",
			url: "/user/email/duplicate",
			data: params,
			success: function(data, status, xhr){
			    g_spanCheckEmail.text(" This email can be used :)");        
			},
			error: function(xhr, status, err){
				g_spanCheckEmail.text(" This email cannot be used :(");        
			}
		});
	});	

	g_registerPassword.on("input keyup paste", function(evt){
		evt.preventDefault();
		g_spanCheckPassword.empty();
		var password = $(this).val();
		if(password == null || password.length ==0 ) return;
		if(!validatePassword(password)) {
			g_spanCheckPassword.text(" Password can only contain alphabet letters and numbers");
			return;
		}
		g_spanCheckPassword.text("");
	});	
 
	g_registerPasswordConfirm.on("input keyup paste", function(evt){
		evt.preventDefault();
		g_spanCheckPasswordConfirm.empty();
		if(!validatePasswordConfirm()) {
			g_spanCheckPasswordConfirm.text(" Doesn't match! ");
			return;
		}
		g_spanCheckPasswordConfirm.text("");
	});	

	var row5=$('<tr>').appendTo(ret);
	var cell51=$('<td>').appendTo(row5);
	var btnRegister = $('<button>', {
		style: "font-family: Serif; font-size: 15pt; background-color: Teal; color: white",
		text: "Register"	
	}).appendTo(cell51);
	btnRegister.on("click", onBtnRegisterClicked);
	return ret;
}

function validatePasswordConfirm(){
	var password = g_registerPassword.val();		
	var passwordConfirm = g_registerPasswordConfirm.val();
	if(password == null || passwordConfirm == null) return false;
	if(password != passwordConfirm) return false;
	return true;
} 
