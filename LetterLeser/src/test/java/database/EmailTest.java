package database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Database;
import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;
import edu.oswego.model.Email;

/**
 * All test for Emails go here.
 * 
 * @author Jimmy Nguyen
 * @since 11/06/2019
 */

class EmailTest {

	private Database db;
	private Mailer mailer;

	@BeforeEach
	void setUp() {
		Settings.loadCredentials();
//		Thread.currentThread().setName("regular");
		db = new Database(edu.oswego.mail.Settings.EMAIL_ADDRESS, new Mailer(edu.oswego.mail.Settings.EMAIL_ADDRESS, edu.oswego.mail.Settings.EMAIL_PWD));
	}

	@AfterEach
	void tearDown() {
		db.truncateTables();
	}

	@Test
	void testInsertEmail() {
		db.query("INSERT INTO email (date_received) VALUE (CURDATE());");
		Email e = db.getEmailById(1);
		assertEquals(e == null, false);
	}
	
	@Test
	void testGetEmail() {
		String date = "2019-10-10";
		db.query("INSERT INTO email (subject) VALUE ('bewbs');");
		Email e = db.getEmailById(1);
		assertEquals(e == null, false);
	}
	
	@Test
	void testGetAllEmail() {
		db.query("INSERT INTO email (subject) VALUES "
				+ "('bewbs2'), "
				+ "('YAP'), "
				+ "('y u no look me in the eye when we make love?!?');");
		db.query("INSERT INTO user_email (user_id, email_id) VALUES "
				+ "(" + db.getUser().getId() + ", 1), "
				+ "(" + db.getUser().getId() + ", 2), "
				+ "(" + db.getUser().getId() + ", 3);"
				);
		
		List<Email> emailList = db.getUserEmails();
		assertEquals(emailList.size(), 3);
	}
	
	@Test
	void testGetAllEmailNone() {
		List<Email> emailList = db.getUserEmails();
		assertEquals(emailList.size(), 0);
	}
	
	@Test
	void testEmailsExist() {
		db.query("INSERT INTO email (subject) VALUES ('bewbs2');");
		db.query("INSERT INTO user_email (user_id, email_id) VALUES (" + db.getUser().getId() + ", 1)");
		assertEquals(db.hasEmails(), true);
	}
	
	@Test
	void testEmailsNotExist() {
		assertEquals(db.hasEmails(), false);
	}

}
