package database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Database;
import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;
import edu.oswego.model.EmailAddress;

/**
 * All test for users go here.
 * 
 * @author Jimmy Nguyen
 * @since 11/06/2019
 */

class UserTest {

	private Database db;
	private Mailer mailer;

	@BeforeEach
	void setUp() throws Exception {
		Settings.loadCredentials();
		db = new Database(edu.oswego.mail.Settings.EMAIL_ADDRESS,
				new Mailer(edu.oswego.mail.Settings.EMAIL_ADDRESS, edu.oswego.mail.Settings.EMAIL_PWD));
	}

	@AfterEach
	void tearDown() throws Exception {
		db.truncateTables();
		db.closeConnection();
	}

	@Test
	void testInsertUser() throws SQLException, ClassNotFoundException {
		db.query("INSERT INTO user (email_address) VALUE ('second@gmail.com');");
		int id = -1;

		ResultSet rs = db.getConnection()
				.prepareStatement("SELECT id FROM user WHERE email_address = 'second@gmail.com';").executeQuery();
		while (rs.next())
			id = rs.getInt(1);

		rs.close();
		assertEquals(id, 2);
	}

	@Test
	void testInsertUserDefault() throws SQLException, ClassNotFoundException {
		EmailAddress ea = db.getUser(edu.oswego.mail.Settings.EMAIL_ADDRESS);
		assertEquals(ea == null, false);
	}
	
	@Test
	void testUserValidatedEmail() throws ClassNotFoundException, SQLException {
		int amt = 500;
		db.setValidatedEmailCount(amt);
		
		assertEquals(db.getValidatedEmails(), amt);
	}

}
