var invalid = 0;
var applied = (1<<0);
var selected = (1<<1);
var present = (1<<2);
var absent = (1<<3);
var assessed = (1<<4);
var hosted = (1<<5);

function User(userJson){
	this.id = parseInt(userJson["id"]);
	this.email = userJson["email"];
	this.name = userJson["name"];
	this.avatar = userJson["avatar"];
}

function Image(imageJson){
	this.id = parseInt(imageJson["id"]);
	this.url = imageJson["url"];
}

function Activity(activityJson){
	this.id = parseInt(activityJson["id"]);
	this.title = activityJson["title"];
	this.content = activityJson["content"];
	this.createdTime = activityJson["created_time"];
	this.applicationDeadline = activityJson["application_deadline"];
	this.isDeadlineExpired = function() {
            var now = getCurrentYmdhisDate();
            if(compareYmdhisDate(now, this.applicationDeadline) >= 0) return true;
            else return false;
	};
	this.beginTime = activityJson["begin_time"];
	this.hasBegun = function() {
            var now = getCurrentYmdhisDate();
            if(compareYmdhisDate(now, this.beginTime) >= 0) return true;
            else return false;
	};

	this.capacity = activityJson["capacity"];
	this.status = parseInt(activityJson["status"]);
	this.relation = activityJson["relation"];
	if(activityJson.hasOwnProperty("images")){
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
        if(commentJson.hasOwnProperty("from")) this.commenterId = parseInt(commentJson["from"]);
        if(commentJson.hasOwnProperty("from_name")) this.commenterName = commentJson["from_name"];
        if(commentJson.hasOwnProperty("activity_id")) this.activityId = parseInt(commentJson["activity_id"]);
        if(commentJson.hasOwnProperty("parent_id")) this.parentId = parseInt(commentJson["parent_id"]);
        if(commentJson.hasOwnProperty("predecessor_id")) this.predecessorId = parseInt(commentJson["predecessor_id"]);
        if(commentJson.hasOwnProperty("generated_time")) this.generatedTime = commentJson["generated_time"];

        if(commentJson.hasOwnProperty("to")) this.replyeeId = parseInt(commentJson["to"]);
        if(commentJson.hasOwnProperty("to_name")) this.replyeeName = commentJson["to_name"];

}

function Assessment(assessmentJson) {
	if(assessmentJson.hasOwnProperty("content")) this.content = assessmentJson["content"];
	if(assessmentJson.hasOwnProperty("from")) this.from = parseInt(assessmentJson["from"]);
	if(assessmentJson.hasOwnProperty("to")) this.to = parseInt(assessmentJson["to"]);
	if(assessmentJson.hasOwnProperty("from_name")) this.from_name = assessmentJson["from_name"];
	if(assessmentJson.hasOwnProperty("to_name")) this.to_name = assessmentJson["to_name"];
}
