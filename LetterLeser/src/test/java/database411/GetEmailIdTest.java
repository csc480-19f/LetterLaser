package database411;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;

/**
 * 29.1
 * @author nguyen
 *
 */

public class GetEmailIdTest {
	
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
	void getEmailId() {
		db.query("INSERT INTO folder ('fold_name') VALUE ('fake folder')");
		db.query("INSERT INTO email (date_received, subject, size, seen, has_attachment, file_name, folder_id)"
				+ "VALUE (CURDATE(), 'asd', 1, 1, 1, 'asdasd.txt', 1)");
		
		db.getEmailById(1);
	}

}
