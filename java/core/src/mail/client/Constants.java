/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

import mail.client.cache.ID;

public class Constants 
{
	static final public String DEFAULT = "Default";
	
	public static final String 
		PART = "_PART#";
	
	public static final String 
		REPOSITORY = "Repository",
		ALL = "All",
		INBOX = "Inbox",
		SENT = "Sent",
		SPAM = "Spam",
		DRAFTS = "Drafts",
		TRASH = "Trash";

	public static final ID
		SETTINGS_ID = ID.fromLong(1),
		MAIL_ID = ID.fromLong(2),
		CONVERSATION_ID = ID.fromLong(3),
		FOLDER_ID = ID.fromLong(4);
}
