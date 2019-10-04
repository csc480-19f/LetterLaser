package edu.oswego.model;

import java.util.List;

public class User {
	
	private EmailAddress emailAddress;
	private List<UserFavourites> userFavourites;
	
	public User(EmailAddress emailAddress, List<UserFavourites> userFavourites) {
		super();
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
