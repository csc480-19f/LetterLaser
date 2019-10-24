package edu.oswego.mail;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import edu.oswego.debug.DebugLogger;

/**
 * Mailer class that has the Session/Store objects as well as host/port/tls
 * settings.
 * 
 * @author Jimmy Nguyen
 * @since 10/23/2019
 */

public class Mailer {

	private Session session;
	private Store storage;
	private String emailAddress, password;

	/**
	 * Creates a mailer object.
	 * 
	 * @param emailAddress
	 * @param password
	 * @see .Database
	 */
	public Mailer(String emailAddress, String password) {
		this.emailAddress = emailAddress;
		this.password = password; // UNSAFE. Lets encrypt.
		DebugLogger.logEvent(Mailer.class.getName(),Level.WARNING,
				"Mailer object created. Information is in local storage stored unencrypted.");
	}
	
	public boolean isConnected() {
		return storage.isConnected();
	}

	/**
	 * Establish a connection using imap
	 * 
	 * @return a javaxmail Session object. Needed for getting a javaxmail Storage
	 *         object.
	 */
	public Session getConnection() {
		if (session == null) {
			Properties properties = new Properties();
			properties.put("mail.imap.host", Settings.HOST);
			properties.put("mail.imap.port", Settings.PORT);
			properties.put("mail.imap.starttls.enable", Settings.TLS_ENABLED);
			session = Session.getDefaultInstance(properties);
		}
		return session;
	}

	/**
	 * Logins with a user and password information
	 * 
	 * @return javaxmail Store object. Needed to pull by special means.
	 */
	public Store getStorage() {
		if (storage == null) {
			try {
				storage = getConnection().getStore(Settings.STORE_TYPE);
			} catch (NoSuchProviderException e) {
				DebugLogger.logEvent(Mailer.class.getName(),Level.WARNING, e.getMessage());
			}
		}

		if (!storage.isConnected()) {
			try {
				storage.connect(Settings.HOST, Settings.EMAIL_ADDRESS, Settings.EMAIL_PWD);
				//storage.connect(Settings.HOST, emailAddress, password);
			} catch (MessagingException e) {
				DebugLogger.logEvent(Mailer.class.getName(),Level.WARNING, e.getMessage());
			}
		}

		return storage;
	}

	/**
	 * Fetches a folder by name
	 * 
	 * @param folderName
	 * @return JavaxMail folder object
	 */
	public Folder getFolder(String folderName) {
		Store store = getStorage();
		Folder folder = null;
		try {
			folder = store.getFolder(folderName);
		} catch (MessagingException e) {
			DebugLogger.logEvent(Mailer.class.getName(),Level.WARNING, e.getMessage());
		}
		return folder;
	}

	/**
	 * Moves emails that were processed/pulled in database to a new invisible folder
	 * called CSC480_19F (creates if not exists). Used for validation/checking.
	 * 
	 * @param originFolderName
	 * @param msgs
	 */
	public void markEmailsInFolder(String originFolderName, Message[] msgs) { // TODO change this to javaxmail folder
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
			DebugLogger.logEvent(Mailer.class.getName(),Level.WARNING, e.getMessage());
		}
	}

	/**
	 * Gets all the emails from a folder
	 * 
	 * @param folderName
	 * @return Message array object
	 */
	public Message[] pullEmails(String folderName) {
		Store store = getStorage();
		try {
			Folder folder = store.getFolder(folderName);
			folder.open(Folder.READ_ONLY);
			Message[] msgs = folder.getMessages();
			return msgs;
		} catch (MessagingException e) {
			DebugLogger.logEvent(Mailer.class.getName(),Level.WARNING, e.getMessage());
		}

		return null;
	}

	/**
	 * Checks if a message has an attachment via multipart and mimebodypart
	 * 
	 * @param m
	 * @return if message has attachment
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

	/**
	 * Fetches the name of an attachment
	 * 
	 * @param m
	 * @return name of attachment
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
			DebugLogger.logEvent(Mailer.class.getName(),Level.WARNING, e.getMessage());
		} catch (IOException e) {
			DebugLogger.logEvent(Mailer.class.getName(),Level.WARNING, e.getMessage());
		}

		return "";
	}

	/**
	 * Gets content of email for sentiment analysis
	 * 
	 * @param message
	 * @return content of email
	 */
	public String getTextFromMessage(Message message) {
		String text = "";
		try {
			if (message.isMimeType("text/plain"))
				text = message.getContent().toString();
			else if (message.isMimeType("multipart/*"))
				text = getTextFromMimeMultipart((MimeMultipart) message.getContent());
		} catch (MessagingException e) {
			DebugLogger.logEvent(Mailer.class.getName(),Level.WARNING, e.getMessage());
		} catch (IOException e) {
			DebugLogger.logEvent(Mailer.class.getName(),Level.WARNING, e.getMessage());
		}
		return text;
	}

	/**
	 * Gets the content of email for sentiment analysis if MMP object
	 * 
	 * @param mmp
	 * @return email content from MimeMultipart object
	 * @see #getTextFromMessage
	 */
	private String getTextFromMimeMultipart(MimeMultipart mmp) {
		String text = "";
		try {
			for (int i = 0; i < mmp.getCount(); i++) {
				BodyPart bodyPart = mmp.getBodyPart(i);
				if (bodyPart.isMimeType("text/plain")) {
					text += "\n" + bodyPart.getContent();
					break;
				} else if (bodyPart.isMimeType("text/html"))
					text += "\n" + org.jsoup.Jsoup.parse((String) bodyPart.getContent()).text();
				else if (bodyPart.getContent() instanceof MimeMultipart)
					text += getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
			}
		} catch (MessagingException e) {
			DebugLogger.logEvent(Mailer.class.getName(),Level.WARNING, e.getMessage());
		} catch (IOException e) {
			DebugLogger.logEvent(Mailer.class.getName(),Level.WARNING, e.getMessage());
		}

		return text;
	}

}
