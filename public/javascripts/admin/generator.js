// Assistant Handlers
function onBtnAcceptClicked(evt){

	evt.preventDefault();
	var btnAccept=$(this);
	var token = $.cookie(g_keyToken);
	var activityId=$(this).data(g_keyId);
	var params={};
 	params[g_keyId]=activityId.toString();
	params[g_keyToken]=token.toString();	
		
	try{
		$.ajax({
			type: "PUT",
			url: "/admin/accept",
			data: params,
			success: function(data, status, xhr){
				var cell=btnAccept.parent(); 
				btnAccept.remove();
				var indicator=$('<div>',
				{
					class: g_classAcceptedIndicator,
					html: 'Accepted'
				}).appendTo(cell);
				cell.data(g_indexStatusIndicator, indicator);
			},
			error: function(xhr, status, err){
				
			}
		});
	} catch(err){

	}
}

function onBtnRejectClicked(evt){
	evt.preventDefault();
	var btnReject=$(this);
	var token = $.cookie(g_keyToken);
	var activityId=$(this).data(g_keyId);
	var params={};
 	params[g_keyId]=activityId.toString();
	params[g_keyToken]=token.toString();	

	try{
		$.ajax({
			type: "PUT",
			url: "/admin/reject", 
			data: params,
			success: function(data, status, xhr){
				var cell=btnReject.parent(); 
				btnReject.remove();

				var indicator=$('<div>',
				{
					class: g_classAcceptedIndicator,
					html: 'Rejected'
				}).appendTo(cell);
				cell.data(g_indexStatusIndicator, indicator);
			}, 
			error: function(xhr, status, err){
				
			}			
		});
	} catch(err){

	}
}

function onBtnDeleteClicked(evt){
	evt.preventDefault();
	var btnDelete=$(this);
	var token = $.cookie(g_keyToken).toString();
	var activityId=$(this).data(g_keyId);
	var params={};
 	params[g_keyId]=activityId;
	params[g_keyToken]=token;	

	try{
		$.ajax({
			type: "PUT",
			url: "/admin/delete", 
			data: params,
			success: function(data, status, xhr){
				var cell=btnDelete.parent(); // javascript dom element
				btnDelete.remove();

				var indicator=$('<div>',
				{
					class: g_classDeletedIndicator,
					html: 'Deleted'
				}).appendTo(cell);
				cell.data(g_indexStatusIndicator, indicator);
			},
			error: function(xhr, status, err){
				
			}
		});
	} catch(err){

	}
}

// Generators
function generateActivityCellForAdmin(activityJson){
	var arrayStatusName=['created','pending','rejected','accepted','expired'];

	var activityId=activityJson[g_keyId];
	var activityTitle=activityJson[g_keyTitle];
	var activityContent=activityJson[g_keyContent];
	var activityStatus=activityJson[g_keyStatus];
    
    var coverImageURL=null;
    do{
        var activityImages=activityJson[g_keyImages];
        if(activityImages==null) break;
        for(var key in activityImages){
           if(activityImages.hasOwnProperty(key)){
               var activityImage=activityImages[key];
               coverImageURL=activityImage[g_keyURL];
               break;
           }
        }
    }while(false);

	var ret=$('<div>', {
		style: "width: 100%; height: 200pt; overflow: auto;"	
	});

	var infoWrap=$('<span>', {
		style: "display: inline-block; margin-left: 5pt"	
	}).appendTo(ret);

	if(coverImageURL!=null){
		var coverImage=$('<img>', {
			class: g_classActivityCoverImage,
			src: coverImageURL
		}).appendTo(infoWrap);
	}

	var cellActivityTitle=$('<plaintext>', {	
		style: "color: black: font-size: 15pt",
		text: activityTitle
	}).appendTo(infoWrap);

	var cellActivityContent=$('<plaintext>', {
		style: "color: black; font-size: 15pt",
		text: activityContent
	}).appendTo(infoWrap);

	var statusIndicator=$('<span>', {
		style: "color: red; font-size: 15pt; margin-left: 5pt",
		text: arrayStatusName[parseInt(activityStatus)] 
	}).appendTo(ret);
	
	var buttonsWrap=$('<span>', {
		style: "margin-left: 5pt"
	}).appendTo(ret); 

	// this condition is temporarily hard-coded
	if(parseInt(activityStatus)!=3){
        var btnAccept=$('<button>', {
            class: g_classBtnAccept,
            text: 'Accept'
        }).appendTo(buttonsWrap);
        btnAccept.bind("click", onBtnAcceptClicked);
        btnAccept.data(g_keyId, activityId);
    }

	if(parseInt(activityStatus)!=2){
        var btnReject=$('<button>', {
			class: g_classBtnReject,
            text: 'Reject'
        }).appendTo(buttonsWrap);
        btnReject.bind("click", onBtnRejectClicked);
        btnReject.data(g_keyId, activityId);
    }

	var btnDelete=$('<button>', {
		class: g_classBtnDelete,
		text: 'Delete'
	}).appendTo(buttonsWrap);
	btnDelete.bind("click", onBtnDeleteClicked);
	btnDelete.data(g_keyId, activityId);
    
	ret.data(g_keyId, activityId);
	ret.data(g_keyTitle, activityTitle);
	ret.data(g_keyContent, activityContent);
	
	var hr=$('<hr>', {
		style: "height: 1pt; color: black; background-color: black"
	}).appendTo(ret);
	return ret;
}
