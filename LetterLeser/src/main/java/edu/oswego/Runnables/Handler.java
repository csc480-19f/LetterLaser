package edu.oswego.Runnables;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.oswego.database.Database;
import edu.oswego.debug.DebugLogger;
import edu.oswego.model.Email;
import edu.oswego.model.EmailAddress;
import edu.oswego.model.UserFavourites;
import edu.oswego.model.UserFolder;
import edu.oswego.props.Interval;
import edu.oswego.props.Time;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import javax.websocket.Session;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class Handler implements Runnable {
	private volatile AtomicReference<Session> session;
	private volatile AtomicReference<Database> database;
	private volatile AtomicReference<JsonObject> googleAccessToken;
	private volatile AtomicBoolean hasEmails;
	private volatile AtomicReference<JsonObject> message;
	private volatile AtomicReference<Thread> oldThread;

	public Handler(AtomicReference<Session> session, AtomicReference<JsonObject> message,
			AtomicReference<JsonObject> googleAccessToken, AtomicReference<Database> database,
			AtomicBoolean hasEmails) {
		this.session = session;
		this.message = message;
		this.googleAccessToken = googleAccessToken;
		this.database = database;
		this.hasEmails = hasEmails;
		this.oldThread = null;
	}

	public Handler(Handler handler, JsonObject message, Thread oldThread) {
		this.session = handler.session;
		this.database = handler.database;
		this.googleAccessToken = handler.googleAccessToken;
		this.hasEmails = handler.hasEmails;
		this.message = new AtomicReference<>(message);
		this.oldThread = new AtomicReference<>(oldThread);
	}

	public String getEmail() {
		return googleAccessToken.get().getAsJsonObject("profileObj").get("email").getAsString();
	}

	@Override
	public void run() {
		{//debug stuff
			DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + "handler has started");
		}
		if(oldThread==null) {
			while (oldThread.get().isAlive()) {
				if (Thread.interrupted()) {
					{//debug stuff
						DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + "Thread interupted when waiting for old thread to die");
					}
					return;
				}
			} // freezes this thread till old thread is
		}

		if (googleAccessToken == null || Thread.interrupted()) {
			{//debug stuff
				DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + "accessToken null or thread interuppeted: "+
						"AccessToken: " + googleAccessToken.toString() +" Thread:"+Thread.interrupted());
			}
			return;
		} else if (message.get().get("MessageType").getAsString().equals("StartUp")) {
			{//debug stuff
				DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + " begine sendingFolders");
			}
			try {
				sendFolders();
			} catch (IOException e) {
				return;
			}
		} else {
			try {
				handleMessage(message.get());
			} catch (InterruptedException e) {
				{//debug stuff
					DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + " Thread detected interrupt call thus terminated");
				}
				return;
			}
		}

	}

	private void sendFolders() throws IOException {
		List<UserFolder> folders = database.get().importFolders();
		List<UserFavourites> favourites = database.get().getUserFavourites();

		{//debug stuff
			DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + "got folders and list of favourites: \n"+
					"folders: "+folders.toString()+
					"favourites: "+favourites.toString());
		}

		// TODO make the string into a json element
		Gson g = new Gson();
		String arrayOfFolders = g.toJson(folders);
		String arrayOfFavs = g.toJson(favourites);
		JsonObject finalPackage = new JsonObject();
		finalPackage.addProperty("DataTypes", "FavoriteNames,FolderNames");
		finalPackage.addProperty("FavoriteNames", arrayOfFavs);
		finalPackage.addProperty("FolderNames", arrayOfFolders);

		{//debug stuff
			DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + "message compiled as:\n"+
					finalPackage.getAsString());
		}

		session.get().getBasicRemote().sendText(finalPackage.getAsString());
	}

	private void handleMessage(JsonObject message) throws InterruptedException {
		// waiting to know if db has data or not: look at ValidationRunnable
		{//debug stuff
			DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + "being handling message: \n"+message.toString());
		}

		while (!hasEmails.get()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				{//debug stuff
					DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + "interrupt called when validation is still preceding, current throwing interruptedException");
				}
				throw new InterruptedException();
			}
		}

		String messageType = message.get("MessageType").getAsString();
		{//debug stuff
			DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + "messageType: "+messageType);
		}
		if (messageType.equals("AddFavorite")) {
			addFavourite(message);
		} else if (messageType.equals("RemoveFavorite")) {
			deleteFavorite(message);
		} else if (messageType.equals("CallFavorite")) {
			{//debug stuff
				DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + "calling Favorite");
			}

			UserFavourites filter = database.get().getUserFavourite(message.get("message").getAsString());
			//TODO have UserFavourite get me an endDate var
			List<Email> emails = database.get().getEmailByFilter(filter.getName(),filter.getStartDate().toString(),filter.getStartDate().toString(),filter.isSeen(),filter.getFolder().getFolder().getName());
			calculateAndSend(emails, database.get().getUser());
		} else {
			parseJsonObject(message.getAsJsonObject("Filter"));
		}

	}

	private void addFavourite(JsonObject message) {
		JsonObject addFav = message.getAsJsonObject("AddFavorite");
		JsonObject filter = addFav.getAsJsonObject("Filter");
		String favoriteName = message.get("FavoriteName").getAsString();
		String folderName = filter.get("FolderName").getAsString();
		String stringDate = filter.get("Date").getAsString();
		String interval = filter.get("Interval").getAsString();
		boolean attachment = filter.get("Attachment").getAsBoolean();
		boolean seen = filter.get("Seen").getAsBoolean();
		{//debug stuff
			DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + " message\n" + addFav.toString() + "\n" + filter.toString() + "\n"
					+ favoriteName + "\n" + folderName + "\n" + stringDate + "\n" + interval + "\n" + attachment + "\n" + seen + "\n");
		}
		try {
			Date date = new SimpleDateFormat("MM/dd/yyyy").parse(stringDate);
			database.get().insertUserFavourites(favoriteName, date, Interval.MONTH, attachment, seen, folderName);
		} catch (ParseException e) {
			{//debug stuff
				DebugLogger.logEvent(Level.SEVERE, "session " + session.get().getId() + " invalid date in addFavourite");
			}
			return;
		}
	}

	private void deleteFavorite(JsonObject message) {
		String favName = message.get("FavoriteName").getAsString();
		database.get().removeUserFavourite(favName);
		{//debug stuff
			DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + " favorite deleted");
		}
	}

	private void parseJsonObject(JsonObject message) throws InterruptedException {
		String folderName = message.get("FolderName").getAsString();
		String stringDate = message.get("Date").getAsString();
		String interval = message.get("Interval").getAsString();
		String attachment = message.get("Attachment").getAsString();
		Boolean seen = message.get("Seen").getAsBoolean();

		Date date;
		Date endDate;
		try {
			LocalDate dt = new LocalDate(new SimpleDateFormat("MM/dd/yyyy").parse(stringDate));
			int amountOfDays;
			if (interval.equals("year")) {
				amountOfDays = Days.daysBetween(dt, dt.plusYears(1)).getDays();
			} else if (interval.equals("month")) {
				amountOfDays = Days.daysBetween(dt, dt.plusMonths(1)).getDays();
			} else {
				amountOfDays = 7;
			}
			date = dt.toDate();
			endDate = dt.plusDays(amountOfDays).toDate();
		} catch (ParseException e) {
			return;
		}

		List<Email> emails = database.get().getEmailByFilter(attachment, Time.parseDateTime(date), Time.parseDateTime(endDate), seen, folderName);
		calculateAndSend(emails, database.get().getUser());


	}

	private void calculateAndSend(List<Email> emails, EmailAddress user) throws InterruptedException {
		// Making all the callables and futures and executing them in threads
		{//debug stuff
			DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + " running calcs on:\n"+
					emails.toString());
		}

		DomainCallable dc = new DomainCallable(emails);
		SentimentScoreCallable ssc = new SentimentScoreCallable(emails);
		FolderCallable fc = new FolderCallable(emails);
		NumOfEmailsCallable noec = new NumOfEmailsCallable(emails);
		SnRCallable src = new SnRCallable(emails, user.getEmailAddress());
		TimeBetweenRepliesCallable tbrc = new TimeBetweenRepliesCallable(emails, user.getEmailAddress());

		FutureTask sscTask = new FutureTask<>(ssc);
		FutureTask dcTask = new FutureTask<>(dc);
		FutureTask fcTask = new FutureTask<>(fc);
		FutureTask noecTask = new FutureTask<>(noec);
		FutureTask srcTask = new FutureTask<>(src);
		FutureTask tbrcTask = new FutureTask<>(tbrc);

		if (Thread.interrupted()) {
			{//debug stuff
				DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + " thread interupted before calc started");
			}
			throw new InterruptedException();
		}

		new Thread(sscTask).start();
		new Thread(dcTask).start();
		new Thread(fcTask).start();
		new Thread(noecTask).start();
		new Thread(srcTask).start();
		new Thread(tbrcTask).start();

		Object dcData;
		Object sscData;
		Object fcData;
		Object noecData;
		Object srcData;
		Object tbrcData;
		// todo add the calculations for each callable
		try {
			sscData = sscTask.get();
			dcData = dcTask.get();
			fcData = fcTask.get();
			noecData = noecTask.get();
			srcData = srcTask.get();
			tbrcData = tbrcTask.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		if (Thread.interrupted()) {
			{//debug stuff
				DebugLogger.logEvent(Level.INFO, "session " + session.get().getId() + " thread interrupted before compiling json");
			}
			throw new InterruptedException();
		}

		JsonObject dataSet = new JsonObject();

		// todo, combine all the data into final jsonobject and send it over to gui

	}

}
