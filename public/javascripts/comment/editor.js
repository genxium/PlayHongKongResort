var g_minContentLength=5;
var g_replyEditor=null;

function removeReplyEditor(){
    if(g_replyEditor){
        g_replyEditor.remove();
        g_replyEditor=null;    
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
    btnSubmit.data(g_keyActivityId, activityId);
    btnSubmit.data(g_keyParentId, parentId);
    btnSubmit.data(g_keyPredecessorId, predecessorId);

    btnSubmit.on("click", function(evt){
		do{
			evt.preventDefault();
			var btn=$(this);
			var editor=btn.closest('p');
			var input=editor.children('input');
			var content=input.val();
			var activityId=btn.data(g_keyActivityId);
			var parentId=btn.data(g_keyParentId);
			var predecessorId=btn.data(g_keyPredecessorId);
			var token=$.cookie(g_keyToken);

			if(content==null || content.length<=g_minContentLength) {
				alert("Please comment with no less than "+g_minContentLength.toString()+" characters!");
				break;	
			}

			var params={};
			params[g_keyContent]=content;
			params[g_keyParentId]=parentId;
			params[g_keyPredecessorId]=predecessorId;
			params[g_keyActivityId]=activityId;
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

function generateCommentCell(commentJson, activityId){
	var ret=$('<div>');
    
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
		btnReply.data(g_keyActivityId, activityId);
		btnReply.data(g_keyParentId, parentId);
		btnReply.data(g_keyPredecessorId, predecessorId);
			btnReply.data("CommentCell", ret);
		btnReply.on("click", function(){
		    removeReplyEditor();
		    var btn=$(this);
		    var activityId=btn.data(g_keyActivityId);
		    var parentId=btn.data(g_keyParentId);
		    var predecessorId=btn.data(g_keyPredecessorId);
		    g_replyEditor=generateReplyEditor(activityId, parentId, predecessorId, commentJson[g_keyCommenterName]);
		    var commentCell=btn.data("CommentCell");
		    commentCell.append(g_replyEditor);
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
		href: "/user/profile/show?"+g_keyUserId+"="+replyee,
		text: "to @"+replyee+": ",
		style: "color: BlueViolet"	
	}).appendTo(spanReplyee);
        var content=$('<span>', {
            text: commentJson[g_keyContent],
            style: "text-align: left; margin-left: 25pt"
        }).appendTo(row);
        
        var commenterName=$('<span>', {
            text: commentJson[g_keyCommenterName], 
            style: "text-align: left; margin-left: 25pt; color: brown;"
        }).appendTo(row);        
        
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
        btnReply.data(g_keyActivityId, activityId);
        btnReply.data(g_keyParentId, parentId);
        btnReply.data(g_keyPredecessorId, predecessorId);
		btnReply.data("CommentCell", ret);
        btnReply.on("click", function(){
            removeReplyEditor();
            var btn=$(this);
            var activityId=btn.data(g_keyActivityId);
            var parentId=btn.data(g_keyParentId);
            var predecessorId=btn.data(g_keyPredecessorId);
            g_replyEditor=generateReplyEditor(activityId, parentId, predecessorId, commentJson[g_keyCommenterName]);
            var commentCell=btn.data("CommentCell");
            commentCell.append(g_replyEditor);
        });
    }while(false);
    return ret;
}

function generateCommentEditor(activityId){
    var ret=$('<div>');
    var input=$('<input>', {
	style: "font-size: 15pt"
    }).appendTo(ret);

    var btnSubmit=$('<button>',{
        text: "Comment!",
	style: "font-size: 15pt; margin-left: 2pt"
    }).appendTo(ret);

    // jQuery.bind() doesn't work here because the DOM element hasn't been created yet
    btnSubmit.on("click", function(evt){
        do{
			evt.preventDefault();
			var editor=$(this).closest('div');
			var input=editor.children('input');
			var content=input.val();
			var token=$.cookie(g_keyToken);
			
			if(content==null || content.length<=g_minContentLength) {
				alert("Please comment with no less than "+g_minContentLength.toString()+" characters!");
				break;	
			}

			var params={};
			params[g_keyContent]=content;
			params[g_keyActivityId]=activityId;
			params[g_keyToken]=token;

			$.ajax({
				type: "POST",
				url: "/comment/submit",
				data: params,
				success: function(data, status, xhr){
					location.reload();
				},
				error: function(xhr, status, err){
					alert("Comment not submitted...");
				}
			});
		}while(false);
    });
    return ret;
}
