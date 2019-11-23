package edu.oswego.runnables;

import javax.websocket.Session;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.oswego.database.Database;
import edu.oswego.mail.Mailer;
import edu.oswego.model.UserFavourites;
import edu.oswego.model.UserFolder;
import edu.oswego.websocket.Messenger;
import java.util.List;

public class ValidationRunnable implements Runnable {

	private Mailer mailer;
	private Database database;
	private boolean validateOrPull;
	private Session session;
	private Messenger messenger = new Messenger();

	public ValidationRunnable(Mailer mailer, Database database, boolean validateOrPull, Session session) {
		this.mailer = mailer;
		this.database = database;
		this.validateOrPull = validateOrPull;
		this.session = session;
	}

	public void setSession(Session session){this.session = session;}

	@Override
	public void run() {
//		Thread.currentThread().setName("validation");
//		System.out.println("validation started");
//		if (validateOrPull) {
//			messenger.sendUpdateStatusMessage(session, "Validating emails.\nvalidateOrPull is true.");
//
//			try {
//
//				database.pull();
//			} catch (Throwable t) {
//				t.printStackTrace();
//				messenger.sendErrorMessage(session, "Error in DB: \n" + t.getMessage());
//				return;
//			}
//			messenger.sendUpdateStatusMessage(session, "Finished validating.\nline 47");
//
//		} else {
//			messenger.sendUpdateStatusMessage(session, "Pulling folders and emails.\nvalidateOrPull is false.");
//
//			List<UserFolder> folders;
//			List<UserFavourites> favourites;
//			try {
//				folders = database.pull();
//				favourites = database.getUserFavourites();
//			} catch (Throwable t) {
//				t.printStackTrace();
//				messenger.sendErrorMessage(session, "Error in DB: \n" + t.getMessage());
//				return;
//			}
//
//			JsonArray ja = new JsonArray();
//			JsonArray ja1 = new JsonArray();
//			for (int i = 0; i < folders.size(); i++) {
//				ja.add(folders.get(i).getFolder().getFullName());
//			}
//			for (int i = 0; i < favourites.size(); i++) {
//				ja.add(favourites.get(i).getName());
//			}
//			JsonObject js = new JsonObject();
//			js.addProperty("messagetype", "logininfo");
//			js.add("foldername", ja);
//			js.add("favoritename", ja1);
//			messenger.sendMessageToClient(session, js);
//		}
//		System.out.println("validationRunnable finished");
	}

}
