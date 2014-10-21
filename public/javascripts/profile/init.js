var g_avatarUploader = null;
var g_sectionUser = null;

// Assistant Callback Functions
function onUploadAvatar(file, responseBar){

	// append an user token for identity
	var token = $.cookie(g_keyToken);
	if (token == null) return;

	var formData = new FormData();
	formData.append(g_keyAvatar, file);
	formData.append(g_keyToken, token);
	
	$.ajax({
		method: "POST",
		url: "/user/avatar/upload", 
		data: formData,
		mimeType: "mutltipart/form-data",
		contentType: false,
		processData: false,
		success: function(data, status, xhr){
			responseBar.text("Uploaded");
		},
		error: function(xhr, status, err){
			responseBar.text("Failed");
		}
	});

}

function listActivitiesAndRefresh() {
	if(g_vieweeId == null) return;
	var page = 1;
	listActivities(page, onListActivitiesSuccess, onListActivitiesError);
}

function queryActivitiesAndRefresh() {
	if(g_vieweeId == null) return;
	var page = 1;
	queryActivities(page, onQueryActivitiesSuccess, onQueryActivitiesError);
}


// Event Handlers
function onBtnUploadAvatarClicked(evt){
	preventDefault();
	var uploader = evt.data;	
	var file = uploader.input[0].files[0];
	if(!validateImage(file))	return;
	onUploadAvatar(file, upload.responseBar);
}

function queryUserDetail(){
	var params={};
	params[g_keyVieweeId] = g_vieweeId;
	var token = $.cookie(g_keyToken);
	if(token != null) params[g_keyToken] = token;
	$.ajax({
		type: "GET",
		url: "/user/detail",
		data: params,
		success: function(data, status, xhr){
			if(g_sectionUser == null) return;
			g_sectionUser.empty();
			var userJson = JSON.parse(data);
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

function AvatarUploader(input, responseBar) {
	this.input = input;
	this.responseBar = responseBar;
	this.hide = function() {
		this.input.hide();
		this.responseBar.hide();
	};
	this.show = function() {
		this.input.show();
		this.responseBar.show();
	};
}

function generateAvatarUploader(par) {
	var row = $("<p>").append(par);
	var sp1 = $("<span>", {
		text: "Avatar"
	}).appendTo(row);
	var sp2 = $("<span>", {
		style: "margin-left: 10pt"
	}).appendTo(row);
	var input = $("<input>", {
		type: "file"
	}).appendTo(sp2);
	var sp3 = $("<span>", {
		style: "margin-left: 10pt"
	}).appendTo(row);
	var btnUpload = $("<button>", {
		text: "upload"
	}).appendTo(sp3);
	var responseBar = $("<p>").appendTo(par);

	var uploader = new AvatarUploader(input, responseBar);
	btnUpload.click(uploader, onBtnUploadAvatarClicked);
	return uploader;
}

function requestProfile() {
	
	g_sectionUser = $("#section-user");
	g_avatarUploader = generateAvatarUploader($("#section-avatar"));

	var relationSelector = createSelector($("#pager-filters"), ["發起的活動", "參與的活動"], [hosted, present], null, null, null, null);
	var orientationSelector = createSelector($("#pager-filters"), ["時間倒序", "時間順序"], [g_orderDescend, g_orderAscend], null, null, null, null);
	var relationFilter = new PagerFilter("relation", relationSelector);
	var orientationFilter = new PagerFilter("orientation", orientationSelector); 
	var filters = [relationFilter, orientationFilter];	
	var pagerCache = new PagerCache(5);
	
	// initialize pager widgets
	g_pager = new Pager($("#pager-screen-activities"), $("#pager-bar-activities"), g_numItemsPerPage, "/activity/list", generateActivitiesListParams, pagerCache, filters, onListActivitiesSuccess, onListActivitiesError);
	
	g_onLoginSuccess = function() {
		queryUserDetail();
		listActivitiesAndRefresh();
		if (g_avatarUploader == null) return;
		g_avatarUploader.show();
	};
	g_onLoginError = null;
	g_onEnter = function() {
		queryUserDetail();
		if (g_avatarUploader == null) return;
		g_avatarUploader.hide();
	};

	g_onActivitySaveSuccess = listActivitiesAndRefresh;
	
	checkLoginStatus();

}

$(document).ready(function(){

	var params = extractParams(window.location.href);
	for(var i = 0; i < params.length; i++){
		var param = params[i];
		var pair = param.split("=");
		if(pair[0] == g_keyVieweeId){
			g_vieweeId = parseInt(pair[1]);
			break;
		}
	}
	
	initTopbar($("#topbar"));
	initActivityEditor($("#wrap"), listActivitiesAndRefresh);
	
	requestProfile();

});
