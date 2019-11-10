package database;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Database;
import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;

/**
 * All test for receieved_email method
 * 
 * @author Jimmy Nguyen
 * @since 11/10/2019
 */

class ReceivedEmailTest {

	private Database db;

	@BeforeEach
	void setUp() throws Exception {
		Settings.loadCredentials();
		db = new Database(edu.oswego.mail.Settings.EMAIL_ADDRESS,
				new Mailer(edu.oswego.mail.Settings.EMAIL_ADDRESS, edu.oswego.mail.Settings.EMAIL_PWD));
	}

	@AfterEach
	void tearDown() throws Exception {
		db.truncateTables();
	}

	@Test
	void testReceivedEmailAddress() throws ClassNotFoundException, SQLException {
		db.query(new String[] { "INSERT INTO email (date_received) VALUE (CURDATE());",
				"INSERT INTO email_addr (email_address) VALUE ('poopsac@uranus.org')",
				"INSERT INTO received_email (email_id, email_addr_id) VALUE (1, 2)" });
		assertEquals(db.getRecipient(1).size(), 1);
	}

	@Test
	void testReceivedAllEmailAddress() {
		db.query(new String[] { "INSERT INTO email (date_received) VALUE (CURDATE());",
				"INSERT INTO email_addr (email_address) VALUE ('poopsac@uranus.org')",
				"INSERT INTO email_addr (email_address) VALUE ('upoop@ipoop.org')",
				"INSERT INTO email_addr (email_address) VALUE ('everybody@poops.org')",
				"INSERT INTO user_email (user_id, email_id) VALUE (1, 1)",
				"INSERT INTO received_email (email_id, email_addr_id) VALUE (1, 1)",
				"INSERT INTO received_email (email_id, email_addr_id) VALUE (1, 2)",
				"INSERT INTO received_email (email_id, email_addr_id) VALUE (1, 3)" });
		assertEquals(db.getAllRecipients().size(), 3);
	}

}
