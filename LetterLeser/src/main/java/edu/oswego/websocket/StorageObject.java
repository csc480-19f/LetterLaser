package edu.oswego.websocket;

import edu.oswego.database.Database;
import edu.oswego.mail.Mailer;
import edu.oswego.runnables.Handler;
import edu.oswego.runnables.ValidationRunnable;

public class StorageObject {

	private Thread handlerThread;
	private Handler handler;
	private Thread validationThread;
	private ValidationRunnable validationRunnable;
	private Mailer mailer;
	private Database database;
	private JSDecryptor jsDecryptor;

	public JSDecryptor getJsDecryptor(){return jsDecryptor;}

	public void setJsDecryptor(JSDecryptor jsDecryptor){this.jsDecryptor = jsDecryptor;}

	public Thread getHanderThread() {
		return handlerThread;
	}

	public void setHanderThread(Thread handlerThread) {
		this.handlerThread = handlerThread;
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public Thread getValidationThread() {
		return validationThread;
	}

	public void setValidationThread(Thread validationThread) {
		this.validationThread = validationThread;
	}

	public ValidationRunnable getValidationRunnable() {
		return validationRunnable;
	}

	public void setValidationRunnable(ValidationRunnable validationRunnable) {
		this.validationRunnable = validationRunnable;
	}

	public void setMailer(Mailer mailer) {
		this.mailer = mailer;
	}

	public Mailer getMailer() {
		return mailer;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public Database getDatabase() {
		return database;
	}

}
