/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package app.service;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import core.constants.ConstantsMailJson;
import core.constants.ConstantsClient;
import core.constants.ConstantsSettings;
import core.constants.ConstantsStorage;
import core.util.JSON_;
import core.util.JSON_.JSONException;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Pair;
import core.util.Strings;

import mail.client.Client;
import mail.client.model.Body;
import mail.client.model.Conversation;
import mail.client.model.Folder;
import mail.client.model.Header;
import mail.client.model.Identity;
import mail.client.model.Mail;
import mail.client.model.Original;
import mail.client.model.Settings;

@Export()
public class JSClient implements Exportable
{
	static LogNull log = new LogNull(JSClient.class);

	Client client;
	
	public JSClient (Client client)
	{
		this.client = client;
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSAppletInterface#buildAddressList(java.lang.String)
	 */
	public List<Identity> buildAddressList(String addresses)
	{
		return client.getMaster().getAddressBook().parseAddressString(addresses);
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSAppletInterface#getDaysLeft(java.lang.Object)
	 */
	public void getDaysLeft (Object callback) throws Exception
	{
		client.getHttpDelegate().execute(
			"POST", 
			ConstantsClient.WEB_SERVER_TOMCAT + "DaysLeft", 
			null, false, false, Strings.toBytes(client.getMaster().getIdentity().getEmail()), new JSResult<String>(callback)
		);
	}
	
	public void flushStore () throws IOException
	{
		try
		{
			log.debug("flushStore");
			
			client.getMaster().getCacheManager().flush();
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	public void updateStore () throws IOException
	{
		try
		{
			log.debug("flushStore");
			
			client.getMaster().getCacheManager().update();
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	public void debugStore () throws IOException
	{
		try
		{
			log.debug("debugStore");
			
			client.getMaster().getCacheManager().debug();
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	public String getSignatureHTML ()
	{
		String signature = client.getMaster().getCacheManager().getSettings().get(ConstantsSettings.SIGNATURE, "");
		return ("\r\n\r\n" + signature + "\r\n").replaceAll("\r\n", "<br>");
	}
	
	public Body calculateReplyBody (MailI mail)
	{
		return calculateReplyBody(mail.deref());
	}
	
	public Body calculateReplyBody (Mail mail)
	{
		return client.getMaster().getActions().calculateSignaturedReplyBody(mail);
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSAppletInterface#sendMail(mail.client.core.Conversation, mail.client.core.Mail)
	 */
	public void sendMail(Conversation conversation, Mail mail)
	{
		try
		{
			client.getMaster().getActions().sendMail(conversation, mail);
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	public void sendMail(Conversation conversation, MailI mail)
	{
		sendMail(conversation, mail.deref());
	}

	
	/* (non-Javadoc)
	 * @see app.service.JSAppletInterface#saveMail(mail.client.core.Conversation, mail.client.core.Mail)
	 */
	public void saveMail(Conversation conversation, Mail mail)
	{
		try
		{
			client.getMaster().getActions().saveMail(conversation, mail);
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	public void saveMail(Conversation conversation, MailI mail)
	{
		saveMail(conversation, mail.deref());
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSAppletInterface#deleteMail(mail.client.core.Conversation, mail.client.core.Mail)
	 */
	public void deleteMail(Conversation conversation, Mail mail)
	{
		try
		{
			client.getMaster().getActions().deleteMail(conversation,mail);
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	public void deleteMail(Conversation conversation, MailI mail)
	{
		deleteMail(conversation, mail.deref());
	}
	
	
	/* (non-Javadoc)
	 * @see app.service.JSAppletInterface#reindexConversation(mail.client.core.Conversation)
	 */
	public void reindexConversation(Conversation conversation)
	{
		try
		{
			client.getMaster().getActions().reindexConversation(conversation);
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSAppletInterface#deleteConversation(mail.client.core.Conversation)
	 */
	public void deleteConversation(Conversation conversation)
	{
		try
		{
			client.getMaster().getActions().deleteConversation(conversation);
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSAppletInterface#checkMail()
	 */
	public void checkMail ()
	{
		log.debug("checkMail");
		
		try
		{
			client.getMaster().getArrivalsMonitor().check();
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSAppletInterface#getFolders()
	 */
	public String[] getSystemFolders ()
	{
		log.debug("getFolders");
		List<String> result = new ArrayList<String>();
		
		List<Folder> folders = client.getMaster().getIndexer().getSystemFolders();
		for (Folder f : folders)
			if (f.isLoaded())
				result.add(f.getId().toString());
		
		return result.toArray(new String[0]);
	}
	
	public String[] getUserFolders ()
	{
		log.debug("getFolders");
		List<String> result = new ArrayList<String>();
		
		List<Folder> folders = client.getMaster().getIndexer().getUserFolders();
		for (Folder f : folders)
			if (f.isLoaded())
				result.add(f.getId().toString());
		
		return result.toArray(new String[0]);
	}

	/* (non-Javadoc)
	 * @see app.service.JSAppletInterface#getFolder(java.lang.String)
	 */
	public Folder getSystemFolder (String folder)
	{
		log.debug("getSystemFolder");
		List<Folder> folders = client.getMaster().getIndexer().getSystemFolders();
		for (Folder f : folders)
			if (f.isLoaded())
				if (f.getId().toString() == folder)
					return f;
		
		return null;
	}

	public Folder getUserFolder (String folder)
	{
		log.debug("getUserFolder");
		List<Folder> folders = client.getMaster().getIndexer().getUserFolders();
		for (Folder f : folders)
			if (f.isLoaded())
				if (f.getId().toString() == folder)
					return f;
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSAppletInterface#replyTo(mail.client.core.Conversation, mail.client.core.Mail, mail.client.core.Body)
	 */
	public Mail replyTo (Conversation conversation, Mail mail)
	{
		try
		{
			return client.getMaster().getActions().replyTo(conversation, mail);
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	public MailI replyTo (Conversation conversation, MailI mail)
	{
		return new MailI(replyTo(conversation, mail.deref()));
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSAppletInterface#replyToAll(mail.client.core.Conversation, mail.client.core.Mail, mail.client.core.Body)
	 */
	public Mail replyToAll (Conversation conversation, Mail mail)
	{
		try
		{
			return client.getMaster().getActions().replyToAll(conversation, mail);
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	public MailI replyToAll (Conversation conversation, MailI mail)
	{
		return new MailI(replyToAll(conversation, mail.deref()));
	}
	

	/* (non-Javadoc)
	 * @see app.service.JSAppletInterface#forward(mail.client.core.Conversation, mail.client.core.Mail)
	 */
	public Mail forward (Conversation conversation, Mail mail)
	{
		try
		{
			return client.getMaster().getActions().forward(conversation, mail);
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}

	public MailI forward (Conversation conversation, MailI mail)
	{
		return new MailI(forward(conversation, mail.deref()));
	}
	
	public Object[] newMail ()
	{
		try
		{
			Pair<Conversation,Mail> pair = client.getMaster().getActions().newMail();
			return new Object[] { pair.first, pair.second };
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSAppletInterface#getAddressList()
	 */
	public Identity[] getAddressList ()
	{
		try
		{
			return client.getMaster().getAddressBook().getAddressList().toArray(new Identity[0]);
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	public Original getOriginal(Mail mail)
	{
		try
		{
			log.debug("getOriginal: ",new Date());
			
			String originalKey = mail.getHeader().getOriginalKey();
			return client.getMaster().getStore().getOriginal(originalKey);
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	public Original getOriginal(MailI mail)
	{
		return getOriginal(mail.deref());
	}	
	
	public void loadAttachments(Mail mail)
	{
		try
		{
			log.debug("loadAttachments: ",new Date());
			client.getMaster().getStore().loadAttachments(mail);
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}

	public void loadAttachments(MailI mail)
	{
		loadAttachments(mail.deref());
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSAppletInterface#getMinimalJSONForConversations(java.lang.Object[])
	 */
	public String getMinimalJSONForConversations (Object[] conversations) throws JSONException
	{
		Object a = JSON_.newArray();
		
		for (Object o : conversations)
		{
			Conversation c = (Conversation)o;
			
			Object json;
			try
			{
				json = JSON_.newObject();
				
				JSON_.put(json, "loaded", JSON_.newBoolean(c.isLoaded()));
				if (c.isLoaded())
				{
					List<String> shortNames = new ArrayList<String>(c.getHeader().getAuthors().size());
					for (Identity i : c.getHeader().getAuthors())
						shortNames.add(i.getShortName());
		
					Header h = c.getHeader();
					JSON_.put(json, "participants", JSON_.newString(Strings.concat(shortNames,", ")));
					JSON_.put(json, "numItems", JSON_.newNumber(c.getNumItems()));
					JSON_.put(json, "subject", JSON_.newString(h.getSubject()!=null?h.getSubject():"No subject"));
					JSON_.put(json, "brief", JSON_.newString(h.getBrief()!=null?h.getBrief():"No brief"));
					JSON_.put(json, "date",JSON_.newString(c.getHeader().getRelativeDate()));
					JSON_.put(json, "state", JSON_.newString(c.getHeader().getTransportState().toString()));
				}
			}
			catch (Exception e)
			{
				log.debug("this " + c + " header " + c.getHeader());
				log.exception(e);
				json = JSON_.newObject();
				JSON_.put(json, "loaded", JSON_.newBoolean(false));
			}
			
			JSON_.add(a,json);
		}
		
		return a.toString();
	}
	
	public void newUserFolder ()
	{
		client.getMaster().getIndexer().newUserFolder("New Folder");
	}
	
	public void deleteUserFolder (String userFolder)
	{
		client.getMaster().getIndexer().deleteUserFolder(getUserFolder(userFolder));
	}
	
	public void addToUserFolder(String userFolder, Conversation conversation)
	{
		client.getMaster().getIndexer().addToUserFolder(
			getUserFolder(userFolder), conversation
		);
	}

	public void removeFromUserFolder(String userFolder, Conversation conversation)
	{
		client.getMaster().getIndexer().removeFromUserFolder(
			getUserFolder(userFolder), conversation
		);
	}
	
	public Settings getSettings ()
	{
		return client.getMaster().getCacheManager().getSettings();
	}
}
