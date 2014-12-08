var invalid = 0;
var applied = (1<<0);
var selected = (1<<1);
var present = (1<<2);
var absent = (1<<3);
var assessed = (1<<4);
var hosted = (1<<5);

function User(userJson){
	this.id = parseInt(userJson["id"]);
	if (userJson.hasOwnProperty("email"))	this.email = userJson["email"];
	this.name = userJson["name"];
	this.avatar = userJson["avatar"];
	if (userJson.hasOwnProperty("unread_count"))	this.unreadCount = parseInt(userJson["unread_count"]);
}

function Image(imageJson){
	this.id = parseInt(imageJson["id"]);
	this.url = imageJson["url"];
}

function Activity(activityJson){
	this.id = parseInt(activityJson["id"]);
	this.title = activityJson["title"];
	this.content = activityJson["content"];
	this.createdTime = parseInt(activityJson["created_time"]);
	this.applicationDeadline = parseInt(activityJson["application_deadline"]);
	this.isDeadlineExpired = function() {
	    var date = new Date();
	    var localNow = 1000 * moment().zone(date.getTimezoneOffset()).unix(); 
	    return localNow > this.applicationDeadline;
	};
	this.beginTime = parseInt(activityJson["begin_time"]);
	this.hasBegun = function() {
	    var date = new Date();
	    var localNow = 1000 * moment().zone(date.getTimezoneOffset()).unix(); 
	    return localNow > this.beginTime;
	};

	if (activityJson.hasOwnProperty("capacity")) this.capacity = parseInt(activityJson["capacity"]);
	if (activityJson.hasOwnProperty("num_applied")) this.numApplied = parseInt(activityJson["num_applied"]);
	if (activityJson.hasOwnProperty("num_selected")) this.numSelected = parseInt(activityJson["num_selected"]);
	if (activityJson.hasOwnProperty("status")) this.status = parseInt(activityJson["status"]);
	if (activityJson.hasOwnProperty("relation")) this.relation = parseInt(activityJson["relation"]);
	if (activityJson.hasOwnProperty("images")){
		var images = new Array();
		var imagesJson = activityJson["images"];
		for(var key in imagesJson){
			var imageJson = imagesJson[key];
			var image = new Image(imageJson);
			images.push(image);
		}
		this.images = images;
	}

	if(activityJson.hasOwnProperty("applied_participants")){
		var participants = new Array();
		var participantsJson = activityJson["applied_participants"];
		for(var key in participantsJson){
			var participantJson = participantsJson[key];
			var participant = new User(participantJson);
			participants.push(participant);
		}
		this.appliedParticipants = participants;
	}

        if(activityJson.hasOwnProperty("selected_participants")){
            var participants = new Array();
            var participantsJson = activityJson["selected_participants"];
            for(var key in participantsJson){
                var participantJson = participantsJson[key];
                var participant = new User(participantJson);
                participants.push(participant);
            }
            this.selectedParticipants = participants;
        }
        if(activityJson.hasOwnProperty("present_participants")){
            var participants = new Array();
            var participantsJson = activityJson["present_participants"];
            for(var key in participantsJson){
                var participantJson = participantsJson[key];
                var participant = new User(participantJson);
                participants.push(participant);
            }
            this.presentParticipants = participants;
        }
        if(activityJson.hasOwnProperty("host")){
            var hostJson = activityJson["host"];
            var host = new User(hostJson);
            this.host = host;
        }
        if(activityJson.hasOwnProperty("viewer")){
            var viewerJson = activityJson["viewer"];
            var viewer = new User(viewerJson);
            this.viewer = viewer;
        }
}

function Comment(commentJson) {

        if(commentJson.hasOwnProperty("id")) this.id = parseInt(commentJson["id"]);
        if(commentJson.hasOwnProperty("content")) this.content = commentJson["content"];
        if(commentJson.hasOwnProperty("from")) this.from = parseInt(commentJson["from"]);
        if(commentJson.hasOwnProperty("from_name")) this.fromName = commentJson["from_name"];
        if(commentJson.hasOwnProperty("activity_id")) this.activityId = parseInt(commentJson["activity_id"]);
        if(commentJson.hasOwnProperty("parent_id")) this.parentId = parseInt(commentJson["parent_id"]);
        if(commentJson.hasOwnProperty("predecessor_id")) this.predecessorId = parseInt(commentJson["predecessor_id"]);
        if(commentJson.hasOwnProperty("generated_time")) this.generatedTime = parseInt(commentJson["generated_time"]);
        if(commentJson.hasOwnProperty("num_children")) this.numChildren = parseInt(commentJson["num_children"]);

        if(commentJson.hasOwnProperty("to")) this.to = parseInt(commentJson["to"]);
        if(commentJson.hasOwnProperty("to_name")) this.toName = commentJson["to_name"];

}

function Assessment(assessmentJson) {

	if(assessmentJson.hasOwnProperty("content")) this.content = assessmentJson["content"];
	if(assessmentJson.hasOwnProperty("from")) this.from = parseInt(assessmentJson["from"]);
	if(assessmentJson.hasOwnProperty("to")) this.to = parseInt(assessmentJson["to"]);
	if(assessmentJson.hasOwnProperty("from_name")) this.from_name = assessmentJson["from_name"];
	if(assessmentJson.hasOwnProperty("to_name")) this.to_name = assessmentJson["to_name"];

}

function Notification(notificationJson) {

	if(notificationJson.hasOwnProperty("id")) this.id = parseInt(notificationJson["id"]);
	if(notificationJson.hasOwnProperty("is_read")) this.isRead = parseInt(notificationJson["is_read"]);
	if(notificationJson.hasOwnProperty("from")) this.from = parseInt(notificationJson["from"]);
	if(notificationJson.hasOwnProperty("to")) this.to = parseInt(notificationJson["to"]);
	if(notificationJson.hasOwnProperty("content")) this.content = notificationJson["content"];
	if(notificationJson.hasOwnProperty("activit_id")) this.activityId = parseInt(notificationJson["activity_id"]);
	if(notificationJson.hasOwnProperty("comment_id")) this.commentId = parseInt(notificationJson["comment_id"]);
	if(notificationJson.hasOwnProperty("assessment_id")) this.assessmentId = parseInt(notificationJson["assessment_id"]);
	if(notificationJson.hasOwnProperty("cmd")) this.cmd = parseInt(notificationJson["cmd"]);
	if(notificationJson.hasOwnProperty("relation")) this.relation = parseInt(notificationJson["relation"]);
	if(notificationJson.hasOwnProperty("status")) this.status = parseInt(notificationJson["status"]);
	if(notificationJson.hasOwnProperty("generated_time")) this.generatedTime = parseInt(notificationJson["generated_time"]);

} 
