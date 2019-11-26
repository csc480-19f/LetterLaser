package edu.oswego.mail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Settings {

	public static final String STORE_TYPE = "imaps";
	public static final String HOST = "imap.gmail.com";
	public static final int PORT = 995;
	public static final boolean TLS_ENABLED = true;
	public static String EMAIL_ADDRESS = "";
	public static String EMAIL_PWD = "";

	private Settings() {
	} // Cannot instantiate an object

	public static void loadCredentials() {
		try {
			Scanner scanner = new Scanner(new File("credentials.txt"));

			scanner.nextLine();
			scanner.nextLine();
			scanner.nextLine();
			scanner.nextLine();
			scanner.nextLine();
			edu.oswego.mail.Settings.EMAIL_ADDRESS = scanner.nextLine();
			edu.oswego.mail.Settings.EMAIL_PWD = scanner.nextLine();

			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
