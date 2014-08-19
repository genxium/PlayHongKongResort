var g_keyStatusIndicator = "status-indicator";

// Assistant Handlers
function onBtnAcceptClicked(evt){

	var btnAccept = $(this);

	evt.preventDefault();
	var data = evt.data;
	var token = $.cookie(g_keyToken);
	var params = {};
 	params[g_keyActivityId] = data[g_keyActivityId];
	params[g_keyToken] = token;

	try{
		$.ajax({
			type: "PUT",
			url: "/admin/accept",
			data: params,
			success: function(data, status, xhr){
				var cell=btnAccept.parent(); 
				btnAccept.remove();
				var indicator = cell.data(g_keyStatusIndicator);
				indicator.text("Accepted");
			},
			error: function(xhr, status, err){
				
			}
		});
	} catch(err){

	}
}

function onBtnRejectClicked(evt){
	var btnReject = $(this);

	evt.preventDefault();
	var data = evt.data;
	var token = $.cookie(g_keyToken);
	var params = {};
 	params[g_keyActivityId] = data[g_keyActivityId];
	params[g_keyToken] = token;

	try{
		$.ajax({
			type: "PUT",
			url: "/admin/reject", 
			data: params,
			success: function(data, status, xhr){
				var cell=btnReject.parent(); 
				btnReject.remove();
				var indicator = cell.data(g_keyStatusIndicator);
				indicator.text("Rejected");
			},
			error: function(xhr, status, err){
				
			}			
		});
	} catch(err){

	}
}

function onBtnDeleteClicked(evt){

        var btnDelete = $(this);

        evt.preventDefault();
        var data = evt.data;
        var token = $.cookie(g_keyToken);
        var params = {};
        params[g_keyActivityId] = data[g_keyActivityId];
        params[g_keyToken] = token;

	try{
		$.ajax({
			type: "PUT",
			url: "/admin/delete", 
			data: params,
			success: function(data, status, xhr){
				var cell=btnDelete.parent(); // javascript dom element
				btnDelete.remove();
				var indicator = cell.data(g_keyStatusIndicator);
				indicator.text("Deleted");
			},
			error: function(xhr, status, err){
				
			}
		});
	} catch(err){

	}
}

// Generators
function generateActivityCellForAdmin(activityJson){

	var arrayStatusName = ['created','pending','rejected','accepted','expired'];
        var activity = new Activity(activityJson);

        var coverImageUrl=null;

        if(activity.images !=null) {
            for(var key in activity.images){
               var image = activity.images[key];
               coverImageUrl = image.url;
               break;
            }
        }

	var ret=$('<div>', {
		style: "width: 100%; height: 200pt; overflow: auto;"	
	});

	var infoWrap=$('<span>', {
		style: "display: inline-block; margin-left: 5pt"	
	}).appendTo(ret);

	if(coverImageUrl != null){
		var coverImage=$('<img>', {
			class: g_classActivityCoverImage,
			src: coverImageUrl
		}).appendTo(infoWrap);
	}

	var cellActivityTitle = $('<plaintext>', {
		style: "color: black: font-size: 15pt",
		text: activity.title
	}).appendTo(infoWrap);

	var cellActivityContent=$('<plaintext>', {
		style: "color: black; font-size: 15pt",
		text: activity.content
	}).appendTo(infoWrap);

	var statusIndicator=$('<span>', {
		style: "color: red; font-size: 15pt; margin-left: 5pt",
		text: arrayStatusName[parseInt(activity.status)]
	}).appendTo(ret);

	ret.data(g_keyStatusIndicator, statusIndicator);
	
	var buttonsWrap=$('<span>', {
		style: "margin-left: 5pt"
	}).appendTo(ret); 

	// this condition is temporarily hard-coded
	if(parseInt(activityStatus) != g_statusAccepted){
            var btnAccept=$('<button>', {
                class: g_classBtnAccept,
                text: 'Accept'
            }).appendTo(buttonsWrap);
            var dAccept = {};
            dAccept[g_keyActivityId] = activity.id;
            btnAccept.on("click", dAccept, onBtnAcceptClicked);
        }

	if(parseInt(activity.status) != g_statusRejected){
            var btnReject=$('<button>', {
                            class: g_classBtnReject,
                text: 'Reject'
            }).appendTo(buttonsWrap);
            var dReject = {};
            dReject[g_keyActivityId] = activity.id;
            btnReject.bind("click", dReject, onBtnRejectClicked);
        }

	var btnDelete=$('<button>', {
		class: g_classBtnDelete,
		text: 'Delete'
	}).appendTo(buttonsWrap);
	var dDelete = {};
	dDelete[g_keyActivityId] = activity.id;
	btnDelete.bind("click", dDelete, onBtnDeleteClicked);
	
	var hr=$('<hr>', {
		style: "height: 1pt; color: black; background-color: black"
	}).appendTo(ret);
	return ret;
}
