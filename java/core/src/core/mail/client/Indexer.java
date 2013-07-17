/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

import java.util.Date;
import java.util.List;

import core.callback.Callback;
import core.callback.CallbackChain;
import core.callback.CallbackDefault;
import core.callbacks.CountDown;
import core.callbacks.Single;
import core.callbacks.Split;
import core.util.LogNull;
import core.util.LogOut;
import mail.client.cache.ID;
import mail.client.cache.Type;
import mail.client.model.Body;
import mail.client.model.Conversation;
import mail.client.model.Dictionary;
import mail.client.model.Direction;
import mail.client.model.Folder;
import mail.client.model.FolderDefinition;
import mail.client.model.FolderFilter;
import mail.client.model.FolderMaster;
import mail.client.model.FolderRepository;
import mail.client.model.FolderSet;
import mail.client.model.Header;
import mail.client.model.Mail;
import mail.client.model.TransportState;

public class Indexer extends Servent<Master>
{
	static LogNull log = new LogNull(Indexer.class);
	
	public static class KnownFolderIds {
		static final 
			ID RootCache = ID.fromLong(1),
			SystemFoldersId = ID.combine(RootCache, ID.fromLong(1)),
			RepositoryId = ID.combine(RootCache, ID.fromLong(2)),
			AllId = ID.combine(RootCache, ID.fromLong(3)),
			InboxId = ID.combine(RootCache, ID.fromLong(4)),
			SentId = ID.combine(RootCache, ID.fromLong(5)),
			DraftsId = ID.combine(RootCache, ID.fromLong(6)),
			SpamId = ID.combine(RootCache, ID.fromLong(7)),
			TrashId = ID.combine(RootCache, ID.fromLong(8)),
			
			UserFoldersId = ID.combine(RootCache, ID.fromLong(100));
			
	};

	FolderMaster systemFolders;
	FolderSet userFolders;
	FolderRepository repository;
	FolderFilter spam;
		
	public Indexer () 
	{
	}
	
	public void firstRunInitialization ()
	{
		log.debug("firstRunInitialization");
		
		CacheManager cache = master.getCacheManager();
		
		systemFolders = (FolderMaster) 
			cache.linkFolder(
				KnownFolderIds.SystemFoldersId, 
				Type.FolderMaster,	
				new FolderDefinition("system")
			);
		systemFolders.markCreate();
		
		userFolders = (FolderSet)
			cache.linkFolder(
				KnownFolderIds.UserFoldersId,
				Type.FolderFilterSet,
				new FolderDefinition("user")
			);
		userFolders.markCreate();
		
		repository = (FolderRepository)cache.linkFolder(
			KnownFolderIds.RepositoryId,
			Type.FolderRepository,
			new FolderDefinition(Constants.REPOSITORY)
		);
		repository.markCreate();
		systemFolders.addFolder(repository);
		
		Folder fs;
		fs = cache.linkFolder(
			KnownFolderIds.AllId,
			Type.FolderFilter,
			new FolderDefinition(Constants.ALL)
				.setState(
					null, 
					TransportState.fromList(TransportState.TRASH,TransportState.SPAM)
				)
		);
		fs.markCreate();
		systemFolders.addFolder(fs);

		fs = cache.linkFolder(
			KnownFolderIds.InboxId,
			Type.FolderFilter,
			new FolderDefinition(Constants.INBOX)
				.setState(
					TransportState.fromList(TransportState.RECEIVED), 
					TransportState.fromList(TransportState.TRASH,TransportState.SPAM)
				)
		);
		fs.markCreate();
		systemFolders.addFolder(fs);
		
		fs = cache.linkFolder(
			KnownFolderIds.SentId,
			Type.FolderFilter,
			new FolderDefinition(Constants.SENT)
				.setState(
					TransportState.fromList(TransportState.SENT), 
					TransportState.fromList(TransportState.TRASH,TransportState.SPAM)
				)
		);
		fs.markCreate();
		systemFolders.addFolder(fs);

		fs = cache.linkFolder(
			KnownFolderIds.DraftsId,
			Type.FolderFilter,
			new FolderDefinition(Constants.DRAFTS)
				.setState(
					TransportState.fromList(TransportState.DRAFT, TransportState.SENDING), 
					TransportState.fromList(TransportState.TRASH, TransportState.SPAM)
				)
		);
		fs.markCreate();
		systemFolders.addFolder(fs);
		
		spam = (FolderFilter)
			cache.linkFolder(
				KnownFolderIds.SpamId,
				Type.FolderFilter,
				new FolderDefinition(Constants.SPAM)
					.setState(
						TransportState.fromList(TransportState.SPAM),
						TransportState.fromList(TransportState.TRASH)
					)
					.setBayesianDictionary(new Dictionary())
					.setAutoBayesian(false)
			);
		
		spam.markCreate();
		systemFolders.addFolder(spam);

		fs = cache.linkFolder(
			KnownFolderIds.TrashId,
			Type.FolderFilter,
			new FolderDefinition(Constants.TRASH)
				.setState(
					TransportState.fromList(TransportState.TRASH), 
					null
				)
		);
		fs.markCreate();
		systemFolders.addFolder(fs);
		
		master.getEventPropagator().signal(Events.Initialize_FolderLoadComplete, (Object[])null);
	}
	
	public void start ()
	{
		log.debug("indexer starting..");
		
		CacheManager cache = master.getCacheManager();
		
		systemFolders = (FolderMaster)cache.getFolder(
			Type.FolderMaster,
			KnownFolderIds.SystemFoldersId
		);

		systemFolders.apply(
			new CallbackDefault() {
				public void onSuccess(Object... arguments) throws Exception {
					onMainFolderSucceeded((Folder)arguments[0]);
				}
				
				public void onFailure(Exception e) {
					onMainFolderFailed(e);
				}
			}
		);
	}
	
	public void onMainFolderSucceeded (Folder f)
	{
		log.debug("onMainFolderSucceeded");
		systemFolders = (FolderMaster)f;
		CacheManager cache = master.getCacheManager();
		repository = (FolderRepository)cache.getFolder(Type.FolderRepository, KnownFolderIds.RepositoryId);
		spam = (FolderFilter)cache.getFolder(Type.FolderFilter, KnownFolderIds.SpamId);
		userFolders = (FolderSet)cache.getFolder(Type.FolderFilterSet, KnownFolderIds.UserFoldersId);
		
		// cause subfolders to instantiate right away
		List<Folder> folders = systemFolders.getFolders();
		Callback countDown = 
			new CountDown(
				folders.size(), 
				getMaster().getEventPropagator().signal_(Events.Initialize_FolderLoadComplete)
			);
		
		for (Folder folder : folders)
			folder.apply(new Split(countDown));
	}	
	
	public void onMainFolderFailed (Exception e)
	{
		log.debug("onMainFolderFailed");
		log.exception(e);
	}

	public synchronized Conversation addMail (Mail mail) throws Exception
	{
		Header header = mail.getHeader();
		boolean isNewConversation;
		Conversation conversation = repository.getMatchingConversation(header);
		if (conversation != null)
		{
			log.debug("founding matching conversation");
			conversation.addItem(mail);
			isNewConversation = false;
		}
		else
		{
			log.debug("new conversation");
			conversation = master.getCacheManager().newConversation(mail);
			isNewConversation = true;
		}

		// before we do anything we do the spam detection
		conversation.getHeader().getTransportState().mark(
			TransportState.SPAM, spam.getFolderDefinition().bayesianMatches(conversation)
		);
		
		if (isNewConversation)
			addConversation(conversation);
		else
			conversationChanged (conversation);

		
		// if the mail is new, no external key yet
		if (mail.getHeader().getExternalKey()!=null)
		{
			log.debug("adding externalKey", mail.getHeader().getExternalKey());
			systemFolders.addExternalKey(mail.getHeader().getExternalKey(), mail.getHeader().getDate());
		}
		
		if (mail.getHeader().getUIDL() != null)
		{
			log.debug("adding UIDL", mail.getHeader().getUIDL());
			systemFolders.addUIDL(mail.getHeader().getUIDL(), mail.getHeader().getDate());
		}
		
		return conversation;
	}	
	
	protected void addFailure (Mail mail) throws Exception
	{
		addMail (mail);
	}
	
	protected void addFailure (String externalKey, Date date)
	{
		systemFolders.addExternalKey(externalKey, date);
	}
	
	public void addFailure (Direction direction, String path, Date date, Exception e)
	{
		try
		{
			e.printStackTrace();
			
			Header header = new Header();
			header.setSubject("Mail failed parsing.  Look at original for file.");
			header.setExternalKey(path);
			header.setDate(date);
			if (direction == Direction.IN)
				header.setTransportState(TransportState.fromList(TransportState.RECEIVED));
			else
				header.setTransportState(TransportState.fromList(TransportState.SENT));
			
			Body body = new Body();
			body.setText("Failed to load: " + e);
			
			Mail mail = master.getCacheManager().newMail(header, body, null);
			addFailure(mail);
		}
		catch (Exception em)
		{
			em.printStackTrace();
			addFailure(path, date);
		}
	}
	
	public void addDuplicate (String externalKey, Date date)
	{
		systemFolders.addExternalKey(externalKey, date);
	}
	
	public boolean containsExternalKey (String externalKey)
	{
		return systemFolders.containsExternalKey(externalKey);
	}
	
	public boolean containsUIDL(String uidl)
	{
		return systemFolders.containsUIDL(uidl);
	}
	
	public synchronized void addConversation (Conversation conversation)
	{
		for (Folder e : systemFolders.getFolders())
			e.conversationAdded(conversation);
		
		for (Folder e : userFolders.getFolders())
			e.conversationAdded(conversation);
	}
	
	public synchronized void removeConversation (Conversation conversation)
	{
		for (Folder e : systemFolders.getFolders())
			e.conversationDeleted(conversation);
		
		for (Folder e : userFolders.getFolders())
			e.conversationDeleted(conversation);
	}
	
	public synchronized void conversationChanged (Conversation conversation)
	{
		if (conversation != null)
		{
			for (Folder f : systemFolders.getFolders())
				f.conversationChanged(conversation);
			
			for (Folder e : userFolders.getFolders())
				e.conversationChanged(conversation);
		}
		
		master.getEventPropagator().signalOnce(Events.ChangedConversation);
	}
	
	public void replyMail (Conversation conversation, Mail mail)
	{
		systemFolders.addUIDL(mail.getHeader().getUIDL(), mail.getHeader().getDate());
		
		conversationChanged(conversation);
	}
	
	public Conversation newMail (Mail mail) throws Exception
	{
		return addMail(mail);
	}

	public Folder getSystemFolder (String folderName)
	{
		for (Folder e : systemFolders.getFolders())
			if (e.isLoaded())
				if (e.getFolderDefinition().getName().equals(folderName))
					return e;
		
		return null;
	}
	
	public FolderFilter getUserFolder (String folderName)
	{
		for (Folder e: userFolders.getFolders())
			if (e.isLoaded())
				if (e.getFolderDefinition().getName().equals(folderName))
					return (FolderFilter)e;

		return null;
	}
	
	public List<Folder> getSystemFolders ()
	{
		return systemFolders.getFolders();
	}
	
	public List<Folder> getUserFolders ()
	{
		return userFolders.getFolders();
	}
	
	public Folder getRepository ()
	{
		return repository;
	}

	public void newUserFolder(String name) 
	{
		userFolders.addFolder(
			getMaster().getCacheManager().newFolder(
				Type.FolderFilter,
				new FolderDefinition(name)
					.setBayesianDictionary(new Dictionary())
					.setAutoBayesian(false)
					.setState(null, TransportState.fromList(TransportState.SPAM, TransportState.TRASH))
			)
		);
	}
	
	public void deleteUserFolder(Folder userFolder)
	{
		userFolders.removeFolder(userFolder);
	}

	public void addToUserFolder(Folder userFolder, Conversation conversation) 
	{
		FolderFilter folder = (FolderFilter)userFolder;
		folder.manuallyAdd (conversation);
	}
	
	public void removeFromUserFolder(Folder userFolder, Conversation conversation)
	{
		FolderFilter folder = (FolderFilter)userFolder;
		folder.manuallyRemove (conversation);
	}

	public FolderSet getInbox() 
	{
		return (FolderSet)getMaster().getCacheManager().getFolder(Type.FolderFilter, KnownFolderIds.InboxId);
	}
}
