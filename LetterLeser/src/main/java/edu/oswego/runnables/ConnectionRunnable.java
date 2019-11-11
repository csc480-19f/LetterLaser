package edu.oswego.runnables;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;

import edu.oswego.database.Settings;

public class ConnectionRunnable implements Runnable {
	
	@Override
	public void run() {
		
		Settings.loadCredentials();
		
		while (true) {
			if (isConnectionOpen()) {
				System.out.println("CONSUME QUEUE STUFF HERE");
			} else {
				System.out.println("Too many connections. Maybe queue up here?");
			}
			
			try {
				Thread.sleep(10L * 500L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	private boolean isConnectionOpen() {
		return getActivateConnections() <= edu.oswego.database.Settings.THRESHOLD_CONNECTION;
	}
	
	private int getActivateConnections() {
		int threads = -1;
		Connection connection = null;
		ResultSet rs = null;
		
		try {
			connection = DriverManager.getConnection("jdbc:mysql://" + Settings.DATABASE_HOST + ":" + Settings.DATABASE_PORT + "/" + "information_schema" + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&user=" + Settings.DATABASE_USERNAME + "&password=" + Settings.DATABASE_PASSWORD);
			rs = connection.prepareStatement("SELECT COUNT(*) FROM PROCESSLIST").executeQuery();

			while (rs.next()) {
				threads = rs.getInt(1);
			}
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(connection);
		}
		
		return threads;
	}

}
