package edu.oswego.props;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Simple static class to access credentials from other classes.
 * 
 * @author Jimmy Nguyen
 * @since 10/08/2019
 */

public class Settings {

	public static String DATABASE_SCHEMA = ""; // should be final when we deploy. Only not final so we can load
												// credentials
	public static String DATABASE_USERNAME = "";
	public static String DATABASE_PASSWORD = "";
	public static String DATABASE_HOST = "";
	public static String DATABASE_PORT = "";

//	public static String EMAIL_ADDRESS = "";
//	public static String EMAIL_PWD = "";

	public static final String[] DATABASE_TABLES = new String[] { "email", "email_addr", "filter_settings", "folder",
			"received_email", "user", "user_email", "user_favourites", "sentiment_score" };
	
	private Settings() {
		// Cannot instantiate an object of settings.
	}

	/*
	 * We don't need this. Only for testing since it's a public repo.
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}