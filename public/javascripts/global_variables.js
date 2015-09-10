// constants
var g_numItemsPerPage = 6;
var g_baseTwo = 2;
var g_baseTen = 10;
var g_rootElement = $(":root");
var g_inf = (1 << 53);

// order is used in sorting (e.g. ascend/descend)
var g_orderDescend = (-1);
var g_orderAscend = (+1);

// direction is used in paging (e.g. scroll up/down)
var g_directionForward = (+1);
var g_directionBackward = (-1);

var g_modeHomepage=0;
var g_modeProfile=1;

// admin email
var g_adminEmail = "admin@qiutongqu.com";

// keyboard ids
var g_idKeyboardEnter=13;

// cookie keys
var g_keyToken = "token";
var g_keyAccessToken = "access_token";
var g_keyParty = "party";
var g_keyPartyNickname = "party_nickname";

// extra request keys 
var g_keyId = "id";
var g_keyOrderKey = "order_key";
var g_keyRelation = "relation";
var g_keyBundle = "bundle";

// extra response keys
var g_keyRet = "ret";
var g_keyCount = "count";
var g_keyData = "data";

// structured data keys (should be consistent with server-side in a protocol manner)
var g_keyPlayer = "player";
var g_keyCode = "code";

var g_keyPlayerId = "player_id";
var g_keyVieweeId = "viewee_id";
var g_keyName = "name";
var g_keyEmail = "email";
var g_keyPassword = "password";
var g_keyAvatar = "avatar";
var g_keyPasswordResetCode = "password_reset_code";
var g_keyUnreadCount = "unread_count";
var g_keyUnassessedCount = "unassessed_count";

var g_keyAge = "age";
var g_keyGender = "gender";
var g_keyMood = "mood";

var g_keyButton = "button";

var g_keyActivities = "activities";

var g_keyActivity = "activity";
var g_keyActivityId = "activity_id";
var g_keyTitle = "title";
var g_keyAddress = "address";
var g_keyContent = "content";
var g_keyCreatedTime = "created_time";
var g_keyBeginTime = "begin_time";
var g_keyDeadline = "application_deadline";
var g_keyImages = "images";
var g_keyStatus = "status";
var g_keyHostId = "host_id";
var g_keyHostName = "host_name";
var g_keyPriority = "priority";
var g_keyOrderMask = "order_mask";
	 
var g_keyAppliedParticipants = "applied_participants";
var g_keySelectedParticipants = "selected_participants";
var g_keyPresentParticipants = "present_participants";
var g_keyAbsentParticipants = "absent_participants";

var g_keyImageId = "image_id"
var g_keyUrl = "url";

var g_keyComment = "comment";
var g_keyCommentId = "comment_id";
var g_keyParentId = "parent_id";
var g_keyPredecessorId = "predecessor_id";
var g_keyCommentType = "type";
var g_keyGeneratedTime = "generated_time";
var g_keyComments = "comments";
var g_keySubComments = "sub_comments";

var g_keyFrom = "from";
var g_keyFromName = "from_name";
var g_keyTo = "to";
var g_keyToName = "to_name";

var g_keyNotification = "notification";
var g_keyNotifications = "notifications";
var g_keyIsRead = "is_read";

// general keys
var g_keyTag = "tag";
var g_keyState = "state"; 
var g_keyCbfunc = "cbfunc";
var g_keyArgs = "args";
var g_keyParams = "params";

var g_keyPage = "page";
var g_keyPageSt = "page_st";
var g_keyPageEd = "page_ed";

var g_keyRefIndex = "ref_index";
var g_keyNumItems = "num_items";
var g_keyOrder = "order";
var g_keyOrientation = "orientation";
var g_keyDirection = "direction";

var g_keyCell = "cell";
var g_keySid = "sid";
var g_keyCaptcha = "captcha";

// constants
var g_statusCreated = 0;
var g_statusPending = 1;
var g_statusRejected = 2;
var g_statusAccepted = 3;

var g_maxApplied = 500;
var g_maxSelected = 250;

// regex patterns
var g_emailPattern = /^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i; // referred to https://jqueryui.com/resources/demos/dialog/modal-form.html
var g_passwordPattern = /^[0-9a-zA-Z_#\\!]{6,32}$/;
var g_playernamePattern = /^[0-9a-zA-Z_]{6,32}$/;

var g_playerAgePattern = /^.{0,16}$/;
var g_playerGenderPattern = /^.{0,16}$/;
var g_playerMoodPattern = /^.{0,64}$/;

/* The following patterns should follow Unicode standard */
var g_activityTitlePattern = /^.{5,64}$/;  
var g_activityAddressPattern = /^.{5,128}$/;
var g_activityContentPattern = /^[\s\S]{15,1024}$/;
var g_commentContentPattern  = /^.{5,128}$/;
var g_assessmentContentPattern = /^.{0,64}$/;

/* Error codes reference: https://www.evernote.com/shard/s50/sh/62bbc660-3794-403c-98d1-a8134c868589/ad7b98b93af61deb774e1154ff08dfd7 */
var g_errNotLoggedIn = 1001;
var g_errPlayerNotFound = 1003;
var g_errPswErr = 1004;

var g_errActivityHasBegun = 3007;
var g_errActivityAppliedLimit = 3008;
var g_errActivitySelectedLimit = 3009;
var g_errActivityCreationLimit = 3010;

var g_errCaptcha = 4001;

var g_errForeignPartyRegistrationRequired = 5001;
var g_errTempForeignPartyRecordNotFound = 5002;

/* theme selection */
var g_theme = "main";

/* foreign party code list */
var g_partyQQ = 1;

/* foreign party app id list */
var g_appIdQQ = 101239106;

/* CDN */
var g_keyRemoteName = "remote_name";
var g_keyUptoken = "uptoken";
var g_keyFileref = "fileref";

var g_cdnQiniu = 1;
var g_cdnDomain = '7xljmm.dl1.z0.glb.clouddn.com';
