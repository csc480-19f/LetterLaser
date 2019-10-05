package edu.oswego.mail;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import edu.oswego.props.Settings;

/**
 * Singleton Mailer class that has the Session/Store objects as well as
 * host/port/tls settings.
 * 
 * @author Jimmy Nguyen
 * @since 10/04/2019
 */

public class Mailer {
	
	private static Session session;
	private static Store storage;
	private final static String HOST = "imap.gmail.com";
	private final static int PORT = 995;
	private final static boolean TLS_ENABLED = true;

	private Mailer() {
		// blank cause singleton
	}

	/*
	 * Establish a connection using imap
	 * 
	 * @return a javaxmail Session object. Needed for getting a javaxmail Storage
	 * object.
	 */
	private static Session getConnection() {
		if (session == null) {
			Properties properties = new Properties();
			properties.put("mail.imap.host", HOST);
			properties.put("mail.imap.port", PORT);
			properties.put("mail.imap.starttls.enable", TLS_ENABLED);
			session = Session.getDefaultInstance(properties);
		}
		return session;
	}

	/*
	 * Logins with a user and password
	 * 
	 * @return javaxmail Store object. Needed to pull by special means.
	 */
	public static Store getStorage() {
		if (storage == null) {
			try {
				storage = getConnection().getStore("imaps");
			} catch (NoSuchProviderException e) {
				e.printStackTrace();
			}
		}

		if (!storage.isConnected()) {
			try {
				storage.connect(HOST, Settings.EMAIL_ADDRESS, Settings.EMAIL_PWD);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}

		return storage;
	}

	public static Folder getFolder(String folderName) {
		Store store = getStorage();
		Folder folder = null;
		try {
			folder = store.getFolder(folderName);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return folder;
	}
	
	
	/*
	 * Uses DB connection and PreparedStatement to execute a query.
	 * 
	 * @return array of javaxmail Message object.
	 */
	public static Message[] pullEmails(String folderName) {
		Store store = getStorage();
		try {
			Folder folder = store.getFolder(folderName);
			folder.open(Folder.READ_ONLY);
			Message[] msgs = folder.getMessages();
			return msgs;
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		return null;
	}

}
