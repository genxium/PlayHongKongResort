// Assistive Callback Functions
function onParticipantsSelectionFormSubmission(formEvt){
	do{
		formEvt.preventDefault(); // prevent default action.

		var formObj = $(this);
		var formData = new FormData(this);
		
		// append user token and activity id for identity
		var token = $.cookie(g_keyLoginStatus.toString());
		formData.append(g_keyUserToken, token);

		var activityId = $(this).data(g_keyActivityId);
		formData.append(g_keyActivityId, activityId.toString());
		
		$.ajax({
			method: "POST",
			url: "/updateActivityParticipants", 
			data: formData,
			contentType: false,
			processData: false,
			success: function(data, status, xhr){
				formObj.remove();
			},
			error: function(xhr, status, errorThrown){

			}
		});
	}while(false);
}

// Generators
function generateActivityDetailViewByJson(activityJson){
	var activityId=activityJson[g_keyActivityId];
	var activityTitle=activityJson[g_keyActivityTitle];
	var activityContent=activityJson[g_keyActivityContent];
	var activityImages=activityJson[g_keyActivityImages];
    alert(JSON.stringify(activityImages));
    var ret=$('<div>',{
            
        });
    
    var title=$('<p>',{
            html: activityTitle.toString()
        }).appendTo(ret);
        
    var content=$('<div>',{
            html: activityContent.toString()
        }).appendTo(ret); 	

    do{
        if(activityImages==null) break;
        var imagesNode=$('<p>',{
            
        }).appendTo(ret);

        for(var key in activityImages){
           if(activityImages.hasOwnProperty(key)){
               var activityImage=activityImages[key];
               var imageUrl=activityImage[g_keyImageURL];
               var imageNode=$('<img>',{
                    src: imageUrl.toString()   
               }).appendTo(imagesNode);
           }
        }

    }while(false);
	return ret;
}
