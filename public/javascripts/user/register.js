var g_sectionRegister=null;

var g_registerUsername=null;
var g_registerEmail=null;
var g_registerPassword=null;
var g_registerPasswordConfirm=null;

var g_spanCheckUsername=null;
var g_spanCheckEmail=null;
var g_spanCheckPassword=null;
var g_spanCheckPasswordConfirm=null;

var g_callbackOnRegisterSuccess=null;
var g_callbackOnRegisterError=null;

function initRegisterWidget(){	
	g_sectionRegister=$("#idSectionRegister");
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
    do{
        var username=g_registerUsername.val();
        var email=g_registerEmail.val();
        var password=g_registerPassword.val();

        if(username==null || username.length==0
            || email==null || email.length==0 || validateEmail(email)==false
            || password==null || password.length==0 || validatePassword(password)==false || validatePasswordConfirm()==false) break;

        var params={};
        params[g_keyName]=username;
        params[g_keyEmail]=email;
        params[g_keyPassword]=password;

        $.ajax({
		type: "POST",
		url: "/user/register",
		data: params,
		success: function(data, status, xhr){
			if(g_callbackOnRegisterSuccess!=null){
				g_callbackOnRegisterSuccess();
			}
		},
		error: function(xhr, status, err){
			if(g_callbackOnRegisterError!=null){
				g_callbackOnRegisterError();
			}   
		}
        });
    }while(false);
}

function generateRegisterForm(){
	var ret=$('<table>');
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
		do{
		    evt.preventDefault();
		    g_spanCheckEmail.empty();
		    var email=$(this).val();
		    if(email==null || email.length==0) break;
		    if(validateEmail(email)==false) {
					 g_spanCheckEmail.text(" Not valid email format");
					 break;
				}
		    var params={};
		    params["email"]=email;
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
		}while(false);
	});	

	g_registerPassword.on("input keyup paste", function(evt){
		do{
		    evt.preventDefault();
		    g_spanCheckPassword.empty();
		    var password=$(this).val();
		    if(password==null || password.length==0) break;
		    if(validatePassword(password)==false) {
					g_spanCheckPassword.text(" Password can only contain alphabet letters and numbers");
					break;
				}
				g_spanCheckPassword.text("");
		}while(false);
	});	
 
	g_registerPasswordConfirm.on("input keyup paste", function(evt){
		do{
			evt.preventDefault();
			g_spanCheckPasswordConfirm.empty();
			if(validatePasswordConfirm()==false) {
				g_spanCheckPasswordConfirm.text(" Doesn't match! ");
				break;
			}
			g_spanCheckPasswordConfirm.text("");
		}while(false);
	});	

	var row5=$('<tr>').appendTo(ret);
	var cell51=$('<td>').appendTo(row5);
	var btnRegister=$('<button>', {
		style: "font-size: 15pt; background-color: aquamarine",
		text: "register"	
	}).appendTo(cell51);
	btnRegister.on("click", onBtnRegisterClicked);
	return ret;
}

function validatePasswordConfirm(){
	var ret=false;
	do{
		var password=g_registerPassword.val();		
		var passwordConfirm=g_registerPasswordConfirm.val();
		if(password==null || passwordConfirm==null) break;
		if(password!=passwordConfirm) break;
		ret=true;	
	}while(false);
	return ret;
} 
