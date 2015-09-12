var g_sectionPlayer = null;
var g_viewee = null;
var g_profileEditor = null;

function ProfileEditor() {
	// player
	this.player = null;

	// avatar
	this.avatarNode = null;
	this.avatarHint = null;

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

	this.composeContent = function(player) {
		if (!player) return null;
		this.player = player;

		// avatar 
		var avatarBox = $('<div>').hide().appendTo(this.content);
		if (this.mode == this.EDITING) avatarBox.show();

		var avatarNormalView = $('<img>', {
			"class": "image-helper",
			src: player.avatar
		}).hide().appendTo(this.content);			
		if (this.mode == this.NORMAL) avatarNormalView.show();	

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
			this.avatarHint = $("<p>").appendTo(this.avatarBox);
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
			formData[g_keyAvatar] = editor.avatarNode.remoteName;
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
	};
}

ProfileEditor.inherits(BaseWidget);

function clearProfile() {
	$("#pager-filters").empty();
	$("#pager-bar-activities").empty();
	$("#pager-screen-activities").empty();
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
			
			// refresh pager for assessments
			if (!(!g_pagerAssessments)) g_pagerAssessments.remove();
			var pagerBar = $("<div>", {
				id: "pager-bar-assessments"
			}).appendTo(g_sectionPlayer);
			var pagerScreen = $("<div>", {
				id: "pager-screen-assessments"
			}).appendTo(g_sectionPlayer);
			var pagerCache = new PagerCache(5);
			var extraParams = {
				to: g_viewee.id
			};
			g_pagerAssessments = new Pager(pagerScreen, pagerBar, 10, "/assessment/list", generateAssessmentsListParams, extraParams, pagerCache, null, onQueryAssessmentsSuccess, onQueryAssessmentsError); 	
			if (!g_loggedInPlayer) return;
			if (g_loggedInPlayer.hasEmail() && !g_loggedInPlayer.isEmailAuthenticated() && g_vieweeId == g_loggedInPlayer.id) {
				var resendHint = null;
				var extraParams2 = {};
				extraParams2[g_keyToken] = getToken();
				var onSuccess = function(data) {
					if (!data) return;
					if (isTokenExpired(data)) {
						logout(null);
						return;
					}
					resendHint.text(MESSAGES.email_verification_sent.format(data[g_keyEmail]));
				};
				var onError = function(err) {
					resendHint.text(MESSAGES.email_verification_not_sent);
				};
				var btnResend = new AjaxButton(TITLES.resend_email_verification, "/player/email/resend", null, "POST", extraParams2, onSuccess, onError);
				btnResend.appendTo(g_sectionPlayer)
				btnResend.button.addClass("caution-button");
				resendHint = $("<p>", {
					"class": "hint-resend"
				}).appendTo(g_sectionPlayer);
			}

			if (g_loggedInPlayer.id == g_vieweeId) return;
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

	var relationSelector = createSelector($("#pager-filters"), [TITLES.hosted_activities, TITLES.joined_activities], [hosted, present], null, null, null, null);
	var orientationSelector = createSelector($("#pager-filters"), [TITLES.time_descendant, TITLES.time_ascendant], [g_orderDescend, g_orderAscend], null, null, null, null);
	var relationFilter = new PagerFilter(g_keyRelation, relationSelector);
	var orientationFilter = new PagerFilter(g_keyOrientation, orientationSelector); 
	var filters = [relationFilter, orientationFilter];	
	var pagerCache = new PagerCache(5);
	
	// initialize pager widgets
	g_pager = new Pager($("#pager-screen-activities"), $("#pager-bar-activities"), g_numItemsPerPage, "/activity/list", generateActivitiesListParams, null, pagerCache, filters, onListActivitiesSuccess, onListActivitiesError);
	
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
