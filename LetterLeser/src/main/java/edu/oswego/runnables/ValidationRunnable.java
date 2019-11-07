package edu.oswego.runnables;


import javax.websocket.Session;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.oswego.database.Database;
import edu.oswego.mail.Mailer;
import edu.oswego.model.UserFavourites;
import edu.oswego.model.UserFolder;

import java.io.IOException;
import java.util.List;

public class ValidationRunnable implements Runnable {

	private Mailer mailer;
	private Database database;
	private boolean validateOrPull;
	private Session session;

	public ValidationRunnable(Mailer mailer, Database database, boolean validateOrPull, Session session) {
		this.mailer = mailer;
		this.database = database;
		this.validateOrPull = validateOrPull;
		this.session = session;
	}
	@Override
	public void run() {
		Thread.currentThread().setName("validation");
		if (validateOrPull) {
			sendUpdateStatusMessage(session,"validating emails");

			try {
				database.pull();
				database.closeConnection();
			}catch(Throwable t){
				sendErrorMessage(session,"error in db: "+t.getMessage());
				return;
			}
			sendUpdateStatusMessage(session,"finished validating");



		} else {
			sendUpdateStatusMessage(session,"Pulling folders and emails");


			List<UserFolder> folders;
			List<UserFavourites> favourites;
			try {
				folders = database.pull();
				database.closeConnection();
				favourites = database.getUserFavourites();
				database.closeConnection();
			}catch(Throwable t){
				sendErrorMessage(session,"error in db: "+t.getMessage());
				return;
			}

			JsonArray ja = new JsonArray();
			JsonArray ja1 = new JsonArray();
			for (int i = 0; i < folders.size(); i++) {
					ja.add(folders.get(i).getFolder().getFullName());
			}
			for(int i=0;i<favourites.size();i++){
				ja.add(favourites.get(i).getName());
			}
			JsonObject js = new JsonObject();
			js.addProperty("messagetype", "foldername");
			js.add("foldername", ja);
			js.add("favoritename", ja1);
			sendMessageToClient(session,js);
		}




	}

	private void sendErrorMessage(Session session,String errorMessage){
		JsonObject js = new JsonObject();
		js.addProperty("messagetype","error");
		js.addProperty("message",errorMessage);
		sendMessageToClient(session,js);
	}

	private void sendUpdateStatusMessage(Session session,String message){
		JsonObject js = new JsonObject();
		js.addProperty("messagetype","statusupdate");
		js.addProperty("message",message);
		sendMessageToClient(session,js);
	}

	/**
	 * Method to send message over to gui
	 * @param session
	 * @param returnMessage
	 */
	private void sendMessageToClient(Session session, JsonObject returnMessage) {
		try {
			session.getBasicRemote().sendText(returnMessage.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
