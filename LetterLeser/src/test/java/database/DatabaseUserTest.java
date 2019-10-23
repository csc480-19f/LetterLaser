package database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Database;
import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;
import edu.oswego.model.UserFavourites;
import edu.oswego.props.Interval;

class DatabaseUserTest {

	private Database db;
	private Mailer mailer;

	@BeforeEach
	void setUp() throws Exception {
		Settings.loadCredentials();
		mailer = new Mailer("AUTH_KEY_INSERTED_HERE");
		db = new Database("csc344testacc@gmail.com", mailer);
		db.truncateTables();
	}

	@AfterEach
	void tearDown() throws Exception {
//		db.truncateTables();
//		mailer.getStorage().close();
	}

	@Test
	void testInsertUser() {
		db.query(new String[] {"USE csc480_19f;", "INSERT INTO user (email_address) VALUE ('first@gmail.com');"});
		int id = -1;
		
		try {
			ResultSet rs = db.getConnection().prepareStatement("SELECT id FROM user WHERE email_address = 'first@gmail.com';").executeQuery();
			while (rs.next())
				id = rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		assertEquals(id, 1);
	}
	
	@Test
	void testFindUserFavourites() {
		try {
			db.query("INSERT INTO folder (fold_name) VALUES ('Apple Developer'), ('INBOX'), ('Misc/UUP/CELT'), ('[Gmail]/Sent Mail');");
			Date utilDate = (Date) new SimpleDateFormat("yyyy-MM-dd").parse("2015-03-14");
			db.insertUserFavourites("Awesome favs", utilDate, Interval.WEEK, false, false, "Apple Developer");
			db.insertUserFavourites("Crappy favs", utilDate, Interval.YEAR, true, true, "INBOX");
			db.insertUserFavourites("Mediocre favs", utilDate, Interval.MONTH, false, true, "Misc/UUP/CELT");
			db.insertUserFavourites("Jimmys favs", utilDate, Interval.WEEK, true, false, "[Gmail]/Sent Mail");
			
			db.removeUserFavourite("Jimmys favs");
			System.out.println("DB ADDEDS");
			
		} catch (ParseException e) {
			e.printStackTrace();
		}

		List<UserFavourites> favList = db.getUserFavourites();
		System.out.println(favList.size());
		
		assertEquals(favList.size(), 3);
	}

}
