function generateActivityCell(jsonActivity){
	var activityTitle=jsonActivity['ActivityTitle'];
	var activityContent=jsonActivity['ActivityContent'];
	var ret=$('<div></div>').css("
									display: table-cell;
									width: 60pt;
									height: 30pt;
									border: 3pt solid #ccccff;
									font-family: Tahoma, sans-serif;
									overflow: hidden;
									background-color: #FF0000;
									padding: 30pt;
								");
	ret.html(activityTitle);
	return ret;
}