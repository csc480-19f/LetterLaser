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

	public void setId(int id) {
		this.id = id;
	}

	public Date getDateReceived() {
		return dateReceived;
	}

	public void setDateReceived(Date dateReceived) {
		this.dateReceived = dateReceived;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public boolean isSeen() {
		return isSeen;
	}

	public void setSeen(boolean isSeen) {
		this.isSeen = isSeen;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean hasAttachment() {
		return hasAttachment;
	}

	public void setAttachment(boolean hasAttachment) {
		this.hasAttachment = hasAttachment;
	}

	public SentimentScore getSentimentScore() {
		return sentimentScore;
	}

	public void setSentimentScore(SentimentScore sentimentScore) {
		this.sentimentScore = sentimentScore;
	}

	public UserFolder getFolder() {
		return folder;
	}

	public void setFolder(UserFolder folder) {
		this.folder = folder;
	}
	
}
