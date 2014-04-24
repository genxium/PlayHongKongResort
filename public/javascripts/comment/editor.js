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
        text: "submit!!!"    
    }).appendTo(ret);
    btnSubmit.on("click", function(evt){
        evt.preventDefault();
        var editor=$(this).closest('p');
        var input=editor.children('input');
        var content=input.val();
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

            },
            error: function(xhr, status, err){
                alert("Comment not submitted...");
            }
        });
    });
    return ret;
}

function generateCommentCell(commentJson, activityId){
    var ret=$('<div>', {
        class: "classCommentCell"
    });
    
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
    
    $('<br>').appendTo(ret);
    var operationRow=$('<p>').appendTo(ret);
    var btnReply=$('<button>',{
        text: "reply"
    }).appendTo(operationRow);
    btnReply.on("click", function(){
        removeReplyEditor();
        g_replyEditor=generateReplyEditor(activityId, commentJson[g_keyCommentAParentId], commentJson[g_keyCommentAId]);
        var commentCell=$(this).closest(".classCommentCell");
        commentCell.append(g_replyEditor);
    });
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
