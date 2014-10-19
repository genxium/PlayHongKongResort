var g_commentId = null;
var g_comment = null;
var g_activityId = null;
var g_activity = null;

var g_minContentLength = 5;
var g_replyEditor = null;

var g_onCommentSubmitSuccess = null;
var g_tabComments = null;

function queryCommentsAndRefresh(activity) {
	var page = 1;
	queryComments(page, onQueryCommentsSuccess, onQueryCommentsError);
}

function queryComments(page, onSuccess, onError){
	// prototypes: onSuccess(data), onError
	var params = generateCommentsQueryParams(g_tabComments, page);
	$.ajax({
		type: "GET",
		url: "/comment/query",
		data: params,
		success: function(data, status, xhr) {
			onSuccess(data);
		},
		error: function(xhr, status, err) {
			onError();
		}
	});
}

function generateCommentsQueryParams(pager, page) {

	if (page == pager.page) return null;
	var direction = page >= pager.page ? g_directionForward : g_directionBackward;
	var refIndex = page >= pager.page ? pager.ed : pager.st;
	if (page == 1) {
		direction = g_directionForward;
		refIndex = 0;
	}

	var params = {};
	if (g_commentId != null)	params[g_keyParentId] = g_commentId;
	if (g_activity != null)		params[g_keyActivityId] = g_activity.id;
	params[g_keyRefIndex] = refIndex;
	params[g_keyPage] = page;
	params[g_keyNumItems] = pager.nItems;
	params[g_keyDirection] = direction;
	params[g_keyOrientation] = g_orderDescend;
	return params;

}

// Tab Q&A a.k.a comments
function onQueryCommentsSuccess(data){
	// this function is only valid in detail's page
	if(g_activity == null) return;

	var jsonResponse = JSON.parse(data);
	if(jsonResponse == null) return;

	var commentsJson = jsonResponse[g_keyComments];
	var length = Object.keys(commentsJson).length;

	var page = parseInt(jsonResponse[g_keyPage]);

	g_tabComments.screen.empty();
	var idx = 0;
	var comments = new Array();
	for(var key in commentsJson){
		var commentJson = commentsJson[key];
		generateCommentCell(g_tabComments.screen, commentJson, g_activity, false);
		var comment = new Comment(commentJson);
		comments.push(comment);
		$('<br>').appendTo(g_tabComments.screen);
		if(idx == 0)	g_tabComments.st = comment.id;
		if(idx == length - 1)	g_tabComments.ed = comment.id;
		++idx;
	}
	
	g_tabComments.cache.putPage(page, comments);
	g_tabComments.page = page;
	g_tabComments.refreshBar(page);
}

function onQueryCommentsError(){
	
}

function removeReplyEditor(){
    if(g_replyEditor == null) return;
    g_replyEditor.remove();
    g_replyEditor = null;
}

function generateReplyEditor(activity, comment){
    var ret = $('<p>');
    var input = $('<input>', {
        placeholder: "to @" + comment.fromName + ":"
    }).appendTo(ret);
    var btnSubmit = $('<button>',{
        text: "SUBMIT REPLY",
        style: "color: white; background-color: gray; border: none"
    }).appendTo(ret);

    btnSubmit.on("click", {input: input}, function(evt) {

                evt.preventDefault();
                var data = evt.data;
                var content = data.input.val();
                var token = $.cookie(g_keyToken);

                if(content == null || content.length <= g_minContentLength) {
                        alert("Please comment with no less than " + g_minContentLength.toString() + " characters!");
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

                $.ajax({
                        type: "POST",
                        url: "/comment/sub/submit",
                        data: params,
                        success: function(data, status, xhr){
                                removeReplyEditor();
                                if(g_onCommentSubmitSuccess == null) return;
                                g_onCommentSubmitSuccess();
                        },
                        error: function(xhr, status, err){
                                alert("Comment not submitted...");
                        }
                });

    });

    var btnCollapse = $('<button>',{
        text: "COLLAPSE",
        style: "color: red; text-decoration: underline; background-color: none; border: none"
    }).appendTo(ret);
    btnCollapse.on("click", function(evt){
        evt.preventDefault();
        removeReplyEditor();
    });
    return ret;
}

function generateCommentCell(par, commentJson, activity, single){
	var comment = new Comment(commentJson);
	var ret = $('<div>').appendTo(par);
        var row = $('<p>').appendTo(ret);
	if (single) row.css("background-color", "Cornsilk");

        var content = $('<span>', {
            text: comment.content,
            style: "text-align: left; margin-left: 25pt; font-size: 14pt"
        }).appendTo(row);

        var spanFromName = $('<span>').appendTo(row);
        var hrefFromName = $('<a>', {
            href: "/user/profile/show?" + g_keyVieweeId + "=" + comment.from,
                text: comment.fromName,
                target: "_blank",
                style: "text-align: left; margin-left: 25pt; color: brown; font-size: 14pt"
        }).appendTo(spanFromName);
        
        var generatedTime = $('<span>', {
            text: truncateMillisec(comment.generatedTime),
            style: "text-align: left; margin-left:  25pt; color: blue; font-size: 14pt"
        }).appendTo(row);

	if (!single && comment.numChildren > 0) {
		var spanView = $("<span>", {
			style: "margin-left: 5px"
		}).appendTo(row);
		$("<a>", {
			text: "view all replies(" + comment.numChildren + ")",
			href: "/comment/view?" + g_keyCommentId + "=" + comment.id.toString() + "&" + g_keyActivityId + "=" + activity.id.toString(),
			target: "_blank",
			style: "cursor: pointer"
		}).appendTo(spanView);
	}

	if (!single) {
		// Sub-Comments
		var subComments = commentJson[g_keySubComments];
		for(var key in subComments){
		    var subCommentJson = subComments[key];
		    generateSubCommentCell(ret, subCommentJson, activity);
		}
	}

        var token = $.cookie(g_keyToken);
        if(token == null || activity.hasBegun()) return;

        var operations = $('<span>',{
                style: "margin-left: 20pt"
        }).appendTo(row);

        var btnReply = $('<button>',{
            text: "reply",
            style: "color: white; background-color: black; border: none"
        }).appendTo(operations);

        var dBtnReply = {};
        dBtnReply[g_keyCell] = ret;

        btnReply.on("click", dBtnReply, function(evt){
            evt.preventDefault();
            var data = evt.data;
            removeReplyEditor();
            g_replyEditor = generateReplyEditor(activity, comment);
            data.cell.append(g_replyEditor);
        });
}

function generateSubCommentCell(par, commentJson, activity){

    var comment = new Comment(commentJson);

    var ret=$('<div>', {
        style: "text-indent: 25pt; border-left: thick solid #000000" 
    }).appendTo(par);

    var row=$('<p>').appendTo(ret);
    var spanTo=$('<span>').appendTo(row);
    var hrefTo=$('<a>', {
            href: "/user/profile/show?" + g_keyVieweeId + "=" + comment.to,
            text: "to @" + comment.toName + ": ",
            target: "_blank",
            style: "color: BlueViolet; font-size: 13pt"
    }).appendTo(spanTo);
    var content=$('<span>', {
        text: comment.content,
        style: "text-align: left; margin-left: 25pt; font-size: 13pt"
    }).appendTo(row);

    var spanFromName = $('<span>').appendTo(row);
    var hrefFromName = $('<a>', {
            href: "/user/profile/show?" + g_keyVieweeId + "=" + comment.from,
            text: comment.fromName,
            target: "_blank",
            style: "text-align: left; margin-left: 25pt; color: brown; font-size: 13pt"
    }).appendTo(spanFromName);

    var generatedTime = $('<span>', {
        text: truncateMillisec(comment.generatedTime),
        style: "text-align: left; margin-left:  25pt; color: blue; font-size: 13pt"
    }).appendTo(row);

    var token = $.cookie(g_keyToken);
    if(token == null || activity.hasBegun()) return ret;

    var operations=$('<span>',{
            style: "margin-left: 20pt"
    }).appendTo(row);

    var btnReply=$('<button>',{
        text: "reply",
        style: "color: white; background-color: black; border: none"
    }).appendTo(operations);

    var dBtnReply = {};
    dBtnReply[g_keyCell] = ret;

    btnReply.on("click", dBtnReply, function(evt){
            evt.preventDefault();
            var data=evt.data;
            removeReplyEditor();
            g_replyEditor = generateReplyEditor(activity, comment);
            data[g_keyCell].append(g_replyEditor);
    });
}

function generateCommentEditor(par, activity){
    var editor = $('<div>').appendTo(par);
    var input = $('<input>', {
        style: "font-size: 15pt"
    }).appendTo(editor);

    var btnSubmit = $('<button>',{
        text: "Comment!",
        style: "font-size: 15pt; margin-left: 2pt"
    }).appendTo(editor);

    // jQuery.bind() doesn't work here because the DOM element hasn't been created yet
    btnSubmit.on("click", function(evt){

		evt.preventDefault();
		var content = input.val();
		var token = $.cookie(g_keyToken);
		
		if(content == null || content.length <= g_minContentLength) {
			alert("Please comment with no less than " + g_minContentLength.toString() + " characters!");
			return;	
		}

		var params={};
		params[g_keyContent] = content;
		params[g_keyActivityId] = activity.id;
		params[g_keyToken] = token;

		$.ajax({
			type: "POST",
			url: "/comment/submit",
			data: params,
			success: function(data, status, xhr){
			    input.val("");
				queryCommentsAndRefresh(activity, null, null);
			},
			error: function(xhr, status, err){
				alert("Comment not submitted...");
			}
		});
    });
}
