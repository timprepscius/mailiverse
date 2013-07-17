/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

import core.callback.Callback;
import core.callback.CallbackEmpty;
import core.constants.ConstantsStorage;
import core.util.LogNull;
import core.util.LogOut;

public class Initializer extends Servent<Master>
{
	static LogNull log = new LogNull(Initializer.class);
	int numRemainingIndexCaches = 3;
	
	public Initializer ()
	{
	}
	
	public void start ()
	{
		EventDispatcher e = master.getEventPropagator();
		
		e.add(Events.Initialize_Start, this, new Callback() {

			@Override
			public void invoke(Object... arguments)
			{
				onInitializeStart();
			}
		});
		
		e.add(Events.Initialize_IndexedCacheAcquired, this, new Callback() {

			@Override
			public void invoke(Object... arguments)
			{
				onIndexedCacheAcquired();
			}
		});
		

		e.add(Events.Initialize_IndexedCacheLoadFailed, this, new Callback() {

			@Override
			public void invoke(Object... arguments)
			{
				onIndexedCacheLoadFailed();
			}
		});
		
		e.add(Events.Initialize_IndexedCacheLoadComplete, this, new Callback() {

			@Override
			public void invoke(Object... arguments)
			{
				onIndexedCacheLoadComplete();
			}
		});
		
		e.add(Events.Initialize_FolderLoadComplete, this, new Callback() {

			@Override
			public void invoke(Object... arguments)
			{
				onFolderLoadComplete();
			}
		});
		
		e.signal(Events.Initialize_Start, (Object[])null);
	}
	
	public void onInitializeStart()
	{
		log.debug("onInitializeStart");
		
		master.getStore().getConnector().ensureDirectories_(
			new String[] {
				ConstantsStorage.CACHE,
				ConstantsStorage.NEW_IN_JSON,
				ConstantsStorage.NEW_OUT_JSON
			}
		).invoke();

		master.getCacheManager().start();
	}

	public void onIndexedCacheLoadFailed ()
	{
		log.debug("onIndexedCacheLoadFailed");

		master.getCacheManager().firstRunInitialization();
		master.getIndexer().firstRunInitialization();
		
		master.getEventPropagator().signal(Events.FirstRunInitialization, (Object[])null);
	}
	
	public void onIndexedCacheAcquired ()
	{
		log.debug("onIndexedCacheLoadAcquire");
		master.getCacheManager().deserializeIndexCaches();
	}

	public void onIndexedCacheLoadComplete ()
	{
		log.debug("onIndexedCacheLoadComplete");
		master.getIndexer().start();
	}
	
	public void onFolderLoadComplete()
	{
		log.debug("onFolderLoadComplete");

		master.getEventPropagator().signal(Events.Initialized, (Object[])null);
	}
}
