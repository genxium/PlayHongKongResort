// general dom elements
var g_sectionActivity = null;
var g_sectionNav = null;
var g_sectionPanes = null;
var g_barButtons = null;

var g_navTab = null;
var g_activityId = null;
var g_activity = null;

function emptyBarButtons() {
	if (!g_barButtons) return;
	g_barButtons.empty();
}

function clearDetail() {
	if (!(!g_sectionActivity)) g_sectionActivity.empty();
	if (!(!g_sectionNav)) g_sectionNav.empty();
	if (!(!g_sectionPanes)) g_sectionPanes.empty();
	emptyBarButtons();
}

function queryActivityDetail(activityId){

        var token = getToken();
    	var params = {};
    	params[g_keyActivityId] = activityId;
        if(!(!token))	params[g_keyToken] = token;

        $.ajax({
			type: "GET",
			url: "/activity/detail",
			data: params,
			success: function(data, status, xhr){
				var activityJson = data;
				g_activity = new Activity(activityJson);
				displayActivityDetail(g_sectionActivity);
				if (!g_loggedInPlayer || g_loggedInPlayer.id == g_activity.host.id) {
					emptyBarButtons();
					return;
				}
				if (g_activity.isDeadlineExpired() && !g_activity.relation) {
					emptyBarButtons();
					return;
				}
				g_barButtons.empty();
				//setDimensions(g_barButtons, "auto", "100px"); // resume dimensions
				attachJoinButton(g_barButtons, g_activity);
			},
			error: function(xhr, status, err){
				alert(ALERTS.not_permitted_to_view_detail);	
				emptyBarButtons();	
				g_sectionActivity.empty();	
			}
        });
}

function displayActivityDetail(par){

	g_onJoined = queryActivityDetail;

	par.empty();
	var ret = $("<div>", {
		"class": "activity-detail-page"
	}).appendTo(par);

	var title = $("<div>", {
		text: g_activity.title,
		"class": "activity-title title-delta"
	}).appendTo(ret);

	var address = $("<div>", {
		text: g_activity.address,
		"class": "activity-addr-detail title-beta"	
	}).appendTo(ret);

	if(!(!g_activity.host.id) && !(!g_activity.host.name)){
			var host = $('<a>', {
					href: "#", 
					text: TITLES.by_host.format(g_activity.host.name),
					"class": "activity-host patch-block-lambda"
			}).appendTo(ret);
			host.click(function(evt){
				evt.preventDefault();
				window.location.hash = ("profile?" + g_keyVieweeId + "=" + g_activity.host.id.toString());	
			});
	}

	displayTimesTable(ret, g_activity);

	var content = $("<div>",{
		text: g_activity.content,
		"class": "activity-content title-lambda"
	}).appendTo(ret);

	if(!(!g_activity.images)) {
		var imagesContainer = $("<div>", {
			"class": "activity-image-container clearfix"
		}).appendTo(ret);
		for(var i = 0; i < g_activity.images.length; ++i){
			var imageNode = $('<div>', {
				"class": "activity-image left"
			}).appendTo(imagesContainer);
			$('<span>',{
				"class": "image-helper"
			}).appendTo(imageNode);
			$('<img>',{
				src: g_activity.images[i].url,
			}).appendTo(imageNode);
		}
	}	

	// Tab participants
	g_tabParticipants.empty();
	g_participantsForm = generateParticipantsSelectionForm(g_tabParticipants, g_activity);

	listCommentsAndRefresh(g_activity);

	// Tab assessments
	initAssessmentModalWidget($("#content"));
	g_batchAssessmentEditor = generateBatchAssessmentEditor(g_tabAssessments, g_activity, queryActivityDetail);

	var token = getToken();
	if(!token)   return ret;

	if (g_activity.hasBegun()) {
		$("<p>", {
			"class": "comment-not-permitted .warning",
			text: MESSAGES.comment_disabled_activity_has_begun
		}).appendTo(ret);
		return ret;
	}

	if (!(!g_activity.status) && g_activity.status != g_statusAccepted) {
		// for host viewing unaccepted activity
		$("<p>", {
			"class": "comment-not-permitted .warning",
			text: MESSAGES.comment_disabled_activity_not_accepted
		}).appendTo(ret);
		return ret;
	}

	// Comment editor
	generateCommentEditor(ret, g_activity);
	g_onCommentSubmitSuccess = function() {
		if (!g_commentId)	listCommentsAndRefresh(g_activity);
		else listSubCommentsAndRefresh(g_commentId); 
	};

	return ret;
}

// Callback Functions
// Assistive Event Handlers
function onBtnSubmitClicked(evt) {
	evt.preventDefault();
	var selectionForm = $(this).parent();	
	selectionForm.submit(onParticipantsSelectionFormSubmission);
	selectionForm.submit();
}

function requestActivityDetail(activityId) {
	clearProfile();
	clearHome();
	clearNotifications();

	g_activityId = activityId;
	g_sectionActivity = $("#section-activity");
	g_sectionNav = $("#section-nav");
	g_sectionPanes = $("#section-panes");
	g_barButtons = $("#section-player");

	var refs = ["tab-comments", "tab-participants", "tab-assessments"];
	var titles = [TITLES.question, TITLES.participant, TITLES.assessment];
	var preactiveRef = refs[0];	
		
	var tabCommentContent = $("<div>", {
		"class": "tab-container"
	});

	var contents = [tabCommentContent, null, null];

	g_navTab = createNavTab(g_sectionNav, refs, titles, preactiveRef, g_sectionPanes, contents);

	g_tabComments = g_navTab.panes[0];
	g_tabParticipants = g_navTab.panes[1];
	g_tabAssessments = g_navTab.panes[2];

	// top-level-comments
	g_pagerComments = new CommentPager(5, "/comment/list", generateCommentsListParams, null, 5, null, onListCommentsSuccess, onListCommentsError);
	g_pagerComments.appendTo(tabCommentContent);
	g_pagerComments.refresh();

	// sub-comments
	g_pagerSubComments = new SubCommentPager(10, "/comment/sub/list", generateCommentsListParams, null, 5, null, onListSubCommentsSuccess, onListSubCommentsError);
	g_pagerSubComments.appendTo(tabCommentContent);
	g_pagerSubComments.hide();
	g_pagerSubComments.refresh();

	var onLoginSuccess = function(data) {
		countNotifications();
		queryActivityDetail(g_activityId);
	};

	var onLoginError = function(err) {
		queryActivityDetail(g_activityId);
	};

	var onLogoutSuccess = function(data) {
		queryActivityDetail(g_activityId);
	};
	
	var onLogoutError = null;

	g_preLoginForm = generatePreLoginForm(g_sectionLogin, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError, true);

	checkLoginStatus();
}
