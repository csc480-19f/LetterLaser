package edu.oswego.Runnables;

import javax.json.Json;
import javax.websocket.Session;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import edu.oswego.database.Database;
import edu.oswego.mail.Mailer;
import edu.oswego.model.UserFolder;

import java.io.IOException;
import java.util.List;

public class ValidationRunnable implements Runnable {

	private Mailer mailer;
	private Database database;
	private boolean validateOrPull;
	private Session session;

	public ValidationRunnable(Mailer mailer, Database database, boolean validateOrPull, Session session){
		this.mailer = mailer;
		this. database = database;
		this.validateOrPull = validateOrPull;
		this.session = session;
	}

	public boolean getValidateOrPull(){
		return validateOrPull;
	}

	@Override
	public void run() {
		if(validateOrPull){

		}else{
			JsonObject js = new JsonObject();
			js.addProperty("messagetype","statusupdate");
			js.addProperty("message","Pulling folders and emails");
			try {
				session.getBasicRemote().sendText(js.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

			List<UserFolder> folders =  database.pull();
			JsonArray ja = new JsonArray();
			for(int i=0;i<folders.size();i++){
				ja.add(folders.get(i).getFolder().getFullName());
			}
			js = new JsonObject();
			js.addProperty("messagetype","foldername");
			js.add("foldername",ja);
			try {
				session.getBasicRemote().sendText(js.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


}
