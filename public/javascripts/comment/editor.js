var g_minContentLength = 5;
var g_replyEditor = null;

var g_onCommentsQuerySuccess = null;
var g_onCommentsQueryError = null;

function queryComments(params, onSuccess, onError){
    if(onSuccess != null) g_onCommentsQuerySuccess = onSuccess;
    if(onError != null) g_onCommentsQueryError = onError;
	$.ajax({
	    type: "GET",
	    url: "/comment/query",
	    data: params,
	    success: g_onCommentsQuerySuccess,
	    error: g_onCommentsQueryError
	});
}

function removeReplyEditor(){
    if(g_replyEditor){
        g_replyEditor.remove();
        g_replyEditor = null;    
    }    
}

function generateReplyEditor(activityId, parentId, predecessorId, toUsername){
    var ret=$('<p>');
    var input=$('<input>', {
		placeholder: "to @"+toUsername+":"
	}).appendTo(ret);
    var btnSubmit=$('<button>',{
        text: "SUBMIT REPLY",
        style: "color: white; background-color: gray; border: none"
    }).appendTo(ret);

    btnSubmit.on("click", {activityId: activityId, parentId: parentId, predecessorId: predecessorId, input: input}, function(evt){
		do{
			evt.preventDefault();
			var data=evt.data;
			var content=data.input.val();
			var token=$.cookie(g_keyToken);

			if(content==null || content.length<=g_minContentLength) {
				alert("Please comment with no less than "+g_minContentLength.toString()+" characters!");
				break;	
			}

			var params={};
			params[g_keyContent]=content;
			params[g_keyParentId]=data.parentId;
			params[g_keyPredecessorId]=data.predecessorId;
			params[g_keyActivityId]=data.activityId;
			params[g_keyToken]=token;

			$.ajax({
				type: "POST",
				url: "/comment/submit",
				data: params,
				success: function(data, status, xhr){
					removeReplyEditor();
				},
				error: function(xhr, status, err){
					alert("Comment not submitted...");
				}
			});
		}while(false);
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

function generateCommentCell(par, commentJson, activityId){
	var ret=$('<div>').appendTo(par);
    
        var row=$('<p>').appendTo(ret);
        var content=$('<span>', {
            text: commentJson[g_keyContent],
            style: "text-align: left; margin-left: 25pt"
        }).appendTo(row);
        
	var commenterId=commentJson[g_keyCommenterId];
	var commenterName=commentJson[g_keyCommenterName];
        var spanCommenterName=$('<span>').appendTo(row);        
	var hrefCommenterName=$('<a>', {
	    href: "/user/profile/show?"+g_keyUserId+"="+commenterId,
            text: commenterName, 
            style: "text-align: left; margin-left: 25pt; color: brown;"
	}).appendTo(spanCommenterName);
        
        var generatedTime=$('<span>', {
            text: commentJson[g_keyGeneratedTime], 
            style: "text-align: left; margin-left:  25pt; color: blue"
        }).appendTo(row);        
        
	do{
		var token=$.cookie(g_keyToken);
		if(token==null) break; 

		var operations=$('<span>',{
				style: "margin-left: 20pt"	
			}).appendTo(row);

		var btnReply=$('<button>',{
		    text: "reply",
		    style: "color: white; background-color: black; border: none"
		}).appendTo(operations);

		var parentId=commentJson[g_keyParentId];
		var predecessorId=commentJson[g_keyId];
		if(parentId==(-1)){
		    // root comment
		    parentId=predecessorId;
		}
		btnReply.on("click", {activityId: activityId, parentId: parentId, predecessorId: predecessorId, cell: ret}, function(evt){
			evt.preventDefault();
			var data=evt.data;
			removeReplyEditor();
			g_replyEditor=generateReplyEditor(data.activityId, data.parentId, data.predecessorId, commentJson[g_keyCommenterName]);
			data.cell.append(g_replyEditor);
		});
	} while(false);

	// Sub-Comments
	var subComments=commentJson[g_keySubComments];
	for(var key in subComments){
		var subCommentJson=subComments[key];
		var subCommentCell=generateSubCommentCell(subCommentJson, activityId);		
		ret.append(subCommentCell);
	}

	return ret;
}

function generateSubCommentCell(commentJson, activityId){
    var ret=$('<div>', {
        style: "text-indent: 25pt; border-left: thick solid #000000" 
    });
    
    do{
        var row=$('<p>').appendTo(ret);
	var replyeeId=commentJson[g_keyReplyeeId];
	var replyeeName=commentJson[g_keyReplyeeName];
	var spanReplyee=$('<span>').appendTo(row);
	var hrefReplyee=$('<a>', {
		href: "/user/profile/show?"+g_keyUserId+"="+replyeeId,
		text: "to @"+replyeeName+": ",
		style: "color: BlueViolet"	
	}).appendTo(spanReplyee);
        var content=$('<span>', {
            text: commentJson[g_keyContent],
            style: "text-align: left; margin-left: 25pt"
        }).appendTo(row);
        
	var commenterId=commentJson[g_keyCommenterId];
	var commenterName=commentJson[g_keyCommenterName];
        var spanCommenterName=$('<span>').appendTo(row);        
	var hrefCommenterName=$('<a>', {
	    href: "/user/profile/show?"+g_keyUserId+"="+commenterId,
            text: commenterName, 
            style: "text-align: left; margin-left: 25pt; color: brown;"
	}).appendTo(spanCommenterName);
        
        var generatedTime=$('<span>', {
            text: commentJson[g_keyGeneratedTime], 
            style: "text-align: left; margin-left:  25pt; color: blue"
        }).appendTo(row);        
        
        var token=$.cookie(g_keyToken);
        if(token==null) break; 

        var operations=$('<span>',{
			style: "margin-left: 20pt"	
		}).appendTo(row);

        var btnReply=$('<button>',{
            text: "reply",
            style: "color: white; background-color: black; border: none"
        }).appendTo(operations);
        var parentId=commentJson[g_keyParentId];
        var predecessorId=commentJson[g_keyId];
        if(parentId==(-1)){
            // root comment
            parentId=predecessorId;
        }
        btnReply.on("click", {activityId: activityId, parentId: parentId, predecessorId: predecessorId, cell: ret}, function(evt){
		evt.preventDefault();
		var data=evt.data;
		removeReplyEditor();
		g_replyEditor=generateReplyEditor(data.activityId, data.parentId, data.predecessorId, commentJson[g_keyCommenterName]);
		data.cell.append(g_replyEditor);
        });
    }while(false);
    return ret;
}

function generateCommentEditor(par, activityId){
    var ret=$('<div>').appendTo(par);
    var input=$('<input>', {
		style: "font-size: 15pt"
    }).appendTo(ret);

    var btnSubmit=$('<button>',{
        text: "Comment!",
		style: "font-size: 15pt; margin-left: 2pt"
    }).appendTo(ret);

    // jQuery.bind() doesn't work here because the DOM element hasn't been created yet
    btnSubmit.on("click", function(evt){

		evt.preventDefault();
		var editor = $(this).closest('div');
		var input = editor.children('input');
		var content = input.val();
		var token = $.cookie(g_keyToken);
		
		if(content == null || content.length <= g_minContentLength) {
			alert("Please comment with no less than " + g_minContentLength.toString() + " characters!");
			return;	
		}

		var params={};
		params[g_keyContent] = content;
		params[g_keyActivityId] = activityId;
		params[g_keyToken] = token;

		$.ajax({
			type: "POST",
			url: "/comment/submit",
			data: params,
			success: function(data, status, xhr){
				var params = {};
				params[g_keyActivityId] = activityId;
				params[g_keyRefIndex] = 0;
				params[g_keyNumItems] = 20;
				params[g_keyDirection] = 1;
				queryComments(params, null, null);
			},
			error: function(xhr, status, err){
				alert("Comment not submitted...");
			}
		});
    });
    return ret;
}
