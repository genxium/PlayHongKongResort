/*
 * variables
 */

// general DOM elements
var g_formAvatar=null;
var g_sectionActivities=null;
var g_sectionUser=null;
var g_sectionResponse=null;
var g_activitiesFilter=null;
var g_activitiesSorter=null;

// input-field keys
var g_idFieldAvatar="idFieldAvatar";

// buttons
var g_btnUploadAvatar=null;

// others
var g_userId=null;

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

function onQueryActivitiesSuccess(data, status, xhr){
	var jsonResponse = JSON.parse(data);
	if(jsonResponse == null) return;
	var count=Object.keys(jsonResponse).length;
	// clean target section
	g_sectionActivities.empty();
	var idx=0;
	// display contents
	for(var key in jsonResponse){
		var activityJson = jsonResponse[key];
		var activityId = activityJson[g_keyId];
		if(idx == 0)	g_sectionActivities.data(g_keyStartingIndex, activityId);
		if(idx == count-1)	g_sectionActivities.data(g_keyEndingIndex, activityId);
		var cell = generateActivityCell(activityJson);
		g_sectionActivities.append(cell);
		++idx;
	}
}

function onQueryActivitiesError(xhr, status, err) {

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

function onBtnPreviousPageClicked(evt){
	
	var pageIndex=g_sectionActivities.data(g_keyPageIndex);
	var startingIndex=g_sectionActivities.data(g_keyStartingIndex);

	var relation = g_activitiesFilter.val();
	var order = g_activitiesSorter.val();
	queryActivities(startingIndex, g_numItemsPerPage, order, g_directionBackward, g_userId, relation, null, onQueryActivitiesSuccess, onQueryActivitiesError);

}

function onBtnNextPageClicked(evt){
	var pageIndex=g_sectionActivities.data(g_keyPageIndex);
	var endingIndex=g_sectionActivities.data(g_keyEndingIndex);
	var relation = g_activitiesFilter.val();
	var order = g_activitiesSorter.val();
	queryActivities(endingIndex, g_numItemsPerPage, order, g_directionForward, g_userId, relation, null, onQueryActivitiesSuccess, onQueryActivitiesError);
}

function onSectionActivitiesScrolled(evt){
	if( $(this).scrollTop() + $(this).height() >= $(document).height() ){
		evt.preventDefault();
	}
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
	params[g_keyUserId]=g_userId;
	var token=$.cookie(g_keyToken);
	if(token!=null) params[g_keyToken]=token;
	$.ajax({
		type: "GET",
		url: "/user/detail",
		data: params,
		success: function(data, status, xhr){
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
