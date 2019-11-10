package database;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Database;
import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;
import edu.oswego.model.UserFavourites;

/**
 * USE THIS AS A TEMPLATE
 * 
 * @author nguyen
 */
class FilterSettingsTest {

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
	void testFilterInsert() {
		db.query("INSERT INTO folder (fold_name) VALUE ('poopsac');");
		db.insertFilter(new Date(), new Date(), "WEEK", true, true, 1);
		db.query("INSERT INTO user_favourites (fav_name, user_id, filter_settings_id) VALUE ('Bastian is op. OW sucks', " + db.getUser().getId() + ", 1);");
		List<UserFavourites> favList = db.getUserFavourites();
		
		assertEquals(favList.size(), 1);
	}
	
	@Test
	void testGetEmailByFilter() {
		String dateUno = "2010-10-10 00:00:00";
		String dateBetween = "'2010-11-09 00:00:00'";
		String dateDos = "2011-11-11 00:00:00";
		db.query("INSERT INTO folder (fold_name) VALUE ('poopsac')");
		db.query("INSERT INTO email (has_attachment, date_received, seen, folder_id) VALUE (1, " + dateBetween + ", 1, 1)");
		db.query("INSERT INTO email (has_attachment, date_received, seen, folder_id) VALUE (1, " + dateBetween + ", 1, 1)");
		db.query("INSERT INTO email (has_attachment, date_received, seen, folder_id) VALUE (1, " + dateBetween + ", 1, 1)");
		db.query("INSERT INTO user_email (user_id, email_id) VALUES (1, 1), (1, 2), (1, 3);");

		assertEquals(db.getEmailByFilter(true, dateUno, dateDos, true, "poopsac").size(), 3);
	}

}
