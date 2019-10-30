package mail;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;

public class MailTest {
	
	Mailer mailer;
	
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

}
