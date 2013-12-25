function generateActivityCell(jsonActivity){
	var activityId=jsonActivity['ActivityId'];
	var activityTitle=jsonActivity['ActivityTitle'];
	var activityContent=jsonActivity['ActivityContent'];
	var cellContent=activityId+" "+activityTitle+"<br/>"+activityContent+"<br/><br/>";
	var ret=$('<div></div>');
	ret.css("display: table-cell;width: 60pt;height: 30pt;border: 3pt solid #ccccff;font-family: Tahoma, sans-serif;overflow: hidden;background-color: #FF0000;padding: 30pt");
	ret.html(cellContent);
	return ret;
}