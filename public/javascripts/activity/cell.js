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
		setBackgroundImageDefault(left, coverImageUrl);
	}

	var middle = $("<span>", {
		style: "display: inline-block; margin-left: 10px; width: 40%; height: 90%;"
	}).appendTo(ret);
	var midTop = $("<div>", {
		style: "margin-bottom: 5pt"
	}).appendTo(middle);
	var title = $("<span>", {
		style: "color: blue; font-size: 15pt; margin-right: 5pt;",
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
	btnDetail.click(activity, function(evt){
		var act = evt.data;
		requestActivityDetail(act.id);
	});
	attachStatusIndicator(rtTop, activity);
}
