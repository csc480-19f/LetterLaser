package database411;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;
import edu.oswego.props.Interval;

public class DatabaseDeletionTest {

	private Database db;

	@BeforeEach
	void setUp() throws Exception {
		Settings.loadCredentials();
		db = new Database(edu.oswego.mail.Settings.EMAIL_ADDRESS, new Mailer(edu.oswego.mail.Settings.EMAIL_ADDRESS, edu.oswego.mail.Settings.EMAIL_PWD));
	}

	@AfterEach
	void tearDown() throws Exception {
		db.truncateTables();
	}
	
	// 10.2
	@Test
	void testUserFavouriteRemoval() throws ParseException {
		db.query("INSERT INTO folder (fold_name) VALUES ('Apple Developer'), ('INBOX'), ('Misc/UUP/CELT'), ('[Gmail]/Sent Mail');");
		Date utilDate = (Date) new SimpleDateFormat("yyyy-MM-dd").parse("2015-03-14");
		db.insertUserFavourites("Awesome favs", utilDate, utilDate, Interval.WEEK, false, false, "Apple Developer");
		db.insertUserFavourites("Crappy favs", utilDate, utilDate, Interval.YEAR, true, true, "INBOX");
		db.insertUserFavourites("Mediocre favs", utilDate, utilDate, Interval.MONTH, false, true, "Misc/UUP/CELT");
		db.insertUserFavourites("Jimmys favs", utilDate, utilDate, Interval.WEEK, true, false, "[Gmail]/Sent Mail");
		db.removeUserFavourite("Jimmys favs");
		
		assertEquals(db.getUserFavourites().size(), 3);
	}

}
