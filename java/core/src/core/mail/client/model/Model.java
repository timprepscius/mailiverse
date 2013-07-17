/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import mail.client.CacheManager;

public class Model extends mail.client.cache.Item
{
	CacheManager manager;
	
	public Model(CacheManager manager)
	{
		this.manager = manager;
	}
	
	public CacheManager getManager ()
	{
		return manager;
	}
	
	protected void onPreLoad ()
	{
		reset();
	}
	
	protected void onDirty ()
	{
		super.onDirty();
		manager.onModelDirty();
	}
	
	public void reset ()
	{
		
	}
}
