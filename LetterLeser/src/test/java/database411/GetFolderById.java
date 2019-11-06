package database411;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.oswego.database.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;
import edu.oswego.model.UserFolder;

import java.sql.SQLException;

/**
 * USE THIS AS A TEMPLATE
 * 
 * @author nguyen
 * @deprecated
 */
class GetFolderById {

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
	void testFolderIdExists() throws SQLException, ClassNotFoundException {
		db.query("INSERT INTO folder (fold_name) VALUE ('testFolder');");
		UserFolder foldName = db.getFolderById(1);
		assertEquals(foldName.getId(), 1);
	}
	
	@Test
	void testFolderIdNoExists() throws SQLException, ClassNotFoundException {
		db.query("INSERT INTO folder (fold_name) VALUE ('testFolder');");
		UserFolder foldName = db.getFolderById(2);
		assertEquals(foldName, null);
	}
}
