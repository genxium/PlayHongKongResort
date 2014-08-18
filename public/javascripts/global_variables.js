// constants
var g_numItemsPerPage = 6;
var g_baseTwo = 2;
var g_baseTen = 10;
var g_rootElement = $(":root");
var g_username = null;
var g_userAvatarURL = "";

// order is used in sorting (e.g. ascend/descend)
var g_orderAscend = (+1);
var g_orderDescend = (-1);

// direction is used in paging (e.g. scroll up/down)
var g_directionForward = (+1);
var g_directionBackward = (-1);

var g_modeHomepage=0;
var g_modeProfile=1;

// keyboard ids
var g_idKeyboardEnter=13;

// cookie keys
var g_keyToken="token";
var g_keyUid="uid";

// extra request keys 
var g_keyId="id";
var g_keyRelation="relation";
var g_keyBundle="bundle";

// structured data keys (should be consistent with server-side in a protocol manner)
var g_keyUser="user";
var g_keyUserId="user_id";
var g_keyName="name";
var g_keyEmail="email";
var g_keyPassword="password";
var g_keyAvatar="avatar";

var g_keyActivity="activity";
var g_keyActivityId="activity_id";
var g_keyTitle="title";
var g_keyContent="content";
var g_keyStatus="status";
var g_keyCreatedTime="created_time";
var g_keyBeginTime="begin_time";
var g_keyDeadline="application_deadline";
var g_keyImages="images";
var g_keyStatus="status";
var g_keyHostId="host_id";
var g_keyHostName="host_name";
	 
var g_keyAppliedParticipants="applied_participants";
var g_keySelectedParticipants="selected_participants";
var g_keyPresentParticipants="present_participants";

var g_keyUrl="url";

var g_keyComment="comment";
var g_keyCommenterId="commenter_id";
var g_keyCommenterName="commenter_name";
var g_keyParentId="parent_id";
var g_keyPredecessorId="predecessor_id";
var g_keyCommentType="type";
var g_keyGeneratedTime="generated_time";
var g_keyReplyeeId="replyee_id";
var g_keyReplyeeName="replyee_name";
var g_keySubComments="sub_comments";

// general keys
var g_keyPageIndex = "page_index";
var g_keyStartingIndex = "starting_index";
var g_keyEndingIndex = "ending_index";

var g_keyRefIndex = "ref_index";
var g_keyNumItems = "num_items";
var g_keyOrder = "order";
var g_keyDirection = "direction";

// constants
var g_statusCreated = 0;
var g_statusPending = 1;
var g_statusRejected = 2;
var g_statusAccepted = 3;
