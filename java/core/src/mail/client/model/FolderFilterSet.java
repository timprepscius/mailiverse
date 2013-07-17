/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import mail.client.CacheManager;
import mail.client.cache.Type;

public class FolderFilterSet extends FolderSet
{

	public FolderFilterSet(CacheManager manager) 
	{
		super(manager, Type.FolderFilter);
	}

}
