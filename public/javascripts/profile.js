var g_sectionUser = null
var g_viewee = null;
var g_profileEditor = null;

function ProfileEditor() {
	this.container = null;
	this.dialog = null;
	this.content = null;

	// avatar
	this.image = null;
	this.btnChoose = null;
	this.hint = null;

	// text fields
	this.age = null;
	this.ageHint = null;
	this.gender = null;
	this.genderHint = null;
	this.mood = null;
	this.moodHint = null;
	
	// controlling buttons
	this.btnSave = null;
	this.btnEdit = null;
	this.btnCancel = null;

	this.NORMAL = 0;	
	this.EDITING = 1;
	this.mode = this.NORMAL; 

	this.validate = function(age, gender, mood) {
		/*
			NEED regex checking for these items
		*/
		return true;
	};

	this.refresh = function(user) {
		if (user == null) return null;

		this.content.empty();
		var form = $("<div>", {
			"class": "avatar-editor-form clearfix"
		}).appendTo(this.content);

		$("<br>").appendTo(this.content);
		
		var tbl = $("<table>", {
			"class": "user-profile-table"
		}).appendTo(this.content);
		var ageRow = $("<tr>").appendTo(tbl); 
		var ageTitle = $("<td>", {
			text: TITLES["age"]
		}).appendTo(ageRow);
		var ageValue = $("<td>").appendTo(ageRow);

		var genderRow = $("<tr>").appendTo(tbl);
		var genderTitle = $("<td>", {
			text: TITLES["gender"]
		}).appendTo(genderRow);	
		var genderValue = $("<td>").appendTo(genderRow);
	
		var moodRow = $("<tr>").appendTo(tbl);
		var moodTitle = $("<td>", {
			text: TITLES["mood"]
		}).appendTo(moodRow);
		var moodValue = $("<td>").appendTo(moodRow);

		if (this.mode == this.EDITING) {
			this.age = $("<input>").appendTo(ageValue); 
			this.age.val(user.age);

			this.gender = $("<input>").appendTo(genderValue);
			this.gender.val(user.gender);
			
			this.mood = $("<input>").appendTo(moodValue);
			this.mood.val(user.mood);
		} else if (this.mode == this.NORMAL) {
			this.age = $("<span>", {
				"class": "user-profile-table-plain-value",
				text: user.age
			}).appendTo(ageValue);
			this.gender = $("<span>", {
				"class": "user-profile-table-plain-value",
				text: user.gender
			}).appendTo(genderValue);
			this.mood = $("<span>", {
				"class": "user-profile-table-plain-value",
				text: user.mood
			}).appendTo(moodValue);
		} else;

		var picContainer = $("<div>", {
			"class": "avatar left"
		}).appendTo(form);

		var picHelper = $("<span>", {
			"class": "image-helper"
		}).appendTo(picContainer);

		this.image = $("<img>", {
			src: user.avatar
		}).appendTo(picContainer); 

		if (g_loggedInUser == null || user.id != g_loggedInUser.id) return;

		var box = $("<div>", {
			"class": "upload left"
		}).hide().appendTo(form);

		this.btnChoose = $("<input>", {
			type: "file",
			text: TITLES["choose_picture"]
		}).change(this, function(evt) {
			evt.preventDefault();
			var editor = evt.data;
			var file = editor.getFile();
			if (file == null) return;
			if (!validateImage(file)) return;
			var reader = new FileReader();
			reader.onload = function (e) {
				editor.image.attr("src", e.target.result);
			}
			reader.readAsDataURL(file);
		}).appendTo(box);
		
		this.hint = $("<p>").appendTo(box);
		if (this.mode == this.EDITING) box.show();
	
		var controlButtonsRow = $("<p>").appendTo(this.content);

		this.btnEdit = $("<button>", {
			text: TITLES["edit"],
			"class": "btn-edit purple" 
		}).click(this, function(evt) {
			var editor = evt.data;
			editor.mode = editor.EDITING;
			editor.refresh(user);
		}).hide().appendTo(controlButtonsRow);
		if (this.mode == this.NORMAL) this.btnEdit.show();

		this.btnCancel = $("<button>", {
			text: TITLES["cancel"],
			"class": "btn-cancel gray"
		}).click(this, function(evt) {
			var editor = evt.data;
			editor.mode = editor.NORMAL;
			editor.refresh(user);
		}).hide().appendTo(controlButtonsRow);
		if (this.mode == this.EDITING) this.btnCancel.show();

		this.btnSave = $("<button>", {
			text: TITLES["save"],	
			"class": "btn-save purple"
		}).click(this, function(evt) {
			evt.preventDefault();
			var editor = evt.data;	
			var file = editor.getFile();
			if (file != null && !validateImage(file))	return;

			var token = $.cookie(g_keyToken);
			if (token == null) return;

			var formData = new FormData();
			formData.append(g_keyToken, token);
			formData.append(g_keyAvatar, file);
			formData.append(g_keyAge, editor.age.val());
			formData.append(g_keyGender, editor.gender.val());
			formData.append(g_keyMood, editor.mood.val());

			var aButton = getTarget(evt);
			disableField(aButton);	
			editor.hint.text(MESSAGES["saving"]);
			
			$.ajax({
				method: "POST",
				url: "/user/save", 
				data: formData,
				mimeType: "mutltipart/form-data",
				contentType: false,
				processData: false,
				success: function(data, status, xhr){
					// update logged in user profile
					var userJson = JSON.parse(data);
					user = g_viewee = g_loggedInUser = new User(userJson);
					enableField(aButton);	
					editor.hint.text(MESSAGES["saved"]);
				},
				error: function(xhr, status, err){
					enableField(aButton);	
					editor.hint.text(MESSAGES["save_failed"]);
				}
			});
		}).hide().appendTo(controlButtonsRow);	
		if (this.mode == this.EDITING) this.btnSave.show();
	
	};
	
	this.appendTo = function(par) {
		this.container = $("<div>").appendTo(par);
		this.dialog = $("<div>").appendTo(this.container);
		this.content = $("<div>").appendTo(this.dialog);
	};	
	this.show = function() {
		this.container.show();
	};

	this.hide = function() {
		this.container.hide();
	};

	this.remove = function() {
		this.container.remove();
	};	
	this.getFile = function() {
		if (this.btnChoose == null) return null;	
		var files = this.btnChoose[0].files;
		if (files == null) return null;	
		if (files.length > 1) {
			alert(ALERTS["choose_one_image"]);
			return null;
		}
		return files[0];
	};
}

function clearProfile() {
	$("#pager-filters").empty();
	$("#pager-bar-activities").empty();
	$("#pager-screen-activities").empty();
	if (g_sectionUser == null) return;
	g_sectionUser.empty();
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
			var userJson = data;
			g_viewee = new User(userJson);
			var username = g_viewee.name;
			g_sectionUser.empty();

			var profile = $("<div>", {
				"class": "user-profile clearfix"
			}).appendTo(g_sectionUser);

			var userInfo = $("<div>", {
				"class": "section-user-info left"
			}).appendTo(profile);

			var name = $("<div>", {
				text: username,
				"class": "section-user-name"
			}).appendTo(userInfo);


			if (g_profileEditor == null) g_profileEditor = new ProfileEditor();
			g_profileEditor.appendTo(profile);	
			g_profileEditor.refresh(g_viewee);
			
			// refresh pager for assessments
			if (g_pagerAssessments != null) g_pagerAssessments.remove();
			var pagerBar = $("<div>", {
				id: "pager-bar-assessments"
			}).appendTo(g_sectionUser);
			var pagerScreen = $("<div>", {
				id: "pager-screen-assessments"
			}).appendTo(g_sectionUser);
			var pagerCache = new PagerCache(5);
			var extraParams = {
				to: g_viewee.id
			};
			g_pagerAssessments = new Pager(pagerScreen, pagerBar, 10, "/assessment/list", generateAssessmentsListParams, extraParams, pagerCache, null, onQueryAssessmentsSuccess, onQueryAssessmentsError); 	
			if (g_loggedInUser == null) return;
			if (g_loggedInUser.isVisitor() && g_vieweeId == g_loggedInUser.id) {
				var hintResend = null;
				var extraParams = {};
				extraParams[g_keyToken] = $.cookie(g_keyToken);
				var onSuccess = function(data) {
					if (data == null) return;
					if (isTokenExpired(data)) {
						logout(null);
						return;
					}
					hintResend.text(MESSAGES["email_verification_sent"].format(data[g_keyEmail]));
				};
				var onError = function(err) {
					hintResend.text(MESSAGES["email_verification_not_sent"]);
				};
				var btnResend = new AjaxButton(TITLES["resend_email_verification"], "/user/email/resend", null, "POST", extraParams, onSuccess, onError);
				btnResend.appendTo(g_sectionUser);
				hintResend = $("<p>", {
					text: "",
					style: "padding: 10px;"
				}).appendTo(g_sectionUser);
			}

			if (g_loggedInUser.id == g_vieweeId) return;
			listAssessmentsAndRefresh();
		}
	});
} 

function requestProfile(vieweeId) {
	clearHome();
	clearDetail();	
	clearNotifications();
	g_vieweeId = vieweeId;

	g_sectionUser = $("#section-user");

	var relationSelector = createSelector($("#pager-filters"), [TITLES["hosted_activities"], TITLES["joined_activities"]], [hosted, present], null, null, null, null);
	var orientationSelector = createSelector($("#pager-filters"), [TITLES["time_descendant"], TITLES["time_ascendant"]], [g_orderDescend, g_orderAscend], null, null, null, null);
	var relationFilter = new PagerFilter(g_keyRelation, relationSelector);
	var orientationFilter = new PagerFilter(g_keyOrientation, orientationSelector); 
	var filters = [relationFilter, orientationFilter];	
	var pagerCache = new PagerCache(5);
	
	// initialize pager widgets
	g_pager = new Pager($("#pager-screen-activities"), $("#pager-bar-activities"), g_numItemsPerPage, "/activity/list", generateActivitiesListParams, null, pagerCache, filters, onListActivitiesSuccess, onListActivitiesError);
	
	var onLoginSuccess = function(data) {
		queryUserDetail();
		listActivitiesAndRefresh();
	};

	var onLoginError = function(err) {
		queryUserDetail();
		listActivitiesAndRefresh();
	};

	var onLogoutSuccess = function(data) {
		queryUserDetail();
		listActivitiesAndRefresh();
	};
	
	var onLogoutError = null;

	g_preLoginForm = generatePreLoginForm(g_sectionLogin, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError, true);

	g_onActivitySaveSuccess = listActivitiesAndRefresh;
	checkLoginStatus();

}
