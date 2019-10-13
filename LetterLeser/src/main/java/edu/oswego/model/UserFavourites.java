package edu.oswego.model;

import java.sql.Date;

import edu.oswego.props.Interval;

public class UserFavourites {
	
	private int id;
	private String name;
	private Date startDate;
	private Interval intervalRange;
	private boolean hasAttachment;
	private boolean isSeen;
	private UserFolder folder;
	
	public UserFavourites(int id, String name, Date startDate, Interval intervalRange, boolean hasAttachment,
			boolean isSeen, UserFolder folder) {
		super();
		this.id = id;
		this.name = name;
		this.startDate = startDate;
		this.intervalRange = intervalRange;
		this.hasAttachment = hasAttachment;
		this.isSeen = isSeen;
		this.folder = folder;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Interval getIntervalRange() {
		return intervalRange;
	}

	public boolean isHasAttachment() {
		return hasAttachment;
	}

	public boolean isSeen() {
		return isSeen;
	}

	public UserFolder getFolder() {
		return folder;
	}

}