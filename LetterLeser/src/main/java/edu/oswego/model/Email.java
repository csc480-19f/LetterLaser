package edu.oswego.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Email object based on SQL table model: email
 * 
 * @author Jimmy Nguyen
 * @since 10/28/2019
 *
 */

public class Email {

	private int id;
	private Timestamp dateReceived;
	private String subject;
	private double size;
	private boolean isSeen;
	private boolean hasAttachment;

	private SentimentScore sentimentScore;
	private UserFolder folder;
	private List<EmailAddress> from;

	// TODO ADD USERFOLDER?

	public Email(int id, Date dateReceived, String subject, double size, boolean isSeen, boolean hasAttachment,
			UserFolder folder) {
		this.id = id;
		this.dateReceived = new Timestamp(dateReceived.getTime());
//		this.dateReceived = dateReceived;
		this.subject = subject;
		this.size = size;
		this.isSeen = isSeen;
		this.hasAttachment = hasAttachment;
		this.sentimentScore = null;
		this.folder = folder;
		this.from = new ArrayList<>();
	}

	public Email(int id, Date dateReceived, String subject, double size, boolean isSeen, boolean hasAttachment,
			SentimentScore sentimentScore) {
		this.id = id;
		this.dateReceived = new Timestamp(dateReceived.getTime());
//		this.dateReceived = dateReceived;
		this.subject = subject;
		this.size = size;
		this.isSeen = isSeen;
		this.hasAttachment = hasAttachment;
		this.sentimentScore = sentimentScore;
		this.from = new ArrayList<>();
	}

	public Email(int id, Date dateReceived, String subject, double size, boolean isSeen, boolean hasAttachment,
			SentimentScore sentimentScore, UserFolder folder) {
		this.id = id;
		this.dateReceived = new Timestamp(dateReceived.getTime());
//		this.dateReceived = dateReceived;
		this.subject = subject;
		this.size = size;
		this.isSeen = isSeen;
		this.hasAttachment = hasAttachment;
		this.sentimentScore = sentimentScore;
		this.folder = folder;
		this.from = new ArrayList<>();
	}

	public Email(int id, Date dateReceived, String subject, double size, boolean isSeen, boolean hasAttachment,
			SentimentScore sentimentScore, UserFolder folder, List<EmailAddress> from) {
		this.id = id;
		this.dateReceived = new Timestamp(dateReceived.getTime());
//		this.dateReceived = dateReceived;
		this.subject = subject;
		this.size = size;
		this.isSeen = isSeen;
		this.hasAttachment = hasAttachment;
		this.sentimentScore = sentimentScore;
		this.folder = folder;
		this.from = from;
	}

	public int getId() {
		return id;
	}

	public Timestamp getDateReceived() {
		return dateReceived;
	}

	public String getSubject() {
		return subject;
	}

	public double getSize() {
		return size;
	}

	public boolean isSeen() {
		return isSeen;
	}

	public boolean hasAttachment() {
		return hasAttachment;
	}

	public SentimentScore getSentimentScore() {
		return sentimentScore;
	}

	public UserFolder getFolder() {
		return folder;
	}

	public List<EmailAddress> getFrom() {
		return from;
	}

}
