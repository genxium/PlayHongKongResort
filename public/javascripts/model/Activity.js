var g_keyActivityId="ActivityId";
var g_keyActivityTitle="ActivityTitle";
var g_keyActivityContent="ActivityContent";
var g_keyActivityStatus="ActivityStatus";
var g_keyActivityCreatedTime="ActivityCreatedTime";
var g_keyActivityBeginTime="ActivityBeginTime";
var g_keyActivityDeadline="ActivityApplictionDeadline";

var g_keyActivityImages="ActivityImages";
var g_keyActivityAppliedParticipants="ActivityAppliedParticipants";
var g_keyActivitySelectedParticipants="ActivitySelectedParticipants";
var g_keyUserActivityRelationId="UserActivityRelationId";

function Activity(activityJson){
    this.id=activityJson[g_keyActivityId],
    this.title=activityJson[g_keyActivityTitle],
    this.content=activityJson[g_keyActivityContent];
    this.status=activityJson[g_keyActivityStatus];
    this.createdTime=activityJson[g_keyActivityCreatedTime];
    this.beginTime=activityJson[g_keyActivityBeginTime];
    this.deadline=activityJson[g_keyActivityDeadline];
}