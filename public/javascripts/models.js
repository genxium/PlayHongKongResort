var invalid = 0;
var applied = (1<<0);
var selected = (1<<1);
var present = (1<<2);
var absent = (1<<3);
var assessed = (1<<4);
var hosted = (1<<5);

function Player(json){
	this.id = parseInt(json["id"]);
	if (json.hasOwnProperty("email"))	this.email = json["email"];
	this.name = json["name"];
	this.avatar =  (json.hasOwnProperty("avatar") ? json["avatar"] : "/assets/icons/anonymous.png");
	if (json.hasOwnProperty("unread_count"))	this.unreadCount = parseInt(json["unread_count"]);
	this.hasAvatar = function() {
		return !(!this.avatar);
	};
	if (json.hasOwnProperty("group_id")) this.groupId = parseInt(json["group_id"]);
	if (json.hasOwnProperty("authentication_status")) this.authenticationStatus = parseInt(json["authentication_status"]);

	this.isVisitor = function() {
	    return (!this.groupId || this.groupId == 0);
	};

	this.hasEmail = function() {
		return !(!this.email);
	};
	this.isEmailAuthenticated = function() {
		return (!(!this.authenticationStatus) && ((this.authenticationStatus & 1) > 0));
	};

	if (json.hasOwnProperty("age")) this.age = json["age"];
	else this.age = "";

	if (json.hasOwnProperty("gender")) this.gender = json["gender"];
	else this.gender = "";

	if (json.hasOwnProperty("mood")) this.mood = json["mood"];
	else this.mood = "";
}

function Image(json){
	this.id = parseInt(json["id"]);
	this.url = json["url"];
}

function Activity(json) {
	this.id = parseInt(json["id"]);
	this.title = json["title"];
	this.address = json["address"];
	this.content = json["content"];
	this.createdTime = parseInt(json["created_time"]);
	this.applicationDeadline = parseInt(json["application_deadline"]);
	this.isDeadlineExpired = function() {
	    var date = new Date();
	    var localNow = 1000 * moment().zone(date.getTimezoneOffset()).unix(); 
	    return localNow > this.applicationDeadline;
	};
	this.beginTime = parseInt(json["begin_time"]);
	this.hasBegun = function() {
	    var date = new Date();
	    var localNow = 1000 * moment().zone(date.getTimezoneOffset()).unix(); 
	    return localNow > this.beginTime;
	};

	if (json.hasOwnProperty("capacity")) this.capacity = parseInt(json["capacity"]);
	if (json.hasOwnProperty("num_applied")) this.numApplied = parseInt(json["num_applied"]);
	if (json.hasOwnProperty("num_selected")) this.numSelected = parseInt(json["num_selected"]);
	if (json.hasOwnProperty("status")) this.status = parseInt(json["status"]);
	this.relation = null;
	this.containsRelation = function() {
		return (this.relation != null && this.relation != undefined);	
	};
	if (json.hasOwnProperty("relation")) this.relation = parseInt(json["relation"]);
	if (json.hasOwnProperty("images")) {
		var images = new Array();
		var imagesJson = json["images"];
		for(var key in imagesJson){
			var json = imagesJson[key];
			var image = new Image(json);
			images.push(image);
		}
		this.images = images;
	}

	if (json.hasOwnProperty("applied_participants")) {
		var participants = new Array();
		var participantsJson = json["applied_participants"];
		for (var key in participantsJson){
			var participantJson = participantsJson[key];
			var participant = new Player(participantJson);
			participants.push(participant);
		}
		this.appliedParticipants = participants;
	}

	if (json.hasOwnProperty("selected_participants")) {
		var participants = new Array();
		var participantsJson = json["selected_participants"];
		for (var key in participantsJson){
			var participantJson = participantsJson[key];
			var participant = new Player(participantJson);
			participants.push(participant);
		}
		this.selectedParticipants = participants;
	}

	if (json.hasOwnProperty("present_participants")) {
		var participants = new Array();
		var participantsJson = json["present_participants"];
		for (var key in participantsJson){
			var participantJson = participantsJson[key];
			var participant = new Player(participantJson);
			participants.push(participant);
		}
		this.presentParticipants = participants;
	}

	if (json.hasOwnProperty("host")) {
		var hostJson = json["host"];
		var host = new Player(hostJson);
		this.host = host;
	}

	if (json.hasOwnProperty("viewer")) {
		var viewerJson = json["viewer"];
		var viewer = new Player(viewerJson);
		this.viewer = viewer;
	}
	
	if (json.hasOwnProperty("priority")) {
		this.priority = parseInt(json["priority"]);
	}

	if (json.hasOwnProperty("order_mask")) {
		this.orderMask = parseInt(json["order_mask"]);
	}
}

function Comment(json) {

        if (json.hasOwnProperty("id")) this.id = parseInt(json["id"]);
        if (json.hasOwnProperty("content")) this.content = json["content"];

        if (json.hasOwnProperty("activity_id")) this.activityId = parseInt(json["activity_id"]);
        if (json.hasOwnProperty("parent_id")) this.parentId = parseInt(json["parent_id"]);
        if (json.hasOwnProperty("predecessor_id")) this.predecessorId = parseInt(json["predecessor_id"]);
        if (json.hasOwnProperty("generated_time")) this.generatedTime = parseInt(json["generated_time"]);
        if (json.hasOwnProperty("num_children")) this.numChildren = parseInt(json["num_children"]);

        if (json.hasOwnProperty("from")) this.from = parseInt(json["from"]);
        if (json.hasOwnProperty("from_player")) this.fromPlayer = new Player(json["from_player"]);

        if (json.hasOwnProperty("to")) this.to = parseInt(json["to"]);
        if (json.hasOwnProperty("to_player")) this.toPlayer = new Player(json["to_player"]);
}

function Assessment(json) {

	if (json.hasOwnProperty("content")) this.content = json["content"];
	if (json.hasOwnProperty("activity_id")) this.activityId = parseInt(json["activity_id"]);

	if (json.hasOwnProperty("from")) this.from = parseInt(json["from"]);
	if (json.hasOwnProperty("to")) this.to = parseInt(json["to"]);
	if (json.hasOwnProperty("from_player")) this.fromPlayer = new Player(json["from_player"]);
	if (json.hasOwnProperty("to_player")) this.toPlayer = new Player(json["to_player"]);

}

function Notification(json) {

	if (json.hasOwnProperty("id")) this.id = parseInt(json["id"]);
	if (json.hasOwnProperty("is_read")) this.isRead = parseInt(json["is_read"]);
	if (json.hasOwnProperty("from")) this.from = parseInt(json["from"]);
	if (json.hasOwnProperty("to")) this.to = parseInt(json["to"]);
	if (json.hasOwnProperty("content")) this.content = json["content"];
	if (json.hasOwnProperty("activity_id")) this.activityId = parseInt(json["activity_id"]);
	if (json.hasOwnProperty("comment_id")) this.commentId = parseInt(json["comment_id"]);
	if (json.hasOwnProperty("assessment_id")) this.assessmentId = parseInt(json["assessment_id"]);
	if (json.hasOwnProperty("cmd")) this.cmd = parseInt(json["cmd"]);
	if (json.hasOwnProperty("relation")) this.relation = parseInt(json["relation"]);
	if (json.hasOwnProperty("status")) this.status = parseInt(json["status"]);
	if (json.hasOwnProperty("generated_time")) this.generatedTime = parseInt(json["generated_time"]);

} 
