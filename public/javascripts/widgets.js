var g_btnPreviousPage=null;
var g_btnNextPage=null;

function initWidgets(callbackPreviousPage, callbackNextPage){
	g_btnPreviousPage=$("#btn_previous_page");
	g_btnNextPage=$("#btn_next_page");
		
	g_btnPreviousPage.on("click", callbackPreviousPage);
	g_btnNextPage.on("click", callbackNextPage);
}
