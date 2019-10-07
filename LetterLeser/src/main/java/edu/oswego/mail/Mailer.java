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

	public static boolean hasAttachment(Message m) {
		try {
			if (m.getContentType().contains("multipart")) {
				Multipart multiPart = (Multipart) m.getContent();
				for (int i = 0; i < multiPart.getCount(); i++) {
					MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
					if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
						return true;
					}
				}
			}
		} catch (MessagingException | IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	public static String getAttachmentName(Message m) {
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
	

	// public static String processAttachment(Message m) {
	// String disposition;
	// try {
	// disposition = m.getDisposition();
	// if (hasAttachment(disposition) && disposition.equals(Part.ATTACHMENT)) {
	// System.out.println("This part is an attachment");
	// String fileName = m.getFileName();
	// System.out.println("The file name of this attachment is " + fileName);
	// return fileName;
	// }
	// } catch (MessagingException e) {
	// e.printStackTrace();
	// }
	//
	// return null;
	// }

	// SELECT * FROM filter_settings
	// JOIN folder
	// on folder.id=filter_settings.folder_id
	// WHERE folder.id= 'SOME_ID';
	//
	// INSERT INTO filter_settings(start_date, end_date, interval_range, folder.id)
	// VALUE ("CURDATE()", "CURDATE()", 7, 1);

	// SELECT * FROM user_favorites
	// JOIN user
	// on user.id=user_favorites.user_id
	// WHERE user.id='SOME_ID';
	//
	// INSERT INTO user_favorites (user_id, fav_name, filter_settings_id) VALUE
	// ('SOME_USER_ID', 'FAVNAME' 'SOME_FILTER_SETTINGS_ID');

}
