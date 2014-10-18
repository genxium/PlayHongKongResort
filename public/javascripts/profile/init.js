var g_formAvatar = null;
var g_sectionUser = null;
var g_sectionResponse = null;

var g_btnUploadAvatar=null;

// Assistant Callback Functions
function onUploadAvatarFormSubmission(formEvt){

	formEvt.preventDefault(); // prevent default action.

	var formObj = $(this);
	var formData = new FormData(this);
	
	// append an user token for identity
	var token = $.cookie(g_keyToken);
	formData.append(g_keyToken, token);
	
	$.ajax({
		method: "POST",
		url: "/user/avatar/upload", 
		data: formData,
		mimeType: "mutltipart/form-data",
		contentType: false,
		processData: false,
		success: function(data, status, xhr){
			g_sectionResponse.html("Uploaded");
		},
		error: function(xhr, status, err){
			g_sectionResponse.html("Failed");
		}
	});

}

function queryActivitiesAndRefresh() {
	if(g_vieweeId == null) return;
	var page = 1;
	queryActivities(page, onQueryActivitiesSuccess, onQueryActivitiesError);
}


// Event Handlers
function onBtnUploadAvatarClicked(evt){

	var file = document.getElementById(g_keyAvatar);
	if(validateImage(file)==false){
		return;
	}

	// set callback function of form submission
	g_formAvatar.submit(onUploadAvatarFormSubmission);
	// invoke submission
	g_formAvatar.submit();
}

function refreshOnEnter(){
	g_formAvatar.hide();
	queryUserDetail();
}

function refreshOnLoggedIn(){
	g_formAvatar.show();
	queryUserDetail();
}

function queryUserDetail(){
	var params={};
	params[g_keyVieweeId] = g_vieweeId;
	var token=$.cookie(g_keyToken);
	if(token!=null) params[g_keyToken]=token;
	$.ajax({
		type: "GET",
		url: "/user/detail",
		data: params,
		success: function(data, status, xhr){
			if(g_sectionUser == null) return;
			g_sectionUser.empty();
			var userJson=JSON.parse(data);
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
	
	initTopbar();
	var relationSelector = createSelector($("#pager-filters"), ["發起的活動", "參與的活動"], [hosted, present], null, null, null, null);
	var orientationSelector = createSelector($("#pager-filters"), ["時間倒序", "時間順序"], [g_orderDescend, g_orderAscend], null, null, null, null);
	var relationFilter = new PagerFilter("relation", relationSelector);
	var orientationFilter = new PagerFilter("orientation", orientationSelector); 
	var filters = [relationFilter, orientationFilter];	
	var pagerCache = new PagerCache(5);
	
	// initialize pager widgets
	g_pager = new Pager($("#pager-screen-activities"), $("#pager-bar-activities"), g_numItemsPerPage, "/activity/query", generateActivitiesQueryParams, pagerCache, filters, onQueryActivitiesSuccess, onQueryActivitiesError);

	g_onQueryActivitiesSuccess = onQueryActivitiesSuccess;
	g_onQueryActivitiesError = onQueryActivitiesError;
	
	g_onLoginSuccess = function(){
		refreshOnLoggedIn();
		queryActivitiesAndRefresh();
	};
	g_onLoginError = null;
	g_onEnter = refreshOnEnter;
	initActivityEditor();

	g_formAvatar = $("#form-avatar");
	g_sectionResponse = $("#section-response");

	g_activitiesFilter = $("#activities-filter");
	g_activitiesFilter.on("change", function(){
		g_pager.relation = $(this).val();
		queryActivitiesAndRefresh();	 
	});

	g_activitiesSorter = $("#activities-sorter");
	g_activitiesSorter.on("change", function(){
		g_pager.orientation = $(this).val();
		queryActivitiesAndRefresh();
	});


	g_sectionUser = $("#section-user");

	g_btnUploadAvatar = $("#btn-upload-avatar");
	g_btnUploadAvatar.on("click", onBtnUploadAvatarClicked);
 	g_onEditorRemoved = queryActivitiesAndRefresh;

	g_onActivitySaveSuccess = queryActivitiesAndRefresh;

	checkLoginStatus();
});
