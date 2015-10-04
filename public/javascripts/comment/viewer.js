var g_commentId = null; // a global g_commentId is set for the convenience of uniformed paramGenerator(pager, page) prototype in class Pager 
var g_pagerSubComments = null;

function SubCommentPager(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError) {
	this.init(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError);

	this.updateScreen = function(data) {
		if (!data) return;

		var subCommentsJson = data[g_keySubComments];
		var length = Object.keys(subCommentsJson).length;

		var pageSt = parseInt(data[g_keyPageSt]);
		var pageEd = parseInt(data[g_keyPageEd]);
		var page = pageSt;

		var comments = [];
		for(var idx = 1; idx <= length; ++idx){
			var commentJson = subCommentsJson[idx - 1];
			var comment = new Comment(commentJson);
			comments.push(comment);
			if (page == this.page)	generateSubCommentCell(this.screen, commentJson, g_activity);

			if (idx % this.nItems !== 0) continue;
			this.cache.putPage(page, comments);
			comments = [];
			++page;
		}
		if (!(!comments) && comments.length > 0) {
			// for the last page
			this.cache.putPage(page, comments);
		}
	};
}

SubCommentPager.inherits(Pager);
SubCommentPager.method("appendTo", function(par) {
	var aPager = this;
	this.content= $('<div>').appendTo(par);
	this.btnBack = $("<button>", {
		text: "< " + TITLES.back,
		"class": "back-button patch-block-lambda"
	}).appendTo(this.content).click(function(evt) {
		evt.preventDefault();
		aPager.hide();
		g_commentId = null;
		g_pagerComments.show();
	});
});

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
	if(!g_activity) return;
	g_pagerSubComments.refreshScreen(data);
}

function onListSubCommentsError(err){
	
}
