package database411;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;

public class DatabaseInsertionTest {
	
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
	void testInsertUser() throws SQLException {
		db.query("INSERT INTO user (email_address) VALUE ('first@gmail.com');");
		int id = -1;
		
		ResultSet rs = db.getConnection().prepareStatement("SELECT id FROM user WHERE email_address = 'first@gmail.com';").executeQuery();
		while (rs.next())
			id = rs.getInt(1); // ID 1 is the edu.oswego.mail.Settings.EMAIL_ADDRESS. That's why we are 2.
		
		assertEquals(id, 2);
	}
	
	
	
}
