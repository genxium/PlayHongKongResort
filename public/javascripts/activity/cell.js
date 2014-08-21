/*
 * variables
 */

// general dom elements
var g_classCellActivityContainer="classCellActivityContainer";
var g_classCellActivityTitle="classCellActivityTitle";
var g_classCellActivityContent="classCellActivityContent";

var g_classActivityCoverImage="classActivityCoverImage";

var g_classActivityStatusIndicator="classActivityStatusIndicator";
var g_classAppliedIndicator="classAppliedIndicator";

// button keys
var g_classBtnJoin="classBtnJoin";
var g_classBtnDetail="classBtnDetail";

// button indexes for cascaded DOM element search
var g_indexBtnJoin="indexBtnJoin";

// Assistant Handlers
function onBtnEditClicked(evt){
    	evt.preventDefault();
    	var data = evt.data;
        var activity = data[g_keyActivity];

        g_onEditorCancelled=function(){
                g_sectionActivityEditor.modal("hide");
        };

        g_activityEditor=generateActivityEditor(activity);
        g_modalActivityEditor.empty();
        g_modalActivityEditor.append(g_activityEditor);

        g_sectionActivityEditor.css("position", "absolute");
        g_sectionActivityEditor.css("height", "90%");
        g_sectionActivityEditor.css("padding", "5pt");
        g_sectionActivityEditor.modal({
                show: true
        });
}

function onBtnJoinClicked(evt){

	var btnJoin=$(this);

	evt.preventDefault();
	var data = evt.data;
	var token = $.cookie(g_keyToken).toString();

	var params={};
	params[g_keyActivityId] = data[g_keyActivityId];
	params[g_keyToken] = token;

	try{
		$.ajax({
			type: "POST",
			url: "/activity/join",
			data: params,
			success: function(data, status, xhr){
				var cell=btnJoin.parent();
				btnJoin.remove();

				var appliedIndicator=$('<div>', {
					class: g_classAppliedIndicator,
					text: 'Applied'
				}).appendTo(cell);
			},
			error: function(xhr, status, errThrown){

			}
		});
	} catch(err){

	}
}

function onBtnDetailClicked(evt){
        evt.preventDefault();
        var data = evt.data;
        var activityId = data[g_keyActivityId];

	try{
		var detailPagePath="/activity/detail/show?"+g_keyActivityId+"="+activityId;
		window.open(detailPagePath);
    	} catch(err){
        	alert(err.toString());
	}
}

// Generators

function generateActivityCell(activityJson){

	var arrayStatusName = ['created','pending','rejected','accepted','expired'];
        var activity = new Activity(activityJson);

	var coverImageUrl = null;
	if(activity.images != null) {
            for(var key in activity.images){
               var img = activity.images[key];
               coverImageUrl = img.url;
               break;
            }
	}

	var statusStr = arrayStatusName[parseInt(activity.status)];

	var ret=$('<div>', {
		class: g_classCellActivityContainer
	});

	if(coverImageUrl!=null){
		var coverImage=$('<img>', {
			class: g_classActivityCoverImage,
			src: coverImageUrl
		}).appendTo(ret);
	}

	var cellActivityTitle=$('<div>', {	
		class: g_classCellActivityTitle,
		html: activity.title
	}).appendTo(ret);

	if(activity.relation == null){
		var btnJoin = $('<button>', {
			class: g_classBtnJoin,
			text: 'Join'
		}).appendTo(ret);
		var dJoin = {};
		dJoin[g_keyActivityId] = activity.id;
		btnJoin.on("click", dJoin, onBtnJoinClicked);

		ret.data(g_indexBtnJoin, btnJoin);
	} else if((activity.relation & applied) > 0
	            && (g_loggedInUser != null && g_loggedInUser.id != activity.host.id)) {
		
		var appliedIndicator=$('<div>', {
			class: g_classAppliedIndicator,
			text: 'Applied'
		}).appendTo(ret);
	} else;

	if(activity.status != null){

		var statusIndicator = $('<div>',{
		    class: g_classActivityStatusIndicator,
		    text: statusStr
		}).appendTo(ret);

		if(parseInt(activity.status) == g_statusCreated){
		    // this condition is temporarily hard-coded
		    var btnEdit = $('<button>', {
			class: g_classBtnEdit,
			text: 'Edit'
		    }).appendTo(ret);
		    var dEdit = {};
		    dEdit[g_keyActivity] = activity;
		    btnEdit.on("click", dEdit, onBtnEditClicked);
		}
	}

	var btnDetail=$('<button>', {
		class: g_classBtnDetail,
		text: 'Detail'
	}).appendTo(ret);
	var dDetail = {};
	dDetail[g_keyActivityId] = activity.id;
	btnDetail.on("click", dDetail, onBtnDetailClicked);
	
	return ret;
}
