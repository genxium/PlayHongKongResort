var invalid = 0;
var applied = (1<<0);
var selected = (1<<1);
var present = (1<<2);
var absent = (1<<3);
var assessed = (1<<4);
var hosted = (1<<5);

function User(userJson){
	this.id = userJson["id"];
	this.email = userJson["email"];
	this.name = userJson["name"];
	this.avatar = userJson["avatar"];
}

function Image(imageJson){
	this.id = imageJson["id"];
	this.url = imageJson["url"];
}

function Activity(activityJson){
	this.id = activityJson["id"];
	this.title = activityJson["title"];
	this.content = activityJson["content"];
	this.createdTime = activityJson["created_time"];
	this.beginTime = activityJson["begin_time"];
	this.applicationDeadline = activityJson["application_deadline"];
	this.capacity = activityJson["capacity"];
	this.status = activityJson["status"];
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

function Assessment(content, to){
	this.content = content;
	this.to = to;
}
