package edu.oswego.database;

import javax.mail.MessagingException;

import edu.oswego.mail.Mailer;
import edu.oswego.runnables.ConnectionRunnable;

/**
 * Test class demonstrating some database functionality.
 *
 * @author nguyen
 * @since 10/29/2019
 * @deprecated
 */
public class DBdemo {

	public static void main(String[] args) throws MessagingException {
		Thread ct = new Thread(new ConnectionRunnable());
		ct.start();
		
//		everythingWorks();
	}
	
	private static void everythingWorks() {
		long ct = System.currentTimeMillis();
		Settings.loadCredentials();
		Mailer mailer = new Mailer(edu.oswego.mail.Settings.EMAIL_ADDRESS, edu.oswego.mail.Settings.EMAIL_PWD);
		Database db = new Database(edu.oswego.mail.Settings.EMAIL_ADDRESS, mailer);

		System.out.println(db.getValidatedEmails());
		// db.truncateTables();
		db.pull();
		db.showConnections();


		db.showTables();
		double time = (double) ((System.currentTimeMillis() - ct) * .001);
		System.out.println("Total runtime: " + time + " seconds\n");

		db.truncateTables();
	}

}