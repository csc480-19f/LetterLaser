package edu.oswego.Runnables;

import javax.mail.MessagingException;
import javax.websocket.Session;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.oswego.mail.Mailer;
import edu.oswego.model.UserFavourites;
import edu.oswego.model.UserFolder;

import java.io.IOException;
import java.sql.SQLException;
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
		if (validateOrPull) {
			sendUpdateStatusMessage(session,"validating emails");

			try {
				database.pull(2);
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			} catch (MessagingException e) {
				e.printStackTrace();
				return;
			}

			sendUpdateStatusMessage(session,"finished validating");



		} else {
			sendUpdateStatusMessage(session,"Pulling folders and emails");

			List<UserFolder> folders = database.pull(2);
			List<UserFavourites> favourites = database.getUserFavourites(2);


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
