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
import edu.oswego.model.Email;
import edu.oswego.model.UserFolder;

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
	void testInsertUser() throws SQLException, ClassNotFoundException {
		db.query("INSERT INTO user (email_address) VALUE ('second@gmail.com');");
		int id = -1;

		ResultSet rs = db.getConnection().prepareStatement("SELECT id FROM user WHERE email_address = 'second@gmail.com';").executeQuery();
		while (rs.next())
			id = rs.getInt(1);

		rs.close();
		assertEquals(id, 2);
	}

	@Test
	void testInsertFolder() throws ClassNotFoundException, SQLException {
		db.query("INSERT INTO folder (fold_name) VALUE ('Bald Rat');");
		int id = db.getFolderId("Bald Rat");
		UserFolder folder = db.getFolderById(id);

		assertEquals(folder == null, false);
	}

	@Test
	void testInsertEmail() throws ClassNotFoundException, SQLException {
		db.query("INSERT INTO email (date_received) VALUE (CURDATE());");
		Email e = db.getEmailById(1);
		assertEquals(e == null, false);
	}

}
