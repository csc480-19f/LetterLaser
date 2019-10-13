package edu.oswego.database;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.oswego.props.Interval;
import edu.oswego.props.Settings;

public class DBdemo {

	public static void main(String[] args) {
		long ct = System.currentTimeMillis();
		Settings.loadCredentials();

		Database db = new Database("csc344testacc@gmail.com", "KEY_HERE");
		db.truncateTables();
		db.pull();
		
		
//		db.pull();
		
		String lastCrawlDate = "2014-01-28";
		Date utilDate;
		try {
			utilDate = (Date) new SimpleDateFormat("yyyy-MM-dd").parse(lastCrawlDate);
			db.insertUserFavourites("Awesome favs", utilDate, Interval.WEEK, false, false, "INBOX");
			db.insertUserFavourites("Poop sac favourites", utilDate, Interval.YEAR, true, true, "INBOX");
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		
//		db.removeUserFavourite("Awesome favs");
		
		
		db.showTables();
//		db.truncateTables();
		
		double time = (double) ((System.currentTimeMillis() - ct) * .001);
		System.out.println("Total runtime: " + time + " seconds\n");
		
//		Mailer.moveToMarkedFolder();
//		Database.insertDummyData(new String[]{
//				"USE csc480_19f;",
//				"INSERT INTO user (email_address) VALUE ('first@gmail.com'); ",
//				"INSERT INTO user (email_address) VALUE ('second@gmail.com'); ",
//				"INSERT INTO user (email_address) VALUE ('third@gmail.com'); ",
//				"INSERT INTO folder (fold_name) VALUE ('INBOX'); ",
//				"INSERT INTO folder (fold_name) VALUE ('SENT'); ",
//				"INSERT INTO folder (fold_name) VALUE ('TRASH'); ",
//				"INSERT INTO filter_settings (start_date, end_date, interval_range, folder_id) " +
//						"VALUES (CURDATE(), CURDATE(), 69, 1), (CURDATE(), CURDATE(), 96, 2), (CURDATE(), CURDATE(), 00, 3);",
//				" INSERT INTO user_favourites (fav_name, user_id, filter_settings_id)" +
//					"VALUES	"
//						+ "('Favourite uno', 1, 1),"
//						+ "('Favourite dos', 1, 2),"
//						+ "('Favourite tres', 2, 3),"
//						+ "('Favourite quatro', 3, 2); ",
//				"INSERT INTO email_addr (email_address)" +
//					"VALUES "
//					+ "('dan@gmail.com'), "
//					+ "('tekashi@gmail.com'), "
//					+ "('priy@gmail.com'); ",
//					"INSERT INTO sentiment_score (positive, negative, neutral, compound) " +
//						"VALUES"
//							+ "(.5, .5, .5, .5), "
//							+ "(.69, .69, .69, .69),"
//							+ "(.101, .101, .101, .101);",
//					"INSERT INTO email (date_received, subject, size, seen, has_attachment, file_name, sentiment_score_id, folder_id)" +
//						"VALUES "
//							+ "(CURDATE(), 'Where my cheese @?', 100, 1, 1, 'cheese.png', 1, 1), "
//							+ "(CURDATE(), 'Tekashi in da h0u$e', 200, 0, 0, null, 2, 2), "
//							+ "(CURDATE(), 'My Milkshake brings all the boise to...', 300, 0, 0, null, 3, 3);",
//					"INSERT INTO user_email (user_id, email_id)" +
//						"VALUES"
//							+ "(1, 1), "
//							+ "(2, 2), "
//							+ "(3, 3);",
//					"INSERT INTO received_email (email_id, email_addr_id)" +
//						"VALUES"
//							+ "(1, 1), "
//							+ "(2, 2), "
//							+ "(3, 3); ",
//					"INSERT INTO recipient_list (email_id, email_addr_id) " +
//						"VALUES"
//							+ "(1, 1), "
//							+ "(2, 2), "
//							+ "(3, 3); ",
//					"UPDATE user SET validated_emails = 10 WHERE id = 1"
//		});
	}

}