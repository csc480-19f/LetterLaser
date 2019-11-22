package edu.oswego.model;

public class ReceivedEmail {

	private Email email;
	private EmailAddress emailAddress;

	public ReceivedEmail(Email email, EmailAddress emailAddress) {
		this.email = email;
		this.emailAddress = emailAddress;
	}

	public Email getEmail() {
		return email;
	}

	public void setEmail(Email email) {
		this.email = email;
	}

	public EmailAddress getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(EmailAddress emailAddress) {
		this.emailAddress = emailAddress;
	}

}
