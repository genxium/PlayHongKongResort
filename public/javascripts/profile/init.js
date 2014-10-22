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
	evt.preventDefault();
	var uploader = evt.data;	
	var file = uploader.trigger.getFile();
	if (!validateImage(file))	return;
	onUploadAvatar(file, uploader.responseBar);
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
			var userJson = JSON.parse(data);
			var username = userJson[g_keyName];
			g_avatarUploader.title.empty();
			$("<span>", {
				text: "Hello, this is  ",
			}).appendTo(g_avatarUploader.title);
			$("<span>", {
				text: username,
				style: "clear: both; color: blue; font-size: 14pt"
			}).appendTo(g_avatarUploader.title);
		}
	});
} 

function AvatarUploader(title, trigger, btn, responseBar) {
	this.title = title;
	this.trigger = trigger;
	this.btn = btn; // the upload button
	this.responseBar = responseBar;
	this.hide = function() {
		this.trigger.hide();
		this.responseBar.hide();
		this.btn.hide();
	};
	this.show = function() {
		this.trigger.show();
		this.responseBar.show();
		this.btn.show();
	};
}

function generateAvatarUploader(par) {
	var title = $("<span>", {
		style: "position: relative;"
	}).appendTo(par); 

	var onChange = function(evt){
                evt.preventDefault();
                previewAvatar(g_avatarUploader);
	};
	var trigger = generateExplorerTriggerSpan(par, onChange, "/assets/icons/add.png", 64, 64, 64, 64);
	setOffset(trigger.node, 256, null);

	var sp2 = $("<span>", {
		style: "position: absolute;",
	}).appendTo(par);
	setOffset(sp2, 352, 32);
	var btnUpload = $("<button>", {
		text: "upload",
		style: "background-color: cadetblue; color: white;"
	}).appendTo(sp2);

	var responseBar = $("<p>", {
		style: "clear: both;"
	}).appendTo(par);
	var uploader = new AvatarUploader(title, trigger, btnUpload, responseBar);
	btnUpload.click(uploader, onBtnUploadAvatarClicked);
	return uploader;
}

function previewAvatar(uploader) {
		
	var file = uploader.trigger.getFile();
	if (file == null) return;
	if (!validateImage(file)) return;
        var reader = new FileReader();

        reader.onload = function (e) {
		uploader.trigger.changePic(e.target.result)
        }

        reader.readAsDataURL(file);

}

function requestProfile() {
	
	g_sectionUser = $("#section-user");
	g_avatarUploader = generateAvatarUploader(g_sectionUser);
	g_avatarUploader.hide();

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
		if (g_loggedInUser.id != g_vieweeId) return;
		if (g_avatarUploader == null) return;
		g_avatarUploader.show();
	};
	g_onLoginError = null;
	g_onEnter = function() {
		queryUserDetail();
		listActivitiesAndRefresh();
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
