function toggleScaling(element){
	var id="#"+element.id.toString();
	$(id).animate({'transform':'scale(1.3, 1.3)'}, 300);
	$(id).animate({'transform':'none'}, 300);
}