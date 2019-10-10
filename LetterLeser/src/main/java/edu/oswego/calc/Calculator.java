package edu.oswego.calc;

import javax.mail.Folder;
import javax.mail.MessagingException;

import edu.oswego.database.Database;
import edu.oswego.mail.Mailer;
import edu.oswego.props.Settings;

public class Calculator {
	
	private static Mailer mailer;
	
	public static void main(String[] args) {
		Settings.loadCredentials();
//		System.out.println(needsUpdate("first@gmail.com"));
		Folder[] folders;
		mailer = new Mailer("");
		try {
			folders = mailer.getStorage().getDefaultFolder().list("*"); // lbls
			for (Folder f : folders) {
				if (!f.getFullName().equals("[Gmail]")) 
					System.out.println(f + " :: " + f.getMessageCount());
			}
			System.out.println("----------------------------------------------------------");
			folders = mailer.getStorage().getDefaultFolder().list("%"); // inbox
			for (Folder f : folders) {
				if (!f.getFullName().equals("[Gmail]")) 
					System.out.println(f + " :: " + f.getMessageCount());
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public static boolean needsUpdate(String emailAddress) {
		Database db = new Database(emailAddress, "");
		int validatedEmails = db.getValidatedEmails(emailAddress);
		Folder validationFolder = mailer.getFolder("CSC480_19f");
		try {
			int newEmails = validationFolder.getMessageCount();
			if (newEmails != validatedEmails) {
				System.out.println("The database emails and user's emails do not match.");
				System.out.println(newEmails + "_" + validatedEmails);
				return true;
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		return false;

	}

}
