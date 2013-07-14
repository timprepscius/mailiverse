/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.async;

import java.util.Date;
import java.util.List;

import core.util.SecureRandom;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.connector.FileInfo;
import core.util.LogNull;
import core.util.LogOut;

public class Lock 
{
	static LogNull log = new LogNull(Lock.class);
	static SecureRandom random = new SecureRandom();
	
	public AsyncStoreConnector connector;	
	String path;
	int intervalSeconds;
	int remainingBeforeRelockSecond;
	Date expiration;
	String version;

	public Lock(AsyncStoreConnector connector, String path, int intervalSeconds, int remainingBeforeRelockSecond)
	{
		this.connector = connector;
		this.path = path;
		this.intervalSeconds = intervalSeconds;
		this.remainingBeforeRelockSecond = remainingBeforeRelockSecond;
	}
	
	protected void reset ()
	{
		version = null;
		expiration = null;
	}
	
	protected Date getExpirationFor (Date timeLocked)
	{
		return new Date(timeLocked.getTime() + intervalSeconds * 1000);
	}
	
	protected boolean hasExpired (Date expiration)
	{
		Date now = new Date();
		return now.after(expiration);
	}
	
	protected boolean closeToExpiration (Date expiration)
	{
		return getRemainingTimeInSeconds(expiration) < 1;
	}
	
	protected long getRemainingTimeInSeconds (Date expiration)
	{
		Date now = new Date();
		return (expiration.getTime() - now.getTime())/1000;
	}
	
	public Callback lock_()
	{
		return 
			connector.list_(path)
				.addCallback(lockOnInfo_())
				.addCallback(possiblyLockIfNecessary_());
	}
	
	public Callback relock_()
	{
		return new CallbackDefault() 
		{
			public void onSuccess(Object... arguments) throws Exception 
			{
				if (expiration == null || closeToExpiration(expiration))
				{
					log.debug(this, "lock close to expired or expired, going to fully lock");
					call(lock_());
				}
				else
				{
					log.debug(this, "lock still active, relock only if necesary.");
					call(possiblyLockIfNecessary_());
				}
			}
		} ;
	}
	
	protected Callback possiblyLockIfNecessary_ ()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception 
			{
				long remainingTime = 0;
				
				if (expiration != null)
				{
					remainingTime = getRemainingTimeInSeconds(expiration);
				}

				if (remainingTime < remainingBeforeRelockSecond)
				{
					log.debug(this, "remainingTime",remainingTime,"<", remainingBeforeRelockSecond, "LOCKING!");
					
					byte[] bytes = new byte[8];
					random.nextBytes(bytes);
					
					call(
						connector.put_(path, bytes)
							.addCallback(storeLock_())
					);
				}
				else
				{
					log.debug(this, "remainingTime",remainingTime,">=", remainingBeforeRelockSecond);
					next(arguments);
				}
			}
		};
	}
	
	public Callback lockOnInfo_ ()
	{
		return new CallbackDefault () {
			@Override
			public void onSuccess(Object... arguments) throws Exception {
				List<FileInfo> fileInfo = (List<FileInfo>) arguments[0];
				
				boolean locked = false;
				if (!fileInfo.isEmpty())
				{
					FileInfo info = fileInfo.get(0);
					Date lockExpiration = getExpirationFor(info.date);
					
					// it's not our lock
					if (!info.version.equals(version))
					{
						locked = !hasExpired(lockExpiration);
						reset();
						
						log.debug(this, "file.date", info.date,"lockExpiration",lockExpiration,"locked",locked);
					}
					else
					{
						log.debug(this, "we have the lock!! setting expiration..", lockExpiration);
						expiration = lockExpiration;
					}
				}
				
				if (locked)
					throw new Exception("Someone else has the lock");
				
				next(arguments);
			}
		};
	}
	
	public Callback storeLock_ ()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				expiration = getExpirationFor(new Date());
				version = (String) arguments[0];
				
				next(arguments);
			}
		};
	}
	
	public void testLock (List<FileInfo> fileInfo) throws Exception
	{
		boolean lockFound = false;
		
		for (FileInfo i : fileInfo)
		{
			log.trace("testLock", i.path, path);
			if (i.path.equals(path))
			{
				lockFound = true;
				
				if (i.version != version)
				{
					throw new Exception("Lock was not obtained");
				}
				else
				{
					log.debug(this, "test lock found the lock, setting the expiration time to the file system.", i.date);
					expiration = getExpirationFor(i.date);
				}
				
				if (hasExpired(expiration))
					throw new Exception("Lock has already expired");
			}
		}
		
		if (!lockFound)
			throw new Exception("Lock not found.");
	}
	
	public Callback testLock_ ()
	{
		return new CallbackDefault () {
			@Override
			public void onSuccess(Object... arguments) throws Exception {
				List<FileInfo> fileInfo = (List<FileInfo>) arguments[0];	
				testLock(fileInfo);
				next(arguments);
			}
		};
	}
	
	public Callback unlock_()
	{
		return new CallbackDefault ()
		{
			public void onSuccess(Object... arguments) throws Exception 
			{
				// never unlock, just let them expire
				
				/*
				if (hasExpired(expiration))
				{
					next(new Exception("Lock has already expired"));
				}
				else
				{
					connector.delete_(path).setReturn(callback).invoke();
					reset();
				}
				*/
				
				next(arguments);
			}
		};
	}


};
