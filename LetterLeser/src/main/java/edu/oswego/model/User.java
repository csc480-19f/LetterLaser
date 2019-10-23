package edu.oswego.model;

import java.util.List;

/**
 * User object based on SQL table model: user
 * 
 * @author Jimmy Nguyen
 * @since 10/23/2019
 *
 */

public class User {
	
	private EmailAddress emailAddress;
	private List<UserFavourites> userFavourites;
	
	public User(EmailAddress emailAddress, List<UserFavourites> userFavourites) {
		this.emailAddress = emailAddress;
		this.userFavourites = userFavourites;
	}

	public EmailAddress getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(EmailAddress emailAddress) {
		this.emailAddress = emailAddress;
	}

	public List<UserFavourites> getUserFavourites() {
		return userFavourites;
	}

	public void setUserFavourites(List<UserFavourites> userFavourites) {
		this.userFavourites = userFavourites;
	}
	
}
