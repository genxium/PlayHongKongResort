var g_sectionComment = null;

function listSubCommentsAndRefresh() {
	var page = 1;
	listSubComments(page, onListSubCommentsSuccess, onListSubCommentsError);
}

function listSubComments(page, onSuccess, onError){
    // prototypes: onSuccess(data), onError
    var params = generateCommentsListParams(g_pager, page);
    $.ajax({
        type: "GET",
        url: "/comment/sub/list",
        data: params,
        success: function(data, status, xhr) {
            onSuccess(data);
        },
        error: function(xhr, status, err) {
            onError();
        }
    });
}

// Tab Q&A a.k.a comments
function onListSubCommentsSuccess(data){
	// this function is only valid in detail's page
	if(g_activity == null) return;

	var jsonResponse = JSON.parse(data);
	if(jsonResponse == null) return;

	var subCommentsJson = jsonResponse[g_keySubComments];
	var length = Object.keys(subCommentsJson).length;

	var page = parseInt(jsonResponse[g_keyPage]);

	g_pager.screen.empty();
	var idx = 0;
	var comments = [];
	for(var idx = 1; idx <= length; ++idx){
		var commentJson = subCommentsJson[idx - 1];
		var comment = new Comment(commentJson);
		comments.push(comment);
		if (page == g_pager.page) {
                    generateSubCommentCell(g_pager.screen, commentJson, g_activity);
                }

                if (idx % g_pager.nItems != 0) continue;
                g_pager.cache.putPage(page, comments);
                comments = [];
                ++page;
	}
	if (comments != null && comments.length > 0) {
                // for the last page
                g_pager.cache.putPage(page, comments);
        }
        g_pager.refreshBar();
}

function onListSubCommentsError(){
	
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
			g_onCommentSubmitSuccess = listSubCommentsAndRefresh;
			postInit();
		},
		error: function(xhr, status, err) {

		}
	});
}

function postInit() {

	var pagerCache = new PagerCache(20);	

	g_pager = new PagerContainer($("#pager-screen-sub-comments"), $("#pager-bar-sub-comments"), g_keyId, g_orderDescend, 5, "/comment/sub/list", generateCommentsListParams, pagerCache, null, onListSubCommentsSuccess, onListSubCommentsError);

	g_onLoginSuccess = function() {
		listSubCommentsAndRefresh();
	};

	g_onLoginError = null;

	g_onEnter = function() {
		listSubCommentsAndRefresh();
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
