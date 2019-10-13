package edu.oswego.model;

import java.sql.Date;

import edu.oswego.sentiment.SentimentScore;

public class Email {
	
	// SENT OR RECEIVED?
	
	private int id;
	private Date dateReceived;
	private String subject;
	private double size;
	private boolean isSeen;
	private String fileName;
	private boolean hasAttachment;
	
	private SentimentScore sentimentScore;
	private UserFolder folder;
	
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

	@Override
	public String toString() {
		return "Email [id=" + id + ", dateReceived=" + dateReceived + ", subject=" + subject + ", size=" + size
				+ ", isSeen=" + isSeen + ", fileName=" + fileName + ", hasAttachment=" + hasAttachment
				+ ", sentimentScore=" + sentimentScore + ", folder=" + folder + "]";
	}

}
