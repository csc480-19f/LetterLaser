package edu.oswego.Runnables;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.websocket.Session;

import com.google.gson.JsonObject;

import edu.oswego.database.Database;
import edu.oswego.websocket.StorageObject;

public class ValidationRunnable implements Runnable {
	private volatile AtomicReference<Session> session;
	private volatile AtomicReference<JsonObject> googleAccessToken;
	private volatile AtomicReference<Database> atomicDatabase;
	private volatile AtomicBoolean emailStored;
	private volatile ConcurrentHashMap<String, StorageObject> validationManager;

	public ValidationRunnable(AtomicReference<Session> session, AtomicReference<JsonObject> googleAccessToken,
			AtomicReference<Database> atomicDatabase, AtomicBoolean emailStored,
			ConcurrentHashMap<String, StorageObject> validationManager) {
		this.emailStored = emailStored;
		this.atomicDatabase = atomicDatabase;
		this.googleAccessToken = googleAccessToken;
		this.session = session;
		this.validationManager = validationManager;
	}

	public ValidationRunnable(ValidationRunnable validationRunnable) {
		this.emailStored = validationRunnable.emailStored;
		this.atomicDatabase = validationRunnable.atomicDatabase;
		this.googleAccessToken = validationRunnable.googleAccessToken;
		this.session = validationRunnable.session;
		this.validationManager = validationRunnable.validationManager;
	}

	@Override
	public void run() {
		// TODO check if that is how you get email from google json object
		String email = googleAccessToken.get().getAsJsonObject("profileObj").get("email").getAsString();
		Database db = atomicDatabase.get();
		if (db.hasEmails(email)) {
			emailStored.compareAndSet(false, true);
			// TODO validate DB

		} else {
			db.pull();
			emailStored.compareAndSet(false, true);
		}

		validationManager.remove(email);
	}

	public AtomicBoolean getEmailStored() {
		return emailStored;
	}
}
