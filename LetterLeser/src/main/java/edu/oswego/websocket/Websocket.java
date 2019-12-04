package edu.oswego.websocket;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.json.Json;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.oswego.mail.Mailer;
import edu.oswego.model.Email;
import edu.oswego.model.UserFolder;
import edu.oswego.runnables.*;

/**
 * Class handles all communication between gui and engine
 */
@ServerEndpoint("/engine")
public class Websocket {
	// this is to manage all current/last active threads for each unique sessions
	private static ConcurrentHashMap<String, StorageObject> sessionMapper = new ConcurrentHashMap<>();
	Messenger messenger = new Messenger();
	JSDecryptor jse = null;

	/**
	 * standard inclusive method that comes with websockets
	 * 
	 * @param session
	 */
	@OnOpen
	public void onOpen(Session session) {
		System.out.println("Session " + session.getId() + " been established");
		try {
			jse = new JSDecryptor();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		messenger.sendPublicKey(session, jse.getPublic());
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
			messenger.sendErrorMessage(session, "invalid jsonMessage try again");
			return;
		}

		String messageType = jsonMessage.get("messagetype").getAsString();
		String decryptedEmail = decryptString(session, jsonMessage.get("email").getAsString());

		if (decryptedEmail == null) {
			return;
		}

		StorageObject storageObject = sessionMapper.get(decryptedEmail);

		if (messageType.equals("filter")) {
			filter(session, decryptedEmail, jsonMessage.get("filter").getAsJsonObject());
		} else if (messageType.equals("login")) {
			String decryptedPass = decryptString(session, jsonMessage.get("pass").getAsString());

			if (decryptedPass == null) {
				return;
			}

			login(session, decryptedEmail, decryptedPass, storageObject);
		}
		// else if (messageType.equals("refresh")) {
		// refresh(storageObject, decryptedEmail, storageObject.getMailer(),
		// storageObject.getDatabase(), true,
		// session);
		// } else if (messageType.equals("addfavorite")) {
		// addFavorite(session, storageObject.getDatabase(),
		// jsonMessage.get("favoritename").getAsString(),
		// jsonMessage.get("filter").getAsJsonObject());
		// } else if (messageType.equals("callfavorite")) {
		// callFavorite(session, storageObject.getDatabase(),
		// jsonMessage.get("favoritename").getAsString(),
		// decryptedEmail);
		// } else if (messageType.equals("removefavorite")) {
		// removeFavorite(session, storageObject.getDatabase(),
		// jsonMessage.get("favoritename").getAsString());
		// } else if (messageType.equals("logout")) {
		// logout(session, decryptedEmail);
		// } else if(messageType.equals("reconnect")){
		// reconnect(session);
		// }
		else {
			messenger.sendUpdateStatusMessage(session,
					"Invalid messageType\n" + "Please send one of these options:\n" + "login, filter\nline 153");

		}

	}

	/**
	 * Standard method that comes with websockets to close a connection
	 * 
	 * @param session
	 */
	@OnClose
	public void onClose(Session session) {
		System.out.println("session closed");
	}

	/**
	 * standard method that comes with websockets to handle errors
	 * 
	 * @param t
	 * @param session
	 */
	@OnError
	public void onError(Throwable t, Session session) {
		System.out.println("onError::");
		messenger.sendUpdateStatusMessage(session,
				"well an error was thrown and actually crashed the thread. The error => " + t.getMessage());
		t.printStackTrace();
	}

	/*
	 * all private functional methods are below directly below are key methods for
	 * message digestion.
	 */

	private void login(Session session, String email, String pass, StorageObject storageObject) {
		// String pass = jsonMessage.get("pass").getAsString();

		Mailer mailer = new Mailer(email, pass);

		messenger.sendUpdateStatusMessage(session, "checking connection");
		boolean connection = mailer.isConnected();
		if (!connection) {
			messenger.sendErrorMessage(session,
					"sorry but we can't connect to your email. maybe invalid email n password");
			return;
		}

		if (storageObject == null) {
			storageObject = new StorageObject();
			storageObject.setMailer(mailer);
			sessionMapper.put(email, storageObject);
		} else {
			Mailer oldMailer = storageObject.getMailer();
			oldMailer.closeMailer();
			storageObject.setMailer(mailer);
			sessionMapper.put(email, storageObject);
		}

		messenger.sendUpdateStatusMessage(session,
				"we found your inbox and getting your folders: this may take a second");

		List<UserFolder> folders = mailer.importFolders();

		messenger.sendUpdateStatusMessage(session, "we got your folders, packing and sending over now");

		messenger.sendLoginInfo(session, folders);

	}

	// TODO Still needs testing
	private void filter(Session session, String email, JsonObject filter) {
		Mailer mailer = sessionMapper.get(email).getMailer();
		String foldername = filter.get("foldername").getAsString();

		messenger.sendUpdateStatusMessage(session, "attempting to pull your emails\nthis can take a while");
		List<Email> emails = mailer.pullEmails(session, messenger, foldername);

		if (emails == null) {
			messenger.sendUpdateStatusMessage(session, "no emails exist in that folder");
			return;
		}else if(emails.size()==0){
			messenger.sendUpdateStatusMessage(session, "no emails exist in that folder");
			return;
		}
		Collections.sort(emails);
		messenger.sendUpdateStatusMessage(session,
				"the emails have been compiled, we will start running the calculations");
		calculations(session, email, emails);
	}

	private String decryptString(Session session, String encryptedEmail) {
		try {
			return jse.decrypt(encryptedEmail);
		} catch (BadPaddingException e) {
			e.printStackTrace();
			messenger.sendErrorMessage(session, "Failed to decrypt email.\n" + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			messenger.sendErrorMessage(session, "Failed to decrypt email.\n" + e.getMessage());
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			messenger.sendErrorMessage(session, "Failed to decrypt email.\n" + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			messenger.sendErrorMessage(session, "Failed to decrypt email. (Generic exception.)\n" + e.getMessage());
		}
		return null;
	}

	private void calculations(Session session, String email, List<Email> emailList) {

		SentimentScoreCallable sentimentScoreCallable = new SentimentScoreCallable(emailList);
		DomainCallable domainCallable = new DomainCallable(emailList);
		FolderCallable folderCallable = new FolderCallable(emailList);
		NumOfEmailsCallable numOfEmailsCallable = new NumOfEmailsCallable(emailList);
		SnRCallable snRCallable = new SnRCallable(emailList, email);
		TimeBetweenRepliesCallable timeBetweenRepliesCallable = new TimeBetweenRepliesCallable(emailList, email);

		FutureTask ssc = new FutureTask(sentimentScoreCallable);
		FutureTask dc = new FutureTask(domainCallable);
		FutureTask fc = new FutureTask(folderCallable);
		FutureTask noec = new FutureTask(numOfEmailsCallable);
		FutureTask snrc = new FutureTask(snRCallable);
		FutureTask tbrc = new FutureTask(timeBetweenRepliesCallable);

		Thread t1 = new Thread(ssc);
		t1.start();
		Thread t2 = new Thread(dc);
		t2.start();
		Thread t3 = new Thread(fc);
		t3.start();
		Thread t4 = new Thread(noec);
		t4.start();
		Thread t5 = new Thread(snrc);
		t5.start();
		Thread t6 = new Thread(tbrc);
		t6.start();

		messenger.sendUpdateStatusMessage(session, "we are running all calculations atm");
		JsonObject sentiment;
		JsonArray domain;
		JsonArray folder;
		JsonArray numOfMail;
		JsonObject sendNRec;
		JsonObject timeBetween;
		try {
			messenger.sendUpdateStatusMessage(session, "getting your sentiment info");
			sentiment = (JsonObject) ssc.get(2, TimeUnit.MINUTES);
			messenger.sendUpdateStatusMessage(session, "got the sentiment\ngetting your domain info");
			domain = (JsonArray) dc.get(2, TimeUnit.MINUTES);
			messenger.sendUpdateStatusMessage(session, "got the domain info\ngetting your folder info");
			folder = (JsonArray) fc.get(2, TimeUnit.MINUTES);
			messenger.sendUpdateStatusMessage(session, "got the folder info\ngetting your the number of emails");
			numOfMail = (JsonArray) noec.get(2, TimeUnit.MINUTES);
			messenger.sendUpdateStatusMessage(session,
					"got the number of emails\ngetting the amount you send and recieve");
			sendNRec = (JsonObject) snrc.get(2, TimeUnit.MINUTES);
			messenger.sendUpdateStatusMessage(session,
					"got the amount of emails you send and receive \ngetting your the time between your emails");
			timeBetween = (JsonObject) tbrc.get(2, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
			messenger.sendUpdateStatusMessage(session,
					"sorry but we ran into an error with our calculations: InterruptedException : " + e.getMessage());
			return;
		} catch (ExecutionException e) {
			e.printStackTrace();
			messenger.sendUpdateStatusMessage(session,
					"sorry but we ran into an error with our calculations: ExecutionException : " + e.getMessage());
			return;
		} catch (TimeoutException e) {
			e.printStackTrace();
			messenger.sendUpdateStatusMessage(session,
					"sorry but we ran into an error with our calculations: TimeoutException : " + e.getMessage());
			return;
		} catch (Exception e) {
			e.printStackTrace();
			messenger.sendUpdateStatusMessage(session,
					"sorry but we ran into an error with our calculations: Exception : " + e.getMessage());
			return;
		}

		messenger.sendUpdateStatusMessage(session,
				"we got all your emails, we are compiling the data and will be ready to send momentarily");

		messenger.sendGraphData(session, sentiment, domain, folder, numOfMail, sendNRec, timeBetween);

	}

	// // TODO Still needs testing
	// private void callFavorite(Session session, Database database, String favname,
	// String email) {
	// UserFavourites userFavourites;
	// try {
	// userFavourites = database.getUserFavourite(favname);
	// }catch(Exception e){
	// messenger.sendErrorMessage(session,"Couldn't get favourites. (Generic
	// exception.)\n"+e.getMessage());
	// return;
	// }
	// Handler handler = new Handler(session, database, email, userFavourites);
	// Thread thread = new Thread(handler);
	// thread.start();
	// }
	//
	// /**
	// * If a validationthread is not happening then refresh is called to make it
	// so.
	// *
	// * @param storageObject
	// * @param email
	// * @param mailer
	// * @param database
	// * @param validateOrPull
	// * @param session
	// */
	// private void refresh(StorageObject storageObject, String email, Mailer
	// mailer, Database database,
	// boolean validateOrPull, Session session) {
	// if (storageObject != null && storageObject.getValidationThread() != null
	// && storageObject.getValidationThread().isAlive()) {
	// messenger.sendUpdateStatusMessage(session,"Validation already occurring." +
	// "\nstorageObject is not null AND storageObject.getValidationThread is not
	// null AND isAlive is true.");
	// }
	// ValidationRunnable vr = new ValidationRunnable(mailer, database,
	// validateOrPull, session);
	// Thread thread = new Thread(vr);
	// thread.start();
	// storageObject.setValidationRunnable(vr);
	// storageObject.setValidationThread(thread);
	// sessionMapper.put(email, storageObject);
	// }
	//
	// /**
	// * This method is the add a new favorite to database and return gui the new
	// list
	// * of favorites
	// *
	// * @param session
	// * @param database
	// * @param favoriteName
	// * @param filter
	// */
	// private void addFavorite(Session session, Database database, String
	// favoriteName, JsonObject filter) {
	// String foldername = filter.get("foldername").getAsString();
	// String sd = filter.get("date").getAsString();
	// String interval = filter.get("interval").getAsString();
	//
	// DateTime startDate = getStartDate(sd);
	// DateTime endDate = getEndDate(startDate, interval);
	// if (startDate == null || endDate == null) {
	// messenger.sendErrorMessage(session,"Invalid dateTime.\nOne or both values are
	// null.");
	// return;
	// }
	//
	// boolean attachment = filter.get("attachment").getAsBoolean();
	// boolean seen = filter.get("seen").getAsBoolean();
	// boolean added;
	// try {
	// added = database.insertUserFavourites(favoriteName, startDate.toDate(),
	// endDate.toDate(),
	// Interval.parse(interval), attachment, seen, foldername);
	// } catch (Throwable t) {
	// messenger.sendErrorMessage(session, "Error in DB: \n" + t.getMessage());
	// return;
	// }
	//
	// if (added) {
	// messenger.sendUpdateStatusMessage(session, "Favorite has been added.\nadded
	// is true.");
	// } else {
	// messenger.sendUpdateStatusMessage(session, "No FolderName.\nadded is
	// false.");
	// }
	//
	// List<UserFavourites> favourites;
	// try {
	// favourites = database.getUserFavourites();
	// } catch (Throwable t) {
	// messenger.sendErrorMessage(session, "Error in DB: \n" + t.getMessage());
	// return;
	// }
	//
	// JsonArray ja = new JsonArray();
	// for (int i = 0; i < favourites.size(); i++) {
	// ja.add(favourites.get(i).getName());
	// }
	//
	// JsonObject js = new JsonObject();
	// js.addProperty("messagetype", "favoritename");
	// js.add("favoritename", ja);
	// messenger.sendMessageToClient(session, js);
	// }
	//
	// /**
	// * method removes favorite from database and sends gui the new list of
	// favorites
	// *
	// * @param session
	// * @param database
	// * @param favoriteName
	// */
	// private void removeFavorite(Session session, Database database, String
	// favoriteName) {
	// // dont return from these catches as they need UserFavourites
	// try {
	// database.removeUserFavourite(favoriteName);
	// } catch (Throwable t) {
	// messenger.sendErrorMessage(session, "Error in DB: " + t.getMessage());
	// return;
	// }
	// messenger.sendUpdateStatusMessage(session, "Favorite has been removed.\nline
	// 382");
	//
	// List<UserFavourites> favourites;
	// try {
	// favourites = database.getUserFavourites();
	// }catch(Exception e){
	// messenger.sendErrorMessage(session,"Error in DB: (Generic exception.)\n"+
	// e.getMessage());
	// return;
	// }
	//
	// JsonArray ja = new JsonArray();
	// for (int i = 0; i < favourites.size(); i++) {
	// ja.add(favourites.get(i).getName());
	// }
	// JsonObject js = new JsonObject();
	// js.addProperty("messagetype", "favoritename");
	// js.add("favoritename", ja);
	// messenger.sendMessageToClient(session, js);
	// }
	//
	// /**
	// * method to remove user instance from memory
	// *
	// * @param session
	// * @param email
	// */
	// private void logout(Session session, String email) {
	// sessionMapper.remove(email);
	// try {
	// session.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// private void reconnect(Session session){
	// StorageObject storageObject = sessionMapper.get("email");
	// if(storageObject==null){
	// messenger.sendErrorMessage(session,"No instance found, login
	// again.\nstorageObject was null.");
	// }else{
	// storageObject.getValidationRunnable().setSession(session);
	// }
	//
	// }
	//
	// /*
	// * below are support methods
	// */
	//
	// /**
	// * Method to obtain end date
	// *
	// * @param startDate
	// * @param interval
	// * @return DateTime
	// */
	// private DateTime getEndDate(DateTime startDate, String interval) {
	// if (startDate == null) {
	// return null;
	// }
	// if (interval.equals("year")) {
	// return startDate.plusYears(1);
	// } else if (interval.equals("month")) {
	// return startDate.plusMonths(1);
	// } else {// week
	// return startDate.plusWeeks(1);
	// }
	// }
	//
	// /**
	// * takes a string and attempts to convert it to a jodaDateTime
	// *
	// * @param sd
	// * @return DateTime
	// */
	// private DateTime getStartDate(String sd) {
	// try {
	// return new DateTime(DateTimeFormat.forPattern("yyyy/MM/dd
	// HH:mm:ss").parseMillis(sd));
	// } catch (IllegalArgumentException iae) {
	// return null;
	// }
	// }
	//
	// /**
	// * wait for a connection to open to db
	// * ~5 seconds before a timeout
	// * @param session
	// */
	// private boolean waitForConnection(Session session){
	// int count=0;
	// while(true){
	// if(Database.isConnection()){
	// return true;
	// }else{
	// return false;
	// }
	// }
	// }

}
