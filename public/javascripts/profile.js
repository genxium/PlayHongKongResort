var g_sectionUser = null;
var g_sectionRegister = null;
var g_viewee = null;
var g_avatarUploader = null;

function emptySectionUser() {
	if (g_sectionUser == null) return;
	setDimensions(g_sectionUser, "100%", 0);
	g_sectionUser.empty();
}

function clearProfile() {
	$("#pager-filters").empty();
	$("#pager-bar-activities").empty();
	$("#pager-screen-activities").empty();
	emptySectionUser();
}

function AvatarUploader(title, trigger, btn, responseBar) {
	this.title = title;
	this.trigger = trigger;
	this.btn = btn; // the upload button
	this.responseBar = responseBar;
	this.hideBtn = function() {
		this.btn.hide();
		this.trigger.disable();
	};
	this.showBtn = function() {
		this.btn.show();
		this.trigger.enable();
	};
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
	this.btn.click(this, function(evt) {
		evt.preventDefault();
		var uploader = evt.data;	
		var file = uploader.trigger.getFile();
		if (!validateImage(file))	return;

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
				uploader.responseBar.text("Uploaded");
			},
			error: function(xhr, status, err){
				uploader.responseBar.text("Failed");
			}
		});
	});
}

function generateAvatarUploader(par) {
	par.empty();
	setDimensions(par, "100%", 96);
	var title = $("<span>", {
		style: "position: absolute;"
	}).appendTo(par); 

	var onChange = function(evt){
                evt.preventDefault();
                previewAvatar(g_avatarUploader);
	};
	var avatar = (g_viewee == null) ? "assets/icons/anonymous.png" : g_viewee.avatar;
	var trigger = generateExplorerTriggerSpan(par, onChange, avatar, 64, 64, 64, 64);
	setOffset(trigger.node, 128, 0);

	var sp2 = $("<span>", {
		style: "position: absolute;",
	}).appendTo(par);
	setOffset(sp2, 128, 64); // behind the trigger
	var btnUpload = $("<button>", {
		text: "upload",
		style: "background-color: cadetblue; color: white;"
	}).appendTo(sp2);

	var responseBar = $("<span>", {
		style: "position: absolute;"
	}).appendTo(par);
	setOffset(responseBar, 300, 64); // roughly to the right of the upload button
	return new AvatarUploader(title, trigger, btnUpload, responseBar);
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
			g_viewee = new User(userJson);
			var username = g_viewee.name;
			g_avatarUploader.title.empty();
			g_avatarUploader.trigger.changePic(g_viewee.avatar);
			$("<span>", {
				text: username,
				style: "clear: both; color: dodgerblue; font: Serif; font-size: 2em"
			}).appendTo(g_avatarUploader.title);
			if (g_loggedInUser == null || g_viewee == null || g_loggedInUser.id != g_viewee.id) return;
			g_avatarUploader.showBtn();

		}
	});
} 

function requestProfile(vieweeId) {
	clearHome();
	clearDetail();	
	clearNotifications();
	
	g_vieweeId = vieweeId;
	g_sectionUser = $("#section-user");
	if (g_registerWidget != null) g_registerWidget.hide();
	g_avatarUploader = generateAvatarUploader(g_sectionUser);
	g_avatarUploader.hideBtn();

	var relationSelector = createSelector($("#pager-filters"), ["發起的活動", "參與的活動"], [hosted, present], null, null, null, null);
	var orientationSelector = createSelector($("#pager-filters"), ["時間倒序", "時間順序"], [g_orderDescend, g_orderAscend], null, null, null, null);
	var relationFilter = new PagerFilter("relation", relationSelector);
	var orientationFilter = new PagerFilter("orientation", orientationSelector); 
	var filters = [relationFilter, orientationFilter];	
	var pagerCache = new PagerCache(5);
	
	// initialize pager widgets
	g_pager = new Pager($("#pager-screen-activities"), $("#pager-bar-activities"), g_numItemsPerPage, "/activity/list", generateActivitiesListParams, pagerCache, filters, onListActivitiesSuccess, onListActivitiesError);
	
	var onLoginSuccess = function(data) {
		queryUserDetail();
		listActivitiesAndRefresh();
	};

	var onLoginError = function(err) {
		queryUserDetail();
		listActivitiesAndRefresh();
		if (g_avatarUploader == null) return;
		g_avatarUploader.hideBtn();
	};

	var onLogoutSuccess = function(data) {
		queryUserDetail();
		listActivitiesAndRefresh();
		if (g_avatarUploader == null) return;
		g_avatarUploader.hideBtn();
	};
	
	var onLogoutError = null;

	g_preLoginForm = generatePreLoginForm(g_sectionLogin, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError);

	g_onActivitySaveSuccess = listActivitiesAndRefresh;
	checkLoginStatus();

}
