package database;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.oswego.database.Database;
import edu.oswego.database.Settings;
import edu.oswego.mail.Mailer;
import edu.oswego.model.SentimentScore;

/**
 * USE THIS AS A TEMPLATE
 * 
 * @author nguyen
 */
class SentimentScoreTest {

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
	void insertSentimentTest() {
		SentimentScore score = new SentimentScore(2.0, 3.0, 4.0, 5.0);
		assertEquals(db.insertSentimentScore(score), 1);
	}
	
//	Needs fixing -- need a getsentimentscorebyid method
//	@Test
//	void insertSentimentIntoEmailTest() {
//		SentimentScore score = new SentimentScore(2.0, 3.0, 4.0, 5.0);
//		db.insertSentimentScore(score);
//		db.query("INSERT INTO email (subject) VALUE ('hello world');");
//		db.insertSentimentScoreIntoEmail(1, 1);
//		assertEquals(db.getEmailById(1).getSentimentScore().getCompound() == 5.0, true);
//	}

}
