var invalid = 0;
var applied = (1<<0);
var selected = (1<<1);
var present = (1<<2);
var absent = (1<<3);
var assessed = (1<<4);
var hosted = (1<<5);

function Player(data){
	this.id = parseInt(data["id"]);
	if (data.hasOwnProperty("email"))	this.email = data["email"];
	this.name = data["name"];
	this.avatar =  (data.hasOwnProperty("avatar") ? data["avatar"] : "/assets/icons/anonymous.png");
	if (data.hasOwnProperty("unread_count"))	this.unreadCount = parseInt(data["unread_count"]);
	this.hasAvatar = function() {
		return !(!this.avatar);
	};
	if (data.hasOwnProperty("group_id")) this.groupId = parseInt(data["group_id"]);
	if (data.hasOwnProperty("authentication_status")) this.authenticationStatus = parseInt(data["authentication_status"]);

	this.isVisitor = function() {
	    return (!this.groupId || this.groupId == 0);
	};

	this.hasEmail = function() {
		return !(!this.email);
	};
	this.isEmailAuthenticated = function() {
		return (!(!this.authenticationStatus) && ((this.authenticationStatus & 1) > 0));
	};

	if (data.hasOwnProperty("age")) this.age = data["age"];
	else this.age = "";

	if (data.hasOwnProperty("gender")) this.gender = data["gender"];
	else this.gender = "";

	if (data.hasOwnProperty("mood")) this.mood = data["mood"];
	else this.mood = "";
}

function Image(data){
	this.id = parseInt(data["id"]);
	this.url = data["url"];
}

function Activity(data) {
	this.id = parseInt(data["id"]);
	this.title = data["title"];
	this.address = data["address"];
	this.content = data["content"];
	this.createdTime = parseInt(data["created_time"]);
	this.applicationDeadline = parseInt(data["application_deadline"]);
	this.isDeadlineExpired = function() {
	    var date = new Date();
	    var localNow = 1000 * moment().zone(date.getTimezoneOffset()).unix(); 
	    return localNow > this.applicationDeadline;
	};
	this.beginTime = parseInt(data["begin_time"]);
	this.hasBegun = function() {
	    var date = new Date();
	    var localNow = 1000 * moment().zone(date.getTimezoneOffset()).unix(); 
	    return localNow > this.beginTime;
	};

	if (data.hasOwnProperty("capacity")) this.capacity = parseInt(data["capacity"]);
	if (data.hasOwnProperty("num_applied")) this.numApplied = parseInt(data["num_applied"]);
	if (data.hasOwnProperty("num_selected")) this.numSelected = parseInt(data["num_selected"]);
	if (data.hasOwnProperty("status")) this.status = parseInt(data["status"]);
	this.relation = null;
	this.containsRelation = function() {
		return (this.relation != null && this.relation != undefined);	
	};
	if (data.hasOwnProperty("relation")) this.relation = parseInt(data["relation"]);
	if (data.hasOwnProperty("images")) {
		var images = new Array();
		var imagesJson = data["images"];
		for(var key in imagesJson){
			var imageJson = imagesJson[key];
			var image = new Image(data);
			images.push(image);
		}
		this.images = images;
	}

	if (data.hasOwnProperty("applied_participants")) {
		var participants = new Array();
		var participantsJson = data["applied_participants"];
		for (var key in participantsJson){
			var participantJson = participantsJson[key];
			var participant = new Player(participantJson);
			participants.push(participant);
		}
		this.appliedParticipants = participants;
	}

	if (data.hasOwnProperty("selected_participants")) {
		var participants = new Array();
		var participantsJson = data["selected_participants"];
		for (var key in participantsJson){
			var participantJson = participantsJson[key];
			var participant = new Player(participantJson);
			participants.push(participant);
		}
		this.selectedParticipants = participants;
	}

	if (data.hasOwnProperty("present_participants")) {
		var participants = new Array();
		var participantsJson = data["present_participants"];
		for (var key in participantsJson){
			var participantJson = participantsJson[key];
			var participant = new Player(participantJson);
			participants.push(participant);
		}
		this.presentParticipants = participants;
	}

	if (data.hasOwnProperty("host")) {
		var hostJson = data["host"];
		var host = new Player(hostJson);
		this.host = host;
	}

	if (data.hasOwnProperty("viewer")) {
		var viewerJson = data["viewer"];
		var viewer = new Player(viewerJson);
		this.viewer = viewer;
	}
	
	if (data.hasOwnProperty("priority")) {
		this.priority = parseInt(data["priority"]);
	}

	if (data.hasOwnProperty("order_mask")) {
		this.orderMask = parseInt(data["order_mask"]);
	}
}

function Comment(data) {

        if (data.hasOwnProperty("id")) this.id = parseInt(data["id"]);
        if (data.hasOwnProperty("content")) this.content = data["content"];

        if (data.hasOwnProperty("activity_id")) this.activityId = parseInt(data["activity_id"]);
        if (data.hasOwnProperty("parent_id")) this.parentId = parseInt(data["parent_id"]);
        if (data.hasOwnProperty("predecessor_id")) this.predecessorId = parseInt(data["predecessor_id"]);
        if (data.hasOwnProperty("generated_time")) this.generatedTime = parseInt(data["generated_time"]);
        if (data.hasOwnProperty("num_children")) this.numChildren = parseInt(data["num_children"]);

        if (data.hasOwnProperty("from")) this.from = parseInt(data["from"]);
        if (data.hasOwnProperty("from_player")) this.fromPlayer = new Player(data["from_player"]);

        if (data.hasOwnProperty("to")) this.to = parseInt(data["to"]);
        if (data.hasOwnProperty("to_player")) this.toPlayer = new Player(data["to_player"]);
}

function Assessment(data) {

	if (data.hasOwnProperty("content")) this.content = data["content"];
	if (data.hasOwnProperty("activity_id")) this.activityId = parseInt(data["activity_id"]);

	if (data.hasOwnProperty("from")) this.from = parseInt(data["from"]);
	if (data.hasOwnProperty("to")) this.to = parseInt(data["to"]);
	if (data.hasOwnProperty("from_player")) this.fromPlayer = new Player(data["from_player"]);
	if (data.hasOwnProperty("to_player")) this.toPlayer = new Player(data["to_player"]);

}

function Notification(data) {

	if (data.hasOwnProperty("id")) this.id = parseInt(data["id"]);
	if (data.hasOwnProperty("is_read")) this.isRead = parseInt(data["is_read"]);
	if (data.hasOwnProperty("from")) this.from = parseInt(data["from"]);
	if (data.hasOwnProperty("to")) this.to = parseInt(data["to"]);
	if (data.hasOwnProperty("content")) this.content = data["content"];
	if (data.hasOwnProperty("activity_id")) this.activityId = parseInt(data["activity_id"]);
	if (data.hasOwnProperty("comment_id")) this.commentId = parseInt(data["comment_id"]);
	if (data.hasOwnProperty("assessment_id")) this.assessmentId = parseInt(data["assessment_id"]);
	if (data.hasOwnProperty("cmd")) this.cmd = parseInt(data["cmd"]);
	if (data.hasOwnProperty("relation")) this.relation = parseInt(data["relation"]);
	if (data.hasOwnProperty("status")) this.status = parseInt(data["status"]);
	if (data.hasOwnProperty("generated_time")) this.generatedTime = parseInt(data["generated_time"]);

} 
