package database;

import java.sql.SQLException;

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
class ReceivedEmailTest {

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
	void testReceivedEmail() throws ClassNotFoundException, SQLException {
		db.query("INSERT INTO email (date_received) VALUE (CURDATE());");
		db.query("INSERT INTO email_addr (email_address) VALUE ('poopsac@uranus.org')");
		db.query("INSERT INTO received_email (email_id, email_addr_id) VALUE (1, 2)");
		
		//getRecipient
		//getAllRecipients
	}

}
