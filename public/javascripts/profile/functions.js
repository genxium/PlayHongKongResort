/*
 * variables
 */

// general DOM elements
var g_formAvatar = null;
var g_sectionUser = null;
var g_sectionResponse = null;

// buttons
var g_btnUploadAvatar=null;

// Assistant Callback Functions
function onUploadAvatarFormSubmission(formEvt){

	formEvt.preventDefault(); // prevent default action.

	var formObj = $(this);
	var formData = new FormData(this);
	
	// append an user token for identity
	var token = $.cookie(g_keyToken);
	formData.append(g_keyToken, token);
	
	$.ajax({
		method: "POST",
		url: "/user/avatar/upload", 
		data: formData,
		mimeType: "mutltipart/form-data",
		contentType: false,
		processData: false,
		success: function(data, status, xhr){
			g_sectionResponse.html("Uploaded");
		},
		error: function(xhr, status, err){
			g_sectionResponse.html("Failed");
		}
	});

}

function queryActivitiesAndRefresh() {
	if(g_vieweeId == null) return;
	queryActivities(0, g_pagerContainer.nItems, g_pagerContainer.orientation, g_directionForward, g_vieweeId, g_pagerContainer.relation, g_pagerContainer.status, onQueryActivitiesSuccess, onQueryActivitiesError);
}


// Event Handlers
function onBtnUploadAvatarClicked(evt){

	var file = document.getElementById(g_keyAvatar);
	if(validateImage(file)==false){
		return;
	}

	// set callback function of form submission
	g_formAvatar.submit(onUploadAvatarFormSubmission);
	// invoke submission
	g_formAvatar.submit();
}

function refreshOnEnter(){
	g_formAvatar.hide();
	queryUserDetail();
}

function refreshOnLoggedIn(){
	g_formAvatar.show();
	queryUserDetail();
}

function queryUserDetail(){
	var params={};
	params[g_keyVieweeId] = g_vieweeId;
	var token=$.cookie(g_keyToken);
	if(token!=null) params[g_keyToken]=token;
	$.ajax({
		type: "GET",
		url: "/user/detail",
		data: params,
		success: function(data, status, xhr){
			if(g_sectionUser == null) return;
			g_sectionUser.empty();
			var userJson=JSON.parse(data);
			var username=userJson[g_keyName];
			var prefix=$("<span>", {
				text: "You are viewing the profile of ",
				style: "color: black"
			}).appendTo(g_sectionUser);
			var sectionUser=$("<span>", {
				text: username,
				style: "color: blue"
			}).appendTo(g_sectionUser);	
		}
	});
} 

function validateImage(file){
	var fileName = file.value;
	var ext = fileName.split('.').pop().toLowerCase();
	if($.inArray(ext, ['gif','png','jpg','jpeg']) == -1) {
	    alert('invalid extension!');
	    return false;
	}
	return true;
}
