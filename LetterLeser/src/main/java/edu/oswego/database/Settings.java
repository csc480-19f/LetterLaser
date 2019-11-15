package edu.oswego.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;

import edu.oswego.debug.DebugLogger;

/**
 * Simple static class to access credentials from other classes.
 * 
 * @author Jimmy Nguyen
 * @since 11/08/2019
 */

public class Settings {

	public static String DATABASE_SCHEMA = "sql9311315";
	public static String DATABASE_USERNAME = "sql9311315";
	public static String DATABASE_PASSWORD = "fdlIwEmj4T";
	public static String DATABASE_HOST = "52.5.226.201";
	public static String DATABASE_PORT = "3306";
	public static int THRESHOLD_CONNECTION = 100;

	public static final String[] DATABASE_TABLES = new String[] { "email", "email_addr", "filter_settings", "folder",
			"received_email", "user", "user_email", "user_favourites", "sentiment_score" };
	
	private Settings() {} // Cannot instantiate an object

	/**
	 * Only for testing purposes
	 * @deprecated
	 */
	public static void loadCredentials() {
		try {
			Scanner scanner = new Scanner(new File("credentials.txt"));

			DATABASE_SCHEMA = scanner.nextLine();
			DATABASE_USERNAME = scanner.nextLine();
			DATABASE_PASSWORD = scanner.nextLine();
			DATABASE_HOST = scanner.nextLine();
			DATABASE_PORT = scanner.nextLine();
			edu.oswego.mail.Settings.EMAIL_ADDRESS = scanner.nextLine();
			edu.oswego.mail.Settings.EMAIL_PWD = scanner.nextLine();

			scanner.close();
			DebugLogger.logEvent(Settings.class.getName(),Level.INFO, "Credentials loaded from local file.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}