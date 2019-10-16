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
		props.put("mail.imap.host", "imap.gmail.com");
		props.put("mail.imap.port", "993");
//		props.put("mail.imap.auth", "true");
//		props.put("mail.imap.starttls.enable", "true"); //TLS
		props.put("mail.imap.ssl.enable", "true");
		props.put("mail.imap.auth.mechanisms", "XOAUTH2");
//		props.put("mail.smtp.ssl.enable", "true");
		
//		Session session = Session.getInstance(props,
//                new javax.mail.Authenticator() {
//                    protected PasswordAuthentication getPasswordAuthentication() {
//                        return new PasswordAuthentication("csc344testacc@gmail.com", "ya29.ImCbB2mNL2dXDGicFe-GAydoDfbMrelbnqRXOAelTt0PZV6GnkxkIOlj5oHVxXrCsFKddg2Bg7_NkxSYoh4ED20xNV8lzOBDF3LTzbakwwToV-Fu028yUXivcBL3-7drCIs");
//                    }
//                });
		Session session = Session.getInstance(props);
         

         try {
        	 Store s = session.getStore("imap");
        	 s.connect("imap.gmail.com", "csc344testacc@gmail.com", "ya29.ImGbB3ta1oTLhip9ML6ABlXuy5cD74nM9Gj1Ze9vMXIRdwYZKUrpw7e55fNqhVuohypgDu8EUGtB7V33S4eb3E3c7Tq58u0Dxt-yd2uKz_BGqVwdF3tztGSVClheeq94WUls");
//        	 s.connect();
			System.out.println("Done:: " + s.isConnected());
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//			store.connect("smtp.gmail.com", "csc344testacc@gmail.com", "ya29.ImCbB2mNL2dXDGicFe-GAydoDfbMrelbnqRXOAelTt0PZV6GnkxkIOlj5oHVxXrCsFKddg2Bg7_NkxSYoh4ED20xNV8lzOBDF3LTzbakwwToV-Fu028yUXivcBL3-7drCIs");
 catch (MessagingException e) {
			// TODO Auto-generated catch block
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
