package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;

import JavaMail.Mailer;
import edu.oswego.model.Label;
import edu.oswego.model.UserFavourites;
import edu.oswego.model.UserFolder;
import Settings;
import SentimentAnalyzer.SentimentScore;

/**
 * Database class to get connection, push/pull data, and submit queries.
 * 
 * @author Jimmy
 * @since 10/04/2019
 */

public class Database {

	private static Connection connection;
	private static List<Address> addrList = new ArrayList<>();
	private static List<UserFolder> folderList = new ArrayList<>();

	
	// WIP
	// SQL DATE
//	private static void getEmailByFilters(String email, String folder, String label, boolean byAttachment,
//			boolean bySeen, Date startDate, Date endDate, int interval) {
//		if (startDate != null || endDate != null) { // same with label
//			// concat dates into query
//		}
//	}

	/*
	 * Singleton-style connection fetch method.
	 * 
	 * @return a MySQL JDBC Connection object
	 */
	public static Connection getConnection() {
		try {
			if (connection == null || connection.isClosed())
				connection = DriverManager.getConnection("jdbc:mysql://" + Settings.DATABASE_HOST + ":"
						+ Settings.DATABASE_PORT + "/" + Settings.DATABASE_SCHEMA
						+ "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&user="
						+ Settings.DATABASE_USERNAME + "&password=" + Settings.DATABASE_PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return connection;
	}

	/*
	 * Displays all INBOX folder messages through console output.
	 */
	public static void showTables() {
		long ct = System.currentTimeMillis();
		ResultSet queryTbl;
		try {
			// show all tables
			queryTbl = getConnection().prepareStatement("show tables").executeQuery();

			while (queryTbl.next()) {
				String tbl = queryTbl.getString(1);
				System.out.println("[INBOX] Table: " + tbl + "\n-------------------");

				// show all attributes from the tables
				ResultSet queryAttr = Database.getConnection().prepareStatement("select * from " + tbl).executeQuery();
				while (queryAttr.next()) {
					ResultSetMetaData md = queryAttr.getMetaData();
					for (int i = 1; i < md.getColumnCount() + 1; i++)
						System.out.println(
								md.getColumnName(i) + "_" + md.getColumnTypeName(i) + " :: " + queryAttr.getString(i));
					System.out.println();
				}
				System.out.println();
				queryAttr.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("Total runtime: " + (System.currentTimeMillis() - ct) + " ms\n");

	}

	/*
	 * Creates a query on database. Should mostly be used for insertions or updates.
	 * Does not return pingback value.
	 * 
	 * @param query statement
	 */
	public static void query(String statement) {
		PreparedStatement ps;
		try {
			ps = getConnection().prepareStatement(statement);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Truncates all tables in the Schema.
	 * 
	 * @see truncateTable method
	 */
	public static void truncateTables() {
		for (String tbl : Settings.DATABASE_TABLES)
			truncateTable(tbl);
	}

	/*
	 * Truncates one table in the Schema.
	 * 
	 * @param table name
	 */
	public static void truncateTable(String table) {
		PreparedStatement ps;
		try {
			ps = getConnection().prepareStatement("TRUNCATE TABLE " + table + ";");
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Pulls all emails from IMAP server and separates meta-data
	 */
	public static void pull() {
		Message[] msgs = Mailer.pullEmails("INBOX");
		for (Message m : msgs) {
			try {
				insertEmailAddress(m.getFrom());
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Inserts into email_addr. Removes duplicates with addrList.
	 * 
	 * @param Array of address objects from Message.getFrom()
	 */
	private static void insertEmailAddress(Address[] addresses) {
		for (int i = 0; i < addresses.length; i++) {
			PreparedStatement ps;
			try {
				if (!addrList.contains(addresses[i])) {
					addrList.add(addresses[i]);
					ps = getConnection()
							.prepareStatement("INSERT INTO email_addr (email_address) VALUE ('" + addresses[i] + "');");
					ps.execute();
					// NEED LIST OF DEEZ
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * For testing/debug purposes via Database Team.
	 */
	public static void insertDummyData() {

		String[] dummyStatements = { "INSERT INTO user (email_address) VALUE ('first@gmail.com');",
				"INSERT INTO user (email_address) VALUE ('second@gmail.com');",
				"INSERT INTO folder (fold_name) VALUE ('poop_sac');",
				"INSERT INTO filter_settings (fav_name, start_date, end_date, interval_range, folder_id) VALUES ('FAV1', CURDATE(), CURDATE(), 69, 1), ('POFIA', CURDATE(), CURDATE(), 96, 1), ('MASTA', CURDATE(), CURDATE(), 55, 1);",
				"INSERT INTO user_favourites (user_id, filter_settings_id) VALUES (1, 1), (2, 2), (1, 3);",
				"UPDATE filter_settings SET fav_name = 'POPSAC_DATA_HERE_FRANK' WHERE fav_name = 'FAV1';" };

		PreparedStatement ps;
		try {
			for (String statement : dummyStatements) {
				ps = getConnection().prepareStatement(statement);
				ps.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// NEED INSERT FOLDER FROM DATABASE

	/*
	 * Fetches folder object (if already exists) or creates one.
	 * 
	 * @param id and name of folder
	 * 
	 * @return new Folder object if it doesn't exist.
	 */
	public static UserFolder getFolder(int id, String name) {
		for (UserFolder folder : folderList)
			if (folder.getId() == id)
				return folder;

		UserFolder fold = new UserFolder(id, name);
		folderList.add(fold);

		return fold;
	}

	public static List<Label> getLabels() {
		try {
			Folder[] f = Mailer.getStorage(Settings.EMAIL_ADDRESS, Settings.EMAIL_PWD).getDefaultFolder().list();
			for (Folder fd : f) {
				System.out.println(">> " + fd.getName());
				System.out.println(fd.getFolder("Alumni").exists());
			}
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Fetches all attributes of user_favourites table.
	 * 
	 * @param user email address to be queried.
	 * 
	 * @return List of UserFavourites objects.
	 */
	public static List<UserFavourites> fetchFavourites(String emailAddress) {
		List<UserFavourites> favsList = new ArrayList<>(); // HMMM CLASS LIST MAYBE?

		System.out.println("FETCHING FAVOURITES FOR :" + emailAddress + "\n----------------------");
		String query = "SELECT user_favourites.id, filter_settings.fav_name, filter_settings.start_date, filter_settings.end_date, filter_settings.interval_range, folder.id, folder.fold_name FROM user JOIN user_favourites ON user.id = user_favourites.user_id JOIN filter_settings ON user_favourites.filter_settings_id = filter_settings.id JOIN folder ON filter_settings.folder_id = folder.id WHERE email_address = '"
				+ emailAddress + "';";

		try {
			ResultSet rs = getConnection().prepareStatement(query).executeQuery();
			while (rs.next()) {
				ResultSetMetaData md = rs.getMetaData();

				// CHECK FOR FOLDER DUPLICATES.
				UserFolder folder = getFolder(rs.getInt(6), rs.getString(7));

				favsList.add(new UserFavourites(1, rs.getString(2), rs.getDate(3), rs.getDate(4), 5, folder));

				// SHOWS ALL ATTR FOR CONSOLE PURPOSE
				for (int i = 1; i < md.getColumnCount() + 1; i++) {
					System.out.println(md.getColumnName(i) + "_" + md.getColumnTypeName(i) + " :: " + rs.getString(i)); // create
				}
				System.out.println("....................");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return favsList;
	}

	/*
	 * Calculates and inserts sentiment score.
	 * 
	 * @param email id in database table and SentimentScore object.
	 * 
	 * @see insertSentimentScore method
	 */
	public static void calculateSentimentScore(int emailId, SentimentScore score) {
		int sentimentId = insertSentimentScore(score);

		if (sentimentId == -1) {
			System.out.println("ERROR HAS OCCURED CALCULATING SENTIMENT SCORE");
			return;
		}

		insertSentimentScoreIntoEmail(emailId, sentimentId);
	}

	/*
	 * Inserts sentiment score into the database.
	 * 
	 * @param SentimentScore object (pos, neg, neu, cmp)
	 * 
	 * @return id attribute from sentiment_score table of inserted object
	 */
	private static int insertSentimentScore(SentimentScore score) {
		try {
			PreparedStatement ps = getConnection()
					.prepareStatement("INSERT INTO sentiment_score (positive, negative, neutral, compound) VALUE ("
							+ score.getPositive() + ", " + score.getNegative() + ", " + score.getNeutral() + ", "
							+ score.getCompound() + ");", Statement.RETURN_GENERATED_KEYS);
			if (ps.executeUpdate() == 1) { // AFFECTED ROW
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next())
					return rs.getInt(1);
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/*
	 * Links sentiment score to an email via Foreign Key UPDATE.
	 * 
	 * @param email id and sentiment score id from database table
	 */
	private static void insertSentimentScoreIntoEmail(int emailId, int sentimentScoreId) {
		try {
			PreparedStatement pstmt = getConnection().prepareStatement(
					"UPDATE email SET sentiment_score_id = " + sentimentScoreId + " WHERE id = " + emailId + ";");
			pstmt.execute();
			System.out.println(sentimentScoreId);

			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// needs return list of emails
//	public static void getRecipientList(int emailId) {
//		String statement = "SELECT email_addr.email_address FROM recipient_list JOIN email_addr ON email_addr.id = recipient_list.email_addr_id WHERE email_id = '"
//				+ emailId + "';";
//		
//	}
//
//	public static void getEmail() {
//		String statement = "SELECT * FROM email WHERE"; // NEED TO HAVE another table for all emails linking to a userId
//	}

}
// http://makble.com/gradle-example-to-connect-to-mysql-with-jdbc-in-eclipse