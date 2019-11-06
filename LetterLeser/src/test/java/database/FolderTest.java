package database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Database;
import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;
import edu.oswego.model.UserFolder;

/**
 * All test for folders go here.
 * @author Jimmy Nguyen
 * @since 11/06/2019
 */

public class FolderTest {
	
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
	void testInsertFolder() throws ClassNotFoundException, SQLException {
		db.query("INSERT INTO folder (fold_name) VALUE ('Bald Rat');");
		int id = db.getFolderId("Bald Rat");
		UserFolder folder = db.getFolderById(id);

		assertEquals(folder == null, false);
	}
	
	@Test
	void testInsertFolderFail() throws ClassNotFoundException, SQLException {
		db.query("INSERT INTO folder (fold_name) VALUE (153);");
		int id = db.getFolderId("Bald Rat");
		UserFolder folder = db.getFolderById(id);

		assertEquals(folder == null, true);
	}
	
	@Test
	void testGetFolder() throws ClassNotFoundException, SQLException {
		String folderName = "Kirstan is Evil";
		db.query("INSERT INTO folder (fold_name) VALUE ('" + folderName + "');");
		int id = db.getFolderId(folderName);
		UserFolder folder = db.getFolderById(id);
		
		assertEquals(folder == null, false);
	}
	
	@Test
	void testGetFolderFail() throws ClassNotFoundException, SQLException {
		String folderName = "Kirstan is Evil";
		db.query("INSERT INTO folder (fold_name) VALUE ('" + folderName + "');");
		int id = db.getFolderId("Kirstan hates tanks");
		UserFolder folder = db.getFolderById(id);
		
		assertEquals(folder == null, true);
	}
	
	
	


}
