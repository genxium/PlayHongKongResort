var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
var g_ws = new WS("ws://128.199.168.153:8888/el/echo");
g_ws.onopen = function() {
	alert("Connection is open!");
};

g_ws.onmessage = function(evt) {
	var msg = evt.data;
	alert("Message received: " + msg);
};

g_ws.onclose = function() {
	alert("Connection is closed!");	
};
