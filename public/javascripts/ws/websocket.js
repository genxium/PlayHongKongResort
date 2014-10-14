var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
var g_ws = null;

function wsConnect() {
	if (g_ws != null) return;
	var token = $.cookie(g_keyToken);
	if (token == null)	return;
	var host = window.location.host;
	g_ws = new WS("ws://" + host + "/el/ws?" + g_keyToken + "=" + token);
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
}

function wsDisconnect() {
	if (g_ws == null) return;
	g_ws.close();
	g_ws = null;
}
