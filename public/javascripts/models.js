function User(userJson){
	this.id=userJson["id"];
	this.email=userJson["email"];
	this.name=userJson["name"];
	this.avatar=userJson["avatar"];
}

function Image(imageJson){
	this.id=imageJson["id"];
	this.url=imageJson["url"];
}

function Activity(activityJson){
	this.id=activityJson["id"];
	this.title=activityJson["title"];
	this.content=activityJson["content"];
	this.createdTime=activityJson["created_time"];
	this.beginTime=activityJson["begin_time"];
	this.applicationDeadline=activityJson["application_deadline"];
	this.capacity=activityJson["capacity"];
	this.status=activityJson["status"];
	this.hostId=activityJson["host_id"];

	if(activityJson.hasOwnProperty("images")){
		var images=new Array();
		var imagesJson=activityJson["images"];
		for(var key in imagesJson){
			var imageJson=imagesJson[key];
			var image=new Image(imageJson);
			images.push(image);
		}
		this.images=images;
	}
}
