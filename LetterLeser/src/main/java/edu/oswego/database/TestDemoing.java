package edu.oswego.database;

import edu.oswego.mail.Mailer;
import edu.oswego.props.Settings;

public class TestDemoing {

	public static void main(String[] args) {
		Settings.loadCredentials();

		Mailer mailer = new Mailer("AUTH_KEY_INSERTED_HERE");
		Database db = new Database("csc344testacc@gmail.com", mailer);

		db.truncateTables();
		db.query("INSERT INTO user (email_address) VALUE ('pn@gmail.com')");
	}

}
