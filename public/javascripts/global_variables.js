// constants
var g_numItemsPerPage=6;
var g_baseTwo=2;
var g_baseTen=10;
var g_rootElement = $(":root");
var g_userName;
var g_userAvatarURL;
var g_directionForward=(+1);
var g_directionBackward=(-1);
var g_modeHomepage=0;
var g_modeProfile=1;

// keyboard ids
var g_idKeyboardEnter=13;

// cookie keys
var g_keyLoginStatus="loginStatusToken";
var g_keyToken="token";

// structured data keys (should be consistent with server-side in a protocol manner)
var g_keyUserId="UserId";
var g_keyUserEmail="UserEmail";
var g_keyUserName="UserName";
var g_keyUserToken="UserToken";
var g_keyUserAvatar="UserAvatar";

var g_keyActivityJson="ActivityJson";
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

var g_keyImageId="ImageId";
var g_keyImageURL="ImageURL";

// callback function pointers
var g_callbackOnActivityEditorRemoved=null;

// general keys
var g_keyPageIndex="pageIndex";
var g_keyStartingIndex="startingIndex";
var g_keyEndingIndex="endingIndex";

var g_keyRefIndex="refIndex";
var g_keyNumItems="numItems";
var g_keyDirection="direction";
