/*
 * variables
 */

// Assistant Handlers

function onBtnDetailClicked(evt){
        evt.preventDefault();
        var data = evt.data;
        var activityId = data[g_keyActivityId];

	var detailPagePath = "/activity/detail/show?" + g_keyActivityId + "=" + activityId;
	window.open(detailPagePath);
}

// Generators

function generateActivityCell(par, activity){

	var coverImageUrl = null;
	if(activity.images != null) {
            for(var key in activity.images){
               var img = activity.images[key];
               coverImageUrl = img.url;
               break;
            }
	}

	var ret = $("<p>", {
		class: "cell-container"
	}).appendTo(par);

	var left = $("<span>", {
		style: "display: inline-block; width: 25%; height: 90%;"
	}).appendTo(ret);
	if(coverImageUrl != null){
		setBackgroundImage(left, coverImageUrl, "contain", "no-repeat", "center");
	}

	var middle = $("<span>", {
		style: "display: inline-block; margin-left: 10px; width: 40%; height: 90%;"
	}).appendTo(ret);
	var midTop = $("<div>", {
		style: "margin-bottom: 5pt"
	}).appendTo(middle);
	var title = $("<span>", {
		style: "color: blue; font-size: 15pt;",
		text: activity.title
	}).appendTo(midTop);
	attachRelationIndicator(midTop, activity);
	displayTimesTable(middle, activity);
	var midBottom = $("<div>", {
		style: "margin-top: 5pt"
	}).appendTo(middle);
	displayParticipantStatistics(midBottom, activity);

	var right = $("<span>", {
		style: "display: inline-block; margin-left: 10px; width: 25%; height: 90%;"
	}).appendTo(ret);

	var rtTop = $("<div>").appendTo(right);
	var btnDetail = $('<button>', {
		class: "btn-detail",
		text: 'Go >'
	}).appendTo(rtTop);
	var dDetail = {};
	dDetail[g_keyActivityId] = activity.id;
	btnDetail.click(dDetail, onBtnDetailClicked);
	attachStatusIndicator(rtTop, activity);

}
