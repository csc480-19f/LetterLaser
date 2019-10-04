package model;

import java.sql.Date;

public class UserFavourites {
	
	private int id;
	private String name;
	private Date startDate, endDate;
	private int intervalRange;
	private UserFolder folder;
	
	public UserFavourites(int id, String name, Date startDate, Date endDate, int intervalRange, UserFolder folder) {
		super();
		this.id = id;
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.intervalRange = intervalRange;
		this.folder = folder;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int getIntervalRange() {
		return intervalRange;
	}

	public void setIntervalRange(int intervalRange) {
		this.intervalRange = intervalRange;
	}

	public UserFolder getFolder() {
		return folder;
	}

	public void setFolder(UserFolder folder) {
		this.folder = folder;
	}

}