var g_minContentLength = 5;
var g_replyEditor = null;

var g_onCommentSubmitSuccess = null;
var g_tabComments = null;

function onPagerButtonClicked(evt) {
	// this function is only valid in detail's page
	if(g_activity == null) return;
	// Please note that this function name might conflict with 
	var button = evt.data;
	var container = button.container;
	var page = button.page;
	if (page == container.page) return;
	var direction = page > container.page ? g_directionForward : g_directionBackward;
	var refIndex = page > container.page ? g_tabComments.ed : g_tabComments.st;

	queryComments(refIndex, container.nItems, direction, g_activity.id, onQueryCommentsSuccess, onQueryCommentsError);
}

function queryCommentsAndRefresh(activity) {
    queryComments(0, g_tabComments.nItems, 1, activity.id, onQueryCommentsSuccess, onQueryCommentsError);
}

function queryComments(refIndex, numItems, direction, activityId, onSuccess, onError){
    var params = {};
    params[g_keyActivityId] = activityId;
    params[g_keyRefIndex] = refIndex;
    params[g_keyNumItems] = numItems;
    params[g_keyDirection] = direction;
    $.ajax({
        type: "GET",
        url: "/comment/query",
        data: params,
        success: onSuccess,
        error: onError
    });
}

// Tab Q&A a.k.a comments
function onQueryCommentsSuccess(data, status, xhr){	
	// this function is only valid in detail's page
	if(g_activity == null) return;

	var oldSt = g_tabComments.st;
	var oldEd = g_tabComments.ed;

	var jsonResponse = JSON.parse(data);
	if(jsonResponse == null) return;
	var length = Object.keys(jsonResponse).length;
	if( length == 0) return;

	g_tabComments.screen.empty();
	var idx = 0;
	for(var key in jsonResponse){
		var commentJson = jsonResponse[key];
		generateCommentCell(g_tabComments.screen, commentJson, g_activity);
		var comment = new Comment(commentJson);
		$('<br>').appendTo(g_tabComments.screen);
		if(idx == 0)	g_tabComments.st = comment.id;
		if(idx == length - 1)	g_tabComments.ed = comment.id;
		++idx;
	}

	createPagerBar(g_tabComments, oldSt, oldEd, onPagerButtonClicked);
}

function onQueryCommentsError(xhr, status, err){
	
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

    var btnCollapse=$('<button>',{
        text: "COLLAPSE",
        style: "color: red; text-decoration: underline; background-color: none; border: none"
    }).appendTo(ret);
    btnCollapse.on("click", function(evt){
        evt.preventDefault();
        removeReplyEditor();
    });
    return ret;
}

function generateCommentCell(par, commentJson, activity){
	var comment = new Comment(commentJson);
	var ret=$('<div>').appendTo(par);
        var row = $('<p>').appendTo(ret);
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
            text: comment.generatedTime,
            style: "text-align: left; margin-left:  25pt; color: blue; font-size: 14pt"
        }).appendTo(row);

        // Sub-Comments
        var subComments = commentJson[g_keySubComments];
        for(var key in subComments){
            var subCommentJson = subComments[key];
            generateSubCommentCell(ret, subCommentJson, activity);
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

        var parentId = comment.parentId;
        var predecessorId = comment.id;
        if(parentId == (-1)){
            // root comment
            parentId = predecessorId;
        }
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
;
    var spanFromName = $('<span>').appendTo(row);
    var hrefFromName = $('<a>', {
            href: "/user/profile/show?" + g_keyVieweeId + "=" + comment.from,
            text: comment.fromName,
            target: "_blank",
            style: "text-align: left; margin-left: 25pt; color: brown; font-size: 13pt"
    }).appendTo(spanFromName);

    var generatedTime = $('<span>', {
        text: comment.generatedTime,
        style: "text-align: left; margin-left:  25pt; color: blue; font-size: 13pt"
    }).appendTo(row);

    var token=$.cookie(g_keyToken);
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
    var input=$('<input>', {
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
				queryCommentsAndRefresh(activity, null, null);
			},
			error: function(xhr, status, err){
				alert("Comment not submitted...");
			}
		});
    });
}
