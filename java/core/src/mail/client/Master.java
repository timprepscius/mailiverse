/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

import mail.client.cache.JSON;
import mail.client.model.AddressBook;
import mail.client.model.Identity;
import core.crypt.Cryptor;
import core.util.Environment;

public class Master 
{
	protected Initializer initializer;
	protected Store store;
	protected Identity identity;
	protected Environment environment;
	protected AddressBook addressBook;
	protected Indexer indexer;
	protected ArrivalsProcessor arrivalsProcessor;
	protected ArrivalsMonitor arrivalsMonitor;
	protected EventDispatcher eventDispatcher;
	protected Actions actions;
	protected Mailer mailer;
	protected Cryptor cryptor;
	protected CacheManager cacheManager;
	protected JSON json;
	
	public Master (
		Store store,
		Identity identity,
		Environment environment,
		Indexer indexer, 
		AddressBook addressBook,
		ArrivalsProcessor arrivalsProcessor, 
		ArrivalsMonitor arrivalsMonitor, 
		EventDispatcher eventDispatcher,
		Actions actions,
		Mailer mailer,
		Cryptor cryptor,
		CacheManager cacheManager,
		JSON json
	)
	{
		this.identity = identity;
		this.environment = environment;
		this.addressBook = addressBook;

		this.store = store;
		store.setMaster(this);

		this.initializer = new Initializer();
		initializer.setMaster(this);

		this.indexer = indexer;
		indexer.setMaster(this);

		this.arrivalsProcessor = arrivalsProcessor;
		arrivalsProcessor.setMaster(this);

		this.arrivalsMonitor = arrivalsMonitor;
		arrivalsMonitor.setMaster(this);

		this.eventDispatcher = eventDispatcher;

		this.actions = actions;
		actions.setMaster(this);

		this.mailer = mailer;
		mailer.setMaster(this);

		this.cryptor = cryptor;

		this.cacheManager = cacheManager;
		cacheManager.setMaster(this);

		this.json = json;
		json.setMaster(this);
	}
	
	public void start() throws Exception
	{
		initializer.start();
	}
	
	public Store getStore ()
	{
		return store;
	}
	
	public Identity getIdentity ()
	{
		return identity;
	}
	
	public Environment getEnvironment ()
	{
		return environment;
	}
	
	public AddressBook getAddressBook ()
	{
		return addressBook;
	}
	
	public Indexer getIndexer()
	{
		return indexer;
	}
	
	public CacheManager getCacheManager()
	{
		return cacheManager;
	}
	
	public ArrivalsProcessor getArrivalsProcessor()
	{
		return arrivalsProcessor;
	}

	public ArrivalsMonitor getArrivalsMonitor() 
	{
		return arrivalsMonitor;
	}

	public EventDispatcher getEventPropagator ()
	{
		return eventDispatcher;
	}
	
	public Actions getActions ()
	{
		return actions;
	}
	
	public Mailer getMailer ()
	{
		return mailer;
	}
	
	public Cryptor getCryptor ()
	{
		return cryptor;
	}
	
	public JSON getJSON ()
	{
		return json;
	}
}
