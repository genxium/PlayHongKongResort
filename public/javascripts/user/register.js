var g_sectionRegister=null;

var g_registerUsername=null;
var g_registerEmail=null;
var g_registerPassword=null;

var g_spanCheckUsername=null;
var g_spanCheckEmail=null;
var g_spanCheckPassword=null;

var g_callbackOnRegisterSuccess=null;
var g_callbackOnRegisterError=null;

function initRegisterWidget(){	
	g_sectionRegister=$("#idSectionRegister");
	var registerForm=generateRegisterForm();
    	g_sectionRegister.append(registerForm);
}


function onBtnRegisterClicked(evt){
    do{
        var username=g_registerUsername.val();
        var email=g_registerEmail.val();
        var password=g_registerPassword.val();

        if(username==null || username.length==0
            || email==null || email.length==0 || validateEmail(email)==false
            || password==null || password.length==0 || validatePassword(password)==false) break;

        var params={};
        params[g_keyUsername]=username;
        params[g_keyUserEmail]=email;
        params[g_keyUserPassword]=password;

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

	g_registerUsername.on("input keyup paste", function(evt){
		do{
		    evt.preventDefault();
		    g_spanCheckUsername.empty();
		    var username=$(this).val();
		    if(username==null || username.length==0) break;

		    var params={};
		    params["username"]=username;
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

	var row4=$('<tr>').appendTo(ret);
	var cell41=$('<td>').appendTo(row4);
	var btnRegister=$('<button>', {
		style: "font-size: 15pt; background-color: aquamarine",
		text: "register"	
	}).appendTo(cell41);
	btnRegister.on("click", onBtnRegisterClicked);
	return ret;
}

 
