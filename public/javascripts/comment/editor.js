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
    btnSubmit.data("ActivityId", activityId);
    btnSubmit.data("ParentId", parentId);
    btnSubmit.data("PredecessorId", predecessorId);

    btnSubmit.on("click", function(evt){
		do{
			evt.preventDefault();
			var btn=$(this);
			var editor=btn.closest('p');
			var input=editor.children('input');
			var content=input.val();
			var activityId=btn.data("ActivityId");
			var parentId=btn.data("ParentId");
			var predecessorId=btn.data("PredecessorId");
			var token=$.cookie(g_keyToken);

			if(content==null || content.length<=g_minContentLength) {
				alert("Please comment with no less than "+g_minContentLength.toString()+" characters!");
				break;	
			}

			var params={};
			params["CommentAContent"]=content;
			params["ParentId"]=parentId;
			params["PredecessorId"]=predecessorId;
			params["ActivityId"]=activityId;
			params["UserToken"]=token;

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
	var ret=$('<div>', {
		style: "border-left: thick solid #000000" 
	});
    
        var row=$('<p>').appendTo(ret);
        var content=$('<span>', {
            text: commentJson[g_keyCommentAContent],
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

		var parentId=commentJson[g_keyCommentAParentId];
		var predecessorId=commentJson[g_keyCommentAId];
		if(parentId==(-1)){
		    // root comment
		    parentId=predecessorId;
		}
		btnReply.data("ActivityId", activityId);
		btnReply.data("ParentId", parentId);
		btnReply.data("PredecessorId", predecessorId);
			btnReply.data("CommentCell", ret);
		btnReply.on("click", function(){
		    removeReplyEditor();
		    var btn=$(this);
		    var activityId=btn.data("ActivityId");
		    var parentId=btn.data("ParentId");
		    var predecessorId=btn.data("PredecessorId");
		    g_replyEditor=generateReplyEditor(activityId, parentId, predecessorId, commentJson[g_keyCommenterName]);
		    var commentCell=btn.data("CommentCell");
		    commentCell.append(g_replyEditor);
		});
	} while(false);

	// Sub-Comments
	var subComments=commentJson["SubComments"];
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

		var toWhom=$('<span>', {
			text: "to @"+commentJson[g_keyReplyeeName]+": ",
			style: "color: BlueViolet"
		}).appendTo(row);

        var content=$('<span>', {
            text: commentJson[g_keyCommentAContent],
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
        var parentId=commentJson[g_keyCommentAParentId];
        var predecessorId=commentJson[g_keyCommentAId];
        if(parentId==(-1)){
            // root comment
            parentId=predecessorId;
        }
        btnReply.data("ActivityId", activityId);
        btnReply.data("ParentId", parentId);
        btnReply.data("PredecessorId", predecessorId);
		btnReply.data("CommentCell", ret);
        btnReply.on("click", function(){
            removeReplyEditor();
            var btn=$(this);
            var activityId=btn.data("ActivityId");
            var parentId=btn.data("ParentId");
            var predecessorId=btn.data("PredecessorId");
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
			params["CommentAContent"]=content;
			params["ActivityId"]=activityId;
			params["UserToken"]=token;

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
