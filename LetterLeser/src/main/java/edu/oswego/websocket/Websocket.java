package edu.oswego.websocket;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.MessagingException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.oswego.Runnables.Handler;
import edu.oswego.Runnables.ValidationRunnable;
import edu.oswego.database.Database;
import edu.oswego.mail.Mailer;
import edu.oswego.model.UserFavourites;
import edu.oswego.model.UserFolder;
import edu.oswego.props.Interval;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Class handles all communication between gui and engine
 */
@ServerEndpoint("/engine")
public class Websocket {
	// this is to manage all current/last active threads for each unique sessions
	private static ConcurrentHashMap<String, StorageObject> sessionMapper = new ConcurrentHashMap<>();

	/**
	 * standard inclusive method that comes with websockets
	 * @param session
	 */
	@OnOpen
	public void onOpen(Session session) {
		System.out.println("Session " + session.getId() + " been established");
	}

	// This method allows you to message a specific user.
	/*
	 * session.getBasicRemote().sendText(message);
	 */
	@OnMessage // method that communicates with clients
	public void onMessage(String message, Session session) {
		Thread.currentThread().setName("regular");
		JsonObject jsonMessage = new JsonParser().parse(message).getAsJsonObject();

		if (jsonMessage == null) {
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
			filter(session,storageObject.getDatabase(),email,jsonMessage.get("filter").getAsJsonObject());
		} else if (messageType.equals("login")) {
			login(session,email,jsonMessage.get("pass").getAsString(),storageObject);
		} else if (messageType.equals("refresh")) {
			refresh(storageObject,email,storageObject.getMailer(),storageObject.getDatabase(),true,session);
		} else if (messageType.equals("addfavorite")) {
			addFavorite(session,storageObject.getDatabase(),jsonMessage.get("favoritename").getAsString(),jsonMessage.get("filter").getAsJsonObject());
		} else if (messageType.equals("callfavorite")) {
			callFavorite(session,storageObject.getDatabase(),jsonMessage.get("favoritename").getAsString(),email);
		} else if (messageType.equals("removefavorite")) {
			removeFavorite(session,storageObject.getDatabase(),jsonMessage.get("favoritename").getAsString());
		} else if (messageType.equals("logout")) {
			logout(session,email);
		} else {
			sendUpdateStatusMessage(session,"invalid messagetype\n" + "please send one of these options:\n"
					+ "login, filter, refresh, addfavorite, callfavorite, removefavorite or logout");

		}

	}

	/**
	 * Standard method that comes with websockets to close a connection
	 * @param session
	 */
	@OnClose
	public void onClose(Session session) {
		System.out.println("session closed");
	}

	/**
	 * standard method that comes with websockets to handle errors
	 * @param t
	 * @param session
	 */
	@OnError
	public void onError(Throwable t, Session session) {
		 System.out.println("onError::");
	}

	/*
	 * all private functional methods are below
	 * directly below are key methods for message digestion.
	 */

	private void login(Session session, String email, String pass,StorageObject storageObject){
		//String pass = jsonMessage.get("pass").getAsString();

		Mailer mailer;
		Database database;

		if (storageObject == null) {
			storageObject = new StorageObject();
			mailer = new Mailer(email, pass);

			sendUpdateStatusMessage(session,"establising connection");

			boolean connectedToDatabase = mailer.isConnected();
			if (!connectedToDatabase) {
				sendUpdateStatusMessage(session,"failed to connect to email");
				return;
			}

			try{
				database = new Database(email, mailer);
			} catch (SQLException e) {
				e.printStackTrace();
				sendErrorMessage(session,"failed to connect to db:\n "+e.getMessage());
				return;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				sendErrorMessage(session,"failed to connect to db:\n "+e.getMessage());
				return;
			}
			sendUpdateStatusMessage(session,"established connection");

			storageObject.setDatabase(database);
			storageObject.setMailer(mailer);
			sessionMapper.put(email, storageObject);
		} else {
			mailer = storageObject.getMailer();
			database = storageObject.getDatabase();
			sendUpdateStatusMessage(session,"established connection");
		}


		boolean hasEmails;
		try {
			hasEmails = database.hasEmails();
		} catch (SQLException e) {
			sendErrorMessage(session,"sqlException: \n"+e.getMessage());
			e.printStackTrace();
			return;
		} catch (ClassNotFoundException e) {
			sendErrorMessage(session,"ClassNotFoundException: \n"+e.getMessage());
			e.printStackTrace();
			return;
		}

		JsonObject js = new JsonObject();

		if (hasEmails) {
			List<UserFolder> folders;
			List<UserFavourites> favourites;

			try {
				folders = database.importFolders();
				favourites = database.getUserFavourites();
			} catch (SQLException e) {
				e.printStackTrace();
				sendErrorMessage(session,"sqlException:\n "+e.getMessage());
				return;
			} catch (MessagingException e) {
				e.printStackTrace();
				sendErrorMessage(session,"MessagingException:\n "+e.getMessage());
				return;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				sendErrorMessage(session,"ClassNotFoundException:\n "+e.getMessage());
				return;
			}
			refresh(storageObject,email,mailer,database,true,session);

			js.addProperty("messagetype", "logininfo");
			JsonArray ja1 = new JsonArray();
			JsonArray ja2 = new JsonArray();
			for (int i = 0; i < folders.size(); i++) {
				ja1.add(folders.get(i).getFolder().getFullName());
			}
			for (int i = 0; i < favourites.size(); i++) {
				ja2.add(favourites.get(i).getName());
			}
			js.add("foldername", ja1);
			js.add("favoritename", ja2);
			sendMessageToClient(session,js);
		} else {
			sendUpdateStatusMessage(session,"nothing found in database, preforming fresh import");
			refresh(storageObject,email,mailer,database,false,session);
		}

	}

	//TODO Still needs testing
	private void filter(Session session, Database database, String email, JsonObject filter){
		Handler handler = new Handler(session, database, email, filter);
		Thread thread = new Thread(handler);
		thread.start();
	}

	//TODO Still needs testing
	private void callFavorite(Session session, Database database, String favname, String email){

		UserFavourites userFavourites;
		try {
			userFavourites = database.getUserFavourite(favname);
		} catch (SQLException e) {
			e.printStackTrace();
			sendErrorMessage(session,"sqlException: \n"+e.getMessage());
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			sendErrorMessage(session,"ClassNotFoundException: \n"+e.getMessage());
			return;
		}
		Handler handler = new Handler(session, database, email, userFavourites);
		Thread thread = new Thread(handler);
		thread.start();
	}

	/**
	 * If a validationthread is not happening then refresh is called to make it so.
	 * @param storageObject
	 * @param email
	 * @param mailer
	 * @param database
	 * @param validateOrPull
	 * @param session
	 */
	private void refresh(StorageObject storageObject,String email,Mailer mailer, Database database, boolean validateOrPull, Session session){
		if(storageObject!=null&&storageObject.getValidationThread()!=null&&storageObject.getValidationThread().isAlive()){
			JsonObject js = new JsonObject();
			js.addProperty("messagetype","statusupdate");
			js.addProperty("message","validation already occuring");
			sendMessageToClient(session,js);
		}
		ValidationRunnable vr = new ValidationRunnable(mailer,database,validateOrPull,session);
		Thread thread = new Thread(vr);
		thread.start();
		storageObject.setValidationRunnable(vr);
		storageObject.setValidationThread(thread);
		sessionMapper.put(email,storageObject);
	}


	/**
	 * This method is the add a new favorite to database and return gui the new list of favorites
	 * @param session
	 * @param database
	 * @param favoriteName
	 * @param filter
	 */
	private void addFavorite(Session session,Database database,String favoriteName, JsonObject filter){
		String foldername = filter.get("foldername").getAsString();
		String sd = filter.get("date").getAsString();
		String interval = filter.get("interval").getAsString();

		DateTime startDate = getStartDate(sd);
		DateTime endDate = getEndDate(startDate, interval);
		if(startDate==null||endDate==null){
			JsonObject js = new JsonObject();
			js.addProperty("messagetype","statusupdate");
			js.addProperty("message","invalid dateTime");
			sendMessageToClient(session, js);
			return;
		}

		boolean attachment = filter.get("attachment").getAsBoolean();
		boolean seen = filter.get("seen").getAsBoolean();
		boolean added;
		try {
			added = database.insertUserFavourites(favoriteName, startDate.toDate(), endDate.toDate(), Interval.parse(interval),
					attachment, seen, foldername);
		} catch (SQLException e) {
			e.printStackTrace();
			sendErrorMessage(session,"SQLException: \n"+e.getMessage());
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			sendErrorMessage(session,"ClassNotFoundException: \n"+e.getMessage());
			return;
		}

		if(added){
			sendUpdateStatusMessage(session,"Favorite has been added");
		}else{
			sendUpdateStatusMessage(session, "No FolderName");
		}

		List<UserFavourites> favourites ;
		try {
			favourites = database.getUserFavourites();
		} catch (SQLException e) {
			e.printStackTrace();
			sendErrorMessage(session,"sqlException: \n"+e.getMessage());
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			sendErrorMessage(session,"ClassNotFoundException: \n"+e.getMessage());
			return;
		}

		JsonArray ja = new JsonArray();
		for (int i = 0; i < favourites.size(); i++) {
			ja.add(favourites.get(i).getName());
		}

		JsonObject js = new JsonObject();
		js.addProperty("messagetype", "favoritename");
		js.add("favoritename", ja);
		sendMessageToClient(session, js);
	}

	/**
	 * method removes favorite from database and sends gui the new list of favorites
	 * @param session
	 * @param database
	 * @param favoriteName
	 */
	private void removeFavorite(Session session, Database database, String favoriteName){
		try {//dont return from these catches as they need UserFavourites
			database.removeUserFavourite(favoriteName);
		} catch (SQLException e) {
			e.printStackTrace();
			sendErrorMessage(session,"ClassNotFoundException: \n"+e.getMessage());
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			sendErrorMessage(session,"ClassNotFoundException: \n"+e.getMessage());
			return;
		}

		sendUpdateStatusMessage(session,"Favorite has been removed");

		List<UserFavourites> favourites;
		try {
			favourites = database.getUserFavourites();
		} catch (SQLException e) {
			e.printStackTrace();
			sendErrorMessage(session,"sqlexception: \n"+e.getMessage());
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			sendErrorMessage(session,"ClassNotFoundException: \n"+e.getMessage());
			return;
		}


		JsonArray ja = new JsonArray();
		for (int i = 0; i < favourites.size(); i++) {
			ja.add(favourites.get(i).getName());
		}
		JsonObject js = new JsonObject();
		js.addProperty("messagetype", "favoritename");
		js.add("favoritename", ja);
		sendMessageToClient(session, js);
	}

	/**
	 * method to remove user instance from memory
	 * @param session
	 * @param email
	 */
	private void logout(Session session, String email){
		sessionMapper.remove(email);
		try {
			session.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	below are support methods
	 */

	/**
	 * Method to obtain end date
	 * @param startDate
	 * @param interval
	 * @return DateTime
	 */
	private DateTime getEndDate(DateTime startDate, String interval) {
		if(startDate==null){return null;}
		if (interval.equals("year")) {
			return startDate.plusYears(1);
		} else if (interval.equals("month")) {
			return startDate.plusMonths(1);
		} else {// week
			return startDate.plusWeeks(1);
		}
	}

	/**
	 * takes a string and attempts to convert it to a jodaDateTime
	 * @param sd
	 * @return DateTime
	 */
	private DateTime getStartDate(String sd){
		try {
			return new DateTime(DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").parseMillis(sd));
		}catch(IllegalArgumentException iae){
			return null;
		}
	}

	private void sendUpdateStatusMessage(Session session,String message){
		JsonObject js = new JsonObject();
		js.addProperty("messagetype","statusupdate");
		js.addProperty("message",message);
		sendMessageToClient(session,js);
	}

	private void sendErrorMessage(Session session,String errorMessage){
		JsonObject js = new JsonObject();
		js.addProperty("messagetype","error");
		js.addProperty("message",errorMessage);
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
