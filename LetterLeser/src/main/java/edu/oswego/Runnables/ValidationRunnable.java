package edu.oswego.Runnables;

import javax.websocket.Session;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.oswego.database.Database;
import edu.oswego.mail.Mailer;
import edu.oswego.model.User;
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
		if (validateOrPull) {
			JsonObject js = new JsonObject();
			js.addProperty("messagetype", "statusupdate");
			js.addProperty("message", "validating emails");
			try {
				session.getBasicRemote().sendText(js.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

			database.pull();

			js = new JsonObject();
			js.addProperty("messagetype", "updatestatus");
			js.addProperty("message", "finished validating");
			try {
				session.getBasicRemote().sendText(js.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}



		} else {
			JsonObject js = new JsonObject();
			js.addProperty("messagetype", "statusupdate");
			js.addProperty("message", "Pulling folders and emails");
			try {
				session.getBasicRemote().sendText(js.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}


			List<UserFolder> folders = database.pull();
			List<UserFavourites> favourites = database.getUserFavourites();
			JsonArray ja = new JsonArray();
			JsonArray ja1 = new JsonArray();
			for (int i = 0; i < folders.size(); i++) {
					ja.add(folders.get(i).getFolder().getFullName());
			}
			for(int i=0;i<favourites.size();i++){
				ja.add(favourites.get(i).getName());
			}
			js = new JsonObject();
			js.addProperty("messagetype", "foldername");
			js.add("foldername", ja);
			js.add("favoritename", ja1);
			try {
				session.getBasicRemote().sendText(js.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}




	}

}
