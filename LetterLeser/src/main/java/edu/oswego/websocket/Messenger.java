package edu.oswego.websocket;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.oswego.model.UserFolder;

import javax.websocket.Session;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Messenger {
    ReentrantLock lock = new ReentrantLock();

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

	public boolean sendLoginInfo(Session session, List<UserFolder> list){
		JsonObject jsonObject = new JsonObject();
		JsonArray ja = new JsonArray();

		for (UserFolder uf:list) {
			ja.add(uf.getFolder().getFullName());
		}

		jsonObject.addProperty("messagetype","logininfo");
		jsonObject.add("foldername",ja);
		jsonObject.add("favoritename",new JsonArray());

		return sendToClient(session,jsonObject);
	}

	public boolean sendGraphData(Session session,int sentiment,JsonArray domain,JsonArray folder,JsonArray numOfMail,JsonObject sendNRec,JsonObject timeBetween){
		JsonObject js = new JsonObject();
		js.addProperty("messagetype", "graphs");
		JsonObject graph = new JsonObject();
		graph.addProperty("sentimentscore", sentiment);
		graph.add("emailbydomain", domain);
		graph.add("emailbyfolder", folder);
		graph.add("emailssentandrecieved", sendNRec);
		graph.add("numberofemails", numOfMail);
		graph.add("timebetweenreplies", timeBetween);
		js.add("graphs", graph);

		return sendToClient(session,js);

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
		    lock.lock();
			session.getBasicRemote().sendText(returnMessage.toString());
			return true;
		} catch (IOException e) {
			System.out.println("failed to send message, connection probably closed");
			return false;
		}finally {
		    lock.unlock();
        }
	}
}
