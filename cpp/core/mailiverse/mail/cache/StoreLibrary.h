/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_cache_StoreLibrary_h__
#define __mailiverse_mail_cache_StoreLibrary_h__

#include "StoreMemory.h"
#include "ItemFactory.h"
#include "Cache.h"
#include "mailiverse/core/crypt/Hash.h"
#include "mailiverse/core/crypt/CryptorAES.h"
#include "mailiverse/core/crypt/CryptorSeed.h"
#include "mailiverse/core/connector/AsyncStoreConnectorSynth.h"
#include "mailiverse/core/connector/Lock.h"
#include "mailiverse/core/connector/FileInfo.h"
#include "mailiverse/core/crypt/Base64.h"
#include "mailiverse/utilities/Thread.h"
#include "mailiverse/utilities/Algorithm.h"
#include "mailiverse/utilities/Log.h"
#include "Constants.h"
#include <set>
#include <map>

namespace mailiverse {
namespace mail {
namespace cache {

class StoreLibrary
{
public:
	struct Delegate
	{
		virtual ~Delegate () {}
		virtual void onNewStore (Store *) = 0;
	};

protected:
	Delegate *delegate;
	core::connector::StoreConnector *connector;
	
	typedef utilities::EmptyMonitor WriteMonitor;
	WriteMonitor writeMonitor;

	String cachePrefix;

	ItemFactoryPtr storeFactory;
	ItemSerializerPtr storeSerializer;

	typedef core::connector::FileInfo::Version FileVersion;
	typedef String FileKey;
	
	std::map<FileKey, FileVersion> remoteVersions;
	std::map<FileKey, FileVersion> localVersions;
	std::map<FileKey, core::crypt::CryptorPtr> cryptors;
	std::map<FileKey, StorePtr> stores;
	
	core::crypt::HashSha256 derivedKeyGenerator;
	core::crypt::CryptorSeed cryptorSeed;
	
protected:

	Key createCryptorKeyFor (String key)
	{
		core::Block b;
		b.insert(b.end(), key.begin(), key.end());
		b.insert(b.end(), cryptorSeed.getSeed().begin(), cryptorSeed.getSeed().end());
		return Key(derivedKeyGenerator.generate(b));
	}
	
	core::crypt::CryptorPtr getOrCreateCryptorForKey (const String &key)
	{
		auto i = cryptors.find(key);
		if (i==cryptors.end())
		{
			core::crypt::CryptorPtr cryptor = new core::crypt::CryptorAES(createCryptorKeyFor(key));
			cryptors[key] = cryptor;
			return cryptor;
		}

		return i->second;
	}
	
	String getStorePath(const String &key)
	{
		return cachePrefix + key;
	}
        
		
protected:

	core::connector::Lock flushLock;
	
	struct SortByCacheType
	{
		int getPriority(String s) const
		{
			static const String priority = "MCFI";
			String file = s.substr(s.rfind("/")+1);
			if (file.empty())
				return -1;
				
			auto find = priority.find(file[0]);
			return find;
		}

		bool operator()(const String &lhs, const String &rhs) const {
			return getPriority(lhs) < getPriority(rhs);
		}
	};
	

	void onLoad (const FileKey &key, const core::connector::StoreConnector::VersionedBlock &results)
	{
		LogDebug(mailiverse::mail::cache::StoreLibrary, "loadSuccess " << key << " " << results.second);

		WriteMonitor::Writer lock(writeMonitor);
		
		auto storeFind = stores.find(key);
		if (storeFind != stores.end())
		{
			StorePtr store = storeFind->second;
			Value value(getOrCreateCryptorForKey(key)->decrypt(
				core::crypt::Base64::decodeBytes(results.first))
			);
			storeSerializer->deserialize(store, value);
			store->markLoad(Version::random());
				
			localVersions[key] = remoteVersions[key] = results.second;
		}
		
		LogDebug(mailiverse::mail::cache::StoreLibrary, "loadSuccess finished " << key);
	}
	
	void doPartialUpdate (bool initiateFullUpdateOnChange)
	{
		LogDebug(mailiverse::mail::cache::StoreLibrary, "doPartialUpdate " << initiateFullUpdateOnChange);

		Vector<Pair<FileKey,FileVersion>> filesToLoad;
	
		bool listingOutOfSync = false;
	
		// scope lock
		{
			WriteMonitor::Reader lock(writeMonitor);

			for (auto &store : stores)
			{
				auto remoteVersion = remoteVersions.find(store.first);
				auto localVersion = localVersions.find(store.first);
				
				if (remoteVersion != remoteVersions.end())
				{
					if (localVersion == localVersions.end() || localVersion->second != remoteVersion->second)
					{
						LogDebug(mailiverse::mail::cache::StoreLibrary, "willLoad " << store.first);
						
						filesToLoad.add(Pair<FileKey, FileVersion>(store.first, remoteVersion->second));
					}
				}
				else
				{
					LogDebug(mailiverse::mail::cache::StoreLibrary, "there was no remote version for a cache, assuming out of sync " << store.first);

					listingOutOfSync = true;
				}
			}
		}
		
		std::sort(
			filesToLoad.begin(), filesToLoad.end(), 
			utilities::ComparatorPairFirst<
				Pair<FileKey,FileVersion>, 
				SortByCacheType
			>()
		);
		
		for (auto &pair : filesToLoad)
		{
			core::connector::StoreConnector::VersionedBlock block = connector->get(getStorePath(pair.first));
			onLoad (pair.first, block);
			
			if (pair.second != block.second)
			{
				LogDebug(mailiverse::mail::cache::StoreLibrary, "listing is out of sync " << pair.first << " " << pair.second << " " << block.second);

				listingOutOfSync = true;
			}
		}
		
		if (listingOutOfSync && initiateFullUpdateOnChange)
		{
			LogDebug(mailiverse::mail::cache::StoreLibrary, "listing was out of sync! initiating full update");
			doUpdate(false);
		}
	}
	
	void doUpdate (bool checkFlushLock)
	{
		LogDebug(mailiverse::mail::cache::StoreLibrary, "doUpdate " << checkFlushLock);

		core::connector::StoreConnector::FileListing
			listing = connector->list(cachePrefix);
	
		if (listing.empty())
			throw Exception("empty");
			
		// populate the remote versions
		remoteVersions.clear();
		for (auto &file : listing)
			remoteVersions[file.relativePath] = file.version;
	
		LogDebug(mailiverse::mail::cache::StoreLibrary, "doUpdate had " << listing.size() << " files");
	
		if (checkFlushLock)
			flushLock.testLock(listing);
			
		doPartialUpdate(false);
	}
	
	void onStore (const FileKey &key, const Version &dataVersion, const core::connector::FileInfo::Version &fileVersion)
	{
		LogDebug(mailiverse::mail::cache::StoreLibrary, "onStore " << key << " " << fileVersion);

		WriteMonitor::Reader lock(writeMonitor);
		
		auto storeFind = stores.find(key);
		if (storeFind != stores.end())
		{
			StorePtr store = storeFind->second;
			store->markStore(dataVersion);
			localVersions[key] = remoteVersions[key] = fileVersion;
		}

		LogDebug(mailiverse::mail::cache::StoreLibrary, "onStore finished " << key);
	}
	
	void doFlush ()
	{
		LogDebug(mailiverse::mail::cache::StoreLibrary, "doFlush");

		flushLock.relock();
		doUpdate(true);
		
		Vector<String> fileKeys;
		Map<String,StorePtr> storesToStore;
		
		// scope lock
		{
			WriteMonitor::Reader lock(writeMonitor);
			for (auto &store : stores)
			{
				if (store.second->hasDirtyChildren())
				{
					fileKeys.add(store.first);
					storesToStore[store.first] = store.second;
				}
			}
		}
		
		LogDebug(mailiverse::mail::cache::StoreLibrary, "found " << storesToStore.size() << " stores to store");

		std::sort(fileKeys.begin(), fileKeys.end(), SortByCacheType());
		
		for (auto &fileKey : fileKeys)
		{
			StorePtr store = storesToStore[fileKey];
			store->lock();
			
			Version dataVersion = store->getLocalVersion();
			
			storesToStore.erase(fileKey);
			
			core::Block block = 
				core::crypt::Base64::encodeBytes(
					getOrCreateCryptorForKey(fileKey)->encrypt(
						storeSerializer->serialize(store).block()
					)
				);

			LogDebug(mailiverse::mail::cache::StoreLibrary, "storing " << fileKey);

			core::connector::FileInfo::Version version = 
				connector->put(getStorePath(fileKey),block);
				
			onStore(fileKey, dataVersion, version);
		}
	}

public:
	StoreLibrary(
		const core::crypt::CryptorSeed &_cryptorSeed, 
		core::connector::StoreConnector *_connector,
		ItemFactory *_storeFactory, 
		ItemSerializer *_storeSerializer,
		const String &_cachePrefix
	) :
		delegate(0),
		connector(_connector),
		storeFactory(_storeFactory),
		storeSerializer(_storeSerializer),
		cryptorSeed(_cryptorSeed),
		cachePrefix(_cachePrefix),
		flushLock(
			_connector, _cachePrefix + "flush.lock",
			Constants::FLUSH_LOCK_TIME_SECONDS, 
			Constants::FLUSH_LOCK_TIME_ALLOWED_BEFORE_RELOCK_SECONDS
		)
	{
	}

	virtual ~StoreLibrary() 
	{
	}

	void setDelegate (Delegate *delegate)
	{
		this->delegate = delegate;
	}
	
	bool hasDirtyChildren ()
	{
		WriteMonitor::Reader reader(writeMonitor);
		for (auto &i : stores)
		{
			if (i.second->hasDirtyChildren())
				return true;
		}
		
		return false;
	}

	virtual Store *instantiate (const String &prefix, const ID &id, bool isNew)
	{
		FileKey key = !(id == ID::NONE) ? (prefix + "_" + id.toFileSystemSafe()) : prefix;
		LogDebug(mailiverse::mail::cache::StoreLibrary, "instantiate " << key << " " << prefix << " " << " " << id.str() << " " << isNew);

		if (stores.find(key)!=stores.end())
		{
			LogDebug(mailiverse::mail::cache::StoreLibrary, "instantiate already exists " << key << " " << prefix << " " << " " << id.str() << " " << isNew);
			return stores[key];
		}	
		
		Store *store = dynamic_cast<Store *>(storeFactory->instantiate(0));
		store->setStoreLibrary(this);
		stores[key] = store;

		LogDebug(mailiverse::mail::cache::StoreLibrary, "instantiating! " << key << " " << prefix << " " << " " << id.str() << " " << isNew);
		
		if (isNew)
		{
			store->markCreate();
		}
		
		if (delegate)
			delegate->onNewStore(store);

		return store;
	}
	
	void update (bool checkLock)
	{
		doUpdate(checkLock);
	}
	
	void partialUpdate (bool initialFullUpdateOnChange)
	{
		doPartialUpdate(initialFullUpdateOnChange);
	}
	
	void flush ()
	{
		doFlush();
	}
};

typedef utilities::SmartPtr<StoreLibrary> StoreLibraryPtr;

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_cache_StoreLibrary_h__ */
