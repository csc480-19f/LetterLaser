package mail;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.mail.Folder;
import javax.mail.MessagingException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.mail.Mailer;
import edu.oswego.mail.Settings;
import edu.oswego.model.Email;
import edu.oswego.websocket.Messenger;

/**
 * All test for mailer methods go here.
 * 
 * @author Jimmy Nguyen
 * @since 11/10/2019
 */

public class MailTest {
	
	private Mailer mailer;
	
	@BeforeEach
	void setUp() throws Exception {
		Settings.loadCredentials();
	 	mailer = new Mailer(edu.oswego.mail.Settings.EMAIL_ADDRESS, edu.oswego.mail.Settings.EMAIL_PWD);
	}

	@AfterEach
	void tearDown() throws Exception {
		mailer.getStorage().close();
	}
	
	@Test
	void testConnectionTrue() {
		assertEquals(mailer.isConnected(), true);
	}
	
	@Test
	void testConnectionFalse() {
		mailer = new Mailer("fake@gmail.com", "fakepassword");
		assertEquals(mailer.isConnected(), false);
	}
	
	@Test 
	void testFolderExists() throws MessagingException {
		Folder folder = mailer.getFolder("CSC480_19F");
		assertEquals(folder.exists(), true);
	}
	
	@Test 
	void testFolderNotExists() throws MessagingException {
		Folder folder = mailer.getFolder("FAKE");
		assertEquals(folder.exists(), false);
	}
	
	@Test
	void testTotalEmailCount() {
		int num = mailer.getTotalEmailCount();
//		System.out.println(mailer.getTotalEmailCount());
		assertEquals(num, 13706);
	}
	
	@Test
	void testPull() {
		List<Email> eList = mailer.pullEmails(new Messenger(), "INBOX");
		System.out.println(eList);
		assertEquals(eList.size() > 0, true);
	}

}
