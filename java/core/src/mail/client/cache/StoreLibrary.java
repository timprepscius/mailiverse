/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import core.callback.Callback;
import core.callback.CallbackChain;
import core.callback.CallbackDefault;
import core.callback.CallbackEmpty;
import core.callbacks.SaveArguments;
import core.connector.FileInfo;
import core.connector.async.AsyncStoreConnector;
import core.connector.async.Lock;
import core.constants.ConstantsStorage;
import core.crypt.Cryptor;
import core.crypt.CryptorAES;
import core.crypt.CryptorSeed;
import core.crypt.HashSha256;
import core.util.Arrays;
import core.util.Comparators;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Maps;
import core.util.Pair;
import core.util.Strings;

public class StoreLibrary
{
	static LogNull log = new LogNull(StoreLibrary.class);
	
	protected Map<String, String> versions = new HashMap<String, String>();
	protected Map<String, Store> stores = new HashMap<String, Store>();
	protected Map<String, Cryptor> cryptors = new HashMap<String, Cryptor>();
	
	ItemSerializer storeSerializer;
	ItemFactory storeFactory;
	
	HashSha256 derivedKeyGenerator = new HashSha256();
	CryptorSeed cryptorSeed;
	
	Lock flushLock;
	AsyncStoreConnector storeConnector;
		
	public StoreLibrary (CryptorSeed cryptorSeed, StoreFactory storeFactory, AsyncStoreConnector storeConnector)
	{
		this.cryptorSeed = cryptorSeed;
		this.storeSerializer = new StoreSerializer();
		this.storeFactory = storeFactory;
		this.storeConnector = storeConnector;
		flushLock = 
			new Lock(
				storeConnector, 
				ConstantsStorage.CACHE_PREFIX + "flush.lock", 
				ConstantsStorage.FLUSH_LOCK_TIME_SECONDS,
				ConstantsStorage.FLUSH_LOCK_TIME_ALLOWED_BEFORE_RELOCK_SECONDS
			);
	}
	
	public String getFullPathFor (String relative)
	{
		return ConstantsStorage.CACHE_PREFIX + relative;
	}
	
	public Store instantiate (String prefix, ID id, boolean isNew)
	{
		log.debug("instantiating", prefix, id, isNew);
		
		Store store = (Store)storeFactory.instantiate(null);
		String relative = id != null ? (prefix + "_" + id.toFileSystemSafe()) : prefix;
		stores.put(relative, store);
		
		if (isNew)
		{
			store.markCreate();
		}
		else
		{
			loadSingleStore_(relative).invoke();
		}
		
		return store;
	}
	
	public byte[] createCryptorKeyFor (String key)
	{
		byte[] data = Arrays.concat(Strings.toBytes(key), cryptorSeed.seed);
		return derivedKeyGenerator.hash(data);
	}
	
	public Cryptor getOrCreateCryptorForKey (String key)
	{
		Cryptor cryptor = cryptors.get(key);
		if (cryptor == null)
		{
			cryptor = new CryptorAES(createCryptorKeyFor(key));
			cryptors.put(key, cryptor);
		}
		
		return cryptor;
	}
		
	public Callback load_ (String key)
	{
		log.debug("load_", key);
		
		Store store = stores.get(key);
		SaveArguments saveArgs = new SaveArguments();
		Cryptor cryptor = getOrCreateCryptorForKey(key);

		return 
			log.debug_("loading", key)
				.addCallback(saveArgs)
				.addCallback(cryptor.decrypt_())
				.addCallback(storeSerializer.deserialize_(store))
				.addCallback(store.markLoad_(Version.random()))
				.addCallback(saveArgs.restore_(101))
				.addCallback(Maps.put_(versions, key));
	}
	
	public Callback store_ (String key, Version version)
	{
		log.debug("store_", key);
		
		Store store = stores.get(key);
		Cryptor cryptor = getOrCreateCryptorForKey(key);

		return 
			log.debug_("storing", key)
				.addCallback(flushLock.relock_())
				.addCallback(storeSerializer.serialize_(store))
				.addCallback(cryptor.encrypt_())
				.addCallback(storeConnector.put_(getFullPathFor(key)))
				.addCallback(Maps.put_(versions, key))
				.addCallback(store.markStore_(version));
	}
	
	static class SortByCacheType implements Comparator<String>
	{
		static String priority = "MCFI";
		
		public int getPriority(String s)
		{
			String file = s.substring(s.lastIndexOf("/")+1);
			return priority.indexOf(file.charAt(0));
		}
		
		public int compare(String lhs, String rhs) {
			return getPriority((String)rhs) - getPriority((String)lhs);
		}
	}
	
	public Callback checkIncludesMainIndex_()
	{
		return new CallbackDefault() {
			public void onSuccess(Object...arguments) throws Exception
			{
				log.debug("found files");
				List<FileInfo> fileInfos = (List<FileInfo>)arguments[0];
				
				boolean hasMainIndex = false;
				for (FileInfo info : fileInfos)
				{
					log.trace(info.path);
					
					if (info.path.endsWith("/I"))
						hasMainIndex = true;
				}
				
				if (!hasMainIndex)
					throw new Exception("Main index not found!");
				
				next(fileInfos);
			}
		} ;
	}
	
	public Callback getFilesToLoad_()
	{
		return new CallbackDefault() {
			public void onSuccess(Object...arguments)
			{
				log.debug("getFilesToLoad_");
				
				List<FileInfo> fileInfos = (List<FileInfo>)arguments[0];
				List<Pair<String, String>> filesToLoad = new ArrayList<Pair<String, String>>();
		
				for (FileInfo info : fileInfos)
				{
					boolean storeForFileIsInstantiated = 
						stores.containsKey(info.relativePath);
					
					if (storeForFileIsInstantiated)
					{
						log.trace("storeForFileIsInstantiated", info.relativePath);
						String remoteVersion = info.version;
						String localVersion = versions.get(info.relativePath);
							
						if (!remoteVersion.equals(localVersion))
						{
							log.debug("will load");
							filesToLoad.add(new Pair<String, String>(info.relativePath, remoteVersion));
						}
					}
				}

				Collections.sort(filesToLoad, new Comparators.SortByFirst<String>(new SortByCacheType()));
				next(filesToLoad);
			}
		};
	}

	public Callback handleFileInfos_ () {
		return 
			new CallbackDefault() {
				@Override
				public void onSuccess(Object... arguments) throws Exception {
					log.debug("handleFileInfos_");
					
					@SuppressWarnings("unchecked")
					List<Pair<String, String>> filesToLoad = (List<Pair<String, String>>)arguments[0];
				
					CallbackChain chain = new CallbackChain();
					for (Pair<String, String> i : filesToLoad)
					{
						log.trace("building load sequence", i.first);
						
						Callback callback = 
							storeConnector.get_(getFullPathFor(i.first))
							.addCallback(load_(i.first));
						
						chain.addCallback(callback);
					}
					
					log.debug("starting load sequence");
					chain.setReturn(callback);
					chain.invoke();
				}
				
			};
	}
	
	public Callback loadSingleStore_(String fileName)
	{
		return 
			storeConnector.get_(getFullPathFor(fileName))
			.addCallback(load_(fileName));
		
	}
	
	public Callback update_ (boolean shouldTestLock)
	{
		return 
			storeConnector.list_(ConstantsStorage.CACHE_PREFIX)
				.addCallback(shouldTestLock ? flushLock.testLock_() : new CallbackEmpty())
				.addCallback(shouldTestLock ? new CallbackEmpty() : checkIncludesMainIndex_())
				.addCallback(getFilesToLoad_())
				.addCallback(handleFileInfos_());
	}

	public Callback flushDirty_()
	{
		log.debug("flushDirty_");
		List<Pair<String, Version>> filesToStore = new ArrayList<Pair<String,Version>>();
		
		for (Map.Entry<String,Store> p : stores.entrySet())
		{
			log.trace("flushDirty_ iterating over", p.getKey(), p.getValue());
			Store store = p.getValue();
			if (store.hasDirtyChildren())
			{
				log.debug("will store",p.getKey(),p.getValue());
				store.lock();
				filesToStore.add(new Pair<String,Version>(p.getKey(),p.getValue().getLocalVersion()));
			}
		}

		Collections.sort(
			filesToStore, 
			new Comparators.SortByFirstReverse<String>(new SortByCacheType())
		);
		
		CallbackChain chain = new CallbackChain();
		for (Pair<String, Version> file : filesToStore)
		{
			log.debug("will store (ordered):", file.first);
			chain.addCallback(store_(file.first, file.second));
		}
		
		return chain;
	}
	
	public Callback flushDirty__ ()
	{
		return 
			new CallbackDefault() {
				public void onSuccess(Object... arguments) throws Exception {
					flushDirty_().setReturn(callback).invoke(arguments);
				}
			};
	}
	
	public boolean hasDirtyChildren ()
	{
		for (Map.Entry<String,Store> p : stores.entrySet())
		{
			Store store = p.getValue();
			if (store.hasDirtyChildren())
			{
				log.debug("found dirty store");
				return true;
			}
		}
		
		return false;
	}
		
	public Callback flush_ ()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				if (hasDirtyChildren())
				{
					call (
						flushLock.lock_()
							.addCallback (update_(true))
							.addCallback (flushDirty__())
							.addCallback(flushLock.unlock_())
					);
				}
				else
				{
					next();
				}
			}
		};
	}
		
	public void start(Callback callback) 
	{
		log.debug("start!");
		
		update_(false).addCallback(callback).invoke();
	}

}
