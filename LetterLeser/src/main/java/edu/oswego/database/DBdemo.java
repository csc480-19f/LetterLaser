package edu.oswego.database;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.mail.Folder;
import javax.mail.MessagingException;

import edu.oswego.mail.Mailer;
import edu.oswego.model.Email;
import edu.oswego.props.Interval;
import edu.oswego.props.Settings;
import edu.oswego.props.Time;

public class DBdemo {

	public static void main(String[] args) {
		long ct = System.currentTimeMillis();
		Settings.loadCredentials();
		
		Mailer mailer = new Mailer("AUTH_KEY_INSERTED_HERE");
		Database db = new Database("csc344testacc@gmail.com", mailer);
		
//		db.truncateTables();
		db.pull();

		try {
			Date utilDate = (Date) new SimpleDateFormat("yyyy-MM-dd").parse("2014-01-28");
			db.insertUserFavourites("Awesome favs", utilDate, Interval.WEEK, false, false, "Apple Developer");
			db.insertUserFavourites("Crappy favs", utilDate, Interval.YEAR, true, true, "INBOX");
			db.insertUserFavourites("Mediocre favs", utilDate, Interval.MONTH, false, true, "Misc/UUP/CELT");
			db.insertUserFavourites("Jimmys favs", utilDate, Interval.WEEK, true, false, "[Gmail]/Sent Mail");
//			db.removeUserFavourite("Awesome favs");
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}

		db.showTables();
		// db.truncateTables();
		
		String startDate = Time.parseDateTime(Time.getDate("2010-03-12"));
		String endDate = Time.parseDateTime(Time.getDate("2014-03-12"));
		List<Email> emailList = db.getEmailByFilter(null, startDate, endDate, false, "Apple Developer");
		
		double time = (double) ((System.currentTimeMillis() - ct) * .001);
		System.out.println("Total runtime: " + time + " seconds\n");
	}
	
	public static boolean needsUpdate(Database db, Mailer mailer) {
		int validatedEmails = db.getValidatedEmails();
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