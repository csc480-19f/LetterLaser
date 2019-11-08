package database;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Database;
import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;

/**
 * USE THIS AS A TEMPLATE
 * 
 * @author nguyen
 */
class UserEmailTest {

	private Database db;
	private Mailer mailer;

	@BeforeEach
	void setUp() throws Exception {
		Settings.loadCredentials();
		db = new Database(edu.oswego.mail.Settings.EMAIL_ADDRESS, new Mailer(edu.oswego.mail.Settings.EMAIL_ADDRESS, edu.oswego.mail.Settings.EMAIL_PWD));
	}

	@AfterEach
	void tearDown() throws Exception {
		db.truncateTables();
	}
	
	@Test
	void testInsertUserEmail() {
		db.query("INSERT INTO email (date_received) VALUE (CURDATE())"); 
		db.query("INSERT INTO user_email (user_id, email_id) VALUE (" + db.getUser().getId() + ", 1)");
		assertEquals(db.getUserEmails().isEmpty(), false);
	}
	
	@Test
	void testGetUserEmails() {
		db.query("INSERT INTO email (seen) VALUE (1)"); 
		db.query("INSERT INTO user_email (user_id, email_id) VALUE (" + db.getUser().getId() + ", 1)");
		assertEquals(db.getUserEmails().isEmpty(), false);
	}

}
