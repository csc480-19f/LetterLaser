package edu.oswego.websocket;

import edu.oswego.Runnables.Handler;
import edu.oswego.Runnables.ValidationRunnable;

public class StorageObject {
	
	private Thread HanderThread;
	private Handler handler;
	private Thread validationThread;
	private ValidationRunnable validationRunnable;
	
	public StorageObject(Thread handerThread, Handler handler, Thread validationThread,
			ValidationRunnable validationRunnable) {
		HanderThread = handerThread;
		this.handler = handler;
		this.validationThread = validationThread;
		this.validationRunnable = validationRunnable;
	}

	public Thread getHanderThread() {
		return HanderThread;
	}

	public void setHanderThread(Thread handerThread) {
		HanderThread = handerThread;
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
	
}
