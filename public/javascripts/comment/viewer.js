var g_commentId = null; // a global g_commentId is set for the convenience of uniformed paramGenerator(pager, page) prototype in class Pager 
var g_pagerSubComments = null;

function listSubCommentsAndRefresh(parentId) {
	var page = 1;
	listSubComments(page, parentId, onListSubCommentsSuccess, onListSubCommentsError);
}

function listSubComments(page, parentId, onSuccess, onError){
	g_commentId = parentId;
	var params = generateCommentsListParams(g_pagerSubComments, page);
	$.ajax({
		type: "GET",
		url: "/comment/sub/list",
		data: params,
		success: function(data, status, xhr) {
		    onSuccess(data);
		},
		error: function(xhr, status, err) {
		    onError(err);
		}
	});
}

// Tab Q&A a.k.a comments
function onListSubCommentsSuccess(data){
	// this function is only valid in detail's page
	if(g_activity == null || data == null) return;

	var subCommentsJson = data[g_keySubComments];
	var length = Object.keys(subCommentsJson).length;

        var pageSt = parseInt(data[g_keyPageSt]);
        var pageEd = parseInt(data[g_keyPageEd]);
        var page = pageSt;

	g_pagerSubComments.screen.empty();
	var comments = [];
	for(var idx = 1; idx <= length; ++idx){
		var commentJson = subCommentsJson[idx - 1];
		var comment = new Comment(commentJson);
		comments.push(comment);
		if (page == g_pagerSubComments.page) {
                    generateSubCommentCell(g_pagerSubComments.screen, commentJson, g_activity);
                }

                if (idx % g_pagerSubComments.nItems != 0) continue;
                g_pagerSubComments.cache.putPage(page, comments);
                comments = [];
                ++page;
	}
	if (comments != null && comments.length > 0) {
                // for the last page
                g_pagerSubComments.cache.putPage(page, comments);
        }
        g_pagerSubComments.refreshBar();
}

function onListSubCommentsError(err){
	
}
