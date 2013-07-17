/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.util.LogNull;

public class Info
{
	static LogNull log = new LogNull (Info.class);
	
	protected ItemOwner owner;
	private Version cacheVersion;
	private Version localVersion;
	private ID id;

	protected Info ()
	{
		cacheVersion = Version.NONE;
		localVersion = Version.NONE;
	}
	
	public ID getId ()
	{
		return id;
	}
	
	public void setId (ID id)
	{
		this.id = id;
	}
	
	public void onExisting ()
	{
		
	}
	
	public void setOwner (ItemOwner owner)
	{
		this.owner = owner;
	}
	
	public void setLocalVersion (Version localVersion)
	{
		this.localVersion = localVersion;
	}
	
	public Version getLocalVersion ()
	{
		return localVersion;
	}
	
	public Version getCacheVersion ()
	{
		return cacheVersion;
	}
	
	public void setCacheVersion (Version cacheVersion)
	{
		this.cacheVersion = cacheVersion;
	}
	
	public final void markNoLoad (Version cacheVersion)
	{
		this.cacheVersion = cacheVersion;
	}
	
	public final void markLoad (Version cacheVersion)
	{
		this.cacheVersion = this.localVersion = cacheVersion;
		onLoaded();
		onModified();
	}

	public void markDeleted ()
	{
		onDeleting();
		this.localVersion = Version.DELETED;
		
		onDirty();
		onModified();
	}
	
	public boolean isDeleted()
	{
		return localVersion.equals(Version.DELETED);
	}
	
	protected void nextVersion ()
	{
		localVersion = Version.random();
	}
	
	private final void markDirtyNoCheck ()
	{
		nextVersion();
		onDirty();
		onModified();
	}
	
    public final void markDirty ()
    {
    	log.debug("info.markDirty", this);
    	if (!isWritable())
    		throw new RuntimeException("markDirty can't be called on an unwritable item");

    	markDirtyNoCheck();
    }
	
	public final boolean isDirty ()
	{
		return !localVersion.equals(cacheVersion);
	}
	
	public boolean hasDirtyChildren ()
	{
		return false;
	}

	public boolean isLoaded ()
	{
		return !localVersion.equals(Version.NONE);
	}
	
	public boolean isWritable ()
	{
		return isLoaded();
	}
	
	final public void markStore (Version version)
	{
		log.debug(this, "markStore", version);
		cacheVersion = version;
		
		onStored();
	}
	
	public void markCreate ()
	{
		log.debug(this, "onCreate");
		markDirtyNoCheck();
		
		onCreate();
	}
	
	public void markPreLoad ()
	{
		onPreLoad();
	}
	
	public void markPreStore ()
	{
		onPreStore();
	}
	
	protected void onCreate ()
	{
	}
	
	protected void onPreLoad ()
	{
	}
	
	protected void onPreStore ()
	{
	}
		
	protected void onLoaded()
	{
		
	}
	
	protected void onStored()
	{
		
	}
	
	protected void onModified ()
	{
		
	}
	
	protected void onDirty ()
	{
		
	}
	
	protected void onDeleting ()
	{
		
	}
	
	protected String classNameLastPart ()
	{
		String s = getClass().getName();
		int lastPeriod = s.lastIndexOf('.');
		return s.substring(lastPeriod == -1 ? 0 : lastPeriod);
	}
	
	public String getStringRepOfLoadState ()
	{
		String str = "";
		if (!isLoaded())
			str += "NotLoaded:";
		if (isDirty())
			str += "DIRTY!:" + getLocalVersion() + ":" + getCacheVersion();
		
		return str;
	}
	
	public String toString()
	{
//		return classNameLastPart() + "(" + getId() + ":" + getLocalVersion() + ":" + getCacheVersion() + ")";
		return classNameLastPart() + "(" + getId() + ":" + getStringRepOfLoadState() + ")";
	}
	
	public Callback markPreLoad_ ()
	{
		return
			new CallbackDefault() {
				public void onSuccess(Object... arguments) throws Exception {
					onPreLoad();
					next(arguments);
				}
			};
	}
	
	public Callback markLoad_ (Version version)
	{
		return
			new CallbackDefault(version) {
				public void onSuccess(Object... arguments) throws Exception {
					Version version = (Version)V(0);
					markLoad(version);
					next(arguments);
				}
			};
	}

	/*
	public Callback markPartialLoad_ (Version version)
	{
		return
			new CallbackDefault(version) {
				public void onSuccess(Object... arguments) throws Exception {
					Version version = (Version)V(0);
					setCacheVersion(version);
					next(arguments);
				}
			};
	}
	*/
	public Callback markStore_ (Version localVersion)
	{
		return
			new CallbackDefault(localVersion) {
				public void onSuccess(Object... arguments) throws Exception {
					Version version = (Version)V(0);
					markStore(version);
					next(arguments);
				}
			};
	}
	
	public Callback markPreStore_()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				onPreStore();
				next(arguments);
			}
		};
	}
	
	public Callback markDeleted_()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				markDeleted();
				next(arguments);
			}
		};
	}
}
