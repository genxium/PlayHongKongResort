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
        params["PredecessorId"]=(-1);
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
