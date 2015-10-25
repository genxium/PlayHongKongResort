var g_sectionPlayer = null;
var g_viewee = null;
var g_profileEditor = null;

/**
 * ProfileEditorImageNode
 * */

function ProfileEditorImageNode(cdn, domain) {
	this.setCDNCredentials(cdn, domain, getToken(), g_loggedInPlayer);	
	this.composeContent = function(data) {
		this.editor = data;
		this.wrap = $('<div>', {
			"class": "preview-container"
		}).appendTo(this.content);

		this.preview = $('<img>', {
			src: this.editor.player.avatar
		}).appendTo(this.wrap);
		
		this.btnChoose = $('<button>', {
			text: TITLES.choose_picture,
			"class": "positive-button"
		}).appendTo(this.wrap);
		setDimensions(this.btnChoose, "100%", null);

		if (cdn == g_cdnQiniu) {
			// reference http://developer.qiniu.com/docs/v6/sdk/javascript-sdk.html
			var node = this;
			this.uploader = Qiniu.uploader({
				runtimes: 'html5,flash,html4',		    
				browse_button: node.btnChoose[0],
				uptoken_url: node.uptokenUrl,
				unique_names: false,
				save_key: false,
				domain: node.bucketDomain,
				container: node.preview[0],
				max_file_size: '2mb',
				max_retries: 2,
				// dragdrop: false, 
				// drop_element: node.preview[0],
				chunk_size: '4mb',
				auto_start: true, 
				init: {
					'FilesAdded': function(up, files) {
						if (!files) return null;
						if (files.length != 1) {
							alert(ALERTS.choose_one_image);
							return;
						}

						var file = files[0];
						if (!validateImage(file)) return;

						node.state = SLOT_UPLOADING; 
						disableField(node.btnChoose);
					},
					'BeforeUpload': function(up, file) {
					},
					'UploadProgress': function(up, file) {
						// TODO: show progress
					},
					'FileUploaded': function(up, file, info) {
					},
					'Error': function(up, err, errTip) {
						node.state = SLOT_UPLOAD_FAILED; 
					},
					'UploadComplete': function() {
						enableField(node.btnChoose);
						if (node.state == SLOT_UPLOAD_FAILED) return;
						var protocolPrefix = "http://";
						var imageUrl = protocolPrefix + node.bucketDomain + "/" + node.remoteName;
						node.preview.attr("src", imageUrl);
						node.state = SLOT_UPLOADED; 
					},
					'Key': function(up, file) {
						// would ONLY be invoked when {unique_names: false , save_key: false}
						return node.remoteName;
					 }
				}
			});
		}	
	};
}
 
ProfileEditorImageNode.inherits(ImageNode);

function ProfileEditor() {

	this.NORMAL = 0;	
	this.EDITING = 1;
	this.mode = this.NORMAL; 

	this.composeContent = function(player) {
		if (!player) return;
		this.player = player;

		// avatar 
		var avatarBox = $('<div>').hide().appendTo(this.content);
		if (this.mode == this.EDITING) avatarBox.show();

		var avatarPreview = $('<div>', {
			"class": "preview-container"
		}).hide().appendTo(this.content);  
		$('<img>', {
			"class": "image-helper",
			src: player.avatar
		}).appendTo(avatarPreview);			
		if (this.mode == this.NORMAL) avatarPreview.show();	

		// misc
		$("<br>").appendTo(this.content);
		
		var tbl = $("<table>", {
			"class": "player-profile-table clear"
		}).appendTo(this.content);
		var ageRow = $("<tr>").appendTo(tbl); 
		var ageTitle = $("<td>", {
			text: TITLES.age
		}).appendTo(ageRow);
		var ageValue = $("<td>").appendTo(ageRow);
		var ageHintCell = $("<td>").appendTo(ageRow);

		var genderRow = $("<tr>").appendTo(tbl);
		var genderTitle = $("<td>", {
			text: TITLES.gender
		}).appendTo(genderRow);	
		var genderValue = $("<td>").appendTo(genderRow);
		var genderHintCell = $("<td>").appendTo(genderRow);
	
		var moodRow = $("<tr>").appendTo(tbl);
		var moodTitle = $("<td>", {
			text: TITLES.mood
		}).appendTo(moodRow);
		var moodValue = $("<td>").appendTo(moodRow);
		var moodHintCell = $("<td>").appendTo(moodRow);

		if (this.mode == this.EDITING) {
			this.age = $("<input>").appendTo(ageValue); 
			this.age.val(player.age);
			this.ageHint = $("<span>").appendTo(ageHintCell);
			
			this.age.on("input keyup paste", this.ageHint, function(evt){
				evt.preventDefault();
				var hint = evt.data;
				hint.empty();
				hint.html("");
				hint.removeClass("warn");
				var val = $(this).val();
				if(!val || val.length === 0) return;
				if(validatePlayerAge(val))	return;
				hint.addClass("warn");
				hint.html(MESSAGES.age_requirement);
			});	

			this.gender = $("<input>").appendTo(genderValue);
			this.gender.val(player.gender);
			this.genderHint = $("<span>").appendTo(genderHintCell);
			this.gender.on("input keyup paste", this.genderHint, function(evt){
				evt.preventDefault();
				var hint = evt.data;
				hint.empty();
				hint.html("");
				hint.removeClass("warn");
				var val = $(this).val();
				if(!val || val.length === 0) return;
				if(validatePlayerGender(val))	return;
				hint.addClass("warn");
				hint.html(MESSAGES.gender_requirement);
			});	
			
			this.mood = $("<input>").appendTo(moodValue);
			this.mood.val(player.mood);
			this.moodHint = $("<span>").appendTo(moodHintCell);
			this.mood.on("input keyup paste", this.moodHint, function(evt){
				evt.preventDefault();
				var hint = evt.data;
				hint.empty();
				hint.html("");
				hint.removeClass("warn");
				var val = $(this).val();
				if(!val || val.length === 0 ) return;
				if(validatePlayerMood(val))	return;
				hint.addClass("warn");
				hint.html(MESSAGES.mood_requirement);
			});	
		} else if (this.mode == this.NORMAL) {
			this.age = $("<span>", {
				"class": "player-profile-table-plain-value",
				text: player.age
			}).appendTo(ageValue);
			this.gender = $("<span>", {
				"class": "player-profile-table-plain-value",
				text: player.gender
			}).appendTo(genderValue);
			this.mood = $("<span>", {
				"class": "player-profile-table-plain-value",
				text: player.mood
			}).appendTo(moodValue);
		} else;

		if (!g_loggedInPlayer || player.id != g_loggedInPlayer.id) return;

		// avatar
		if (this.mode == this.EDITING) {
		        var domain = queryCDNDomainSync();
			this.avatarNode = new ProfileEditorImageNode(g_cdnQiniu, domain);
			this.avatarNode.appendTo(avatarBox);	
			this.avatarNode.refresh(this);
			this.avatarHint = $("<p>", {
				"class": "player-profile-avatar-hint"
			}).appendTo(avatarBox);
		}
	
		// buttons
		var buttonRow = $("<p>").appendTo(this.content);

		this.btnEdit = $("<button>", {
			text: TITLES.edit,
			"class": "btn-edit positive-button" 
		}).hide().appendTo(buttonRow).click(this, function(evt) {
			var editor = evt.data;
			editor.mode = editor.EDITING;
			editor.refresh(player);
		});
		if (this.mode == this.NORMAL) this.btnEdit.show();

		this.btnCancel = $("<button>", {
			text: TITLES.cancel,
			"class": "btn-cancel negative-button"
		}).hide().appendTo(buttonRow).click(this, function(evt) {
			var editor = evt.data;
			editor.mode = editor.NORMAL;
			editor.refresh(player);
		});
		if (this.mode == this.EDITING) this.btnCancel.show();

		this.btnSave = $("<button>", {
			text: TITLES.save,	
			"class": "btn-save positive-button"
		}).hide().appendTo(buttonRow).click(this, function(evt) {
			evt.preventDefault();
			var editor = evt.data;	
			var token = getToken();
			if (!token) return;

			var formData = {};
			formData[g_keyToken] = token;

			if (editor.avatarNode.state == SLOT_UPLOADED) { 
				formData[g_keyAvatar] = editor.avatarNode.remoteName;
			}
			formData[g_keyAge] =  editor.age.val();
			formData[g_keyGender] =  editor.gender.val();
			formData[g_keyMood] =  editor.mood.val();

			var aButton = getTarget(evt);
			disableField(aButton);	
			editor.avatarHint.text(MESSAGES.saving);
			
			$.ajax({
				method: "POST",
				url: "/player/save", 
				data: formData,
				success: function(data, status, xhr){
					enableField(aButton);	
					// update logged in player profile
					player = g_viewee = g_loggedInPlayer = new Player(data);
					editor.avatarHint.text(MESSAGES.saved);
				},
				error: function(xhr, status, err){
					enableField(aButton);	
					editor.avatarHint.text(MESSAGES.save_failed);
				}
			});
		});	
		if (this.mode == this.EDITING) this.btnSave.show();

		if (!g_loggedInPlayer) return;
		if (g_loggedInPlayer.hasEmail() && !g_loggedInPlayer.isEmailAuthenticated() && g_vieweeId == g_loggedInPlayer.id) {
			this.resendHint = $("<p>", {
				"class": "hint-resend"
			}).hide().appendTo(this.content);
			this.btnResend = new AjaxButton(TITLES.resend_email_verification);
			var editor = this;
			var dButton = {
				url: "/player/email/resend",
				type: "POST",
				clickData: null,
				extraParams: {
					token: getToken()
				},
				onSuccess: function(data) {
					if (!data) return;
					if (isStandardFailure(data) || isTokenExpired(data) || isPlayerNotFound(data)) {
						logout(null);
						return;
					}
					editor.resendHint.text(MESSAGES.email_verification_sent.format(data[g_keyEmail]));
				},
				onError: function(err) {
					editor.resendHint.text(MESSAGES.email_verification_not_sent);
				}
			};
			this.btnResend.appendTo(this.content);
			this.btnResend.hide();
			this.btnResend.refresh(dButton);
			this.btnResend.button.addClass("caution-button");
			if (this.mode == this.NORMAL) {
				this.btnResend.show();
				this.resendHint.show();
			}
		}
	};
}

ProfileEditor.inherits(BaseWidget);

function ProfileActivityPager(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError) {
	this.init(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError);

	this.updateScreen = function(data) {
		if (!data) return;
		var pageSt = parseInt(data[g_keyPageSt]);
		var pageEd = parseInt(data[g_keyPageEd]);
		var page = pageSt;

		var activitiesJson = data[g_keyActivities];
		var length = Object.keys(activitiesJson).length;

		var activities = [];
		for(var idx = 1; idx <= length; ++idx) {
			var activityJson = activitiesJson[idx - 1];
			var activity = new Activity(activityJson);
			activities.push(activity);
			if (page == this.page)	generateActivityCell(this.screen, activity);
			if (idx % this.nItems != 0) continue;
			this.cache.putPage(page, activities);
			activities = [];
			++page;	
		}
		if (activities != null && activities.length > 0) {
			// for the last page
			this.cache.putPage(page, activities);
		}
	};
}

ProfileActivityPager.inherits(Pager);

function clearProfile() {
	$("#pager-activities").empty();
	if (!g_sectionPlayer) return;
	g_sectionPlayer.empty();
}

function queryPlayerDetail(){
	var params={};
	params[g_keyVieweeId] = g_vieweeId;
	var token = getToken();
	if(!(!token)) params[g_keyToken] = token;
	$.ajax({
		type: "POST",
		url: "/player/detail",
		data: params,
		success: function(data, status, xhr){
			if(!g_sectionPlayer) return;
			g_viewee = new Player(data);
			var playername = g_viewee.name;
			g_sectionPlayer.empty();

			var profile = $("<div>", {
				"class": "player-profile clearfix"
			}).appendTo(g_sectionPlayer);

			var playerInfo = $("<div>", {
				"class": "section-player-info"
			}).appendTo(profile);

			var name = $("<div>", {
				text: playername,
				"class": "section-player-name"
			}).appendTo(playerInfo);

			if (!g_profileEditor) g_profileEditor = new ProfileEditor();
			g_profileEditor.appendTo(profile);	
			g_profileEditor.refresh(g_viewee);
			
			var pagerAssessmentSection = $("#pager-assessments");
			// refresh pager for assessments
			if (!(!g_pagerAssessments)) g_pagerAssessments.remove();
			var extraParams = {
				to: g_viewee.id
			};
			g_pagerAssessments = new AssessmentPager(10, "/assessment/list", generateAssessmentsListParams, extraParams, 5, null, onListAssessmentsSuccess, onListAssessmentsError); 	
			g_pagerAssessments.appendTo(pagerAssessmentSection);
			g_pagerAssessments.refresh();

			if (!g_loggedInPlayer || g_loggedInPlayer.id == g_vieweeId) return;
			listAssessmentsAndRefresh();
		}
	});
} 

function requestProfile(vieweeId) {
	clearHome();
	clearDetail();	
	clearNotifications();
	g_vieweeId = vieweeId;
	g_sectionPlayer = $("#section-player");
	
	// initialize pager 
	var filterMap = {};
	filterMap[g_keyRelation] = [[TITLES.hosted_activities, TITLES.joined_activities], [hosted, present]]; 
	filterMap[g_keyOrientation] = [[TITLES.time_descendant, TITLES.time_ascendant], [g_orderDescend, g_orderAscend]];

	g_pagerActivity = new ProfileActivityPager(g_numItemsPerPage, "/activity/list", generateActivitiesListParams, null, 5, filterMap, onListActivitiesSuccess, onListActivitiesError);
	g_pagerActivity.appendTo("#pager-activities");
	g_pagerActivity.refresh();
	
	var onLoginSuccess = function(data) {
		queryPlayerDetail();
		listActivitiesAndRefresh();
	};

	var onLoginError = function(err) {
		queryPlayerDetail();
		listActivitiesAndRefresh();
	};

	var onLogoutSuccess = function(data) {
		queryPlayerDetail();
		listActivitiesAndRefresh();
	};
	
	var onLogoutError = null;

	g_preLoginForm = generatePreLoginForm(g_sectionLogin, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError, true);

	g_onActivitySaveSuccess = listActivitiesAndRefresh;
	checkLoginStatus();

}
