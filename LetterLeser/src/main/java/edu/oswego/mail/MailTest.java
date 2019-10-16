package edu.oswego.mail;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

public class MailTest {

	public static void main(String[] args) {
		try2();
		System.out.println("CONNECTING...");
	}

	public static void try2() {
		
		Properties props = new Properties();
		props.put("mail.imap.ssl.enable", "true"); // required for Gmail
		props.put("mail.imap.auth.mechanisms", "XOAUTH2");
		Session session = Session.getInstance(props);
		
		try {
			Store store = session.getStore("imap");
			store.connect("imap.gmail.com", "csc344testacc@gmail.com", "ya29.Il-bB-TKj79dWENPnBkZfq2VCHIbKk3lmNJvq-yq8HyZlsW2iuS12oJFVeJdsPn5Vz70qDlXuzZJ8MSonAr6JaEdhBUKSKQJYxbHozxday2tS8IWGkl31cYDh9-3av45Lg");
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public static void try1() {
		edu.oswego.props.Settings.loadCredentials();
		Properties properties = new Properties();
//		properties.put("mail.imap.host", Settings.HOST);
//		properties.put("mail.imap.port", Settings.PORT);
		properties.put("mail.imap.ssl.enable", "true");
//		properties.put("mail.imap.starttls.enable", Settings.TLS_ENABLED);
		properties.put("mail.imap.auth.mechanisms", "XOAUTH2");
		Session session = Session.getInstance(properties);
		Store store;
		try {
			store = session.getStore("imap");
			store.connect("imap.gmail.com", "csc344testacc@gmail.com", "ya29.Il-bB63OvgxJkLuJKYuzmo3S0sw-nD-uIrjh1I9AGQbkGvqRAk_9oclZTSuEpW80lIUQtEcUxwQdlFMLbVFDIdLYVIrm9oKR07fxgeD7cXcsKzR0YfFNJDWGBiwmnhZYJw");
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
}
