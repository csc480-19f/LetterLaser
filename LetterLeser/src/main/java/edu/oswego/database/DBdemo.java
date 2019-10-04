package edu.oswego.database;

import edu.oswego.props.Settings;

public class DBdemo {

	public static void main(String[] args) {
		Settings.loadCredentials();
		
		Database.truncateTables();
		Database.insertDummyData();
		Database.pull();
		Database.showTables();
		Database.fetchFavourites("first@gmail.com");
		
		Database.getLabels();
	}

} 