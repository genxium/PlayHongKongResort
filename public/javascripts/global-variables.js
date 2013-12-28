// constants
var g_nItemsPerPage=1;
var g_baseTwo=2;
var g_baseTen=10;
var g_rootElement = $(":root");
var g_userName="";
var g_editingActivityId=(-1);

// cookie keys
var g_keyLoginStatus="loginStatusToken";

// structured data keys
var g_keyActivityId="ActivityId";
var g_keyActivityTitle="ActivityTitle";
var g_keyActivityContent="ActivityContent";

// general DOM element key
var g_classActivityEditor="classActivityEditor";
var g_classActivityEditorContainer="classActivityEditorContainer";
var g_idSectionUserInfo="idSectionUserInfo";
var g_idSectionTitle="idSectionTitle";
var g_idLoggedInUserMenu="idLoggedInUserMenu";

// input-box keys
var g_classActivityTitle="classActivityTitle";
var g_classActivityContent="classActivityContent";
var g_classFieldEmail="classFieldEmail";
var g_classFieldPassword="classFieldPassword";
var g_idFieldEmail="idFieldEmail";
var g_idFieldPassword="idFieldPassword";

// button keys
var g_idBtnLogin="idBtnLogin";
var g_idBtnRegister="idBtnRegister";
var g_idBtnCreate="idBtnCreate";
var g_classBtnSubmit="classBtnSubmit";
var g_classBtnLogout="classBtnLogout";
var g_classBtnUpdate="classBtnUpdate";
var g_classBtnCancel="classBtnCancel";

// in memory DOM elements
var g_domLoggedInUserMenu=null;