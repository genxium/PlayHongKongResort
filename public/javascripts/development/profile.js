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

function refreshProfileInfoTable(par, fieldKey, fieldVal, title, regex, requirement, disabled) {
	if (!par[fieldKey]) {
		par[fieldKey] = $('<div>', {
			"class": "profile-info-table-row"
		}).appendTo(par);
	}
	if (!par[fieldKey].title) {
		par[fieldKey].title = $('<div>', {
			"class": "profile-info-table-title",
			text: title 
		}).appendTo(par[fieldKey]); 
	}
	if (!par[fieldKey].input) {
		par[fieldKey].input = $("<input>", {
			"class": "profile-info-table-input"
		}).appendTo(par[fieldKey]); 
	}
	par[fieldKey].input.val(fieldVal);
	if (disabled)	disableField(par[fieldKey].input);

	if (!par[fieldKey].hint) {
		par[fieldKey].hint = $("<div>", {
			"class": "profile-info-table-hint"
		}).appendTo(par[fieldKey]);
		par[fieldKey].hint.regex = regex;
		par[fieldKey].input.on("input keyup paste", par[fieldKey].hint, function(evt){
			evt.preventDefault();
			var hint = evt.data;
			var aInput = getTarget(evt);
			hint.empty();
			hint.html("");
			hint.removeClass("warn");
			var val = aInput.val();
			if(!val || val.length === 0) return;
			if(regex.test(val))	return;
			hint.addClass("warn");
			hint.html(requirement);
		});	
	}
} 

function ProfileEditor() {
	// TODO: add transitional animations to cover the abrupt GUI changes  

	this.NORMAL = 0;	
	this.EDITING = 1;
	this.mode = this.NORMAL; 
	this.infoTableVarList = [
		["age", TITLES.age, g_playerAgePattern, MESSAGES.age_requirement],
		["gender", TITLES.gender, g_playerGenderPattern, MESSAGES.gender_requirement],
		["mood", TITLES.mood, g_playerMoodPattern, MESSAGES.mood_requirement]
	];

	this.composeContent = function(player) {
		if (!player) return;
		this.player = player;

		// avatar 
		if (!this.avatarBox)	this.avatarBox = $('<div>', {
			"class": "profile-avatar-box"
		}).appendTo(this.content);

		if (!this.avatarPreview) {
			this.avatarPreview = $('<div>', {
				"class": "preview-container"
			}).hide().appendTo(this.content);  
			$('<img>', {
				src: player.avatar
			}).appendTo(this.avatarPreview);
		}
		if (this.mode == this.NORMAL) this.avatarPreview.show();	
		else this.avatarPreview.hide();	

		if (!this.infoTable) {
			this.infoTable = $("<div>", {
				"class": "profile-info-table clear"
			}).appendTo(this.content);

			var disabled = (this.mode == this.NORMAL);		
			for (var idx in this.infoTableVarList) {
				var tmpList = this.infoTableVarList[idx];
				refreshProfileInfoTable(this.infoTable, tmpList[0], player[tmpList[0]], tmpList[1], tmpList[2], tmpList[3], disabled);
			}
		}

		if (!g_loggedInPlayer || player.id != g_loggedInPlayer.id) return;

		if (!this.avatarNode) {
		        var domain = queryCDNDomainSync();
			this.avatarNode = new ProfileEditorImageNode(g_cdnQiniu, domain);
			this.avatarNode.appendTo(this.avatarBox);	
			this.avatarNode.refresh(this);
			this.avatarHint = $("<p>", {
				"class": "profile-avatar-hint"
			}).appendTo(this.avatarBox);
		}
		if (this.mode == this.EDITING)	{
			this.avatarNode.show();
			this.avatarHint.show();	
		} else {
			this.avatarNode.hide();
			this.avatarHint.hide();	
		}
	
		// buttons
		if (!this.buttonRow) this.buttonRow = $("<p>").appendTo(this.content);

		if (!this.btnEdit) {
			this.btnEdit = $("<button>", {
				text: TITLES.edit,
				"class": "profile-btn-edit positive-button" 
			}).hide().appendTo(this.buttonRow).click(this, function(evt) {
				var editor = evt.data;
				editor.mode = editor.EDITING;
				editor.refresh(player);
			});
		}
		if (this.mode == this.NORMAL) this.btnEdit.show();
		else this.btnEdit.hide();

		if (!this.btnCancel) {
			this.btnCancel = $("<button>", {
				text: TITLES.cancel,
				"class": "profile-btn-cancel negative-button"
			}).hide().appendTo(this.buttonRow).click(this, function(evt) {
				var editor = evt.data;
				editor.mode = editor.NORMAL;
				editor.refresh(player);
			});
		}
		if (this.mode == this.EDITING) this.btnCancel.show();
		else this.btnCancel.hide();

		if (!this.btnSave) {
			this.btnSave = $("<button>", {
				text: TITLES.save,	
				"class": "btn-save positive-button"
			}).hide().appendTo(this.buttonRow).click(this, function(evt) {
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
		}
		if (this.mode == this.EDITING) this.btnSave.show();
		else this.btnSave.hide();

		if (!g_loggedInPlayer) return;
		if (g_loggedInPlayer.hasEmail() && !g_loggedInPlayer.isEmailAuthenticated() && g_vieweeId == g_loggedInPlayer.id) {
			if (!this.btnResend) {
				this.resendHint = $("<p>", {
					"class": "profile-resend-hint"
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
						if (!data || isStandardFailure(data) || isTokenExpired(data) || isPlayerNotFound(data)) {
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
				this.btnResend.refresh(dButton);
				this.btnResend.button.addClass("caution-button");
			}
			if (this.mode == this.NORMAL) {
				this.btnResend.show();
				this.resendHint.show();
			} else {
				this.btnResend.hide();
				this.resendHint.hide();
			}
		}
	};
}

ProfileEditor.inherits(BaseWidget);
// for lazy-loading
ProfileEditor.method('refresh', function(data) {
	if (!this.content) return;	
	this.composeContent(data);
});

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
