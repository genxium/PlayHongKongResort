var g_comment = null;
var g_activityId = null;
var g_activity = null;

var g_replyEditor = null;

var g_onCommentSubmitSuccess = null;

var g_pagerComments = null;

function CommentPager(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError) {
	this.init(numItemsPerPage, url, paramsGenerator, extraParams, cacheSize, filterMap, onSuccess, onError);

	this.updateScreen = function(data) {
		if (!data) return;

		var commentsJson = data[g_keyComments];
		var length = Object.keys(commentsJson).length;

		var pageSt = parseInt(data[g_keyPageSt]);
		var pageEd = parseInt(data[g_keyPageEd]);
		var page = pageSt;

		var comments = [];
		for(var idx = 1; idx <= length; ++idx) {
			var commentJson = commentsJson[idx - 1];
			var comment = new Comment(commentJson);
			comments.push(comment);
			if (page == this.page)	generateCommentCell(this.screen, commentJson, g_activity, false);

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

CommentPager.inherits(Pager);

function CommentReplyEditor(input, submit, collapse) {
	this.input = input;
	this.submit = submit;
	this.collapse = collapse;
	this.focus = function() {
		this.input.focus();
	};
	this.remove = function() {
		this.input.parent().remove();
	};
}

function listCommentsAndRefresh(activity) {
	var page = 1;
        listComments(page, onListCommentsSuccess, onListCommentsError);
}

function listComments(page, onSuccess, onError){
	var params = generateCommentsListParams(g_pagerComments, page);
	$.ajax({
		type: "GET",
		url: "/comment/list",
		data: params,
		success: function(data, status, xhr) {
			onSuccess(data);
		},
		error: function(xhr, status, err) {
			onError(err);
		}
	});
}

function generateCommentsListParams(pager, page) {
	var params = {};
	if (!(!g_commentId))	params[g_keyParentId] = g_commentId;
	if (!(!g_activity))		params[g_keyActivityId] = g_activity.id;
	var pageSt = page - 2;
        var pageEd = page + 2;
        var offset = (pageSt < 1 ? (pageSt - 1) : 0);
        pageSt -= offset;
        pageEd -= offset;
        params[g_keyPageSt] = pageSt;
        params[g_keyPageEd] = pageEd;
	params[g_keyNumItems] = pager.nItems;
	params[g_keyOrientation] = g_orderDescend;
	return params;
}

// Tab Q & A a.k.a comments
function onListCommentsSuccess(data){
	// this function is only valid in detail's page
	if(!g_activity) return;
	g_pagerComments.refreshScreen(data);
}

function onListCommentsError(err){

}

function removeReplyEditor(){
    if (!g_replyEditor) return;
    g_replyEditor.remove();
    g_replyEditor = null;
}

function generateReplyEditor(par, activity, comment){
    var ret = $('<p>').appendTo(par);
    var input = $('<input>', {
        placeholder: HINTS.reply.format(comment.fromPlayer.name)
    }).appendTo(ret);
    var btnSubmit = $('<button>',{
        text: TITLES.submit_comment_reply,
        "class": "comment-submit positive-button"
    }).appendTo(ret);

    btnSubmit.click(input, function(evt) {

                evt.preventDefault();
		var aInput = evt.data;
                var content = evt.data.val();
                var token = getToken();

                if (!content || !validateCommentContent(content)) {
			alert(ALERTS.comment_requirement);
			return;
		}

                var parentId = comment.parentId == (-1) ? comment.id : comment.parentId;
                var params={};
                params[g_keyContent] = content;
                params[g_keyParentId] = parentId;
                params[g_keyPredecessorId] = comment.id;
                params[g_keyActivityId] = activity.id;
                params[g_keyToken] = token;
                params[g_keyTo] = comment.from;
		
		var aButton = getTarget(evt); 

		disableField(aButton);
		disableField(aInput);
                $.ajax({
                        type: "POST",
                        url: "/el/comment/sub/submit",
                        data: params,
                        success: function(data, status, xhr){
				enableField(aButton);
				enableField(aInput);
				if (isTokenExpired(data)) {
					logout(null);
					return;
				}
                                removeReplyEditor();
                                if(!g_onCommentSubmitSuccess) return;
                                g_onCommentSubmitSuccess();
                        },
                        error: function(xhr, status, err){
				enableField(aButton);
				enableField(aInput);
                                alert(MESSAGES.comment_reply_not_submitted);
                        }
                });

    });

    var btnCollapse = $('<button>',{
        text: TITLES.collapse,
        "class": "negative-button"
    }).appendTo(ret);

    btnCollapse.click(function(evt){
        evt.preventDefault();
        removeReplyEditor();
    });
	
    return new CommentReplyEditor(input, btnSubmit, btnCollapse);
}

function generateCommentCell(par, commentJson, activity, single){
	var comment = new Comment(commentJson);
	var ret = $('<div>', {
	    "class": "comment-group"
	}).appendTo(par);
        var row = $('<div>', {
		"class": "comment-row clearfix"
	}).appendTo(ret);
	if (single) row.addClass("single-row");

	var content = $('<div>', {
		text: comment.content,
		"class": "comment-content left"
	}).appendTo(row);

	var commentFrom = $('<div>', {
		"class": "comment-from left"
	}).appendTo(row);
	var hrefFromName = $('<a>', {
		"class": "patch-block-lambda",
		href: "#",
		text: comment.fromPlayer.name,
		target: "_blank"
	}).appendTo(commentFrom);
	hrefFromName.click(function(evt) {
		evt.preventDefault();
		window.location.hash = ("profile?" + g_keyVieweeId + "=" + comment.from.toString());	
	});
        
        var generatedTime = $('<div>', {
            text: gmtMiilisecToLocalYmdhis(comment.generatedTime),
            "class": "comment-time left"
        }).appendTo(row);

	if (!single && comment.numChildren > 3) {
		var spanView = $("<div>", {
			"class": "comment-view left patch-block-lambda"
		}).appendTo(row);
		var viewAll = $("<a>", {
			text: TITLES.view_all_replies.format(comment.numChildren)
		}).appendTo(spanView).click(comment.id, function(evt) {
			evt.preventDefault();
			g_pagerComments.hide();
			g_pagerSubComments.show();

			var commentId = evt.data;	
			listSubCommentsAndRefresh(commentId);
		});
	}


	if (!single) {
		// Sub-Comments
		var subComments = commentJson[g_keySubComments];
		for(var key in subComments){
		    var subCommentJson = subComments[key];
		    generateSubCommentCell(ret, subCommentJson, activity);
		}
	}

        var token = getToken();
        if(!token || activity.hasBegun()) return;

        var operations = $('<div>',{
                "class": "comment-action left"
        }).appendTo(row);

        var btnReply = $('<button>',{
            text: TITLES.reply,
            "class": "comment-reply positive-button"
        }).appendTo(operations);

        btnReply.click(ret, function(evt){
            evt.preventDefault();
            var cell = evt.data;
            removeReplyEditor();
            g_replyEditor = generateReplyEditor(cell, activity, comment);
            g_replyEditor.focus();
        });
}

function generateSubCommentCell(par, commentJson, activity){

	var comment = new Comment(commentJson);

	var ret = $("<div>", {
		"class": "comment-group subgroup"
	}).appendTo(par);

	var row = $("<div>", {
		"class": "comment-row clearfix"
	}).appendTo(ret);
	var commentTo = $('<div>', {
		"class": "comment-to left"
	}).appendTo(row);
	var hrefTo = $('<a>', {
		"class": "patch-block-lambda",
		href: "#",
		text: TITLES.replied_to.format(comment.toPlayer.name),
		target: "_blank",
	}).appendTo(commentTo);
	hrefTo.click(function(evt) {
		evt.preventDefault();
		window.location.hash = ("profile?" + g_keyVieweeId + "=" + comment.to.toString());	
	});

	var content = $('<div>', {
		text: comment.content,
		"class": "comment-content left"
	}).appendTo(row);

	var commentFrom = $('<div>', {
		"class": "comment-from left"
	}).appendTo(row);
	var hrefFromName = $('<a>', {
		"class": "patch-block-lambda",
		href: "#",
		text: comment.fromPlayer.name,
		target: "_blank"
	}).appendTo(commentFrom);
	hrefFromName.click(function(evt) {
		evt.preventDefault();
		window.location.hash = ("profile?" + g_keyVieweeId + "=" + comment.from.toString());	
	});

	var generatedTime = $('<div>', {
		text: gmtMiilisecToLocalYmdhis(comment.generatedTime),
		"class": "comment-time left"
	}).appendTo(row);

	var token = getToken();
	if(!token || activity.hasBegun()) return ret;

	var operations = $('<div>',{
		"class": "comment-action left"
	}).appendTo(row);

	var btnReply = $('<button>',{
		text: TITLES.reply,
		"class": "comment-reply positive-button"
	}).appendTo(operations);

	btnReply.click(ret, function(evt){
		evt.preventDefault();
		var cell = evt.data;
		removeReplyEditor();
		g_replyEditor = generateReplyEditor(cell, activity, comment);
		g_replyEditor.focus();
	});
}

function generateCommentEditor(par, activity){
    var editor = $('<div>', {
		"class": "activity-comment"
	}).appendTo(par);
    var input = $('<input>', {
    }).appendTo(editor);
    var btnSubmit = $('<button>',{
        text: TITLES.submit_comment_question,
    	"class": "positive-button"
    }).appendTo(editor);

    var inputCounter = new WordCounter("", 5, 128, g_commentContentPattern, ALERTS.comment_requirement);
    inputCounter.appendCounter(editor);

    input.on("input paste keyup", inputCounter, function(evt){
			evt.data.update($(this).val());
    });

    btnSubmit.click({counter: inputCounter, input: input}, function(evt){

		evt.preventDefault();
		var content = input.val();
		var token = getToken();
		
		if(!content || !validateCommentContent(content)) {
			return;	
		}

		var params={};
		params[g_keyContent] = content;
		params[g_keyActivityId] = activity.id;
		params[g_keyToken] = token;

		var aCounter = evt.data.counter;
		var aInput = evt.data.input;
				
		var aButton = $(evt.srcElement ? evt.srcElement : evt.target);
		disableField(aButton);
		disableField(aInput);
		$.ajax({
			type: "POST",
			url: "/el/comment/submit",
			data: params,
			success: function(data, status, xhr){
				enableField(aButton);
				enableField(aInput);
				if (isTokenExpired(data)) {
					logout(null);
					return;
				}
				aInput.val("");
				aCounter.update("");
				listCommentsAndRefresh(activity, null, null);
			},
			error: function(xhr, status, err){
				enableField(aButton);
				enableField(aInput);
				alert(ALERTS.comment_question_not_submitted);
			}
		});
    });
}
