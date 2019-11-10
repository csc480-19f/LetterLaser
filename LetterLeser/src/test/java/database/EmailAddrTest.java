package database;

import edu.oswego.database.Database;

import static org.junit.Assert.assertEquals;

import java.sql.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;
import edu.oswego.model.Email;
import edu.oswego.model.EmailAddress;

/**
 * USE THIS AS A TEMPLATE
 * 
 * @author nguyen
 */
class EmailAddrTest {

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
	void testGetReceivedEmail() {
		db.query("INSERT INTO email (subject) VALUES "
				+ "('sub1');");
		db.query("INSERT INTO email_addr (email_address) VALUES "
				+ "('test@mail'),"
				+ "('test2@mail'),"
				+ "('test3@mail');");
		db.query("INSERT INTO received_email (email_id, email_addr_id) VALUES "
				+ "(1, 1),"
				+ "(1, 2),"
				+ "(1, 3);");
		
		
		List<EmailAddress> emailListTest = db.getReceivedEmail(1);
		assertEquals(emailListTest.size(), 3);
	}
	
	@Test
	void testGetReceivedEmailFail() {
		List<EmailAddress> emailListTest = db.getReceivedEmail(1);
		assertEquals(emailListTest.size(), 0);
	}
	
	@Test
	void testEmailExisterinoPistachinoCappucino() {
		db.query("INSERT INTO email_addr (email_address) VALUE ('testerino@mail.com');");
		String testMail = "testerino@mail.com";
		assertEquals(db.emailAddressExists(testMail), true);
	}
	
	@Test
	void testEmailNoExisterinoPistachinoCappucino() {
		db.query("INSERT INTO email_addr (email_address) VALUE ('testerino@mail.com');");
		String testMail = "IAMNOTHERE@mail.com";
		assertEquals(db.emailAddressExists(testMail), false);
	}
	
	@Test
	void testParseAddress() {
		String testParse = db.parseAddress("test'123@gmail.com");
		assertEquals(testParse.equals("test`123@gmail.com"), true);
	}
	
	@Test
	void testParseAddressFail() {
		String testParse = db.parseAddress("test'123@gmail.com");
		assertEquals(testParse.equals("test'123@gmail.com"), false);
	}
	
	
	@Test
	void testReceivedEmailExists() {
		db.query("INSERT INTO email (subject) VALUES "
				+ "('sub1');");
		db.query("INSERT INTO email_addr (email_address) VALUES "
				+ "('test@mail'),"
				+ "('test2@mail'),"
				+ "('test3@mail');");
		db.query("INSERT INTO received_email (email_id, email_addr_id) VALUES "
				+ "(1, 1),"
				+ "(1, 2),"
				+ "(1, 3);");
		assertEquals(db.receivedEmailExists(1, 1), true);
	}
	
	@Test
	void testReceivedEmailNoExists() {
		db.query("INSERT INTO email (subject) VALUES "
				+ "('sub1');");
		db.query("INSERT INTO email_addr (email_address) VALUES "
				+ "('test@mail'),"
				+ "('test2@mail'),"
				+ "('test3@mail');");
		db.query("INSERT INTO received_email (email_id, email_addr_id) VALUES "
				+ "(1, 1),"
				+ "(1, 2),"
				+ "(1, 3);");
		assertEquals(db.receivedEmailExists(2, 1), false);
	}
}
