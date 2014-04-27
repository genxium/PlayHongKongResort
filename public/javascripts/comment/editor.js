var g_replyEditor=null;

function removeReplyEditor(){
    if(g_replyEditor){
        g_replyEditor.remove();
        g_replyEditor=null;    
    }    
}

function generateReplyEditor(activityId, parentId, predecessorId){
    var ret=$('<p>');
    var input=$('<input>').appendTo(ret); 
    var btnSubmit=$('<button>',{
        text: "SUBMIT REPLY",
        style: "color: white; background-color: gray; border: none"
    }).appendTo(ret);
    btnSubmit.data("ActivityId", activityId);
    btnSubmit.data("ParentId", parentId);
    btnSubmit.data("PredecessorId", predecessorId);

    btnSubmit.on("click", function(evt){
        evt.preventDefault();
        var btn=$(this);
        var editor=btn.closest('p');
        var input=editor.children('input');
        var content=input.val();
        var activityId=btn.data("ActivityId");
        var parentId=btn.data("ParentId");
        var predecessorId=btn.data("PredecessorId");
        var token=$.cookie(g_keyLoginStatus.toString());

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
        class: "classCommentCell",
        style: "border-left: thick solid #000000" 
    });
    
    do{
        var infoRow=$('<p>').appendTo(ret);
        var content=$('<span>', {
            text: commentJson[g_keyCommentAContent],
            style: "text-align: left; float: left"
        }).appendTo(infoRow);
        
        var commenterName=$('<span>', {
            text: commentJson[g_keyCommenterName], 
            style: "position: absolute; left: 200px"
        }).appendTo(infoRow);        
        
        var commenterName=$('<span>', {
            text: commentJson[g_keyGeneratedTime], 
            style: "position: absolute; left: 500px; color: blue"
        }).appendTo(infoRow);        
        
        var token=$.cookie(g_keyLoginStatus.toString());
        if(token==null) break; 

        $('<br>').appendTo(ret);
        var operationRow=$('<p>').appendTo(ret);
        var btnReply=$('<button>',{
            text: "reply",
            style: "position: absolute; left: 500px; color: white; background-color: black; border: none"
        }).appendTo(operationRow);

        var parentId=commentJson[g_keyCommentAParentId];
        var predecessorId=commentJson[g_keyCommentAId];
        if(parentId==(-1)){
            // root comment
            parentId=predecessorId;
        }
        btnReply.data("ActivityId", activityId);
        btnReply.data("ParentId", parentId);
        btnReply.data("PredecessorId", predecessorId);
        btnReply.on("click", function(){
            removeReplyEditor();
            var btn=$(this);
            var activityId=btn.data("ActivityId");
            var parentId=btn.data("ParentId");
            var predecessorId=btn.data("PredecessorId");
            g_replyEditor=generateReplyEditor(activityId, parentId, predecessorId);
            var commentCell=$(this).closest(".classCommentCell");
            commentCell.append(g_replyEditor);
        });
    }while(false);
    return ret;
}

function generateCommentEditor(activityId){
    var ret=$('<div>');
    var input=$('<input>', {

    }).appendTo(ret);
    input.css("font-size", 18);

    var btnSubmit=$('<button>',{
        text: "Comment!"
    }).appendTo(ret);

    // jQuery.bind() doesn't work here because the DOM element hasn't been created yet
    btnSubmit.on("click", function(evt){
        evt.preventDefault();
        var editor=$(this).closest('div');
        var input=editor.children('input');
        var content=input.val();
        var token=$.cookie(g_keyLoginStatus.toString());

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
    });
    return ret;
}
