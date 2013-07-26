/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_connector_Lock_h__
#define __mailiverse_core_connector_Lock_h__

#include "ConnectorException.h"
#include "mailiverse/Exception.h"
#include "mailiverse/Types.h"
#include "mailiverse/core/Types.h"
#include "StoreConnector.h"
#include "mailiverse/utilities/Log.h"
#include "mailiverse/core/util/Random.h"

namespace mailiverse {
namespace core {
namespace connector {

class Lock 
{
public:
	typedef ConnectorException Exception;

protected:
	util::Random random;
	StoreConnectorPtr connector;
	String path;
	Date::ValueType intervalSeconds;
	Date::ValueType remainingBeforeRelockSecond;
	Date expiration;
	String version;

protected:

	void reset ()
	{
		version = "";
		expiration = Date::None;
	}
	
	Date getExpirationFor (const Date &timeLocked)
	{
		return Date(timeLocked.getTime() + intervalSeconds * 1000);
	}
	
	bool hasExpired (Date expiration)
	{
		Date now = Date::now();
		return now.after(expiration);
	}

	bool closeToExpiration (Date expiration)
	{
		return getRemainingTimeInSeconds(expiration) < 1;
	}

	long getRemainingTimeInSeconds (Date expiration)
	{
		Date now = Date::now();
		return (expiration.getTime() - now.getTime())/1000;
	}
	
public:
	Lock (StoreConnector *_connector, const String &path, int intervalSeconds, int remainingBeforeRelockSecond) :
		connector(_connector),
		expiration(Date::None)
	{
		this->path = path;
		this->intervalSeconds = intervalSeconds;
		this->remainingBeforeRelockSecond = remainingBeforeRelockSecond;
	}
	
	virtual ~Lock () {}
	
	void relock()
	{
		if (expiration == Date::None || closeToExpiration(expiration))
		{
			LogDebug(core::connector::Lock, "lock close to expired or expired, going to fully lock");
			lock();
		}
		else
		{
			LogDebug(core::connector::Lock, "lock still active, relock only if necesary.");
			possiblyLockIfNeccesary();
		}
	}
	
	void lock()
	{
		StoreConnector::FileListing listing = connector->list(path);
		onLockInfo(listing);
		possiblyLockIfNeccesary();
	}
	
	void unlock()
	{
	/*
		if (hasExpired(expiration))
		{
			throw Exception("Lock has already expired"));
		}
		else
		{
			connector->delete(path);
			reset();
		}
	*/
	}
	
	void testLock(const StoreConnector::FileListing &fileInfo)
	{
		bool lockFound = false;

		for (FileInfo i : fileInfo)
		{
			LogDebug(core::connector::Lock, "testLock " << i.path << " " << path);
			if (i.path == path)
			{
				lockFound = true;

				if (i.version != version)
				{
					throw Exception("Lock was not obtained");
				}
				else
				{
					LogDebug(core::connector::Lock, "test lock found the lock, setting the expiration time to the file system. " << i.date.getTime());
					expiration = getExpirationFor(i.date);
				}

				if (hasExpired(expiration))
					throw Exception("Lock has already expired");
			}
		}

		if (!lockFound)
			throw Exception("Lock not found.");
	}
	
	void possiblyLockIfNeccesary()
	{
		long remainingTime = 0;

		if (expiration != Date::None)
		{
			remainingTime = getRemainingTimeInSeconds(expiration);
		}

		if (remainingTime < remainingBeforeRelockSecond)
		{
			LogDebug(core::connector::Lock, "remainingTime" << remainingTime << "<" << remainingBeforeRelockSecond << "LOCKING!");

			core::Block b = random.nextBytes(8);
			storeLock(connector->put(path, b));
		}
		else
		{
			LogDebug(core::connector::Lock, "remainingTime" << remainingTime << ">=" <<  remainingBeforeRelockSecond);
		}
	}
	
	void onLockInfo(const StoreConnector::FileListing &fileInfo)
	{
		bool locked = false;
		if (!fileInfo.isEmpty())
		{
			FileInfo info = fileInfo.get(0);
			Date lockExpiration = getExpirationFor(info.date);

			// it's not our lock
			if (info.version != version)
			{
				locked = !hasExpired(lockExpiration);
				reset();

				LogDebug(core::connector::Lock, "file.date" << info.date.getTime() << "lockExpiration" << lockExpiration.getTime() << "locked" << locked);
			}
			else
			{
				LogDebug(core::connector::Lock, "we have the lock!! setting expiration.." <<lockExpiration.getTime());
				expiration = lockExpiration;
			}
		}

		if (locked)
			throw Exception("Someone else has the lock");
	}
	
	void storeLock (const FileInfo::Version &_version)
	{
		expiration = getExpirationFor(Date::now());
		version = _version;
	}
} ;


} // namespace
} // namespace
} // namespace

#endif
