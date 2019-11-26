package edu.oswego.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import edu.oswego.debug.DebugLogger;
import edu.oswego.model.Email;
import edu.oswego.model.EmailAddress;
import edu.oswego.model.SentimentScore;
import edu.oswego.model.UserFolder;
import edu.oswego.sentiment.AnalyzeThis;
import edu.oswego.websocket.Messenger;

/**
 * Mailer class that has the Session/Store objects as well as host/port/tls
 * settings.
 * 
 * @author Jimmy Nguyen
 * @since 10/27/2019
 */

public class Mailer {

	private Session session;
	private Store storage;
	private String emailAddress, password;

	public List<UserFolder> importFolders() {
		List<UserFolder> folderList = new ArrayList<>();
		Folder[] folders;
		try {
			folders = getStorage().getDefaultFolder().list("*");
			for (Folder f : folders) {
				if (!f.getFullName().equals("[Gmail]") && !f.getFullName().equals("CSC480_19F")
						&& !f.getFullName().equals("[Gmail]/All Mail")) {
					folderList.add(new UserFolder(0, f));
				}
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		return folderList;
	}

	/**
	 * Creates a mailer object.
	 * 
	 * @param emailAddress
	 * @param password
	 */
	public Mailer(String emailAddress, String password) {
		this.emailAddress = emailAddress;
		this.password = password; // UNSAFE. Lets encrypt.
		DebugLogger.logEvent(Mailer.class.getName(), Level.WARNING,
				"Mailer object created. Information is in local storage stored unencrypted.");
	}

	/**
	 * Checks if a a connection can be made with the mailer credentials.
	 * 
	 * @return wehther a connection can be made or not.
	 */
	public boolean isConnected() {
		try {
			Properties props = new Properties();
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.auth", "true");
			Session session = Session.getInstance(props, null);
			Transport transport = session.getTransport("smtp");
			transport.connect("smtp.gmail.com", 587, emailAddress, password);
			transport.close();
			return true;
		} catch (AuthenticationFailedException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		return false;
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
				DebugLogger.logEvent(Mailer.class.getName(), Level.WARNING, e.getMessage());
			}
		}

		if (!storage.isConnected()) {
			try {
				// storage.connect(Settings.HOST, Settings.EMAIL_ADDRESS, Settings.EMAIL_PWD);
				storage.connect(Settings.HOST, emailAddress, password);
			} catch (MessagingException e) {
				DebugLogger.logEvent(Mailer.class.getName(), Level.WARNING, e.getMessage());
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
			DebugLogger.logEvent(Mailer.class.getName(), Level.WARNING, e.getMessage());
		}
		return folder;
	}

	public int getTotalEmailCount() {
		int sum = 0;
		try {
			Folder[] folders = getStorage().getDefaultFolder().list("*");
			for (Folder f : folders) {
				if (!f.getFullName().equals("[Gmail]") && !f.getFullName().equals("CSC480_19F")
						&& !f.getFullName().equals("[Gmail]/All Mail")) {
					sum += f.getMessageCount();
				}
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		return sum;
	}

	/**
	 * Moves emails that were processed/pulled in database to a new invisible folder
	 * called CSC480_19F (creates if not exists). Used for validation/checking.
	 * 
	 * @param originFolderName
	 * @param msgs
	 */
//	public void markEmailsInFolder(String originFolderName, Message[] msgs) { // TODO change this to javaxmail folder
//		// MAKE HIDDEN FOLDER... maybe subscribed?
//
//		if (msgs == null)
//			if (msgs.length == 0)
//				return;
//
//		System.out.println("MARKING: " + originFolderName + " :: " + msgs.length);
//
//		Folder folder = null;
//		try {
//			folder = getStorage().getFolder("CSC480_19F");
//
//			if (!folder.exists()) {
//				if (folder.create(Folder.HOLDS_MESSAGES)) {
//					folder.setSubscribed(true);
//					System.out.println("FOLDER MADE!");
//				}
//			}
//
//			folder.open(Folder.READ_WRITE);
//
//			Folder originFolder = getStorage().getFolder(originFolderName);
//			originFolder.open(Folder.READ_WRITE);
//			// MUST CHECK IF MESSAGE ALREADY EXISTS IN FOLDER OR NOT. ONLY COPY IF NOT. DID
//			// NOT DO YET.
//			if (msgs.length > 0)
//				originFolder.copyMessages(msgs, folder);
//			// originFolder.close();
//		} catch (MessagingException e) {
//			DebugLogger.logEvent(Mailer.class.getName(), Level.WARNING, e.getMessage());
//		}
//	}

	/**
	 * Gets all the emails from a folder
	 * 
	 * @param folderName
	 * @return Message array object
	 */
	public List<Email> pullEmails(javax.websocket.Session session, Messenger messenger, String folderName) {
		Store store = getStorage();
		List<Email> emails = new ArrayList<>();
		try {
			Folder folder = store.getFolder(folderName);
			folder.open(Folder.READ_ONLY);
			Message[] msgs = folder.getMessages();
			int totalMessages = msgs.length;
			int progress = 0;
			int counter = 0;
			for (Message m : msgs) {

				boolean damnBugs = false;
				List<EmailAddress> from = new ArrayList<>();
				for (Address a : m.getFrom()) {
					try {
						String[] temp = a.toString().split("<|>");
						String address = temp[temp.length - 1];
						from.add(new EmailAddress(0, address));
					} catch (Exception e) {
						System.out.println(a.toString());
						damnBugs = true;
					}
				}
				if (damnBugs) {
					progress++;
					continue;
				}
				Email e = new Email(m.getMessageNumber(), m.getReceivedDate(), m.getSubject(), m.getSize(),
						m.isSet(Flags.Flag.SEEN), hasAttachment(m),
						// Here is probably where we should calculate sentiment scores if anywhere
						// AnalyzeThis.evaluateSentiment(m.getContent().toString()), - Jim
						new SentimentScore(0, 0, 0, 0), 
						new UserFolder(0, m.getFolder()), 
						from);
				emails.add(e);
				progress++;
				if (counter == 15) {
					messenger.sendUpdateStatusMessage(session,
							"we have gathered " + progress + " out of " + totalMessages + " emails");
					counter = -1;
				}
				counter++;

			}

			messenger.sendUpdateStatusMessage(session, "we have gather all the emails");
			return emails;
		} catch (MessagingException e) {
			DebugLogger.logEvent(Mailer.class.getName(), Level.WARNING, e.getMessage());
			messenger.sendUpdateStatusMessage(session, "umm, their was a messagingException that was thrown....");
		}

		return null;
	}
	
	public List<Email> pullEmails(Messenger messenger, String folderName) {
		Store store = getStorage();
		List<Email> emails = new ArrayList<>();
		try {
			Folder folder = store.getFolder(folderName);
			folder.open(Folder.READ_ONLY);
			Message[] msgs = folder.getMessages();
			int totalMessages = msgs.length;
			int progress = 0;
			int counter = 0;
			for (Message m : msgs) {

				boolean damnBugs = false;
				List<EmailAddress> from = new ArrayList<>();
				for (Address a : m.getFrom()) {
					try {
						String[] temp = a.toString().split("<|>");
						String address = temp[temp.length - 1];
						from.add(new EmailAddress(0, address));
					} catch (Exception e) {
						System.out.println(a.toString());
						damnBugs = true;
					}
				}
				if (damnBugs) {
					progress++;
					continue;
				}
				Email e = new Email(m.getMessageNumber(), m.getReceivedDate(), m.getSubject(), m.getSize(),
						m.isSet(Flags.Flag.SEEN), hasAttachment(m),
						// Here is probably where we should calculate sentiment scores if anywhere
						// AnalyzeThis.evaluateSentiment(m.getContent().toString()), - Jim
						new SentimentScore(0, 0, 0, 0), 
						new UserFolder(0, m.getFolder()), 
						from);
				emails.add(e);
				progress++;
				if (counter == 15) {
					counter = -1;
				}
				counter++;

			}

			return emails;
		} catch (MessagingException e) {
			DebugLogger.logEvent(Mailer.class.getName(), Level.WARNING, e.getMessage());
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
			if (m.getContentType().contains("multipart") && m.getContent() instanceof Multipart) {
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
	 * @deprecated
	 */
	public String getAttachmentName(Message m) {
		try {
			Multipart multiPart = (Multipart) m.getContent();
			for (int i = 0; i < multiPart.getCount(); i++) {
				MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
				if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()))
					return part.getFileName();
			}
		} catch (MessagingException e) {
			DebugLogger.logEvent(Mailer.class.getName(), Level.WARNING, e.getMessage());
		} catch (IOException e) {
			DebugLogger.logEvent(Mailer.class.getName(), Level.WARNING, e.getMessage());
		} catch (Exception e) {
			return "ERR";
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
			DebugLogger.logEvent(Mailer.class.getName(), Level.WARNING, e.getMessage());
		} catch (IOException e) {
			DebugLogger.logEvent(Mailer.class.getName(), Level.WARNING, e.getMessage());
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
			DebugLogger.logEvent(Mailer.class.getName(), Level.WARNING, e.getMessage());
		} catch (IOException e) {
			DebugLogger.logEvent(Mailer.class.getName(), Level.WARNING, e.getMessage());
		}

		return text;
	}

	public void closeMailer() {
		try {
			storage.close();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

}
