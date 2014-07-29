function Activity(activityJson){
	if(activityJson.hasOwnProperty("id")){
		this.id=activityJson["id"];
	}
	if(activityJson.hasOwnProperty("title")){
		this.title=activityJson["title"];
	}
	if(activityJson.hasOwnProperty("content")){
		this.content=activityJson["content"];
	}
	if(activityJson.hasOwnProperty("created_time")){
		this.createdTime=activityJson["created_time"];
	}
	if(activityJson.hasOwnProperty("begin_time")){
		this.beginTime=activityJson["begin_time"];
	}
	if(activityJson.hasOwnProperty("application_deadline")){
		this.applicationDeadline=activityJson["application_deadline"];
	}
	if(activityJson.hasOwnProperty("capacity")){
		this.capacity=activityJson["capacity"];
	}
	if(activityJson.hasOwnProperty("status")){
		this.status=activityJson["status"];
	}
	if(activityJson.hasOwnProperty("host_id")){
		this.hostId=activityJson["host_id"];
	}
}
