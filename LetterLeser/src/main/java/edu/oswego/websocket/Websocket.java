package edu.oswego.websocket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.oswego.Runnables.Handler;
import edu.oswego.Runnables.ValidationRunnable;
import edu.oswego.database.Database;
import edu.oswego.mail.Mailer;
import edu.oswego.props.MessageType;

@ServerEndpoint("/engine")
public class Websocket {
	// this is to manage all current/last active threads for each unique sessions
	ConcurrentHashMap<String, StorageObject> sessionThreadMapper = new ConcurrentHashMap<>();
	ConcurrentHashMap<String, StorageObject> validationManager = new ConcurrentHashMap<>();

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
		StorageObject storageObject = sessionThreadMapper.get(session.getId());

		JsonObject jsonMessage = makeJsonObject(message);
		if (jsonMessage == null) {
			try {
				session.getBasicRemote().sendText("invalid_jsonObject: you've been disconnected");
				session.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// The first message that must be sent to the websocket is a valid
		// googlejsonobject which will construct everything
		if (storageObject == null) {
			String email = jsonMessage.getAsJsonObject("profileObj").get("email").getAsString();
			Mailer mailer = new Mailer(jsonMessage.get("accessToken").getAsString());
			Database db = new Database(email, mailer);

			AtomicBoolean emailsExist = new AtomicBoolean(false);
			AtomicReference<Database> database = new AtomicReference<>(db);
			AtomicReference<JsonObject> googleOauth2 = new AtomicReference<>(jsonMessage);
			AtomicReference<Session> atomicSession = new AtomicReference<>(session);
			AtomicReference<JsonObject> atomicMessage = new AtomicReference<>(new JsonObject());
			atomicMessage.get().addProperty("MessageType", "StartUp");

			StorageObject validationStorageObject = validationManager.get(email);
			Thread validationThread = null;
			ValidationRunnable validationRunnable = null;
			if (validationStorageObject == null) {
				validationRunnable = new ValidationRunnable(atomicSession, googleOauth2, database, emailsExist,
						validationManager);
				validationThread = new Thread(validationRunnable);
				validationStorageObject = new StorageObject(null, null, validationThread, validationRunnable);
				validationManager.put(email, validationStorageObject);
			} else {
				validationThread = validationStorageObject.getValidationThread();
				validationRunnable = validationStorageObject.getValidationRunnable();
				emailsExist = validationRunnable.getEmailStored();
			}

			Handler handler = new Handler(atomicSession, atomicMessage, googleOauth2, database, emailsExist);
			Thread handlerThread = new Thread(handler);

			validationThread.start();
			handlerThread.start();

			storageObject = new StorageObject(handlerThread, handler, validationThread, validationRunnable);
			sessionThreadMapper.put(session.getId(), storageObject);

		} else if (MessageType.checkType(MessageType.REFRESH, jsonMessage.get("MessageType"))) {
			databaseValidation(session);
		} else {
			newRequest(session, jsonMessage, storageObject);
		}

	}

	@OnClose // method to disconnect from a session : this also interrupts everything and
				// stops everything
	public void onClose(Session session) {
		System.out.println("onClose");
		sessionThreadMapper.get(session.getId()).getHanderThread().interrupt();
		sessionThreadMapper.remove(session.getId());
	}

	@OnError
	public void onError(Throwable t, Session session) {
		System.out.println("onError::");
	}

	/*
	 * all private functional methods are below
	 */

	private void databaseValidation(Session session) {
		StorageObject main = sessionThreadMapper.get(session.getId());
		Handler handler = main.getHandler();
		String email = handler.getEmail();
		StorageObject validation = validationManager.get(email);
		if (validation == null) {
			// TODO Please update. Will always be null at this location.
			ValidationRunnable vr = new ValidationRunnable(validation.getValidationRunnable());
			Thread validationThread = new Thread(vr);
			validationThread.start();
			validationManager.put(email, new StorageObject(null, null, validationThread, vr));
		}

	}

	private void newRequest(Session session, JsonObject message, StorageObject storageObject) {
		Thread handlerThread = storageObject.getHanderThread();
		if (handlerThread.isAlive()) {
			handlerThread.interrupt();
		}
		Handler handler = new Handler(storageObject.getHandler(), message, handlerThread);
		Thread newHandlerThread = new Thread(handler);
		newHandlerThread.start();
		storageObject.setHanderThread(newHandlerThread);
		storageObject.setHandler(handler);
		sessionThreadMapper.put(session.getId(), storageObject);

	}

	// new JsonParser().parse(message).getAsJsonObject();
	private JsonObject makeJsonObject(String message) {
		return new JsonParser().parse(message).getAsJsonObject();
	}



}
