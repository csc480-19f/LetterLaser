package edu.oswego.mail;

import edu.oswego.model.Email;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

	/**
	 * serializeFolder: Takes a List of emails and appends them to a file by the
	 * name [Email]-[Folder]. If no file by this name exists, then it is created.
	 * Assumes that the list contains emails of the same folder.
	 *
	 * @param user:
	 *            The email address of the current user
	 * @param emails:
	 *            The emails to be serialized
	 * @throws IOException
	 */
//	public void serializeFolder(String user, List<Email> emails) throws IOException {
//		File folder = new File("emails");
//		if (!folder.exists())
//			folder.mkdir();
//
//		String imapFolder = emails.get(0).getFolder();
//
//		File f = new File("emails/" + user + "-" + imapFolder);
//		if (!f.exists())
//			f.createNewFile();
//		OutputStream os = new FileOutputStream(f, true);
//		os.flush();
//
//		for (Email email : emails) {
//			byte[] bytes = email.toBytes();
//			os.write(bytes.length);
//			os.write(bytes);
//		}
//
//		os.close();
//	}

	/**
	 * deserializeFolder: deserializes and returns a list of emails from the given
	 * folder and given user email. Returns null if the specified folder doesn't
	 * exist within the filesystem.
	 *
	 * @param user:
	 *            the email address of the current user
	 * @param imapFolder:
	 *            the name of the folder to return
	 * @return a list of emails from the given folder
	 * @throws IOException
	 */
//	public List<Email> deserializeFolder(String user, String imapFolder) throws IOException {
//		File folder = new File("emails");
//		if (!folder.exists())
//			return null;
//		File f = new File("emails/" + user + "-" + imapFolder);
//		if (!f.exists())
//			return null;
//
//		List<Email> emails = new ArrayList<>();
//		InputStream is = new FileInputStream(f);
//		while (is.available() != 0) {
//			int length = is.read();
//			byte[] bytes = new byte[length];
//			is.read(bytes);
//			emails.add(new Email(bytes));
//		}
//		is.close();
//		return emails;
//	}
}
