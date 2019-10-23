package edu.oswego.mail;

/**
 * Settings file for Mailer
 * 
 * @author Jimmy Nguyen
 * @since 10/23/2019
 *
 */
public class Settings {

	public static final String STORE_TYPE = "imaps";
	public static final String HOST = "imap.gmail.com";
	public static final int PORT = 995;
	public static final boolean TLS_ENABLED = true;

	private Settings() {} // Cannot instantiate an object
	
	// Will not need when we get authkey. This is for testing.
	public static String EMAIL_ADDRESS = "";
	public static String EMAIL_PWD = "";

}
