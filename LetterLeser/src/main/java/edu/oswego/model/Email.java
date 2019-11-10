package edu.oswego.model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import edu.oswego.sentiment.SentimentScore;

/**
 * Email object based on SQL table model: email
 * 
 * @author Jimmy Nguyen
 * @since 10/28/2019
 *
 */

public class Email {
	
	private int id;
	private Date dateReceived;
	private String subject;
	private double size;
	private boolean isSeen;
	private String fileName;
	private boolean hasAttachment;

	private SentimentScore sentimentScore;
	private UserFolder folder;
	private List<EmailAddress> from;

	public Email(int id, Date dateReceived, String subject, double size, boolean isSeen, String fileName,
			boolean hasAttachment) {
		this.id = id;
		this.dateReceived = dateReceived;
		this.subject = subject;
		this.size = size;
		this.isSeen = isSeen;
		this.fileName = fileName;
		this.hasAttachment = hasAttachment;
		this.sentimentScore = null;
		this.folder = null;
		this.from = new ArrayList<>();
	}
	
	public Email(int id, Date dateReceived, String subject, double size, boolean isSeen, String fileName,
			boolean hasAttachment, SentimentScore sentimentScore) {
		this.id = id;
		this.dateReceived = dateReceived;
		this.subject = subject;
		this.size = size;
		this.isSeen = isSeen;
		this.fileName = fileName;
		this.hasAttachment = hasAttachment;
		this.sentimentScore = sentimentScore;
		this.from = new ArrayList<>();
	}
	
	public Email(int id, Date dateReceived, String subject, double size, boolean isSeen, String fileName,
			boolean hasAttachment, SentimentScore sentimentScore, UserFolder folder) {
		this.id = id;
		this.dateReceived = dateReceived;
		this.subject = subject;
		this.size = size;
		this.isSeen = isSeen;
		this.fileName = fileName;
		this.hasAttachment = hasAttachment;
		this.sentimentScore = sentimentScore;
		this.folder = folder;
		this.from = new ArrayList<>();
	}

	public Email(int id, Date dateReceived, String subject, double size, boolean isSeen, String fileName,
			boolean hasAttachment, SentimentScore sentimentScore, UserFolder folder, List<EmailAddress> from) {
		this.id = id;
		this.dateReceived = dateReceived;
		this.subject = subject;
		this.size = size;
		this.isSeen = isSeen;
		this.fileName = fileName;
		this.hasAttachment = hasAttachment;
		this.sentimentScore = sentimentScore;
		this.folder = folder;
		this.from = from;
	}

	public int getId() {
		return id;
	}

	public Date getDateReceived() {
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

	public String getFileName() {
		return fileName;
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
