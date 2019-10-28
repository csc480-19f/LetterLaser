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



	@Override
	public void run() {

	}



}
