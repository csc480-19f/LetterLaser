package edu.oswego.websocket;

import com.google.gson.JsonObject;

import javax.websocket.Session;
import java.io.IOException;

public class Messenger {

    public void sendPublicKey(Session session, String message){
        JsonObject js = new JsonObject();
        js.addProperty("messagetype","key");
        js.addProperty("message",message);
        sendToClient(session,js);
    }

    public void sendUpdateStatusMessage(Session session, String message){
        JsonObject js = new JsonObject();
        js.addProperty("messagetype","statusupdate");
        js.addProperty("message",message);
        sendToClient(session,js);
    }

    public void sendErrorMessage(Session session,String errorMessage){
        JsonObject js = new JsonObject();
        js.addProperty("messagetype","error");
        js.addProperty("message",errorMessage);
        sendToClient(session,js);
    }

    public void sendMessageToClient(Session session, JsonObject returnMessage){
        sendToClient(session,returnMessage);
    }

    /**
     * Method to send message over to gui
     * @param session
     * @param returnMessage
     */
    private synchronized void sendToClient(Session session, JsonObject returnMessage) {
        System.out.println("before"+returnMessage.toString());
        try {
            session.getBasicRemote().sendText(returnMessage.toString());
            System.out.println("after"+returnMessage.toString());
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
}
