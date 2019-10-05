package edu.oswego.database;

public class DBdemo {

	public static void main(String[] args) {
		Settings.loadCredentials();
		
//		Database.truncateTables();
//		Database.insertDummyData();
		//Database.pull();
		System.out.println("PRIY: " + Database.priy());
//		Database.showTables();
		//Database.fetchFavourites("first@gmail.com");
		
//		Database.getLabels();
	}

} 