function toggleScaling(element){
	this.animate({'transform':'scale(1.3, 1.3)'}, 300);
	this.animate({'transform':'none'}, 300);
}

/*
 * Return a modal handle
 * */
function createModal(par, message){
	var container=$("<div class='modal fade' tabindex='-1' role='dialog' aria-labelledby='' aria-hidden='true'>", {
		style: "width: 80%; height: 80%"
	}).appendTo(par);	
	var dialog=$("<div class='modal-dialog modal-lg'>").appendTo(container);
	var content=$("<div class='modal-content'>", {
		text: message
	}).appendTo(dialog);
	return container;
}

function showModal(container){
	container.modal({
                show: true    
        });
}

function hideModal(container){
	container.modal("hide");
}

function removeModal(container){
	container.empty();
	container.remove();
}
