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
import java.util.logging.Level;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.dbutils.DbUtils;

import edu.oswego.debug.DebugLogger;
import edu.oswego.mail.Mailer;
import edu.oswego.model.Email;
import edu.oswego.model.EmailAddress;
import edu.oswego.model.UserFavourites;
import edu.oswego.model.UserFolder;
import edu.oswego.props.Interval;
import edu.oswego.sentiment.SentimentScore;

/**
 * Database class to get connection, push/pull data, and submit queries.
 * 
 * @author Jimmy
 * @since 11/06/2019
 */

// TODO redo Javadocs

public class Database {

	private EmailAddress user;
	private Mailer mailer;

	// for an email
	public List<EmailAddress> getRecipient(int emailId) {
		List<EmailAddress> eaList = new ArrayList<>();

		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection.prepareStatement("SELECT * FROM received_email WHERE email_id = " + emailId + ";")
					.executeQuery();
			while (rs.next())
				eaList.add(new EmailAddress(rs.getInt(1), rs.getString(2)));

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}
		return eaList;
	}

	public List<EmailAddress> getAllRecipients() {
		List<EmailAddress> eaList = new ArrayList<>();
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection.prepareStatement("SELECT email_addr.id, email_addr.email_address FROM received_email "
					+ "JOIN user_email ON received_email.email_id = user_email.email_id "
					+ "JOIN email_addr ON email_addr.id = received_email.email_addr_id " + "WHERE user_email.user_id = "
					+ user.getId() + ";").executeQuery();
			while (rs.next())
				eaList.add(new EmailAddress(rs.getInt(1), rs.getString(2)));

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		return eaList;
	}

	/**
	 * Gets a single email by the email id attribute in the database
	 *
	 * @param id
	 * @return Email object
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public Email getEmailById(int id) {
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection.prepareStatement("SELECT * FROM email WHERE id = " + id + ";").executeQuery();
			while (rs.next()) {
				Email em = new Email(rs.getInt(1), rs.getDate(2), rs.getString(3), rs.getDouble(4), rs.getBoolean(5),
						rs.getString(6), rs.getBoolean(7));
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(connection);
				return em;
			}

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}
		return null;
	}

	/**
	 * Gets a list of emails for current user;
	 *
	 * @return List of emails
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @see #getEmailById(int)
	 */
	public List<Email> getUserEmails() {
		List<Email> emailList = new ArrayList<>();
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection.prepareStatement("SELECT * FROM user_email WHERE user_id = " + user.getId() + ";")
					.executeQuery();
			while (rs.next())
				emailList.add(getEmailById(rs.getInt(3)));

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}
		return emailList;
	}

	/**
	 * Experimental purposes only.
	 * 
	 * @deprecated
	 */
	public void showTables() {
		Connection connection = getConnection();
		ResultSet queryTbl = null;
		ResultSet queryAttr = null;

		try {
			// show all tables
			queryTbl = connection.prepareStatement("show tables").executeQuery();

			while (queryTbl.next()) {
				String tbl = queryTbl.getString(1);
				System.out.println("[FOLDER] Table: " + tbl + "\n-------------------");

				queryAttr = connection.prepareStatement("select * from " + tbl).executeQuery();
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
			DbUtils.closeQuietly(queryTbl);
			DbUtils.closeQuietly(queryAttr);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(queryTbl);
			DbUtils.closeQuietly(queryAttr);
			DbUtils.closeQuietly(connection);
		}
	}

	public void showConnections() {
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			// SHOW FULL PROCESSLIST
			rs = connection.prepareStatement("SHOW FULL PROCESSLIST;").executeQuery();

			while (rs.next()) {
				System.out.println("DB: " + rs.getString(4) + "_" + rs.getString(5) + " :: " + rs.getString(8) + "\t"
						+ rs.getInt(6));
			}
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}
	}

	public int getConnectionCount() {
		int threads = -1;
		Connection connection = getConnection();
		ResultSet rs = null;

		try {
			// SHOW FULL PROCESSLIST
			rs = connection
					.prepareStatement(
							"USE information_schema; SELECT COUNT(*) FROM PROCESSLIST WHERE db ='csc480_19f';")
					.executeQuery();

			while (rs.next()) {
				threads = rs.getInt(1);
			}
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		return threads;
	}

	/**
	 * @param emailAddress
	 * 
	 * @param mailer
	 */
	public Database(String emailAddress, Mailer mailer) {
		user = getUser(emailAddress);
		this.mailer = mailer;
		DebugLogger.logEvent(Database.class.getName(), Level.INFO,
				"Database obj created for: " + user.getId() + " <" + user.getEmailAddress() + ">");
	}

	/**
	 * Used just to truncate tables
	 * 
	 * @deprecated
	 */
	public Database() {
		truncateTables();
	}

	/**
	 * Create and establish a database connection
	 * 
	 * @return JavaMail Connection object
	 */

	// int i = 0;
	public Connection getConnection() {
		// System.out.println(++i);
		Connection connection = null;

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			if (connection == null || connection.isClosed()) {
				connection = DriverManager.getConnection("jdbc:mysql://" + Settings.DATABASE_HOST + ":"
						+ Settings.DATABASE_PORT + "/" + Settings.DATABASE_SCHEMA
						+ "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&user="
						+ Settings.DATABASE_USERNAME + "&password=" + Settings.DATABASE_PASSWORD);
				DebugLogger.logEvent(Database.class.getName(), Level.INFO,
						"Connection has been established with database.");
			}
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
			e.printStackTrace();
		}

		return connection;
	}

	/**
	 * Pulls mailer emails into database
	 */
	public List<UserFolder> pull() {
		List<UserFolder> folderList = importFolders();

		if (getValidatedEmails() == mailer.getTotalEmailCount())
			return folderList;

		List<Integer> emailIdList = new ArrayList<>();
		List<String> messageList = new ArrayList<>();
		List<Integer> msgLengthList = new ArrayList<>();

		DebugLogger.logEvent(Database.class.getName(), Level.INFO, "Pulling emails from " + user.getEmailAddress());
		int s = 0;
		int stopper = 1; // limit our pull for testing

		for (UserFolder f : folderList) {
			Message[] msgs = mailer.pullEmails(f.getFolder().getFullName()); // Do not use "[Gmail]/All Mail");
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

					messageList.add(mailer.getTextFromMessage(m));

					s++;
					if (s > stopper)
						break;

				} catch (MessagingException e) {
					DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
				}
			}
			// break;
			// strange ghost email added at the end....?

			// break;
			// TODO Mark emails by validation
			// If not already in folder, copy it over.
			// NULL POINTER EXCEPTION. FIX DIS yo.

			mailer.markEmailsInFolder(f.getFolder().getFullName(), msgs);
			msgLengthList.add(msgs.length);
			// semes to work so far...
			// break;
		}

		// TODO CHECK IF THIS WORKS
		// String[] mArr = messageList.toArray(new String[messageList.size()]);
		// SentimentScore[] ss = AnalyzeThis.process(mArr);
		// for (int i = 0; i < emailIdList.size(); i++) {
		// System.out.println("SS CALC");
		// calculateSentimentScore(emailIdList.get(i), ss[i]);
		// }

		int sum = 0;
		for (Integer c : msgLengthList)
			sum += c;
		setValidatedEmailCount(sum);

		DebugLogger.logEvent(Database.class.getName(), Level.INFO,
				"Emails have been pulled for " + user.getId() + " <" + user.getEmailAddress() + ">");
		return folderList;
	}

	/**
	 * Aggregates all emails under the param conditions
	 *
	 * @param startDate
	 * @param endDate
	 * @param seen
	 * @param folderName
	 * @return List of Email objects
	 */
	public List<Email> getEmailByFilter(boolean hasAttachment, String startDate, String endDate, boolean seen,
			String folderName) {
		List<Email> emailList = new ArrayList<>();
		List<String> filterStatements = new ArrayList<>();

		String selectionStatement = "SELECT * FROM email JOIN user_email on user_email.id = " + user.getId()
				+ " WHERE ";

		if (hasAttachment)
			filterStatements.add("has_attachment = 1");
		if (seen)
			filterStatements.add("seen = 1");
		if (startDate != null)
			filterStatements.add("date_received >= '" + startDate + "'");
		if (endDate != null) // engine will calculate endDate with interval given to them.
			filterStatements.add("date_received <= '" + endDate + "'");
		if (seen)
			filterStatements.add("folder_id = " + getFolderId(folderName));

		for (int i = 0; i < filterStatements.size(); i++) {
			selectionStatement += filterStatements.get(i);
			if (i < (filterStatements.size() - 1))
				selectionStatement += " AND ";
		}

		selectionStatement += ";";

		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection.prepareStatement(selectionStatement, Statement.RETURN_GENERATED_KEYS).executeQuery();

			while (rs.next()) {
				// TODO getSentimentScoreById
				Email e = new Email(rs.getInt(1), rs.getDate(2), rs.getString(3), rs.getDouble(4), rs.getBoolean(5),
						rs.getString(6), rs.getBoolean(7), null, getFolderById(rs.getInt(9)),
						getReceivedEmail(rs.getInt(1)));
				emailList.add(e);
			}

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		DebugLogger.logEvent(Database.class.getName(), Level.INFO,
				"Query submitted for " + user.getId() + " <" + user.getEmailAddress() + ">");
		return emailList;
	}

	/**
	 * Inserts a user object and gives back the id value
	 * 
	 * @param emailAddress
	 * @return database id of user. -1 if id there is no insertion.
	 */
	private int insertUser(String emailAddress) {
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = connection.prepareStatement("INSERT INTO user (email_address) VALUE ('" + emailAddress + "')",
					Statement.RETURN_GENERATED_KEYS);
			if (ps.executeUpdate() == 0)
				DebugLogger.logEvent(Database.class.getName(), Level.INFO, "Could not insert a user, no rows affected");

			rs = ps.getGeneratedKeys();

			if (rs.next()) {
				DebugLogger.logEvent(Database.class.getName(), Level.INFO,
						"New user created " + rs.getInt(1) + " <" + emailAddress + ">");
				return rs.getInt(1);
			}

			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);

		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}
		return -1;
	}

	/**
	 * fetches a user (email address object) based on a string email search.
	 *
	 */
	public EmailAddress getUser(String emailAddress) {
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection.prepareStatement("SELECT * FROM user WHERE email_address = '" + emailAddress + "';",
					Statement.RETURN_GENERATED_KEYS).executeQuery();
			while (rs.next()) {
				EmailAddress ea = new EmailAddress(rs.getInt(1), rs.getString(2));
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(connection);
				return ea;
			}
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		return new EmailAddress(insertUser(emailAddress), emailAddress);
	}

	/**
	 * Gets a EmailAddress object of current db object user. Not the same as
	 * getUser(String emailAddress).
	 * 
	 * @return EmailAddress object
	 */
	public EmailAddress getUser() {
		return user;
	}

	/**
	 * Gets a folder id based on its name
	 * 
	 * @param folderName
	 * @return database id number of the folder
	 */
	public int getFolderId(String folderName) {
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection.prepareStatement("SELECT id FROM folder WHERE fold_name = '" + folderName + "';")
					.executeQuery();
			if (rs.next()) {
				int id = rs.getInt(1);
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(connection);
				return id;
			}

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		return -1;
	}

	/**
	 * Gets a folder based on its id number
	 * 
	 * @param id
	 * @return UserFolder object
	 */
	public UserFolder getFolderById(int id) {
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection.prepareStatement("SELECT * FROM folder WHERE id = " + id + ";").executeQuery();
			if (rs.next()) {
				UserFolder uf = new UserFolder(rs.getInt(1), mailer.getFolder(rs.getString(2)));
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(connection);
				return uf;
			}
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		return null;
	}

	// TODO JOIN
	/**
	 * Fetches UserFavourites from database by the favourite name
	 * 
	 * @param favName
	 * @return UserFavourites object
	 */
	public UserFavourites getUserFavourite(String favName) {
		UserFavourites uf = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		try {

			rs = connection.prepareStatement(
					"SELECT user_favourites.id, fav_name, start_date, end_date, interval_range, has_attachment, is_seen, folder_id From user_favourites \n"
							+ "JOIN filter_settings ON filter_settings.id = user_favourites.filter_settings_id WHERE user_id = "
							+ user.getId() + " AND" + "fav_name = '" + favName + "';")
					.executeQuery();

			while (rs.next()) {
				uf = new UserFavourites(rs.getInt(1), rs.getString(2), rs.getDate(2), rs.getDate(3),
						Interval.parse(rs.getString(4)), rs.getBoolean(5), rs.getBoolean(6),
						getFolderById(rs.getInt(7)));
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(connection);
				// break?
				return uf;
			}

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		return uf;
	}

	/**
	 * Fetches a list of UserFavourites belonging to session user
	 * 
	 * @see #getUserFavourite
	 * @return List of UserFavourites
	 */
	public List<UserFavourites> getUserFavourites() {
		List<UserFavourites> ufList = new ArrayList<>();
		Connection connection = getConnection();
		ResultSet rs = null;

		try {

			rs = connection.prepareStatement(
					"SELECT user_favourites.id, fav_name, start_date, end_date, interval_range, has_attachment, is_seen, folder_id From user_favourites \n"
							+ "JOIN filter_settings ON filter_settings.id = user_favourites.filter_settings_id WHERE user_id = "
							+ user.getId()+ ";")
					.executeQuery();

			while (rs.next()) {
				ufList.add(new UserFavourites(rs.getInt(1), rs.getString(2), rs.getDate(2), rs.getDate(3),
						Interval.parse(rs.getString(4)), rs.getBoolean(5), rs.getBoolean(6),
						getFolderById(rs.getInt(7))));
			}

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		DebugLogger.logEvent(Database.class.getName(), Level.INFO,
				"Favourites fetched for " + user.getId() + " <" + user.getEmailAddress() + ">");

		return ufList;
	}

	// TODO convert this to string with Time object available now. No need for ps.
	/**
	 * Inserts a user favourite based on several parameters belong
	 * 
	 * @param favName
	 * @param intervalRange
	 * @param hasAttachment
	 * @param isSeen
	 * @param folderName
	 * @return boolean if operation was completed successfully
	 * @see #insertFilter
	 */
	public boolean insertUserFavourites(String favName, java.util.Date startDate, java.util.Date endDate,
			Interval intervalRange, boolean hasAttachment, boolean isSeen, String folderName) {
		int folderId = getFolderId(folderName);
		if (folderId == -1)
			return false;

		int filterId = insertFilter(startDate, endDate, intervalRange.toString(), hasAttachment, isSeen, folderId);

		query("INSERT INTO user_favourites (filter_settings_id, user_id, fav_name) VALUE (" + filterId + ", "
				+ user.getId() + ", '" + favName + "');");

		DebugLogger.logEvent(Database.class.getName(), Level.INFO,
				"User Favourite added for " + user.getId() + " <" + user.getEmailAddress() + ">");

		return true;
	}

	/**
	 * Inserts a filter_settings record in the database
	 * 
	 *
	 * @param intervalRange
	 * @param hasAttachment
	 * @param isSeen
	 * @param folderId
	 * @return database id of filter_settings record
	 * @see #insertUserFavourites
	 */
	private int insertFilter(java.util.Date startDate, java.util.Date endDate, String intervalRange,
			boolean hasAttachment, boolean isSeen, int folderId) {
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(
					"INSERT INTO filter_settings (start_date, end_date, interval_range, has_attachment, is_seen, folder_id) VALUE (?, ?, ?, ?, ?, ?);",
					Statement.RETURN_GENERATED_KEYS);

			ps.setObject(1, startDate);
			ps.setObject(2, endDate);
			ps.setString(3, intervalRange);
			ps.setBoolean(4, hasAttachment);
			ps.setBoolean(5, isSeen);
			ps.setInt(6, folderId);

			if (ps.executeUpdate() == 0)
				DebugLogger.logEvent(Database.class.getName(), Level.WARNING,
						"Could not insert filter. No rows affected");

			rs = ps.getGeneratedKeys();

			if (rs.next()) {
				int id = rs.getInt(1);
				DbUtils.closeQuietly(ps);
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(connection);
				return id;
			}

		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}
		return -1;
	}

	/**
	 * Removes a user_favourites record by favourite name
	 * 
	 * @param favName
	 * @see #insertUserFavourites
	 */
	public void removeUserFavourite(String favName) {
		query("DELETE FROM user_favourites WHERE fav_name = '" + favName + "' AND user_id = " + user.getId() + ";");
		DebugLogger.logEvent(Database.class.getName(), Level.INFO,
				"User favourite removal submitted for " + user.getId() + " <" + user.getEmailAddress() + ">");
	}

	/**
	 * Checks if a folder exists in the database
	 * 
	 * @param folderName
	 * @param folderList
	 * @return boolean value whether the folder exists or not
	 */
	private boolean folderExists(String folderName, List<UserFolder> folderList) {
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection.prepareStatement("SELECT * FROM folder WHERE fold_name = '" + folderName + "'")
					.executeQuery();
			while (rs.next()) {
				folderList.add(new UserFolder(rs.getInt(1), mailer.getFolder(rs.getString(2))));
				return true;
			}

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}
		return false;
	}

	/**
	 * Imports all folders from a user's mail folder
	 * 
	 * @return List of UserFolder
	 * @see UserFolder
	 */
	public List<UserFolder> importFolders() {
		List<UserFolder> folderList = new ArrayList<>();
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Folder[] folders = mailer.getStorage().getDefaultFolder().list("*");
			for (Folder f : folders) {
				if (!folderExists(f.getFullName(), folderList) && !f.getFullName().equals("[Gmail]")
						&& !f.getFullName().equals("CSC480_19F") && !f.getFullName().equals("[Gmail]/All Mail")) {

					ps = connection.prepareStatement("INSERT INTO folder (fold_name) VALUE ('" + f.getFullName() + "')",
							Statement.RETURN_GENERATED_KEYS);
					if (ps.executeUpdate() == 0)
						DebugLogger.logEvent(Database.class.getName(), Level.WARNING,
								"Could not insert into folder, no rows affected");

					rs = ps.getGeneratedKeys();
					if (rs.next()) {
						folderList.add(new UserFolder(rs.getInt(1), f));
						System.out.println("ADDED FOLDER:\t" + f.getFullName());
					}
				}
			}

			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);

		} catch (SQLException | MessagingException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		return folderList;
	}

	/**
	 * Get an email based on its message
	 * 
	 * @param m
	 * @return database id of the email record
	 */
	private int getEmailId(Message m) {
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection
					.prepareStatement("SELECT * FROM email WHERE date_received = ? AND subject = ? AND size = ?");
			ps.setObject(1, m.getReceivedDate());
			ps.setString(2, m.getSubject());
			ps.setInt(3, m.getSize());

			rs = ps.executeQuery();
			while (rs.next())
				return rs.getInt(1);

			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}
		return -1;
	}

	/**
	 * Inserts an email record into the database
	 * 
	 * @param m
	 * @param folderList
	 * @param emailIdList
	 * @return database id of email record
	 */
	private int insertEmail(Message m, List<UserFolder> folderList, List<Integer> emailIdList) {

		int emailId = -1;
		emailId = getEmailId(m); // TODO Duplicates?
		if (emailId != -1)
			return emailId;

		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(
					"INSERT INTO email (date_received, subject, size, seen, has_attachment, file_name, folder_id) VALUE (?, ?, ?, ?, ?, ?, ?);",
					Statement.RETURN_GENERATED_KEYS);
			ps.setObject(1, m.getReceivedDate());
			ps.setString(2, m.getSubject());
			ps.setDouble(3, m.getSize());
			ps.setBoolean(4, m.getFlags().contains(Flags.Flag.SEEN));

			boolean hasAttachment = mailer.hasAttachment(m);
			ps.setBoolean(5, hasAttachment);

			String fileName = null;
			if (hasAttachment) {
				fileName = mailer.getAttachmentName(m);
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
				DebugLogger.logEvent(Database.class.getName(), Level.WARNING,
						"Could not insert into email, no rows affected");

			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				emailId = rs.getInt(1);
				emailIdList.add(emailId);
			}

			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);

		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} catch (MessagingException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		return emailId;
	}

	/**
	 * Checks if an email address exists
	 * 
	 * @param emailAddress
	 * @return boolean whether email address exists or not
	 */
	private boolean emailAddressExists(String emailAddress) {
		int size = -1;

		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection
					.prepareStatement("SELECT count(*) FROM email_addr WHERE email_address = '" + emailAddress + "'")
					.executeQuery();
			while (rs.next()) {
				size = rs.getInt(1);
			}

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}
		return size >= 1;
	}

	/**
	 * Parses an email with invalid characters (such as apostrophe's)
	 * 
	 * @param address
	 * @return legal string value of email address
	 */
	private String parseAddress(Address address) {
		String addressParsed = "";
		if (address.toString().contains("<")) {
			String addrParser[] = address.toString().replace("'", "`").split("<");
			addressParsed = addrParser[1].replace(">", "");
		} else
			addressParsed = address.toString().replace("'", "`");

		return addressParsed;
	}

	/**
	 * Inserts an email address record into database
	 * 
	 * @param addresses
	 * @return list of EmailAddress
	 * @see EmailAddress
	 */
	private List<EmailAddress> insertEmailAddress(Address[] addresses) {
		List<EmailAddress> emailAddrList = new ArrayList<>();

		for (Address a : addresses) {
			String address = parseAddress(a);
			Connection connection = getConnection();
			ResultSet rs = null;
			PreparedStatement ps = null;
			try {
				if (!emailAddressExists(address)) {
					ps = connection.prepareStatement(
							"INSERT INTO email_addr (email_address) VALUE ('" + address + "');",
							Statement.RETURN_GENERATED_KEYS);

					if (ps.executeUpdate() == 0)
						DebugLogger.logEvent(Database.class.getName(), Level.WARNING,
								"Could not insert an email address");

					rs = ps.getGeneratedKeys();
					if (rs.next())
						emailAddrList.add(new EmailAddress(rs.getInt(1), address));

				} else {
					rs = connection
							.prepareStatement("SELECT * FROM email_addr WHERE email_address = '" + address + "';")
							.executeQuery();
					while (rs.next())
						emailAddrList.add(new EmailAddress(rs.getInt(1), rs.getString(2)));
				}

				DbUtils.closeQuietly(ps);
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(connection);
			} catch (SQLException e) {
				DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
			} finally {
				DbUtils.closeQuietly(ps);
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(connection);
			}
		}

		return emailAddrList;
	}

	/**
	 * Gets all emails received based on emailId
	 * 
	 * @param emailId
	 * @return list of EmailAddress
	 */
	private List<EmailAddress> getReceivedEmail(int emailId) {
		List<EmailAddress> addressIdList = new ArrayList<>();

		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection.prepareStatement(
					"SELECT email_addr_id, email_address FROM received_email JOIN email_addr ON email_addr.id = received_email.email_addr_id"
							+ " WHERE received_email.email_id = " + emailId + ";")
					.executeQuery();
			while (rs.next())
				addressIdList.add(new EmailAddress(rs.getInt(1), rs.getString(2)));

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		return addressIdList;
	}

	/**
	 * Checks if received email record exists
	 * 
	 * @param emailId
	 * @param emailAddrId
	 * @return boolean if receieved email exists
	 */
	private boolean receivedEmailExists(int emailId, int emailAddrId) {
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection.prepareStatement("SELECT * FROM received_email WHERE email_id = " + emailId
					+ " AND email_addr_id = " + emailAddrId + ";").executeQuery();
			while (rs.next())
				return true;

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		return false;
	}

	/**
	 * Inserts a received email record into database
	 * 
	 * @param emailId
	 * @param emailAddrId
	 */
	private void insertReceivedEmails(int emailId, int emailAddrId) {
		if (!receivedEmailExists(emailId, emailAddrId))
			query("INSERT INTO received_email (email_id, email_addr_id) VALUE ('" + emailId + "', " + emailAddrId
					+ ");");
	}

	/**
	 * Checks if an email exists for a user exists
	 * 
	 * @param addr
	 * @param emailId
	 * @return if email exists for user
	 * @see #insertUserEmail
	 */
	private boolean userEmailExists(EmailAddress addr, int emailId) {
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection.prepareStatement(
					"SELECT * FROM user_email WHERE user_id = " + addr.getId() + " AND email_id = " + emailId + ";")
					.executeQuery();
			while (rs.next())
				return true;

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		return false;
	}

	/**
	 * Inserts a user email record
	 * 
	 * @param addr
	 * @param emailId
	 */
	private void insertUserEmail(EmailAddress addr, int emailId) {
		if (!userEmailExists(addr, emailId))
			query("INSERT INTO user_email (user_id, email_id) VALUE (" + addr.getId() + ", " + emailId + ");");
	}

	/**
	 * Gets count of all emails by folder name
	 * 
	 * @param folderName
	 * @return number of emails
	 */
	public int getEmailCountByFolder(String folderName) {
		int size = 0;
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection.prepareStatement("SELECT * FROM user " + "JOIN user_email ON user.id = user_email.user_id "
					+ "JOIN email ON email.id = user_email.email_id " + "JOIN folder ON folder.id = email.folder_id "
					+ "WHERE folder.fold_name != '" + folderName + "';").executeQuery();

			while (rs.next())
				size++;

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);

		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		return size;
	}

	/**
	 * Sets validated amount of emails (in CSC480_19f folder) for user after a pull
	 *
	 * @param count
	 */
	public void setValidatedEmailCount(int count) {
		query("UPDATE user SET validated_emails = " + count + " WHERE email_address = '" + user.getEmailAddress()
				+ "';");
		DebugLogger.logEvent(Database.class.getName(), Level.INFO,
				"Validation count submitted for " + user.getId() + " <" + user.getEmailAddress() + ">");
	}

	/**
	 * Gets number of validated emails
	 * 
	 * @return number of validated emails
	 * @see #setValidatedEmailCount
	 */
	public int getValidatedEmails() {
		int validatedEmails = 0;
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection
					.prepareStatement("SELECT * FROM user WHERE user.email_address = '" + user.getEmailAddress() + "'")
					.executeQuery();
			while (rs.next())
				validatedEmails = rs.getInt(3);

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		return validatedEmails;
	}

	/**
	 * Creates a database query. This is a dangerous operation if unchecked.
	 * 
	 * @param statement
	 */
	public void query(String statement) {
		Connection connection = getConnection();
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(statement);
			ps.execute();
			DebugLogger.logEvent(Database.class.getName(), Level.INFO, "Query made for statement: " + statement);

			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(connection);
		}
	}

	/**
	 * Creates a database query with a bunch of statements.
	 * 
	 * @param statements
	 * @deprecated
	 * @see #query
	 */
	public void query(String[] statements) {
		for (String statement : statements)
			query(statement);
	}

	// TODO change this so it's only for a user
	/**
	 * Truncates all tables. Dangerous as it removes all data for all users.
	 * 
	 * @see #truncateTable
	 */
	public void truncateTables() {
		for (String tbl : Settings.DATABASE_TABLES)
			truncateTable(tbl);
		DebugLogger.logEvent(Database.class.getName(), Level.SEVERE, "Database tables have been truncated completely.");
	}

	/**
	 * Truncates a single table
	 * 
	 * @param table
	 */
	public void truncateTable(String table) {
		query("TRUNCATE TABLE " + table + ";");
		DebugLogger.logEvent(Database.class.getName(), Level.SEVERE, table + " has been truncated completely");
	}

	/**
	 * Checks if a user has emails.
	 * 
	 *
	 * @return if user has emails
	 */
	public boolean hasEmails() {
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			rs = connection.prepareStatement("SELECT * from user " + "JOIN user_email ON user_email.user_id = user.id "
					+ "JOIN email ON email.id = user_email.email_id WHERE email_address = '" + user.getEmailAddress()
					+ "';").executeQuery();
			int size = 0;

			while (rs.next()) {
				size++;
				if (size > 0)
					return true;
			}

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}

		return false;
	}

	// TODO NEEDS THIS IMPLEMENTATION OF SENTIMENT ANALYZER TO SCORE
	/**
	 * Uses sentiment score analyzer to calculate and insert the value
	 * 
	 * @param emailId
	 * @param score
	 * @see #insertSentimentScore
	 * @see #insertSentimentScoreIntoEmail
	 */
	public void calculateSentimentScore(int emailId, SentimentScore score) {
		int sentimentId = insertSentimentScore(score);

		if (sentimentId == -1) {
			System.out.println("ERROR HAS OCCURED CALCULATING SENTIMENT SCORE");
			return;
		}

		insertSentimentScoreIntoEmail(emailId, sentimentId);
	}

	/**
	 * Inserts a sentiment score record into the database
	 * 
	 * @param score
	 * @return database id of sentiment score record
	 */
	private int insertSentimentScore(SentimentScore score) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConnection()
					.prepareStatement("INSERT INTO sentiment_score (positive, negative, neutral, compound) VALUE ("
							+ score.getPositive() + ", " + score.getNegative() + ", " + score.getNeutral() + ", "
							+ score.getCompound() + ");", Statement.RETURN_GENERATED_KEYS);
			if (ps.executeUpdate() == 1) { // AFFECTED ROW
				rs = ps.getGeneratedKeys();
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			// DbUtils.closeQuietly(ps);
			// DbUtils.closeQuietly(rs);
			// DbUtils.closeQuietly(connection);
		}
		return -1;
	}

	/**
	 * Inserts sentiment score reference into an email record
	 * 
	 * @param emailId
	 * @param sentimentScoreId
	 */
	private void insertSentimentScoreIntoEmail(int emailId, int sentimentScoreId) {
		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement(
					"UPDATE email SET sentiment_score_id = " + sentimentScoreId + " WHERE id = " + emailId + ";");
			ps.execute();
		} catch (SQLException e) {
			DebugLogger.logEvent(Database.class.getName(), Level.WARNING, e.getMessage());
		} finally {
			// DbUtils.closeQuietly(ps);
			// DbUtils.closeQuietly(connection);
		}
	}

}