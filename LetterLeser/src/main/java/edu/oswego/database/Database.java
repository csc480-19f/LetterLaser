package edu.oswego.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;

import SentimentAnalyzer.SentimentScore;
import edu.oswego.mail.Mailer;
import edu.oswego.model.EmailAddress;
import edu.oswego.model.Label;
import edu.oswego.model.MailFolder;
import edu.oswego.model.UserFolder;
import edu.oswego.props.Settings;

/**
 * Database class to get connection, push/pull data, and submit queries.
 * 
 * @author Jimmy
 * @since 10/04/2019
 */

public class Database {

	private static Connection connection;
	// private static List<Email> emailList = new ArrayList<>();
	private static List<UserFolder> folderList = new ArrayList<>();
	private static List<Integer> emailIdList = new ArrayList<>();
	private static List<Label> labelList = new ArrayList<>();

	public static void setValidatedEmailCount(String emailAddress, int count) {
		query("UPDATE user SET validated_emails = " + count + " WHERE email_address = '" + emailAddress + "';");
	}
	
	private static boolean folderExists(String folderName) {
		ResultSet queryTbl;
		int size = -1;
		try {
			queryTbl = getConnection()
					.prepareStatement("SELECT count(*) FROM folder WHERE fold_name = '" + folderName + "'")
					.executeQuery();
			while (queryTbl.next()) {
				size = queryTbl.getInt(1);
				break;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return size >= 1;
	}
	
	private static boolean labelExists(String labelName) {
		ResultSet queryTbl;
		int size = -1;
		try {
			queryTbl = getConnection()
					.prepareStatement("SELECT count(*) FROM label WHERE lb_name = '" + labelName + "'")
					.executeQuery();
			while (queryTbl.next()) {
				size = queryTbl.getInt(1);
				break;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return size >= 1;
	}

	private static boolean emailAddressExists(String emailAddress) {
		ResultSet queryTbl;
		int size = -1;
		try {
			queryTbl = getConnection()
					.prepareStatement("SELECT count(*) FROM email_addr WHERE email_address = '" + emailAddress + "'")
					.executeQuery();
			while (queryTbl.next()) {
				size = queryTbl.getInt(1);
				break;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return size >= 1;
	}
	
	public static void importFolders() {
		try {
			Folder[] folders = Mailer.getStorage().getDefaultFolder().list("%");
			
			for (Folder f : folders) {
				if (!folderExists(f.getFullName()) && !f.getFullName().equals("[Gmail]") && !f.getFullName().equals("CSC480_19F")) {
					PreparedStatement ps = getConnection().prepareStatement(
							"INSERT INTO folder (fold_name) VALUE ('" + f.getFullName() + "')",
							Statement.RETURN_GENERATED_KEYS);
					if (ps.executeUpdate() == 0)
						throw new SQLException("Could not insert into folder, no rows affected");

					ResultSet generatedKeys = ps.getGeneratedKeys();

					if (generatedKeys.next()) {
						folderList.add(new UserFolder(generatedKeys.getInt(1), f));
						System.out.println("ADDED FOLDER:\t" + f.getFullName());
					}
				}
			}

		} catch (SQLException | MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public static void importLabels() {
		try {
			Folder[] folders = Mailer.getStorage().getDefaultFolder().list("*");
			
			for (Folder f : folders) {
				if (f.getFullName().equals("[Gmail]"))
					break;
				else if (!labelExists(f.getFullName()) && !folderExists(f.getFullName()) && !f.getFullName().equals("CSC480_19F")) {
					PreparedStatement ps = getConnection().prepareStatement(
							"INSERT INTO label (lb_name) VALUE ('" + f.getFullName() + "')",
							Statement.RETURN_GENERATED_KEYS);
					if (ps.executeUpdate() == 0)
						throw new SQLException("Could not insert into folder, no rows affected");

					ResultSet generatedKeys = ps.getGeneratedKeys();

					if (generatedKeys.next()) {
						labelList.add(new Label(generatedKeys.getInt(1), f));
						System.out.println("ADDED LBL:\t" + f.getFullName());
					}
				}
			}

		} catch (SQLException | MessagingException e) {
			e.printStackTrace();
		}
	}

	private static int insertEmail(Message m) {
		// no duplicates in case in multiple folders? CHECK DATE RECEIVED AT SAME TIME
		// AND OTHER SHET?
		int emailId = -1;
		PreparedStatement ps;
		try {
			// ps = getConnection().prepareStatement("INSERT INTO email (date_received,
			// subject, size, seen, has_attachment, file_name folder_id) VALUE (?, ?, ?, ?,
			// ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
			ps = getConnection().prepareStatement(
					"INSERT INTO email (date_received, subject, size, seen, has_attachment, file_name, folder_id) VALUE (?, ?, ?, ?, ?, ?, ?);",
					Statement.RETURN_GENERATED_KEYS);
			ps.setObject(1, m.getReceivedDate());
			ps.setString(2, m.getSubject());
			ps.setDouble(3, m.getSize());
			ps.setBoolean(4, m.getFlags().contains(Flags.Flag.SEEN));

			boolean hasAttachment = Mailer.hasAttachment(m);
			ps.setBoolean(5, hasAttachment);

			String fileName = null;
			if (hasAttachment) {
				fileName = Mailer.getAttachmentName(m);
			}
			ps.setString(6, fileName);

			int folderId = -1;
			for (UserFolder f : folderList) {
				if (f.getFolder().getFullName().equals(m.getFolder().getFullName())) {
					folderId = f.getId();
					break;
				}
			}
			ps.setInt(7, folderId);

			if (ps.executeUpdate() == 0)
				throw new SQLException("Could not insert into folder, no rows affected");

			ResultSet generatedKeys = ps.getGeneratedKeys();
			if (generatedKeys.next()) {
				emailId = generatedKeys.getInt(1);
				emailIdList.add(emailId);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return emailId;
	}

	private static List<EmailAddress> insertEmailAddress(Address[] addresses) {
		List<EmailAddress> emailAddrList = new ArrayList<>();
		
		for (int i = 0; i < addresses.length; i++) {
			PreparedStatement ps;
			try {
				String address = "";
				if (addresses[i].toString().contains("<")) {
					String addrParser[] = addresses[i].toString().replace("'", "`").split("<");
					address = addrParser[1].replace(">", "");	// can also do substring but need to know index of <. Still iterating but less mem
				} else 
					address = addresses[i].toString();
				
				if (!emailAddressExists(address)) {
					ps = getConnection().prepareStatement("INSERT INTO email_addr (email_address) VALUE ('" + address + "');", Statement.RETURN_GENERATED_KEYS);

					if (ps.executeUpdate() == 0)
						throw new SQLException("Could not insert into folder, no rows affected");

					int emailAddressId = -1;
					ResultSet generatedKeys = ps.getGeneratedKeys();
					if (generatedKeys.next()) {
						emailAddressId = generatedKeys.getInt(1);
					}
					
					emailAddrList.add(new EmailAddress(emailAddressId, address));
				} else {
					ResultSet rs = getConnection().prepareStatement("SELECT * FROM email_addr WHERE email_address = '" + address + "';").executeQuery();
					while (rs.next())
						emailAddrList.add(new EmailAddress(rs.getInt(1), rs.getString(2)));
					rs.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return emailAddrList;
	}
	
	public static void pull() {
		// order matters. Folders must come first so labels don't import folders.
		importFolders(); // make function not static. Lets change this so pass as param to menthods so take list?
		importLabels();

		// take a folder as parent and do this for labels? since labels and folders don't cross one another.
		for (UserFolder f : folderList) {
			insertion(f);
		}
		
		for (Label l: labelList) {
			insertion(l);
		}
	}
	
	private static void insertion(MailFolder f) {
		int i = 0; 
		int stopper = 10;
		Message[] msgs = Mailer.pullEmails(f.getFolder().getFullName()); // Do not use "[Gmail]/All Mail");
		for (Message m : msgs) {
			try {
				List<EmailAddress> fromList = insertEmailAddress(m.getFrom());// get this list and return for user_email table
				int emailId = insertEmail(m);
				for (EmailAddress ea : fromList) {
					insertUserEmail(ea, emailId);
					insertReceivedEmails(emailId, ea.getId());
				}
				emailIdList.add(emailId);
				if (f instanceof Label)
					insertUserLabelList(emailId, m); // not working?
				
				i++;
				if (i > stopper)
					return;

			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
		
		// If not already in folder, copy it over.
		Mailer.markEmailsInFolder(f.getFolder().getFullName(), msgs);
	}
	
	// error here.
	private static void insertReceivedEmails(int emailId, int emailAddrId) {
		// if not already there. UGH GOTTA DO CHEKS 4 EVRYTHANG
		query("INSERT INTO received_email (email_id, email_addr_id) VALUE ('" + emailId + "', " + emailAddrId + ");");
	}

	// WIP
	private static void insertUserLabelList(int emailId, Message m) {
		for (Label l: labelList) {
			Folder f = Mailer.getFolder(l.getFolder().getFullName());
			try {
				f.open(Folder.READ_WRITE);
				if (Arrays.asList(f.getMessages()).contains(m)) {
					System.out.println("SAME");
					query("INSERT INTO label_list (email_id, label_id) VALUE ('" + emailId + "', " + l.getId() + ");");
				}
				f.close();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	private static void insertUserEmail(EmailAddress addr, int emailId) {
		query("INSERT INTO user_email (user_id, email_id) VALUE ('" + addr.getId() + "', " + emailId + ");");
	}

	public static int getEmailCountByFolder(String folderName) {
		// can get sent count by putting foldername as sent. received is all folders
		// aggregate.
		// lets be able to get all folders to do this?
		ResultSet queryTbl;
		int size = 0;
		try {
			queryTbl = getConnection().prepareStatement("SELECT * FROM user "
					+ "JOIN user_email ON user.id = user_email.user_id "
					+ "JOIN email ON email.id = user_email.email_id " + "JOIN folder ON folder.id = email.folder_id "
					+ "WHERE folder.fold_name != '" + folderName + "';").executeQuery();

			while (queryTbl.next())
				size++;

			queryTbl.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return size;
	}

	// WIP
	// SQL DATE // filter on attachment? // need attachment name
	// private static static void getEmailByFilters(String email, String folder,
	// String label, boolean byAttachment,
	// boolean bySeen, Date startDate, Date endDate, int interval) {
	// if (startDate != null || endDate != null) { // same with label
	// // concat dates into query
	// }
	// }

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
	 * For testing/debug purposes via Database Team.
	 */
	public static void insertDummyData(String[] dummyStatements) {
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
	// public static UserFolder getFolder(int id, String name) {
	// for (UserFolder folder : folderList)
	// if (folder.getId() == id)
	// return folder;
	//
	// UserFolder fold = new UserFolder(id, name);
	// folderList.add(fold);
	//
	// return fold;
	// }

	// needs return list of emails
	// public static void getRecipientList(int emailId) {
	// String statement = "SELECT email_addr.email_address FROM recipient_list "
	// + "JOIN email_addr ON email_addr.id = recipient_list.email_addr_id "
	// + "WHERE email_id = '"
	// + emailId + "';";
	// }
	//
	// public static void getEmail() {
	// String statement = "SELECT * FROM email WHERE"; // NEED TO HAVE another table
	// for all emails linking to a userId
	// }

	/**
	 * --------------- ALL DONE GO HERE
	 */

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
	 * Gets list of validated emails set in user attribute (3).
	 * 
	 * @return number of validated emails (must be updated)
	 */
	public static int getValidatedEmails(String emailAddress) {
		int validatedEmails = 0;
		ResultSet queryTbl;
		try {
			queryTbl = getConnection()
					.prepareStatement("SELECT * FROM user WHERE user.email_address = '" + emailAddress + "'")
					.executeQuery();
			while (queryTbl.next())
				validatedEmails = queryTbl.getInt(3);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return validatedEmails;
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
	 * Figures out if a user has email.
	 * 
	 * @return whether a user has emails in their account
	 * 
	 * @param email address of the user.
	 */
	public static boolean hasEmails(String emailAddress) {
		ResultSet queryTbl;
		try {
			queryTbl = getConnection()
					.prepareStatement("SELECT * from user " + "JOIN user_email ON user.id = user_email.id "
							+ "JOIN email ON email.id = user_email.email_id WHERE email = " + emailAddress + ";")
					.executeQuery();
			int size = 0;

			while (queryTbl.next()) {
				size++;
				if (size > 0)
					return true;
			}

			queryTbl.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static List<Label> getLabels() {
		try {
			Folder[] f = Mailer.getStorage().getDefaultFolder().list();
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

	// /*
	// * Fetches all attributes of user_favourites table.
	// *
	// * @param user email address to be queried.
	// *
	// * @return List of UserFavourites objects.
	// */
	// public static List<UserFavourites> fetchFavourites(String emailAddress) {
	// List<UserFavourites> favsList = new ArrayList<>(); // HMMM CLASS LIST MAYBE?
	//
	// System.out.println("FETCHING FAVOURITES FOR :" + emailAddress +
	// "\n----------------------");
	// String query = "SELECT user_favourites.id, filter_settings.fav_name,
	// filter_settings.start_date, filter_settings.end_date,
	// filter_settings.interval_range, folder.id, folder.fold_name FROM user JOIN
	// user_favourites ON user.id = user_favourites.user_id JOIN filter_settings ON
	// user_favourites.filter_settings_id = filter_settings.id JOIN folder ON
	// filter_settings.folder_id = folder.id WHERE email_address = '"
	// + emailAddress + "';";
	//
	// try {
	// ResultSet rs = getConnection().prepareStatement(query).executeQuery();
	// while (rs.next()) {
	// ResultSetMetaData md = rs.getMetaData();
	//
	// // CHECK FOR FOLDER DUPLICATES.
	// UserFolder folder = getFolder(rs.getInt(6), rs.getString(7));
	//
	// favsList.add(new UserFavourites(1, rs.getString(2), rs.getDate(3),
	// rs.getDate(4), 5, folder));
	//
	// // SHOWS ALL ATTR FOR CONSOLE PURPOSE
	// for (int i = 1; i < md.getColumnCount() + 1; i++) {
	// System.out.println(md.getColumnName(i) + "_" + md.getColumnTypeName(i) + " ::
	// " + rs.getString(i)); // create
	// }
	// System.out.println("....................");
	// }
	//
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	//
	// return favsList;
	// }

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

}
// http://makble.com/gradle-example-to-connect-to-mysql-with-jdbc-in-eclipse