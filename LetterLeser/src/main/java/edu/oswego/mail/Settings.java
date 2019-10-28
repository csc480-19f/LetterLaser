package edu.oswego.mail;

/**
 * Settings file for Mailer
 * 
 * @author Jimmy Nguyen
 * @since 10/28/2019
 *
 */
@SuppressWarnings("ALL")
public class Settings {

	public static final String STORE_TYPE = "imaps";
	public static final String HOST = "imap.gmail.com";
	public static final int PORT = 995;
	public static final boolean TLS_ENABLED = true;

	private Settings() {} // Cannot instantiate an object
	
}
