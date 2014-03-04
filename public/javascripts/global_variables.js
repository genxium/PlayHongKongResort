// constants
var g_nItemsPerPage=1;
var g_baseTwo=2;
var g_baseTen=10;
var g_rootElement = $(":root");
var g_userName;
var g_userAvatarURL;

// keyboard ids
var g_idKeyboardEnter=13;

// cookie keys
var g_keyLoginStatus="loginStatusToken";

// structured data keys (should be consistent with server-side in a protocol manner)
var g_keyUserId="UserId";
var g_keyUserEmail="UserEmail";
var g_keyUserName="UserName";
var g_keyUserToken="UserToken";
var g_keyUserAvatar="UserAvatar";

var g_keyActivityId="ActivityId";
var g_keyActivityTitle="ActivityTitle";
var g_keyActivityContent="ActivityContent";
var g_keyActivityStatus="ActivityStatus";
var g_keyActivityCreatedTime="ActivityCreatedTime";
var g_keyActivityBeginTime="ActivityBeginTime";
var g_keyActivityDeadline="ActivityApplicationDeadline";
var g_keyActivityImages="ActivityImages";
	 
var g_keyActivityAppliedParticipants="ActivityAppliedParticipants";
var g_keyActivitySelectedParticipants="ActivitySelectedParticipants";
var g_keyUserActivityRelationId="UserActivityRelationId";

var g_keyImageId="ImageId";
var g_keyImageURL="ImageURL";

// callback function pointers
var g_callbackOnActivityEditorRemoved=null;

// general keys
var g_pageIndexKey="pageIndex";
