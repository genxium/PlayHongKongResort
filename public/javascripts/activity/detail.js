// general dom elements
var g_sectionActivity = null;
var g_sectionNav = null;
var g_sectionPanes = null;
var g_barButtons = null;

var g_navTab = null;
var g_activityId = null;
var g_activity = null;

function clearDetail() {
	if (g_sectionActivity != null) g_sectionActivity.empty();
	if (g_sectionNav != null) g_sectionNav.empty();
	if (g_sectionPanes != null) g_sectionPanes.empty();
	if (g_barButtons != null) g_barButtons.empty();
}

function queryActivityDetail(activityId){

        var token = $.cookie(g_keyToken);
    	var params = {};
    	params[g_keyActivityId] = activityId;
        if(token != null)	params[g_keyToken] = token;

        $.ajax({
		type: "GET",
		url: "/activity/detail",
		data: params,
		success: function(data, status, xhr){
			var activityJson = JSON.parse(data);
			g_activity = new Activity(activityJson);
			g_barButtons.empty();
			var buttonContainer = $("<span>", {
				style: "float: right; width: 20%; height: 10%;"
			}).appendTo(g_barButtons);
			attachJoinButton(buttonContainer, g_activity);
			displayActivityDetail(g_sectionActivity);
		},
		error: function(xhr, status, err){

		}
        });
}

function displayActivityDetail(par){

	g_onJoined = queryActivityDetail;

	par.empty();
	var ret = $("<div>").appendTo(par);
        var row1 = $("<p>").appendTo(ret);

	var title = $("<span>",{
		text: g_activity.title.toString(),
		style: "font-size: 18pt; color: blue"
	}).appendTo(row1);

        if(g_activity.host.id != null && g_activity.host.name != null){
                var sp1 = $('<span>', {
                        text: " -- by",
                        style: "margin-left: 20%; font-size : 14pt"
                }).appendTo(row1);
                var sp2 = $('<span>', {
                        style: "margin-left: 5pt"
                }).appendTo(row1);
                var host = $('<a>', {
			href: "#", 
                        text: "@" + g_activity.host.name,
                        style: "font-size: 14pt; font-weight: bold;"
                }).appendTo(sp2);
		host.click(function(evt){
			evt.preventDefault();
			window.location.hash = (g_keyVieweeId + "=" + g_activity.host.id.toString());	
		});
        }

        displayTimesTable(ret, g_activity);

	var content = $('<div>',{
		text: g_activity.content,
		style: "margin-top: 10px; font-size: 15pt"
	}).appendTo(ret);

	if(g_activity.images != null) {
		// the images are expected to be arranged in a non-uniform manner(not confirmed), thus they should not be bounded to static CSS styling, the current style is a temporary solution
		var constantHeight = 128;
		var imagesNode = $('<p>').appendTo(ret);
		for(var i=0;i<g_activity.images.length;++i){
			$('<img>',{
				src: g_activity.images[i].url,
				style: "width: auto; height: " + constantHeight.toString() + "px;"
			}).appendTo(imagesNode);
		}
	}	

	// Tab participants
	g_tabParticipants.empty();
	g_participantsForm = generateParticipantsSelectionForm(g_tabParticipants, g_activity);

	listCommentsAndRefresh(g_activity);

	// Tab assessments
	var viewer = null;
	if(g_activity.hasOwnProperty("viewer")) viewer = g_activity.viewer;
	g_batchAssessmentEditor = generateBatchAssessmentEditor(g_tabAssessments, g_activity, queryActivityDetail);

	var token = $.cookie(g_keyToken);
	if(token == null)   return ret;

	if(g_activity.hasBegun()) {
	    $("<p>", {
	        style: "color: red; font-size: 13pt",
	        text: "Q & A is disabled because the activity has begun. You can still view existing conversations"
	    }).appendTo(ret);
	    return ret;
	}

	// Comment editor
	generateCommentEditor(ret, g_activity);
	g_onCommentSubmitSuccess = function() {
		if (g_commentId == null)	listCommentsAndRefresh(g_activity);
		else listSubCommentsAndRefresh(g_commentId); 
	}

	return ret;
}

// Callback Functions
function onParticipantsSelectionFormSubmission(formEvt){
	
	formEvt.preventDefault(); // prevent default action.
	var selectedParticipants = new Array();
	for(var i = 0; i < g_participantsForm.labels.length; i++) {
		var box = g_participantsForm.boxes[i];
		if (box == null || !box.is(":checked")) continue;
		var participantId = g_participantsForm.participantsId[i];
		if(participantId == g_activity.host.id) continue;
		selectedParticipants.push(participantId);
	}
	// append user token and activity id for identity
	var token = $.cookie(g_keyToken);
	if(token == null) return;

	var params={};
	params[g_keyToken] = token;
	params[g_keyActivityId] = g_activityId;
	params[g_keySelectedParticipants] = JSON.stringify(selectedParticipants);

	$.ajax({
		type: "POST",
		url: "/activity/participants/update",
		data: params,
		success: function(data, status, xhr){
			for(var i = 0; i < g_participantsForm.labels.length; ++i){
			    var label = g_participantsForm.labels[i];
			    // ignore selected participants
			    if(g_participantsForm.participantsStatus[i] == g_aliasSelected) continue;
			    var box = g_participantsForm.boxes[i];
			    if(!box.is(":checked")) continue;
                            label.css("background-color", "aquamarine");
                            box.hide();
			}
		},
		error: function(xhr, status, err) {

		}
	});
}

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
	g_sectionNav = $("#section-nav")
	g_sectionPanes = $("#section-panes");
	g_barButtons = $("#bar-buttons");

	var refs = ["tab-comments", "tab-participants", "tab-assessments"];
	var titles = ["Q & A", "參與者", "評價"];
	var preactiveRef = refs[0];	
		
	var tabCommentContent = $("<div>", {
		style: "margin-top: 5pt; width: 100%; position: relative;"
	});
	var commentsContainer = $("<div>", {
		style: "position: absolute; width: 100%; height: auto; left: 0px; top: 0px;"
	}).appendTo(tabCommentContent); 
	var commentPagerBar = $("<p>").appendTo(commentsContainer);
	var commentPagerScreen = $("<div>").appendTo(commentsContainer);

	// sub-comments' container is initially invisible 
	var subCommentsContainer = $("<div>", {
		style: "position: absolute; width: 0%; height: auto; left: 100%; top 0px;"
	}).appendTo(tabCommentContent);
	var btnBack = $("<button>", {
		text: "< BACK",
		style: "border: none; background-color: white; color: crimson; cursor: pointer; margin-top: 5px; margin-bottom: 5px;"
	}).appendTo(subCommentsContainer);
	// note that the back button in sub-comments' container takes both pagers as input
	btnBack.click(function(evt) {
		evt.preventDefault();
		g_commentId = null;
		g_pagerComments.expand(null);
		setOffset(g_pagerComments.screen.parent(), "0%", null);
		g_pagerSubComments.squeeze();
		setOffset(g_pagerSubComments.screen.parent(), "100%", null);
	});
	var subCommentPagerBar = $("<p>").appendTo(subCommentsContainer);
	var subCommentPagerScreen = $("<div>").appendTo(subCommentsContainer);

	var contents = [tabCommentContent, null, null];

	g_navTab = createNavTab(g_sectionNav, refs, titles, preactiveRef, g_sectionPanes, contents);

	g_tabComments = g_navTab.panes[0];
	g_tabParticipants = g_navTab.panes[1];
	g_tabAssessments = g_navTab.panes[2];

	var commentsCache = new PagerCache(5);
	g_pagerComments = new Pager(commentPagerScreen, commentPagerBar, 5, "/comment/list", generateCommentsListParams, commentsCache, null, onListCommentsSuccess, onListCommentsError);

	var subCommentsCache = new PagerCache(20);	

	g_pagerSubComments = new Pager(subCommentPagerScreen, subCommentPagerBar, 20, "/comment/sub/list", generateCommentsListParams, subCommentsCache, null, onListSubCommentsSuccess, onListSubCommentsError);
	
	// squeeze the sub-comments' container
	g_pagerSubComments.squeeze();

	var onLoginSuccess = function(data) {
		queryActivityDetail(g_activityId);
	};

	var onLoginError = function(err) {
		queryActivityDetail(g_activityId);
	};

	var onLogoutSuccess = function(data) {
		queryActivityDetail(g_activityId);
	};
	
	var onLogoutError = null;

	g_preLoginForm = generatePreLoginForm(g_sectionLogin, onLoginSuccess, onLoginError, onLogoutSuccess, onLogoutError);

	checkLoginStatus();
}
