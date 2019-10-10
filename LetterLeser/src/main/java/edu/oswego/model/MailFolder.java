package edu.oswego.model;

import javax.mail.Folder;
@Deprecated
public interface MailFolder {
	
	int getId();
	Folder getFolder();
	
	// don't need anymore. Was used when we needed to distinquish labels/folders.

}
