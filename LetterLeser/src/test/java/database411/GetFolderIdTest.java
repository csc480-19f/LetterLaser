package database411;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Database;
import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;
import edu.oswego.model.EmailAddress;

/**
 * USE THIS AS A TEMPLATE
 * 
 * @author nguyen
 * @deprecated
 */
class GetFolderIdTest {

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
	
	// 1.1
		@Test
		void testValidEmailAddress() {
			String email = "kleafasffaf@edu.com";
			db = new Database(email, mailer);
			EmailAddress emailAddress = db.getUser(email);
			
			assertEquals(emailAddress.getId(), 2);
		}
		
//		// Dis b broke | FRONT END
//		@Test
//		void testInvalidEmailAddress() {
//			String email = "sadasdasdas";
//			db = new Database(email, mailer);
//			EmailAddress emailAddress = db.getUser(email);
//			
//			assertEquals(emailAddress.getId(), 2);
//		}
		
		//1.1a Dis b broke too | FRONT END
//		@Test
//		void testInvalidEmailAddressA() {
//			String email = null;
//			db = new Database(email, mailer);
//			EmailAddress emailAddress = db.getUser(email);
//			
//			assertEquals(emailAddress.getId(), 2);
//		}
		
		//1.b
//		@Test
//		void testInvalidEmailAddressSize() {
//			String email = "asddd@edu.com";
//			db = new Database(email, mailer);
//			EmailAddress emailAddress = db.getUser(email);
//			
//			assertEquals(emailAddress.getId(), 2);
//		}
		
		// 10.1
		@Test
		void testFolderExists() {
			db.query("INSERT INTO folder (fold_name) VALUE ('popsac');");
			int id = db.getFolderId("popsac");
			assertEquals(id, 1);
		}
		
		@Test
		void testFolderNotExists() {
			db.query("INSERT INTO folder (fold_name) VALUE ('popsac');");
			int id = db.getFolderId("poopsac");
			assertEquals(id, -1);
		}

}
