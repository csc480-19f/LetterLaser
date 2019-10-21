package edu.oswego.Runnables;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.oswego.database.Database;
import edu.oswego.model.Email;
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
		while (oldThread.get().isAlive()) {
			if (Thread.interrupted()) {
				return;
			}
		} // freezes this thread till old thread is
		if (googleAccessToken == null || Thread.interrupted()) {

		} else if (message.get().get("MessageType").getAsString().equals("StartUp")) {
			try {
				sendFolders();
			} catch (IOException e) {
				// cant really do anything
			}
		} else {
			try {
				handleMessage(message.get());
			} catch (InterruptedException e) {

			}
		}

	}

	private void sendFolders() throws IOException {
		List<UserFolder> folders = database.get().importFolders();
		List<UserFavourites> favourites = database.get().getUserFavourites();
		// TODO make the string into a json element
		Gson g = new Gson();
		String arrayOfFolders = g.toJson(folders);
		String arrayOfFavs = g.toJson(favourites);
		JsonObject finalPackage = new JsonObject();
		finalPackage.addProperty("DataTypes", "FavoriteNames,FolderNames");
		finalPackage.addProperty("FavoriteNames", arrayOfFavs);
		finalPackage.addProperty("FolderNames", arrayOfFolders);

		session.get().getBasicRemote().sendText(finalPackage.getAsString());
	}

	private void handleMessage(JsonObject message) throws InterruptedException {
		// waiting to know if db has data or not: look at ValidationRunnable
		while (!hasEmails.get()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				throw new InterruptedException();
			}
		}

		String messageType = message.get("MessageType").getAsString();
		if (messageType.equals("AddFavorite")) {
			addFavourite(message);
		} else if (messageType.equals("RemoveFavorite")) {
			deleteFavorite(message);
		} else if (messageType.equals("CallFavorite")) {
			UserFavourites filter = database.get().getUserFavourite(message.get("message").getAsString());
			//TODO have UserFavourite get me an endDate var
			List<Email> emails = database.get().getEmailByFilter(filter.getName(),filter.getStartDate().toString(),filter.getStartDate().toString(),filter.isSeen(),filter.getFolder().getFolder().getName());
			List<UserFolder> folders = database.get().importFolders();
			calculateAndSend(emails,folders);
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

		try {
			Date date = new SimpleDateFormat("MM/dd/yyyy").parse(stringDate);
			database.get().insertUserFavourites(favoriteName, date, Interval.MONTH, attachment, seen, folderName);
		} catch (ParseException e) {
			return;
		}
	}

	private void deleteFavorite(JsonObject message) {
		String favName = message.get("FavoriteName").getAsString();
		database.get().removeUserFavourite(favName);
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
		List<UserFolder> folders = database.get().importFolders();
		calculateAndSend(emails,folders);


	}

	private void calculateAndSend(List<Email> emails, List<UserFolder> folders) throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		// Making all the callables and futures and executing them in threads
		DomainCallable dc = new DomainCallable(emails);
		SentimentScoreCallable ssc = new SentimentScoreCallable(emails);
		FolderCallable fc = new FolderCallable(folders);
		NumOfEmailsCallable noec = new NumOfEmailsCallable(emails);
		SnRCallable src = new SnRCallable(emails);
		TimeBetweenRepliesCallable tbrc = new TimeBetweenRepliesCallable(emails);

		FutureTask sscTask = new FutureTask<>(ssc);
		FutureTask dcTask = new FutureTask<>(dc);
		FutureTask fcTask = new FutureTask<>(fc);
		FutureTask noecTask = new FutureTask<>(noec);
		FutureTask srcTask = new FutureTask<>(src);
		FutureTask tbrcTask = new FutureTask<>(tbrc);

		if (Thread.interrupted()) {
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

		JsonObject dataSet = new JsonObject();

		// todo, combine all the data into final jsonobject and send it over to gui

	}

}
