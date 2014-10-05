var g_sectionComment = null;
var g_pagerContainer = null;
var g_commentId = null;
var g_activityId = null;
var g_comment = null;
var g_activity = null;

function querySubCommentsAndRefresh() {
    querySubComments(0, g_pagerContainer.nItems, g_directionForward, g_commentId, onQuerySubCommentsSuccess, onQuerySubCommentsError);
}

function querySubComments(refIndex, numItems, direction, commentId, onSuccess, onError){
    // prototypes: onSuccess(data), onError
    var params = {};
    params[g_keyParentId] = commentId;
    params[g_keyRefIndex] = refIndex;
    params[g_keyNumItems] = numItems;
    params[g_keyDirection] = direction;
    $.ajax({
        type: "GET",
        url: "/comment/sub/query",
        data: params,
        success: function(data, status, xhr) {
            onSuccess(data);
        },
        error: function(xhr, status, err) {
            onError();
        }
    });
}

function generateCommentsQueryParams(container, page) {

	if (page == container.page) return null;
	var direction = page > container.page ? g_directionForward : g_directionBackward;
	var refIndex = page > container.page ? g_pagerContainer.ed : g_pagerContainer.st;

	var params = {};
	params[g_keyActivityId] = g_activity.id;
	params[g_keyRefIndex] = refIndex;
	params[g_keyNumItems] = container.nItems;
	params[g_keyDirection] = direction;

	return params;

}

// Tab Q&A a.k.a comments
function onQuerySubCommentsSuccess(data){
	// this function is only valid in detail's page
	if(g_activity == null) return;

	var oldSt = g_pagerContainer.st;
	var oldEd = g_pagerContainer.ed;

	var jsonResponse = JSON.parse(data);
	if(jsonResponse == null) return;
	var length = Object.keys(jsonResponse).length;
	if( length == 0) return;

	g_pagerContainer.screen.empty();
	var idx = 0;
	for(var key in jsonResponse){
		var commentJson = jsonResponse[key];
		generateSubCommentCell(g_pagerContainer.screen, commentJson, g_activity);
		var comment = new Comment(commentJson);
		$('<br>').appendTo(g_pagerContainer.screen);
		if(idx == 0)	g_pagerContainer.st = comment.id;
		if(idx == length - 1)	g_pagerContainer.ed = comment.id;
		++idx;
	}

	createPagerBar(g_pagerContainer, oldSt, oldEd, onQuerySubCommentsSuccess, onQuerySubCommentsError);
}

function onQuerySubCommentsError(){
	
}

function generateSubCommentsQueryParams(container, page) {

	if (container == null || container.page == null) return;
	if (page == container.page) return null;
	var direction = page > container.page ? g_directionForward : g_directionBackward;
	var refIndex = page > container.page ? g_pagerContainer.ed : g_pagerContainer.st;

	var params = {};
	params[g_keyParentId] = g_commentId;
	params[g_keyRefIndex] = refIndex;
	params[g_keyNumItems] = container.nItems;
	params[g_keyDirection] = direction;

	return params;

}

function querySingleComment() {
	if (g_commentId == null) return;
	var params = {};
	params[g_keyCommentId] = g_commentId;
	$.ajax({
		type: "GET",
		url: "/comment/single/query?" + g_keyCommentId + "=" + g_commentId,
		success: function(data, status, xhr) {
			var commentJson = JSON.parse(data);
			g_comment = new Comment(commentJson);
			g_sectionComment = $("#section-comment");
			generateCommentCell(g_sectionComment, commentJson, g_activity, true);
			g_onCommentSubmitSuccess = querySubCommentsAndRefresh;
			postInit();
		},
		error: function(xhr, status, err) {

		}
	});
}

function postInit() {
	g_pagerContainer = new PagerContainer($("#pager-screen-sub-comments"), $("#pager-bar-sub-comments"),
			g_keyId, g_orderDescend, 5,
			"/comment/sub/query", generateSubCommentsQueryParams);

	g_onLoginSuccess = function() {
		querySubCommentsAndRefresh();
	};

	g_onLoginError = null;

	g_onEnter = function() {
		querySubCommentsAndRefresh();
	};

	initActivityEditor();

	checkLoginStatus();
}

// execute on start
$(document).ready(function(){

	var params = extractParams(window.location.href);
	for(var i = 0; i < params.length; i++){
		var param = params[i];
		var pair = param.split("=");
		if(pair[0] == g_keyCommentId) {
			g_commentId = parseInt(pair[1]);
		}
		if(pair[0] == g_keyActivityId) {
			g_activityId = parseInt(pair[1]);
		} 
	}
	
	initTopbar();

        var token = $.cookie(g_keyToken);
    	var params = {};
    	params[g_keyActivityId] = g_activityId;
        if(token != null)	params[g_keyToken] = token;

        $.ajax({
            type: "GET",
            url: "/activity/detail",
            data: params,
            success: function(data, status, xhr){
                var activityJson = JSON.parse(data);
    		g_activity = new Activity(activityJson);
		if (g_activity == null) return;
		querySingleComment();
            },
            error: function(xhr, status, err){

            }
        });

});
