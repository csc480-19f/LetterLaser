package edu.oswego.model;

import javax.mail.Folder;

public class UserFolder {

	private int id;
	private Folder folder;

	public UserFolder(int id, Folder folder) {
		this.id = id;
		this.folder = folder;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Folder getFolder() {
		return folder;
	}

	public void setFolder(Folder folder) {
		this.folder = folder;
	}

	@Override
	public String toString() {
		return "UserFolder [id=" + id + ", folder=" + folder + "]";
	}
	
}
