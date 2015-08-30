package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.WebSocket;

public class Websocket {
    public static WebSocket<JsonNode> connect(final String token) {
        return new WebSocket<JsonNode>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {

                try {
                    String output = "Connection trial received.";
                    if (token != null)
                        output += (" token: " + token);
                    System.out.println(output);
                    JsonNode res = Json.newObject();
                    out.write(res);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

}
