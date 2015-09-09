var invalid = 0;
var applied = (1<<0);
var selected = (1<<1);
var present = (1<<2);
var absent = (1<<3);
var assessed = (1<<4);
var hosted = (1<<5);

function Player(data){
	this.id = parseInt(data.id);
	this.email = data.email;
	this.name = data.name;
	this.avatar =  (data.hasOwnProperty("avatar") ? data.avatar : "/assets/icons/anonymous.png");
	this.unreadCount = parseInt(data.unread_count);
	this.hasAvatar = function() {
		return !(!this.avatar);
	};
	this.groupId = parseInt(data.group_id);
	this.authenticationStatus = parseInt(data.authentication_status);

	this.isVisitor = function() {
	    return (!this.groupId || this.groupId === 0);
	};

	this.hasEmail = function() {
		return !(!this.email);
	};
	this.isEmailAuthenticated = function() {
		return (!(!this.authenticationStatus) && ((this.authenticationStatus & 1) > 0));
	};

	this.age = data.age;
	this.gender = data.gender;
	this.mood = data.mood;
}

function Image(data){
	this.id = parseInt(data.id);
	this.url = data.url;
}

function Activity(data) {
	this.id = parseInt(data.id);
	this.title = data.title;
	this.address = data.address;
	this.content = data.content;
	this.createdTime = parseInt(data.created_time);
	this.applicationDeadline = parseInt(data.application_deadline);
	this.isDeadlineExpired = function() {
	    var date = new Date();
	    var localNow = 1000 * moment().zone(date.getTimezoneOffset()).unix(); 
	    return localNow > this.applicationDeadline;
	};
	this.beginTime = parseInt(data.begin_time);
	this.hasBegun = function() {
	    var date = new Date();
	    var localNow = 1000 * moment().zone(date.getTimezoneOffset()).unix(); 
	    return localNow > this.beginTime;
	};

	this.capacity = parseInt(data.capacity);
	this.numApplied = parseInt(data.num_applied);
	this.numSelected = parseInt(data.num_selected);
	this.status = parseInt(data.status);
	this.relation = null;
	this.containsRelation = function() {
		return !(!this.relation);	
	};
	this.relation = parseInt(data.relation);
	if (data.hasOwnProperty("images")) {
		var images = [];
		var imagesData = data.images;
		for(var key in imagesData){
			var imageData = imagesData[key];
			var image = new Image(imageData);
			images.push(image);
		}
		this.images = images;
	}

	if (data.hasOwnProperty("applied_participants")) {
		var participants = new Array();
		var participantsData = data.applied_participants;
		for (var key in participantsData){
			var participantData = participantsData[key];
			var participant = new Player(participantData);
			participants.push(participant);
		}
		this.appliedParticipants = participants;
	}

	if (data.hasOwnProperty("selected_participants")) {
		var participants = [];
		var participantsData = data.selected_participants;
		for (var key in participantsData){
			var participantData = participantsData[key];
			var participant = new Player(participantData);
			participants.push(participant);
		}
		this.selectedParticipants = participants;
	}

	if (data.hasOwnProperty("present_participants")) {
		var participants = [];
		var participantsData = data.present_participants;
		for (var key in participantsData){
			var participantData = participantsData[key];
			var participant = new Player(participantData);
			participants.push(participant);
		}
		this.presentParticipants = participants;
	}

	var hostData = data.host;
	var host = new Player(hostData);
	this.host = host;

	if (data.hasOwnProperty("viewer")) {
		var viewerData = data.viewer;
		var viewer = new Player(viewerData);
		this.viewer = viewer;
	}
	
	if (data.hasOwnProperty("priority")) {
		this.priority = parseInt(data.priority);
	}

	if (data.hasOwnProperty("order_mask")) {
		this.orderMask = parseInt(data.order_mask);
	}
}

function Comment(data) {

        if (data.hasOwnProperty("id")) this.id = parseInt(data.id);
        if (data.hasOwnProperty("content")) this.content = data.content;

        if (data.hasOwnProperty("activity_id")) this.activityId = parseInt(data.activity_id);
        if (data.hasOwnProperty("parent_id")) this.parentId = parseInt(data.parent_id);
        if (data.hasOwnProperty("predecessor_id")) this.predecessorId = parseInt(data.predecessor_id);
        if (data.hasOwnProperty("generated_time")) this.generatedTime = parseInt(data.generated_time);
        if (data.hasOwnProperty("num_children")) this.numChildren = parseInt(data.num_children);

        if (data.hasOwnProperty("from")) this.from = parseInt(data.from);
        if (data.hasOwnProperty("from_player")) this.fromPlayer = new Player(data.from_player);

        if (data.hasOwnProperty("to")) this.to = parseInt(data.to);
        if (data.hasOwnProperty("to_player")) this.toPlayer = new Player(data.to_player);
}

function Assessment(data) {

	this.content = data.content;
	this.activityId = parseInt(data.activity_id);

	this.from = parseInt(data.from);
	this.to = parseInt(data.to);
	this.fromPlayer = new Player(data.from_player);
	this.toPlayer = new Player(data.to_player);

}

function Notification(data) {

	this.id = parseInt(data.id);
	this.isRead = parseInt(data.is_read);
	this.from = parseInt(data.from);
	this.to = parseInt(data.to);
	this.content = data.content;
	this.activityId = parseInt(data.activity_id);
	this.commentId = parseInt(data.comment_id);
	this.assessmentId = parseInt(data.assessment_id);
	this.cmd = parseInt(data.cmd);
	this.relation = parseInt(data.relation);
	this.status = parseInt(data.status);
	this.generatedTime = parseInt(data.generated_time);

} 
