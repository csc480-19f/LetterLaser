package edu.oswego.websocket;

import com.google.gson.JsonObject;

import javax.websocket.Session;
import java.io.IOException;

public class Messenger {

	public boolean sendPublicKey(Session session, String message) {
		JsonObject js = new JsonObject();
		js.addProperty("messagetype", "key");
		js.addProperty("message", message);
		return sendToClient(session, js);
	}

	public boolean sendUpdateStatusMessage(Session session, String message) {
		JsonObject js = new JsonObject();
		js.addProperty("messagetype", "statusupdate");
		js.addProperty("message", message);
		return sendToClient(session, js);
	}

	public boolean sendErrorMessage(Session session, String errorMessage) {
		JsonObject js = new JsonObject();
		js.addProperty("messagetype", "error");
		js.addProperty("message", errorMessage);
		return sendToClient(session, js);
	}

	public boolean sendMessageToClient(Session session, JsonObject returnMessage) {
		return sendToClient(session, returnMessage);
	}

	/**
	 * Method to send message over to gui
	 * 
	 * @param session
	 * @param returnMessage
	 */
	private synchronized boolean sendToClient(Session session, JsonObject returnMessage) {
		try {
			session.getBasicRemote().sendText(returnMessage.toString());
			return true;
		} catch (IOException e) {
			System.out.println("failed to send message, connection probably closed");
			return false;
		}
	}
}
