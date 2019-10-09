package edu.oswego.mail;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

import edu.oswego.props.Settings;

/**
 * Mailer class that has the Session/Store objects as well as
 * host/port/tls settings.
 * 
 * @author Jimmy Nguyen
 * @since 10/08/2019
 */

public class Mailer {

	private Session session;
	private Store storage;
	private final String HOST = "imap.gmail.com";
	private final int PORT = 995;
	private final boolean TLS_ENABLED = true;

	public Mailer(String accessKey) {

	}

	/*
	 * Establish a connection using imap
	 * 
	 * @return a javaxmail Session object. Needed for getting a javaxmail Storage
	 * object.
	 */
	public Session getConnection() {
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
	public Store getStorage() {
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

	/*
	 * Fetches a gmail folder
	 * 
	 * @return javaxmail Folder object
	 * 
	 * @param folder name
	 */
	public Folder getFolder(String folderName) {
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
	 * Moves emails that were processed in the database to a new folder called
	 * CSC480_19F (creates it if not exists). Used for validation.
	 * 
	 * @param Folder name that the email belongs to and an array of messages.
	 */
	public void markEmailsInFolder(String originFolderName, Message[] msgs) {
		// MAKE HIDDEN FOLDER... maybe subscribed?
		Folder folder = null;
		try {
			folder = getStorage().getFolder("CSC480_19F");

			if (!folder.exists()) {
				if (folder.create(Folder.HOLDS_MESSAGES)) {
					folder.setSubscribed(true);
					System.out.println("FOLDER MADE!");
				}
			}

			folder.open(Folder.READ_WRITE);

			Folder originFolder = getStorage().getFolder(originFolderName);
			originFolder.open(Folder.READ_WRITE);
			// MUST CHECK IF MESSAGE ALREADY EXISTS IN FOLDER OR NOT. ONLY COPY IF NOT. DID
			// NOT DO YET.
			originFolder.copyMessages(msgs, folder);
			originFolder.close();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Uses DB connection and PreparedStatement to execute a query.
	 * 
	 * @return array of javaxmail Message object.
	 * 
	 * @param folder name
	 */
	public Message[] pullEmails(String folderName) {
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

	/*
	 * Checks if a message has an attachment
	 * 
	 * @return boolean if message is multipart
	 */
	public boolean hasAttachment(Message m) {
		try {
			if (m.getContentType().contains("multipart")) {
				Multipart multiPart = (Multipart) m.getContent();
				for (int i = 0; i < multiPart.getCount(); i++) {
					MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
					if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()))
						return true;
				}
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	/*
	 * Fetches attachment name based on message
	 * 
	 * @return name of attachment
	 * 
	 * @param javaxmail message object
	 */
	public String getAttachmentName(Message m) {
		try {
			Multipart multiPart = (Multipart) m.getContent();
			for (int i = 0; i < multiPart.getCount(); i++) {
				MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
				if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()))
					return part.getFileName().toString();
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}

}
