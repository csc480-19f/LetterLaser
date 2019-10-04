package JavaMail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;

/**
 * @deprecated Create a new demo class with more structure.
 *
 */

@Deprecated
public class MailDemo {

	// https://www.tutorialspoint.com/javamail_api/javamail_api_checking_emails.htm
	
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		System.out.print("Email: ");
		String username = input.nextLine();
		System.out.print("Password: ");
		String password = input.nextLine();
		
		Store store = Mailer.getStorage(username, password);
		
		try {
			readInboxAttributes(store);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//testTimeAlt(store);
		//testTime(store);
		
		try {
			store.close();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		System.out.println("\n\n\n\n\n\n\n");
		
	}

	private static void readInboxAttributes(Store store) throws IOException {
		try {
			Folder inboxFolder = store.getFolder("INBOX");
			inboxFolder.open(Folder.READ_ONLY);
			
			Message[] messages = inboxFolder.getMessages();
			
			// Just do attribute operation for ONE email
			for (Message m : messages) {
				System.out.println("---------------------------------");
				System.out.println(m.getContent().toString());
				System.out.println("Msg #: " + m.getMessageNumber());
				System.out.println("Content: " + m.getContentType());
				System.out.println("Description: " + m.getDescription());
				System.out.println("Disposition: " + m.getDisposition());
				System.out.println("File Name: " + m.getFileName());
				System.out.println("Line Count: " + m.getLineCount());
				System.out.println("Size: " + m.getSize());
				System.out.println("Subject: " + m.getSubject());
				System.out.println("Headers: " + m.getAllHeaders().toString());
				System.out.println("Recipients: " + m.getAllRecipients().toString());
				System.out.println("Data Handler: " + m.getDataHandler().toString());
				System.out.println("Flags: " + m.getFlags());
				System.out.println("From: " + m.getFrom().toString());
				System.out.println("Folder: " + m.getFolder());
				System.out.println("Attachment: " + m.ATTACHMENT);
				System.out.println("Date Received: " + m.getReceivedDate());
				System.out.println("Reply To: " + m.getReplyTo());
				System.out.println("Sent Date: " + m.getSentDate());
			}
			
			inboxFolder.close(false);
			
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	private static void testTime(Store store) {
		try {
			Folder inboxFolder = store.getFolder("INBOX");
			inboxFolder.open(Folder.READ_ONLY);
			
			Message[] messages = inboxFolder.getMessages();

			List<Integer> sizes = new ArrayList<>();
			List<Long> times = new ArrayList<>();
			long tb = System.currentTimeMillis();
			
			for (Message m : messages) {
				long current = System.currentTimeMillis();
				System.out.println("---------------------------------");
				System.out.println("Msg #: " + m.getMessageNumber());
				System.out.println("Size: " + m.getSize());
				long qTime = System.currentTimeMillis() - current;
				System.out.println("Time: " + qTime + " ms");
				sizes.add(m.getSize());
				times.add(qTime);
			}
			
			System.out.println("Time for " + messages.length + " email iteration (n): " + (System.currentTimeMillis() - tb) + " ms");
			int avgSize = 0;
			for (Integer i : sizes) {
				avgSize += i;
			}
			int avgTime = 0;
			for (Long i: times) {
				avgTime += i;
			}
			System.out.println("Average email size: " + (avgSize / sizes.size()) + " bytes");
			System.out.println("Average email time: " + (avgTime / times.size()) + " ms");
			
			inboxFolder.close(false);
			
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	// MIME MESSAGE REDUCE TIME?
	//https://stackoverflow.com/questions/20237801/reading-from-javamail-takes-a-long-time
	private static void testTimeAlt(Store store) {
		Folder inboxFolder;
		try {
			inboxFolder = store.getFolder("INBOX");
			inboxFolder.open(Folder.READ_ONLY);
			
			
			MimeMessage[] messages = (MimeMessage[]) inboxFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), true));
			for (int i = 0 ; i< messages.length ; i++){
			    MimeMessage cmsg = new MimeMessage(messages[i]);
			    Object obj = cmsg.getContent(); 
			    System.out.println(cmsg.getMessageNumber() + "_" + cmsg.getSubject().toString());
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
