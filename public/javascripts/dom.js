$(document).ready(function(){
     $("#submit_btn").click(function(){
   
     		 							$.post("index.php",
                       							{
                       								r: "site/message",
                         							ajax: "message_request", 
                         							message: $("#input_box").val()
                       							},
		       			   						function(data, status, xhr){
 			 										$("#return_box").html(data);
     											}
     									);
     									
   	});								
});

$(document).ready(function(){
     $("#next_page_btn").click(function(){
     									var currentPageId = $.cookie("currentPageId");
     									if(currentPageId == null) {currentPageId = 0;}
                      					var targetPageId = parseInt(currentPageId,10)+1;
   										queryItems(parseInt(currentPageId,10)+1);
   	});								
});

$(document).ready(function(){
     $("#previous_page_btn").click(function(){
     									var currentPageId = $.cookie("currentPageId");
     									if(currentPageId == null) {currentPageId = 0;}
   										queryItems(parseInt(currentPageId,10)-1);
   	});								
});