package database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import edu.oswego.database.Database;
import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;

/**
 * USE THIS AS A TEMPLATE
 * 
 * @author nguyen
 * @deprecated
 */
class DatabaseTemplateTest {

	private Database db;
	private Mailer mailer;

	@BeforeEach
	void setUp() throws Exception {
		Settings.loadCredentials();
		mailer = new Mailer(edu.oswego.mail.Settings.EMAIL_ADDRESS, edu.oswego.mail.Settings.EMAIL_PWD);
		db = new Database(edu.oswego.mail.Settings.EMAIL_ADDRESS, mailer);
	}

	@AfterEach
	void tearDown() throws Exception {
		db.truncateTables();
	}

}
