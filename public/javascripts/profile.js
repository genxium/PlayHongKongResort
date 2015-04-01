var g_sectionUser = null;
var g_viewee = null;

function emptySectionUser() {
	if (g_sectionUser == null) return;
	g_sectionUser.empty();
}

function clearProfile() {
	$("#pager-filters").empty();
	$("#pager-bar-activities").empty();
	$("#pager-screen-activities").empty();
	emptySectionUser();
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
			var pic = $("<div>", {
				"class": "section-user-avatar left"
			}).appendTo(profile);
			var imageHelper = $("<span>", {
				"class": "image-helper"
			}).appendTo(pic);
			var profileImage = $("<img>", {
				src: g_viewee.avatar
			}).appendTo(pic);
			//setBackgroundImageDefault(pic, avatar);
			var userInfo = $("<div>", {
				"class": "section-user-info left"
			}).appendTo(profile);
			var name = $("<div>", {
				text: username,
				"class": "section-user-name"
			}).appendTo(userInfo);
			
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
