package edu.oswego.model;

import javax.mail.Folder;

/**
 * User Folder object based on SQL table model: user_folder
 * 
 * @author Jimmy Nguyen
 * @since 10/23/2019
 *
 */

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

	public Folder getFolder() {
		return folder;
	}

	@Override
	public String toString() {
		return "UserFolder [id=" + id + ", folder=" + folder + "]";
	}
	
}
