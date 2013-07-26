/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef CACHEINFO_H_
#define CACHEINFO_H_

#include "ID.h"
#include "Value.h"
#include "Version.h"
#include "mailiverse/utilities/Log.h"

namespace mailiverse {
namespace mail {
namespace cache {

class Cache;
DECLARE_SMARTPTR(Cache);

class Info
{
private:
	Version cacheVersion;
	Version localVersion;
	ID id;

protected:
	void nextVersion()
	{
		localVersion = Version::random();
	}

public:
	Info();
	virtual ~Info();

	virtual void setID(const ID &_id)
	{
		id = _id;
	}
	
	virtual const ID &getID ()
	{
		return id;
	}

	const Version &getLocalVersion ()
	{
		return localVersion;
	}

	void setLocalVersion(const Version &_localVersion)
	{
		localVersion = _localVersion;
	}

	const Version &getCacheVersion ()
	{
		return cacheVersion;
	}

	void setCacheVersion (const Version &_cacheVersion)
	{
		cacheVersion = _cacheVersion;
	}

	void markLoad (const Version &_cacheVersion)
	{
		LogDebug(mailverse::mail::cache::Info, this << " markLoad");
		
		localVersion = _cacheVersion;
		cacheVersion = localVersion;
		onLoaded();
		onModified();
	}

	void markDeleted ()
	{
		LogDebug(mailverse::mail::cache::Info, this << " markDeleted");

		onDeleting();
		localVersion = Version::DELETED;
		onDirty();
		onModified();
	}

	bool isDeleted ()
	{
		return localVersion == Version::DELETED;
	}

	void markCreate ()
	{
		LogDebug(mailverse::mail::cache::Info, this << " markCreate");
		
		markDirtyNoCheck();
		onCreate();
	}

	void willMarkDirty ()
	{
		if (!isWritable())
			throw Exception("Attempted to markDirty unwritable item");
	}

	void markDirty ()
	{
		LogDebug(mailverse::mail::cache::Info, this << " markDirty");

		if (!isWritable())
			throw Exception("Attempted to markDirty unwritable item");
			
		markDirtyNoCheck();
	}
	
	void markDirtyNoCheck ()
	{
		LogDebug(mailverse::mail::cache::Info, this << " markDirtyNoCheck");

		nextVersion();
		onDirty();
		onModified();
	}
	
	void markStore (const Version &version)
	{
		LogDebug(mailverse::mail::cache::Info, this << " markStore");

		cacheVersion = version;
		onStore();
	}
	
	void markShutdown ()
	{
		LogDebug(mailiverse::core::mail::cache::Info, this << " before onShutdown  " << SMARTPTR_INFO(this));
		
		onShutdown ();
		
		LogDebug(mailiverse::core::mail::cache::Info, this << " after onShutdown  " << SMARTPTR_INFO(this));
	}
	
	virtual void onShutdown ()
	{
	}
	
	virtual void onDirty ()
	{
	}

	virtual bool isDirty ()
	{
		return localVersion != cacheVersion;
	}
	
	virtual bool hasDirtyChildren ()
	{
		return false;
	}

	virtual bool isLoaded ()
	{
		return localVersion != Version::NONE;
	}
	
	virtual bool isWritable ()
	{
		return isLoaded();
	}
	
	virtual void onCreate ()
	{
	}
	
	virtual void onDeleting ()
	{
	}

	virtual void onStore ()
	{
	}

	virtual void onExisting ()
	{
	}

	virtual void onLoaded ()
	{

	}

	virtual void onModified ()
	{
	}
};

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* CACHEINFO_H_ */
