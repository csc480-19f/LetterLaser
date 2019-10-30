package mail;

import static org.junit.Assert.assertEquals;

import javax.mail.Folder;
import javax.mail.MessagingException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;

public class MailTest {
	
	private Mailer mailer;
	
	@BeforeEach
	void setUp() throws Exception {
		Settings.loadCredentials();
	 	mailer = new Mailer(edu.oswego.mail.Settings.EMAIL_ADDRESS, edu.oswego.mail.Settings.EMAIL_PWD);
	}

	@AfterEach
	void tearDown() throws Exception {
//		mailer.getStorage().close();
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
		assertEquals(num, 13692);
	}

}
