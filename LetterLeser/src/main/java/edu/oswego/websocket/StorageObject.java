package edu.oswego.websocket;

import edu.oswego.Runnables.Handler;
import edu.oswego.Runnables.ValidationRunnable;

public class StorageObject {
	
	private Thread handlerThread;
	private Handler handler;
	private Thread validationThread;
	private ValidationRunnable validationRunnable;
	
	public StorageObject(Thread handlerThread, Handler handler, Thread validationThread,
			ValidationRunnable validationRunnable) {
		this.handlerThread = handlerThread;
		this.handler = handler;
		this.validationThread = validationThread;
		this.validationRunnable = validationRunnable;
	}

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
	@Override
	public String toString(){
		return this.getClass().getName() + "@" + Integer.toHexString(this.hashCode()) + "\n" +
				handlerThread.getClass().getName() + "@" + Integer.toHexString(handlerThread.hashCode()) + "\n" +
				handler.getClass().getName() + "@" + Integer.toHexString(handler.hashCode()) + "\n" +
				validationThread.getClass().getName() + "@" + Integer.toHexString(validationThread.hashCode()) + "\n" +
				validationRunnable.getClass().getName() + "@" + Integer.toHexString(validationRunnable.hashCode());
	}
	
}
