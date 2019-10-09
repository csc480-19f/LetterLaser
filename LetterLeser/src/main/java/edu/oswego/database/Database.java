package edu.oswego.database;

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
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;

import SentimentAnalyzer.SentimentScore;
import edu.oswego.mail.Mailer;
import edu.oswego.model.EmailAddress;
import edu.oswego.model.Label;
import edu.oswego.model.UserFavourites;
import edu.oswego.model.UserFolder;
import edu.oswego.props.Settings;

/**
 * Database class to get connection, push/pull data, and submit queries.
 * 
 * @author Jimmy
 * @since 10/04/2019
 */

public class Database {

	private Connection connection;
	private EmailAddress user;

	private List<UserFolder> folderList;
	// private Mailer mailer;

	public Database(String emailAddress, String oauth2) {
		user = getUser(emailAddress);
		// mailer = new Mailer();
		pull();
	}

	private EmailAddress getUser(String emailAddress) {
		ResultSet rs;
		try {
			rs = getConnection().prepareStatement("SELECT * FROM user WHERE email_address = '" + emailAddress + "';",
					Statement.RETURN_GENERATED_KEYS).executeQuery();
			while (rs.next())
				return new EmailAddress(rs.getInt(1), rs.getString(2));

			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return new EmailAddress(insertUser(emailAddress), emailAddress);
	}

	private int insertUser(String emailAddress) {
		PreparedStatement ps;
		try {
			ps = getConnection().prepareStatement("INSERT INTO user (email_address) VALUE ('" + emailAddress + "')",
					Statement.RETURN_GENERATED_KEYS);
			if (ps.executeUpdate() == 0)
				throw new SQLException("Could not insert into folder, no rows affected");

			ResultSet generatedKeys = ps.getGeneratedKeys();

			if (generatedKeys.next())
				return generatedKeys.getInt(1);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	private int insertFilter(java.util.Date utilDate, java.util.Date utilDate2, int intervalRange, String folderName) {
		int folderId = getFolderId(folderName);

		PreparedStatement ps;
		try {
			ps = getConnection().prepareStatement(
					"INSERT INTO filter_settings (start_date, end_date, interval_range, folder_id) VALUE (?, ?, ?, ?);",
					Statement.RETURN_GENERATED_KEYS);
			ps.setObject(1, utilDate);
			ps.setObject(2, utilDate2);
			ps.setInt(3, intervalRange);
			ps.setInt(4, folderId);

			if (ps.executeUpdate() == 0)
				throw new SQLException("Could not insert into folder, no rows affected");

			ResultSet generatedKeys = ps.getGeneratedKeys();

			if (generatedKeys.next())
				return generatedKeys.getInt(1);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	private int getFolderId(String folderName) {
		for (UserFolder f : folderList)
			if (f.getFolder().getFullName().equals(folderName))
				return f.getId();
		return -1;
	}
	
	private String getFolderName(int id) {
		for (UserFolder f : folderList)
			if (f.getId() == id)
				return f.getFolder().getFullName();
		return null;
	}
	
	private UserFolder getFolderById(int id) {
		for (UserFolder f : folderList)
			if (f.getId() == id)
				return f;
		return null;
	}
	
	

	public boolean insertUserFavourites(String favName, java.util.Date utilDate, java.util.Date utilDate2,
			int intervalRange, String folderName) {
		int folderId = getFolderId(folderName);
		if (folderId == -1)
			return false;

		int filterId = insertFilter(utilDate, utilDate2, intervalRange, folderName);
		
		query("INSERT INTO user_favourites (filter_settings_id, user_id, fav_name) VALUE (" + filterId + ", "
				+ user.getId() + ", '" + favName + "');");

		return true;
	}
	
	public List<UserFavourites> fetchInitializeLoad() {
		
		// if need length of emails, folders use mailer
		
		List<UserFavourites> ufList = new ArrayList<>();
		// LIST TO RETURN OF ALL USER FAVS
		try {
			ResultSet rs = getConnection().prepareStatement("SELECT * FROM user_favourites WHERE user_id = '" + user.getId() + "';", Statement.RETURN_GENERATED_KEYS).executeQuery();
			
			while (rs.next()) {
				ResultSet rs2 = getConnection().prepareStatement("SELECT * FROM filter_settings WHERE id = '" + rs.getInt(1) + "';", Statement.RETURN_GENERATED_KEYS).executeQuery();
				while (rs2.next())
					ufList.add(new UserFavourites(rs.getInt(1), rs.getString(2), rs2.getDate(2), rs2.getDate(3), rs2.getInt(4), getFolderById(rs2.getInt(5))));
				rs2.close();
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return ufList;
	}

	public void pull() {
		folderList = importFolders();
		List<Integer> emailIdList = new ArrayList<>();

		int i = 0;
		int stopper = 10; // limit our pull for testing

		for (UserFolder f : folderList) {
			Message[] msgs = Mailer.pullEmails(f.getFolder().getFullName()); // Do not use "[Gmail]/All Mail");
			for (Message m : msgs) {
				try {
					List<EmailAddress> fromList = insertEmailAddress(m.getFrom());// get this list and return for
																					// user_email table
					int emailId = insertEmail(m, folderList, emailIdList);
					for (EmailAddress ea : fromList) {
						insertReceivedEmails(emailId, ea.getId());
					}
					insertUserEmail(user, emailId);
					emailIdList.add(emailId);

					i++;
					if (i > stopper)
						return;

				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}

			// If not already in folder, copy it over.
			// NULL POINTER EXCEPTION. FIX DIS yo.
			// Mailer.markEmailsInFolder(f.getFolder().getFullName(), msgs);
		}
	}

	public void setValidatedEmailCount(String emailAddress, int count) {
		query("UPDATE user SET validated_emails = " + count + " WHERE email_address = '" + emailAddress + "';");
	}

	private boolean folderExists(String folderName, List<UserFolder> folderList) {
		ResultSet rs;
		try {
			rs = getConnection().prepareStatement("SELECT * FROM folder WHERE fold_name = '" + folderName + "'")
					.executeQuery();
			while (rs.next()) {
				folderList.add(new UserFolder(rs.getInt(1), Mailer.getFolder(rs.getString(2))));
				return true;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	public List<UserFolder> importFolders() {
		List<UserFolder> folderList = new ArrayList<>();
		try {
			Folder[] folders = Mailer.getStorage().getDefaultFolder().list("*");

			for (Folder f : folders) {
				if (!folderExists(f.getFullName(), folderList) && !f.getFullName().equals("[Gmail]")
						&& !f.getFullName().equals("CSC480_19F") && !f.getFullName().equals("[Gmail]/All Mail")) {
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
		return folderList;
	}

	private int getEmailId(Message m) {
		try {
			PreparedStatement pstmt = getConnection()
					.prepareStatement("SELECT * FROM email WHERE date_received = ? AND subject = ? AND size = ?");
			pstmt.setObject(1, m.getReceivedDate());
			pstmt.setString(2, m.getSubject());
			pstmt.setInt(3, m.getSize());

			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				return rs.getInt(1);

		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return -1;
	}

	private int insertEmail(Message m, List<UserFolder> folderList, List<Integer> emailIdList) {
		int emailId = -1;

		emailId = getEmailId(m);

		if (emailId != -1)
			return emailId;

		PreparedStatement ps;
		try {
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

	private boolean emailAddressExists(String emailAddress) {
		ResultSet rs;
		int size = -1;
		try {
			rs = getConnection()
					.prepareStatement("SELECT count(*) FROM email_addr WHERE email_address = '" + emailAddress + "'")
					.executeQuery();
			while (rs.next()) {
				size = rs.getInt(1);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return size >= 1;
	}

	private String parseAddress(Address address) {
		String addressParsed = "";
		if (address.toString().contains("<")) {
			String addrParser[] = address.toString().replace("'", "`").split("<");
			addressParsed = addrParser[1].replace(">", ""); // can also do substring but need to know index of <. Still
															// iterating but less mem
		} else {
			addressParsed = address.toString().replace("'", "`");
		}

		return addressParsed;
	}

	private List<EmailAddress> insertEmailAddress(Address[] addresses) {
		List<EmailAddress> emailAddrList = new ArrayList<>();

		for (Address a : addresses) {
			String address = parseAddress(a);

			try {
				// NOT EXIST
				if (!emailAddressExists(address)) {
					PreparedStatement ps = getConnection().prepareStatement(
							"INSERT INTO email_addr (email_address) VALUE ('" + address + "');",
							Statement.RETURN_GENERATED_KEYS);

					if (ps.executeUpdate() == 0)
						throw new SQLException("Could not insert into folder, no rows affected");

					ResultSet generatedKeys = ps.getGeneratedKeys();
					if (generatedKeys.next()) {
						emailAddrList.add(new EmailAddress(generatedKeys.getInt(1), address));
					}
					// EXISTS
				} else {
					ResultSet rs = getConnection()
							.prepareStatement("SELECT * FROM email_addr WHERE email_address = '" + address + "';")
							.executeQuery();
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

	private boolean receivedEmailExists(int emailId, int emailAddrId) {
		ResultSet rs;
		try {
			rs = getConnection().prepareStatement("SELECT * FROM received_email WHERE email_id = " + emailId
					+ " AND email_addr_id = " + emailAddrId + ";").executeQuery();
			while (rs.next())
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	private void insertReceivedEmails(int emailId, int emailAddrId) {
		if (!receivedEmailExists(emailId, emailAddrId)) {
			query("INSERT INTO received_email (email_id, email_addr_id) VALUE ('" + emailId + "', " + emailAddrId
					+ ");");
		}
	}

	private boolean userEmailExists(EmailAddress addr, int emailId) {
		ResultSet rs;
		try {
			rs = getConnection().prepareStatement(
					"SELECT * FROM user_email WHERE user_id = " + addr.getId() + " AND email_id = " + emailId + ";")
					.executeQuery();
			while (rs.next())
				return true;
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	private void insertUserEmail(EmailAddress addr, int emailId) {
		if (!userEmailExists(addr, emailId))
			query("INSERT INTO user_email (user_id, email_id) VALUE (" + addr.getId() + ", " + emailId + ");");
	}

	public int getEmailCountByFolder(String folderName) {
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
	// private void getEmailByFilters(String email, String folder,
	// String label, boolean byAttachment,
	// boolean bySeen, Date startDate, Date endDate, int interval) {
	// if (startDate != null || endDate != null) { // same with label
	// // concat dates into query
	// }
	// }

	public void showTables() {
		ResultSet queryTbl;
		try {
			// show all tables
			queryTbl = getConnection().prepareStatement("show tables").executeQuery();

			while (queryTbl.next()) {
				String tbl = queryTbl.getString(1);
				System.out.println("[INBOX] Table: " + tbl + "\n-------------------");

				// show all attributes from the tables
				ResultSet queryAttr = getConnection().prepareStatement("select * from " + tbl).executeQuery();
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
	}

	public void query(String statement) {
		PreparedStatement ps;
		try {
			ps = getConnection().prepareStatement(statement);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertDummyData(String[] dummyStatements) {
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

	private Connection getConnection() {
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

	public int getValidatedEmails(String emailAddress) {
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

	public void truncateTables() {
		for (String tbl : Settings.DATABASE_TABLES)
			truncateTable(tbl);
	}

	public void truncateTable(String table) {
		PreparedStatement ps;
		try {
			ps = getConnection().prepareStatement("TRUNCATE TABLE " + table + ";");
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean hasEmails(String emailAddress) {
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

	public List<Label> getLabels() {
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
	// public List<UserFavourites> fetchFavourites(String emailAddress) {
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

	public void calculateSentimentScore(int emailId, SentimentScore score) {
		int sentimentId = insertSentimentScore(score);

		if (sentimentId == -1) {
			System.out.println("ERROR HAS OCCURED CALCULATING SENTIMENT SCORE");
			return;
		}

		insertSentimentScoreIntoEmail(emailId, sentimentId);
	}

	private int insertSentimentScore(SentimentScore score) {
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

	private void insertSentimentScoreIntoEmail(int emailId, int sentimentScoreId) {
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
