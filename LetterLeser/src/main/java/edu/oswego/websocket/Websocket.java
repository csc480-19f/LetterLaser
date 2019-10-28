package edu.oswego.websocket;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import javax.json.Json;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.xml.crypto.Data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.oswego.Runnables.Handler;
import edu.oswego.Runnables.ValidationRunnable;
import edu.oswego.database.Database;
import edu.oswego.debug.DebugLogger;
import edu.oswego.mail.Mailer;
import edu.oswego.model.UserFavourites;
import edu.oswego.model.UserFolder;
import edu.oswego.props.MessageType;

@ServerEndpoint("/engine")
public class Websocket {
	// this is to manage all current/last active threads for each unique sessions
	private static ConcurrentHashMap<String, StorageObject> sessionMapper = new ConcurrentHashMap<>();

	@OnOpen
	public void onOpen(Session session) {
		DebugLogger.logEvent(Websocket.class.getName(),Level.INFO,"session "+session.getId()+" opened Connection");
		//System.out.println("Session " + session.getId() + " been established");
	}

	// This method allows you to message a specific user.
	/*
	 * session.getBasicRemote().sendText(message);
	 */
	@OnMessage // method that communicates with clients
	public void onMessage(String message, Session session) {

		JsonObject jsonMessage = new JsonParser().parse(message).getAsJsonObject();

		if (jsonMessage == null) {
			{//debug stuff
				DebugLogger.logEvent(Websocket.class.getName(),Level.SEVERE, "session " + session.getId() + "jsonMessage was null\n" +
								"message was :\n" + message);
			}
			try {
				session.getBasicRemote().sendText("invalid_jsonObject: you've been disconnected");
				session.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String messageType = jsonMessage.get("messagetype").getAsString();
		String email = jsonMessage.get("email").getAsString();
		StorageObject storageObject = sessionMapper.get(email);
		if (messageType.equals("filter")) {



		} else if (messageType.equals("login")) {
			{//debug stuff
				DebugLogger.logEvent(Websocket.class.getName(),Level.INFO, "session " + session.getId() + " refreshing data");
			}
			String pass = jsonMessage.get("pass").getAsString();

			Mailer mailer;
			Database database;

			if(storageObject==null){
				storageObject = new StorageObject();
				mailer = new Mailer(email,pass);

				JsonObject js = new JsonObject();
				js.addProperty("messagetype","statusupdate");
				js.addProperty("message","establising connection");
				sendMessageToClient(session,js.toString());

				boolean connectedToDatabase = mailer.isConnected();
				if(!connectedToDatabase){
					js = new JsonObject();
					js.addProperty("messagetype","statusupdate");
					js.addProperty("message","invalid credentials");
					sendMessageToClient(session,js.toString());
					return;
				}
				js = new JsonObject();
				js.addProperty("messagetype","statusupdate");
				js.addProperty("message","established connection");
				sendMessageToClient(session,js.toString());

				database = new Database(email,mailer);
				sessionMapper.put(email,storageObject);
			}else{
				mailer = storageObject.getMailer();
				database = storageObject.getDatabase();
			}




			JsonObject js = new JsonObject();
			if(database.hasEmails()){
				List<UserFolder> folders = database.importFolders();
				List<UserFavourites> favourites = database.getUserFavourites();
				JsonArray ja1 = new JsonArray();
				JsonArray ja2 = new JsonArray();
				for(int i=0;i<folders.size();i++){
					ja1.add(folders.get(i).getFolder().getFullName());
				}
				for(int i=0;i<favourites.size();i++){
					ja2.add(favourites.get(i).getName());
				}
				js.addProperty("messagetype","logininfo");
				js.add("foldername",ja1);
				js.add("favoritename",ja2);
				sendMessageToClient(session,js.toString());
			}else{
				js = new JsonObject();
				js.addProperty("messagetype","statusupdate");
				js.addProperty("message","nothing found in database, preforming fresh import");
				sendMessageToClient(session,js.toString());
				if(storageObject.getValidationRunnable()==null) {
					ValidationRunnable vr = new ValidationRunnable(mailer, database, false, session);
					Thread thread = new Thread(vr);
					thread.start();
					storageObject.setValidationRunnable(vr);
					storageObject.setValidationThread(thread);
					sessionMapper.put(email,storageObject);
				}else{
					js = new JsonObject();
					js.addProperty("messagetype","statusupdate");
					js.addProperty("message","Validating Database already in progress");
					sendMessageToClient(session,js.toString());
				}
			}

		} else if(messageType.equals("refresh")){
			databaseValidation(session);
		} else if(messageType.equals("addfavorite")){
			Database database = storageObject.getDatabase();
			String favoriteName = jsonMessage.get("favoritename").getAsString();
			JsonObject filter = jsonMessage.get("filter").getAsJsonObject();
			String foldername = filter.get("foldername").getAsString();
			String startDate = filter.get("date").getAsString();


			boolean attachment = filter.get("attachment").getAsBoolean();
			boolean seen = filter.get("seen").getAsBoolean();
			database.insertUserFavourites();
		} else if(messageType.equals("callfavorite")){
			Database database = storageObject.getDatabase();

		} else if(messageType.equals("removefavorite")){
			Database database = storageObject.getDatabase();
			database.removeUserFavourite(jsonMessage.get("favoritename").getAsString());
			JsonObject js = new JsonObject();
			js.addProperty("messagetype","statusupdate");
			js.addProperty("message","Favorite has been removed");
			sendMessageToClient(session,js.toString());
		} else if(messageType.equals("logout")){

		} else{
			JsonObject js = new JsonObject();
			js.addProperty("messagetype","statusupdate");
			js.addProperty("message","invalid messagetype\n" +
					"please send one of these options:\n" +
					"login, filter, refresh, addfavorite, callfavorite, removefavorite or logout");
			sendMessageToClient(session,js.toString());
		}

	}

	@OnClose // method to disconnect from a session : this also interrupts everything and stops everything
	public void onClose(Session session) {
		/*System.out.println("onClose");
		sessionThreadMapper.get(session.getId()).getHanderThread().interrupt();
		sessionThreadMapper.remove(session.getId());*/
		DebugLogger.logEvent(Websocket.class.getName(),Level.INFO,"session "+session.getId()+" closed Connection");
	}

	@OnError
	public void onError(Throwable t, Session session) {
		//System.out.println("onError::");
		DebugLogger.logEvent(Websocket.class.getName(),Level.WARNING,"session "+session.getId()+" threw error: "+t.getMessage());
	}

	/*
	 * all private functional methods are below
	 */

	private void databaseValidation(Session session) {


	}

	private void newRequest(Session session, JsonObject message, StorageObject storageObject) {


	}

	private void sendMessageToClient(Session session, String returnMessage){
		try{
			session.getBasicRemote().sendText(returnMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



}
