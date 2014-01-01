function onBtnUploadAvatarClicked(evt){

	if(validateImage()==false){
		return;
	}
	
	var form = $("#"+g_idFormAvatar);

	form.submit( function(formEvt){
		var formObj = $(this);
		var formData = new FormData(this);
		
		// append an user token for identity
		var token = $.cookie(g_keyLoginStatus.toString());
		formData.append(g_keyUserToken, token);
		
		$.ajax({
			method: "POST",
			url: "/upload", 
			data: formData,
			mimeType: "mutltipart/form-data",
			contentType: false,
			processData: false,
			success: function(data, status, xhr){
		    	$("#"+g_idSectionResponse).html(data);
			},
			error: function(xhr, status, errorThrown){
				$("#"+g_idSectionResponse).html("Failed");
			}
		});
		formEvt.preventDefault(); // prevent default action.
	});
	form.submit();
}