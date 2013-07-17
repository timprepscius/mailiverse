/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.callbacks.CountDown;
import core.callbacks.Split;
import core.callbacks.SuccessFailure;
import core.connector.async.AsyncStoreConnector;
import core.constants.ConstantsClient;
import core.constants.ConstantsClient;
import core.constants.ConstantsSettings;
import core.crypt.CryptorSeed;
import core.util.LogNull;
import core.util.LogOut;
import mail.client.cache.Cache;
import mail.client.cache.ID;
import mail.client.cache.IndexedCache;
import mail.client.cache.IndexedCacheSerializer;
import mail.client.cache.ItemCacheFactory;
import mail.client.cache.ItemSerializer;
import mail.client.cache.JSON;
import mail.client.cache.StoreFactory;
import mail.client.cache.StoreLibrary;
import mail.client.cache.Type;
import mail.client.model.Attachments;
import mail.client.model.Body;
import mail.client.model.Conversation;
import mail.client.model.Folder;
import mail.client.model.FolderDefinition;
import mail.client.model.Header;
import mail.client.model.Model;
import mail.client.model.ModelFactory;
import mail.client.model.Mail;
import mail.client.model.ModelSerializer;
import mail.client.model.Settings;

public class CacheManager extends Servent<Master>
{
	static LogNull log = new LogNull(CacheManager.class);
	
	Cache masterCache;
	IndexedCache cacheMail;
	IndexedCache cacheConversation;
	IndexedCache cacheFolder;
	Settings settings;
	
	boolean isCaching = false;
	
	ModelFactory itemFactory;
	
	StoreLibrary library;
	AsyncStoreConnector connector;
		
	public CacheManager (CryptorSeed cryptorSeed, AsyncStoreConnector connector)
	{
		this.connector = connector;
		this.itemFactory = new ModelFactory(this);
		library = new StoreLibrary(cryptorSeed, new StoreFactory(85 * 1024), connector);
	}
	
	public void onModelDirty ()
	{
		log.debug("markDirty");
		master.getEventPropagator().signal(Events.CacheDirty, (Object[])null);
	}
	
	public void start ()
	{
		log.debug("start");
		
		JSON json = getMaster().getJSON();

		this.masterCache = new Cache(
			null,
			new MasterCacheSerializer(json),
			library.instantiate("I", null, false) // false because I'm manually initiating below .start(...)
		);
		
		this.settings = new Settings(this);
		this.settings.setId(Constants.SETTINGS_ID);
		this.masterCache.link(settings);
		
		ItemSerializer itemSerializer = new ModelSerializer(json);
		
		this.cacheMail = new IndexedCache(
			new ItemCacheFactory ("M", library, itemFactory, itemSerializer)
		);
		this.cacheMail.setId(Constants.MAIL_ID);
		masterCache.link(cacheMail);
		
		this.cacheConversation = new IndexedCache(
			new ItemCacheFactory ("C", library, itemFactory, itemSerializer)
		);
		this.cacheConversation.setId(Constants.CONVERSATION_ID);
		masterCache.link(cacheConversation);

		this.cacheFolder = new IndexedCache(
			new ItemCacheFactory ("F", library, itemFactory, itemSerializer)
		);
		this.cacheFolder.setId(Constants.FOLDER_ID);
		masterCache.link(cacheFolder);
		
		//-------------------------------------------------------------
		
		Callback countDown = 
			new CountDown(
				4,
				getMaster().getEventPropagator().signal_(Events.Initialize_IndexedCacheLoadComplete)
			);
			
		// some how I need to watch when the indexCaches have been loaded
		settings.apply(new Split(countDown));
		cacheMail.apply(new Split(countDown));
		cacheConversation.apply(new Split(countDown));
		cacheFolder.apply(new Split(countDown));
		
		//-------------------------------------------------------------
		
		library.start(
			new SuccessFailure(
				getMaster().getEventPropagator().signal_(Events.Initialize_IndexedCacheAcquired, (Object[])null),
				getMaster().getEventPropagator().signal_(Events.Initialize_IndexedCacheLoadFailed, (Object[])null)
			)
		);
	}
	
	public void deserializeIndexCaches ()
	{
		log.debug("deserializeIndexCaches");
	}
	
	public void firstRunInitialization ()
	{
		log.debug("firstRunInitialization");

		// we create the cache the root folders are in
		cacheFolder.newCache(Indexer.KnownFolderIds.RootCache);
		
		cacheMail.markCreate();
		cacheConversation.markCreate();
		cacheFolder.markCreate();
		masterCache.markCreate();
		
		settings.markCreate();
		settings.set(Settings.VERSION, Settings.CURRENT_VERSION);
	}
	
	public Settings getSettings ()
	{
		return settings;
	}
	
	public String createUIDL (ID id)
	{
		return "<" + id + ConstantsClient.ATHOST + ">";
	}
	
	public Mail newMail(Header header, Body body, Attachments attachments) throws Exception
	{
		log.debug("newMail");
		
		
		Mail mail = (Mail)itemFactory.instantiate(Type.Mail);
		cacheMail.put(mail);

		mail.setHeader(header);
		
		// if there is no UIDL we supply one, based off of the external key
		if (header.getUIDL()==null)
			header.setUIDL(createUIDL(mail.getId()));
		
		mail.setBody(body);
		mail.setAttachments(attachments);

		master.getEventPropagator().signalOnce(Events.NewMail, mail);
		return mail;
	}
	
	public void deleteMail(Mail mail)
	{
		log.debug("deleteMail");
		master.getEventPropagator().signalOnce(Events.DeleteMail, mail);
		mail.markDeleted();
	}
	
	public Conversation newConversation (Mail mail) throws Exception
	{
		log.debug("newConversation");
		
		Conversation conversation = (Conversation)itemFactory.instantiate(Type.Conversation);
		cacheConversation.put(conversation);
		
		conversation.addItem(mail);
		master.getEventPropagator().signalOnce(Events.NewConversation, conversation);
		
		return conversation;
	}
	
	public void deleteConversation(Conversation conversation)
	{
		log.debug("deleteConversation");
		master.getEventPropagator().signalOnce(Events.DeleteConversation, conversation);
		conversation.markDeleted();
	}
	
	public Folder newFolder(Type type, FolderDefinition folderDefinition)
	{
		log.debug("newFolder", type);
		Folder folder = (Folder)itemFactory.instantiate(type);
		cacheFolder.put(folder);
		
		folder.setFolderDefinition(folderDefinition);
		master.getEventPropagator().signalOnce(Events.NewFolder, folder);
		
		return folder;
	}
	
	public Folder linkFolder(ID id, Type type, FolderDefinition folderDefinition)
	{
		log.debug("newFolder", type, id);
		Folder folder = (Folder)itemFactory.instantiate(type);
		cacheFolder.link(id, folder);
		
		folder.setFolderDefinition(folderDefinition);
		master.getEventPropagator().signalOnce(Events.NewFolder, folder);
		
		return folder;
	}
	
	public Mail getMail(ID uid)
	{
		return (Mail) cacheMail.getAndAcquire(Type.Mail, uid);
	}
	
	public void putMail(Mail m)
	{
		cacheMail.put(m);
	}
	
	public Conversation getConversation(ID uid)
	{
		return (Conversation) cacheConversation.getAndAcquire(Type.Conversation, uid);
	}
	
	public void putConversation(Conversation c)
	{
		cacheConversation.put(c);
	}
	
	public Folder getFolder(Type type, ID id)
	{
		return (Folder)cacheFolder.getAndAcquire(type, id);
	}
	
	public void putFolder(Folder f)
	{
		cacheFolder.put(f);
	}
	
	public boolean isFullyCached ()
	{
		return (!library.hasDirtyChildren() && !masterCache.hasDirtyChildren());
	}
	
	public Callback onCacheFinished_ () 
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				isCaching = false;
				if (isFullyCached())
					master.getEventPropagator().signal(Events.CacheClean, (Object[])null);

				master.getEventPropagator().signal(Events.CacheSuccess, (Object[])null);
				master.getEventPropagator().signal(Events.CacheEnd, (Object[])null);
				
				next(arguments);
			}
			
			public void onFailure (Exception e)
			{
				isCaching = false;
				master.getEventPropagator().signal(Events.CacheFailure, e);
				master.getEventPropagator().signal(Events.CacheEnd, (Object[])null);
				
				next(e);
			}
		};
	}
	
	public void flush ()
	{
		if (isCaching)
			return;
		
		if (getMaster().getArrivalsMonitor().isChecking())
			return;
		
		if (isFullyCached())
			return;
		
		doFlush();
	}
	
	protected void doFlush ()
	{
		log.debug("doFlush");
		
		isCaching = true;
		master.getEventPropagator().signal(Events.CacheBegin, (Object[])null);
		
		masterCache.debug_()
			.addCallback(masterCache.flush_())
			.addCallback(masterCache.checkClean_())
			.addCallback(masterCache.debug_())
			.addCallback(library.flush_())
			.addCallback(masterCache.debug_())
			.addCallback(onCacheFinished_())
			.invoke();
	}
	
	public Callback update_ ()
	{
		return library.update_(false);
	}
	
	public void update ()
	{
		update_().invoke();
	}
	
	public void debug ()
	{
		masterCache.debug_().invoke();
	}
	
	public void onSettingsChanged (Settings settings)
	{
		getMaster().getIdentity().setName(settings.get(ConstantsSettings.USERNAME));
	}
}
